import PropTypes from 'prop-types';
import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
import Joi from 'joi';
import Immutable from 'immutable';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import * as Domain from '../../domain';
//
import {
  RoleManager,
  AuthorizationPolicyManager,
  DataManager,
  FormAttributeManager
} from '../../redux';

const DEFAULT_EVALUATOR_TYPE = 'eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator';
const formAttributeManager = new FormAttributeManager();
let roleManager = null;
let manager = null;

/**
 * Table of role's granted permissions.
 *
 * @author Radek Tomiška
 */
export class AuthorizationPolicyTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    // Init managers - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    manager = this.getRequestManager(props.match.params, new AuthorizationPolicyManager());
    roleManager = this.getRequestManager(props.match.params, new RoleManager());

    this.state = {
      ...this.state,
      authorizableType: null,
      evaluatorType: null
    };
  }

  getContentKey() {
    return 'content.role.authorization-policies';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();

    this.context.store.dispatch(this.getManager().fetchSupportedEvaluators());
    this.context.store.dispatch(this.getManager().fetchAuthorizableTypes());
    this.context.store.dispatch(roleManager.fetchAllAuthorities());
    this.context.store.dispatch(roleManager.fetchAvailableAuthorities());
    //
    this.refs.filterForm.focus();
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    if (nextProps.match.params) {
      // Init managers - evaluates if we want to use standard (original) manager or
      // universal request manager (depends on existing of 'requestId' param)
      manager = this.getRequestManager(nextProps.match.params, manager || new AuthorizationPolicyManager());
      roleManager = this.getRequestManager(nextProps.match.params, roleManager || new RoleManager());
    }
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  showDetail(entity) {
    const {
      forceSearchParameters,
      supportedEvaluators,
      authorizableTypes
    } = this.props;
    //
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    let roleId = null;
    if (forceSearchParameters.getFilters().has('roleId')) {
      roleId = forceSearchParameters.getFilters().get('roleId');
    }
    //
    const entityFormData = _.merge({}, entity, {
      role: entity._embedded && entity._embedded.role ? entity._embedded.role : roleId,
      basePermissions: entity.basePermissions ? entity.basePermissions.split(',') : null,
      groupPermission: // Create composite value (~option) identifier - authorizableType can be null.
        entity.groupPermission
          ?
          `${entity.groupPermission}${entity.authorizableType ? '-' : ''}${entity.authorizableType || ''}`
          :
          null
    });
    //
    let authorizableType = null;
    const _authorizableType =
      !entity.groupPermission
        ?
        null
        :
        authorizableTypes.find(type => {
          return type.group === entity.groupPermission && type.type === entity.authorizableType;
        });
    if (_authorizableType) {
      authorizableType = {
        niceLabel: this._getAuthorizableTypeNiceLabel(_authorizableType._authorizableType, _authorizableType.type),
        value: _authorizableType.id,
        group: _authorizableType.group,
        type: _authorizableType.type
      };
    }
    this.setState({
      detail: {
        show: true,
        entity: entityFormData
      },
      evaluatorType: supportedEvaluators.has(entity.evaluatorType) ? this._toEvaluatorOption(supportedEvaluators.get(entity.evaluatorType)) : null,
      authorizableType
    }, () => {
      // @todo-upgrade-10 - Remove set timeout after update react-bootstap!
      setTimeout(() => {
        this.refs.groupPermission.focus();
      }, 10);
    });
  }

  closeDetail() {
    const {detail} = this.state;
    //
    this.setState({
      detail: {
        ...detail,
        show: false
      },
      authorizableType: null,
      evaluatorType: null
    });
  }

  save(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    if (this.refs.formInstance) {
      if (!this.refs.formInstance.isValid()) {
        return;
      }
    }
    const formEntity = this.refs.form.getData();
    //
    // transform properties
    if (this.refs.formInstance) {
      formEntity.evaluatorProperties = this.refs.formInstance.getProperties();
    }
    // transform base permissions
    if (formEntity.basePermissions) {
      formEntity.basePermissions = formEntity.basePermissions.join(',');
    }
    // transform authorizableType
    if (formEntity.groupPermission) {
      formEntity.authorizableType = formEntity.groupPermission.type;
      formEntity.groupPermission = formEntity.groupPermission.group;
    }
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({
        message: this.i18n('save.success', {
          count: 1,
          record: this.getManager().getNiceLabel(entity)
        })
      });
      // TODO: trimmed vs. not trimmed view ...
      this.refs.table.reload();
    }
    //
    super.afterSave(entity, error);
  }

  onChangeAuthorizableType(authorizableType) {
    const {evaluatorType} = this.state;
    const newState = {authorizableType};
    //
    if (authorizableType && authorizableType.type === null) {
      newState.evaluatorType = DEFAULT_EVALUATOR_TYPE;
    } else if (evaluatorType && Utils.Ui.getSimpleJavaType(evaluatorType.entityType) !== 'Identifiable') {
      newState.evaluatorType = null;
    }
    //
    this.setState(newState, () => {
      if (newState.evaluatorType !== undefined) {
        this.refs.evaluatorType.setValue(newState.evaluatorType);
      }
      this.refs.basePermissions.setValue(null);
    });
  }

  onChangeEvaluatorType(evaluatorType) {
    this.setState({
      evaluatorType
    }, () => {
      if (evaluatorType && !evaluatorType.supportsPermissions) {
        this.refs.basePermissions.setValue(null);
      }
    });
  }

  /**
   * Resolve bese permission with module localization
   *
   * @param  {[type]} perrmission [description]
   * @return {[type]}             [description]
   */
  _getBasePermissionNiceLabel(permission) {
    let _permission = permission;
    const {allAuthorities} = this.props;
    if (!allAuthorities) {
      // will be refreshed after authorities are loaded
      return null;
    }
    if (!permission.name) {
      _permission = this._getUniqueBasePermissions(this.props.allAuthorities).get(permission);
    }
    if (!_permission) {
      return permission;
    }
    //
    return this.i18n(
      `${_permission.module ? _permission.module : 'core'}:permission.base.${_permission.name}`,
      {defaultValue: _permission.name}
    );
  }

  _getGroupPermissionNiceLabel(groupPermission) {
    return this.i18n(
      `${groupPermission.module ? groupPermission.module : 'core'}:permission.group.${groupPermission.name}`,
      {defaultValue: groupPermission.name}
    );
  }

  _getAuthorizableTypeNiceLabel(groupName, authorizableType) {
    const {allAuthorities} = this.props;
    //
    if (!allAuthorities) {
      return '';
    }
    const groupPermission = allAuthorities.find(p => {
      return p.name === groupName;
    });
    if (!groupPermission) {
      return '';
    }
    //
    const label = this._getGroupPermissionNiceLabel(groupPermission);
    return `${label}${!authorizableType ? '' : ` (${Utils.Ui.getSimpleJavaType(authorizableType)})`}`;
  }

  _getUniqueBasePermissions(allAuthorities, authorizableType = null) {
    let _uniqueBasePermissions = new Immutable.Map();
    if (allAuthorities) {
      allAuthorities.forEach(groupPermission => {
        if (!authorizableType || authorizableType.group === groupPermission.name) {
          groupPermission.permissions.forEach(permission => {
            _uniqueBasePermissions = _uniqueBasePermissions.set(permission.name, permission);
          });
        }
      });
    }
    return _uniqueBasePermissions;
  }

  _getSupportedEvaluators(authorizableType = null) {
    const {supportedEvaluators} = this.props;
    //
    const _supportedEvaluators = [];
    if (!supportedEvaluators) {
      return _supportedEvaluators;
    }
    //
    supportedEvaluators.forEach(evaluator => {
      // TODO: add filter to BE and evaluate all superclasses
      if ((!authorizableType
          && (Utils.Ui.getSimpleJavaType(evaluator.entityType) === 'Identifiable')
          && (Utils.Ui.getSimpleJavaType(evaluator.evaluatorType) !== 'CodeableEvaluator'))
        ||
        (authorizableType
          && (authorizableType.type === evaluator.entityType || Utils.Ui.getSimpleJavaType(evaluator.entityType) === 'Identifiable'))) {
        _supportedEvaluators.push(this._toEvaluatorOption(evaluator));
      }
    });
    //
    return _supportedEvaluators;
  }

  _toEvaluatorOption(evaluator) {
    return {
      niceLabel: formAttributeManager.getLocalization(evaluator.formDefinition, null, 'label', Utils.Ui.getSimpleJavaType(evaluator.evaluatorType)),
      value: evaluator.evaluatorType,
      description: formAttributeManager.getLocalization(evaluator.formDefinition, null, 'help', evaluator.description),
      supportsPermissions: evaluator.supportsPermissions,
      formDefinition: evaluator.formDefinition
    };
  }

  render() {
    const {
      uiKey,
      columns,
      forceSearchParameters,
      _showLoading,
      supportedEvaluators,
      authorizableTypes,
      allAuthorities,
      availableAuthorities,
      _permissions,
      className,
      showAddButton,
      showRowSelection
    } = this.props;
    const {detail, evaluatorType, authorizableType} = this.state;
    //
    if (!manager || !roleManager) {
      return null;
    }
    //
    let formInstance = new Domain.FormInstance({});
    if (evaluatorType && evaluatorType.formDefinition && detail.entity) {
      formInstance = new Domain.FormInstance(evaluatorType.formDefinition).setProperties(detail.entity.evaluatorProperties);
    }
    const showProperties = formInstance && evaluatorType && evaluatorType.formDefinition && evaluatorType.formDefinition.formAttributes.length > 0;
    //
    const _supportedEvaluators = this._getSupportedEvaluators(authorizableType);
    const _authorizableTypes = [];
    if (authorizableTypes) {
      authorizableTypes.forEach(type => {
        _authorizableTypes.push({
          niceLabel: this._getAuthorizableTypeNiceLabel(type.group, type.type),
          value: type.id,
          type: type.type,
          group: type.group
        });
      });
      _authorizableTypes.sort((one, two) => {
        return one.niceLabel.localeCompare(two.niceLabel);
      });
    }
    const _uniqueBasePermissions = this._getUniqueBasePermissions(allAuthorities, authorizableType);
    const _basePermissions = _uniqueBasePermissions.toArray().map(permission => {
      return {
        niceLabel: this._getBasePermissionNiceLabel(permission),
        value: permission.name
      };
    }).sort((one, two) => {
      return one.niceLabel.localeCompare(two.niceLabel);
    });
    //
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={manager}
          forceSearchParameters={forceSearchParameters}
          showRowSelection={showRowSelection}
          className={className}
          filterOpened
          rowClass={
            ({rowIndex, data}) => {
              if (!data[rowIndex].groupPermission) { // wildcard permission
                return Utils.Ui.getDisabledRowClass(data[rowIndex]);
              }
              // installed vs. available authorities - authority from disabled module
              if (availableAuthorities && !availableAuthorities.find(p => { return p.name === data[rowIndex].groupPermission; })) {
                return 'disabled';
              }
              return Utils.Ui.getDisabledRowClass(data[rowIndex]);
            }
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, {evaluatorType: DEFAULT_EVALUATOR_TYPE})}
                rendered={_supportedEvaluators.length > 0 && manager.canSave() && showAddButton}
                icon="fa:plus">
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }
          filter={
            <Filter
              ref="filterForm"
              onSubmit={this.useFilter.bind(this)}
              onCancel={this.cancelFilter.bind(this)}
              _authorizableTypes={_authorizableTypes}/>
          }>

          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({rowIndex, data}) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column
            property="authorizableType"
            header={this.i18n('entity.AuthorizationPolicy.authorizableType.label')}
            sort
            sortProperty="groupPermission"
            rendered={_.includes(columns, 'authorizableType')}
            width={175}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({rowIndex, data, property}) => {
                const propertyValue = data[rowIndex][property];
                return (
                  <span
                    title={propertyValue}>{this._getAuthorizableTypeNiceLabel(data[rowIndex].groupPermission, propertyValue)}
                  </span>
                );
              }
            }/>
          <Advanced.Column
            property="basePermissions"
            header={this.i18n('entity.AuthorizationPolicy.basePermissions.label')}
            rendered={_.includes(columns, 'basePermissions')}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({rowIndex, data, property}) => {
                const propertyValue = data[rowIndex][property];
                if (!propertyValue) {
                  return null;
                }
                return propertyValue.split(',').map(permission => {
                  return (
                    <Basic.Div>
                      {this._getBasePermissionNiceLabel(permission)}
                    </Basic.Div>
                  );
                });
              }
            }/>
          <Advanced.Column
            property="evaluatorType"
            header={this.i18n('entity.AuthorizationPolicy.evaluatorType.label')}
            sort
            rendered={_.includes(columns, 'evaluatorType')}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({rowIndex, data, property}) => {
                const propertyValue = data[rowIndex][property];
                let _evaluatorType;
                if (supportedEvaluators && supportedEvaluators.has(propertyValue)) {
                  _evaluatorType = this._toEvaluatorOption(supportedEvaluators.get(propertyValue));
                }
                return (
                  <span title={propertyValue}>
                    {
                      _evaluatorType
                        ?
                        formAttributeManager.getLocalization(_evaluatorType.formDefinition, null, 'label', Utils.Ui.getSimpleJavaType(propertyValue))
                        :
                        Utils.Ui.getSimpleJavaType(propertyValue)
                    }
                  </span>
                );
              }
            }/>
          <Advanced.Column
            face="text"
            header={this.i18n('entity.AuthorizationPolicy.evaluatorProperties.label')}
            rendered={_.includes(columns, 'evaluatorProperties')}
            width="25%"
            cell={
              /* eslint-disable react/no-multi-comp */
              ({rowIndex, data}) => {
                const entity = data[rowIndex];
                if (!entity.evaluatorProperties) {
                  return null;
                }
                let _evaluatorType;
                if (supportedEvaluators && supportedEvaluators.has(entity.evaluatorType)) {
                  _evaluatorType = this._toEvaluatorOption(supportedEvaluators.get(entity.evaluatorType));
                }
                return [..._.keys(entity.evaluatorProperties).map(parameterName => {
                  if (parameterName.lastIndexOf('core:', 0) === 0) {
                    return null;
                  }
                  if (Utils.Ui.isEmpty(entity.evaluatorProperties[parameterName])) {
                    // not filled (false is needed to render)
                    return null;
                  }
                  return (
                    <div style={{wordWrap: 'anywhere'}}>
                      {
                        _evaluatorType
                          ?
                          formAttributeManager.getLocalization(_evaluatorType.formDefinition, {code: parameterName}, 'label', parameterName)
                          : parameterName
                      }
                      :
                      {Utils.Ui.toStringValue(entity.evaluatorProperties[parameterName])}
                    </div>
                  );
                }).values()];
              }
            }/>
          <Advanced.Column
            header={this.i18n('entity.Role._type')}
            sort
            sortProperty="role.name"
            cell={
              /* eslint-disable react/no-multi-comp */
              ({rowIndex, data}) => {
                const entity = data[rowIndex];
                //
                return (
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={entity.role}
                    entity={entity._embedded.role}
                    face="popover"
                    showIcon/>
                );
              }
            }
            rendered={_.includes(columns, 'role')}/>
          <Advanced.Column
            property="description"
            header={this.i18n('entity.AuthorizationPolicy.description.label')}
            face="text"
            sort
            rendered={_.includes(columns, 'description')}/>
          <Advanced.Column
            property="disabled"
            header={this.i18n('entity.AuthorizationPolicy.disabled.label')}
            face="bool"
            sort
            rendered={_.includes(columns, 'disabled')}/>
          <Advanced.Column
            property="seq"
            header={this.i18n('entity.AuthorizationPolicy.seq.label')}
            face="text"
            sort
            rendered={_.includes(columns, 'seq')}/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>
          <Basic.Modal.Header
            closeButton={!_showLoading}
            text={this.i18n('create.header')}
            rendered={Utils.Entity.isNew(detail.entity)}/>
          <Basic.Modal.Header
            closeButton={!_showLoading}
            text={this.i18n('edit.header', {name: this.getManager().getNiceLabel(detail.entity)})}
            rendered={!Utils.Entity.isNew(detail.entity)}/>
          <Basic.Modal.Body>
            <form onSubmit={this.save.bind(this, {})}>
              <Basic.AbstractForm
                ref="form"
                data={detail.entity}
                showLoading={_showLoading}
                readOnly={!manager.canSave(detail.entity, _permissions)}>
                <Basic.Row>
                  <Basic.Col lg={6}>
                    <Basic.SelectBox
                      ref="role"
                      manager={roleManager}
                      label={this.i18n('entity.AuthorizationPolicy.role')}
                      readOnly={!Utils.Entity.isNew(detail.entity) || !_.includes(columns, 'role')}
                      required/>
                    <Basic.EnumSelectBox
                      ref="groupPermission"
                      options={_authorizableTypes}
                      onChange={this.onChangeAuthorizableType.bind(this)}
                      label={this.i18n('entity.AuthorizationPolicy.authorizableType.label')}
                      placeholder={this.i18n('entity.AuthorizationPolicy.authorizableType.placeholder')}
                      helpBlock={this.i18n('entity.AuthorizationPolicy.authorizableType.help')}
                      searchable
                      useObject/>
                    <Basic.EnumSelectBox
                      ref="basePermissions"
                      options={_basePermissions}
                      label={this.i18n('entity.AuthorizationPolicy.basePermissions.label')}
                      placeholder={this.i18n('entity.AuthorizationPolicy.basePermissions.placeholder')}
                      helpBlock={this.i18n('entity.AuthorizationPolicy.basePermissions.help')}
                      readOnly={(evaluatorType && evaluatorType.supportsPermissions !== undefined) ? !evaluatorType.supportsPermissions : false}
                      searchable
                      multiSelect
                      required={(evaluatorType && evaluatorType.supportsPermissions !== undefined) ? evaluatorType.supportsPermissions : true}/>
                    <Basic.TextField
                      ref="seq"
                      validation={
                        Joi
                          .number()
                          .integer()
                          .min(0)
                          .max(32767)
                          .allow(null)
                      }
                      label={this.i18n('entity.AuthorizationPolicy.seq.label')}
                      help={this.i18n('entity.AuthorizationPolicy.seq.help')}/>
                    <Basic.TextArea
                      ref="description"
                      label={this.i18n('entity.AuthorizationPolicy.description.label')}
                      max={2000}/>
                    <Basic.Checkbox
                      ref="disabled"
                      label={this.i18n('entity.AuthorizationPolicy.disabled.label')}
                      helpBlock={this.i18n('entity.AuthorizationPolicy.disabled.help')}/>
                  </Basic.Col>
                  <Basic.Col lg={6}>
                    <Basic.EnumSelectBox
                      ref="evaluatorType"
                      options={_supportedEvaluators}
                      onChange={this.onChangeEvaluatorType.bind(this)}
                      label={this.i18n('entity.AuthorizationPolicy.evaluatorType.label')}
                      helpBlock={evaluatorType ? evaluatorType.description : null}
                      readOnly={authorizableType ? authorizableType.type === null : false}
                      required/>
                    <Basic.Alert
                      text={this.i18n('evaluator.default')}
                      rendered={authorizableType ? authorizableType.type === null : false}/>

                    <Basic.Div style={showProperties ? {} : {display: 'none'}}>
                      <Basic.ContentHeader
                        text={this.i18n('entity.AuthorizationPolicy.evaluatorProperties.title')}/>
                      <Advanced.EavForm
                        ref="formInstance"
                        formInstance={formInstance}
                        useDefaultValue={Utils.Entity.isNew(detail.entity)}/>
                    </Basic.Div>

                  </Basic.Col>
                </Basic.Row>

              </Basic.AbstractForm>
              {/* onEnter action - is needed because footer submit button is outside form */}
              <input type="submit" className="hidden"/>
            </form>
          </Basic.Modal.Body>

          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={this.closeDetail.bind(this)}
              showLoading={_showLoading}>
              {this.i18n('button.close')}
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoading={_showLoading}
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={manager.canSave(detail.entity, _permissions)}
              onClick={this.save.bind(this, {})}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

AuthorizationPolicyTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Button for create user will be shown
   */
  showAddButton: PropTypes.bool,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};

AuthorizationPolicyTable.defaultProps = {
  columns: ['authorizableType', 'basePermissions', 'evaluatorType', 'evaluatorProperties', 'role', 'description', 'disabled', 'seq'],
  forceSearchParameters: null,
  _showLoading: false,
  _permissions: null,
  showRowSelection: true,
  showAddButton: true
};

function select(state, component) {
  return {
    supportedEvaluators: DataManager.getData(state, AuthorizationPolicyManager.UI_KEY_SUPPORTED_EVALUATORS),
    authorizableTypes: DataManager.getData(state, AuthorizationPolicyManager.UI_KEY_AUTHORIZABLE_TYPES),
    availableAuthorities: DataManager.getData(state, RoleManager.UI_KEY_AVAILABLE_AUTHORITIES),
    allAuthorities: DataManager.getData(state, RoleManager.UI_KEY_ALL_AUTHORITIES),
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`)
      || Utils.Ui.isShowLoading(state, AuthorizationPolicyManager.UI_KEY_SUPPORTED_EVALUATORS)
      || Utils.Ui.isShowLoading(state, AuthorizationPolicyManager.UI_KEY_AUTHORIZABLE_TYPES)
      || Utils.Ui.isShowLoading(state, RoleManager.UI_KEY_ALL_AUTHORITIES_UIKEY),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(AuthorizationPolicyTable);

/**
 * Table filter component
 *
 * @author Radek Tomiška
 * @author Patrik Stloukal
 */
class Filter extends Advanced.Filter {

  focus() {
    this.refs.text.focus();
  }

  render() {
    const {onSubmit, onCancel, _authorizableTypes} = this.props;
    //
    return (
      <Advanced.Filter onSubmit={onSubmit}>
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row className="last">
            <Basic.Col lg={4}>
              <Advanced.Filter.TextField
                ref="text"
                placeholder={this.i18n('content.role.authorization-policies.filter.text.placeholder')}
                help={Advanced.Filter.getTextHelp()}/>
            </Basic.Col>
            <Basic.Col lg={4}>
              <Basic.EnumSelectBox
                ref="groupPermission"
                options={
                  _authorizableTypes.map(authorizableType => ({
                    niceLabel: authorizableType.niceLabel,
                    value: authorizableType.group
                  }))
                }
                placeholder={this.i18n('entity.AuthorizationPolicy.authorizableType.label')}
                searchable/>
            </Basic.Col>
            <Basic.Col lg={4} className="text-right">
              <Advanced.Filter.FilterButtons cancelFilter={onCancel}/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }
}
