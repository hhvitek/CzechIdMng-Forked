import React, { PropTypes } from 'react';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import {RequestIdentityRoleManager, RoleRequestManager, IdentityContractManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import RoleSelectByIdentity from './RoleSelectByIdentity';
import RoleConceptDetail from './RoleConceptDetail';
import FormInstance from '../../domain/FormInstance';
import ConfigLoader from '../../utils/ConfigLoader';
import IncompatibleRoleWarning from '../role/IncompatibleRoleWarning';

const uiKeyIncompatibleRoles = 'request-incompatible-roles-';
const requestIdentityRoleManager = new RequestIdentityRoleManager();
const roleRequestManager = new RoleRequestManager();
const identityContractManager = new IdentityContractManager();

/**
 * Table for keep identity role concepts.
 *
 * @author Vít Švanda
 */
export class RequestIdentityRoleTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    const {request} = this.props;

    let showChangesOnly = false;
    if (request && request.state === 'EXECUTED') {
      showChangesOnly = true;
    }
    this.state = {
      conceptData: [],
      showRoleByIdentitySelect: false,
      detail: {
        show: false,
        entity: {},
        add: false
      },
      validationErrors: null,
      showChangesOnly
    };
  }

  componentDidMount() {
    super.componentDidMount();
  }

  componentWillReceiveProps(nextProps) {
    const {request} = nextProps;
    if (request
      && request.state === 'EXECUTED'
      && this.props.request
      && this.props.request.state !== 'EXECUTED') {
      this.setState({showChangesOnly: true});
    }
  }

  getContentKey() {
    return 'content.task.IdentityRoleConceptTable';
  }

  getManager() {
    return requestIdentityRoleManager;
  }

  getDefaultSearchParameters() {
    let searchParameters = this.getManager().getDefaultSearchParameters();
    //
    if (this.props.showEnvironment) {
      searchParameters = searchParameters.setFilter('roleEnvironment', ConfigLoader.getConfig('concept-role.table.filter.environment', []));
    }
    //
    return searchParameters;
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  /**
   * Close modal dialog
   */
  _closeDetail() {
    this.setState({
      detail: {
        ...this.state.detail,
        show: false,
        add: false
      },
      validationErrors: null
    });
  }

  /**
   * Compute background color row (added, removed, changed)
   */
  _getRowClass({rowIndex, data}) {
    const operation = data[rowIndex].operation;
    if (!operation) {
      return null;
    }
    if (operation === 'ADD') {
      return 'bg-success';
    }
    if (operation === 'REMOVE') {
      return 'bg-danger';
    }
    if (operation === 'UPDATE') {
      return 'bg-warning';
    }
    return null;
  }

  /**
   * Create new IdentityRoleConcet with virtual ID (UUID)
   */
  _addConcept() {
    const newIdentityRoleConcept = {operation: 'ADD'};
    this._showDetail(newIdentityRoleConcept, true, true);
  }

  _showDetail(entity, isEdit = false, multiAdd = false) {
    this.setState({
      detail: {
        show: true,
        edit: isEdit && !entity.automaticRole && !entity.directRole,
        entity,
        add: multiAdd
      }
    });
  }

  _showRoleByIdentitySelect() {
    this.setState({
      showRoleByIdentitySelect: true
    }, () => {
      this.refs.roleSelectByIdentity.getWrappedInstance().focus();
    });
  }

  _hideRoleByIdentitySelect() {
    this.setState({
      showRoleByIdentitySelect: false
    });
  }

  _getIncompatibleRoles(role) {
    const { incompatibleRoles } = this.props;
    //
    if (!incompatibleRoles) {
      return [];
    }
    //
    return incompatibleRoles.filter(ir => ir.directRole.id === role.id);
  }

  _filterOpen(open) {
    this.refs.table.getWrappedInstance()._filterOpen(open);
  }

  _toggleShowChangesOnly() {
    this.setState({
      showChangesOnly: !this.refs.switchShowChangesOnly.getValue()
    });
  }

  _createConceptsByIdentity() {
    const { request, replaceUrl} = this.props;
    this.setState({
      showLoading: true
    }, () => {
      const roleRequestByIdentity = this.refs.roleSelectByIdentity.getWrappedInstance().createRoleRequestByIdentity();
      roleRequestByIdentity.roleRequest = request.id;
      this.context.store.dispatch(roleRequestManager.copyRolesByIdentity(roleRequestByIdentity, null, (requestReturned, error) => {
        if (error) {
          this.setState({
            showLoading: false
          }, () => { this.addError(error); });
        } else {
          if (!request.id) {
            this.reload();
            replaceUrl(requestReturned.id);
          }
          this._hideRoleByIdentitySelect();
          this.setState({
            showLoading: false
          }, () => { this.reload(); });
        }
      }));
    });
  }

  _internalSave(event) {
    if (event) {
      event.preventDefault();
    }

    const form = this.refs.roleConceptDetail.getWrappedInstance().getForm();
    const eavForm = this.refs.roleConceptDetail.getWrappedInstance().getEavForm();
    if (!form.isFormValid()) {
      return;
    }
    if (eavForm && !eavForm.isValid()) {
      return;
    }
    //
    this.setState({
      showLoading: true
    }, () => {
      const { request, replaceUrl} = this.props;

      const entity = form.getData();
      entity.roleRequest = request.id;
      let eavValues = null;
      if (eavForm) {
        eavValues = {values: eavForm.getValues()};
      }

      // Conversions
      if (entity.identityContract && _.isObject(entity.identityContract)) {
        entity.identityContract = entity.identityContract.id;
      }
      if (entity.role && _.isArray(entity.role)) {
        entity.roles = entity.role;
        entity.role = null;
      }
      // Add EAV to entity
      entity._eav = [eavValues];
      // Save entity
      this.context.store.dispatch(requestIdentityRoleManager.createEntity(entity, null, (createdEntity, error) => {
        if (error) {
          // If error contains parameters with attributes, then is it validation error
          if (error.parameters && error.parameters.attributes) {
            this.setState({
              validationErrors: error.parameters.attributes,
              showLoading: false
            });
          } else {
            this.addError(error);
            this.setState({
              showLoading: false
            });
          }
        } else {
          this.setState({
            showLoading: false
          }, () => {
            if (!request.id) {
              request.id = createdEntity.roleRequest;
              replaceUrl(createdEntity.roleRequest);
            }
            this._closeDetail();
            // We need to fetch incompatibleRoles (could be changed)
            this.context.store.dispatch(roleRequestManager.fetchIncompatibleRoles(request.id, `${ uiKeyIncompatibleRoles }${ request.id }`));
            this.reload();
          });
        }
      }));
    });
  }

  _internalDelete(data) {
    const {request, replaceUrl} = this.props;
    this.setState({showLoadingActions: true}, () => {
      data.roleRequest = request.id;
      this.context.store.dispatch(this.getManager().deleteEntity(data, null, (json, error) => {
        if (error) {
          this.addError(error);
        }
        this.setState({showLoadingActions: false});
        if (!request.id) {
          request.id = json.roleRequest;
          replaceUrl(json.roleRequest);
        }
        // We need to fetch incompatibleRoles (could be changed)
        this.context.store.dispatch(roleRequestManager.fetchIncompatibleRoles(request.id, `${ uiKeyIncompatibleRoles }${ request.id }`));
        this.reload();
      }));
    });
  }

  /**
   * Gets duplicated concept or identity role for the given concept
   */
  _getOriginalForDuplicate(concept) {
    if (!concept.duplicate || !concept._embedded || !concept._embedded.duplicates) {
      return null;
    }
    const duplicates = concept._embedded.duplicates;
    if (duplicates.identityRoles.length > 0) {
      return duplicates.identityRoles[0];
    }
    if (duplicates.concepts.length > 0) {
      return duplicates.concepts[0];
    }
    //
    return null;
  }

  beforeDelete(bulkActionValue, selectedEntities) {
    const {request} = this.props;

    for (const entity of selectedEntities) {
      entity.roleRequest = request.id;
    }
  }

  afterDelete(entities) {
    const {request, replaceUrl} = this.props;

    if (!request.id && entities && entities.length > 0) {
      replaceUrl(entities[0].roleRequest);
    } else {
      this.reload();
    }
  }

  reload() {
    this.refs.table.getWrappedInstance().reload();
  }

  /**
   * We cannot show delete action for automatic and business roles
   */
  _showRowSelection({rowIndex, data}) {
    if (data && rowIndex >= 0) {
      const request = data[rowIndex];
      if (request && (request.directRole || request.automaticRole)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Generate cell with detail button and info components
   */
  renderDetailCell({rowIndex, data}) {
    const requestIdentityRole = data[rowIndex];
    const role = requestIdentityRole._embedded.role;
    const operation = requestIdentityRole.operation;
    if (!role) {
      return '';
    }
    const content = [];
    //
    content.push(
      <Advanced.DetailButton
        title={ this.i18n('button.detail') }
        onClick={ this._showDetail.bind(this, requestIdentityRole, operation !== 'REMOVE', false) }/>
    );
    content.push(
      <IncompatibleRoleWarning incompatibleRoles={ this._getIncompatibleRoles(role) }/>
    );
    if (requestIdentityRole.duplicate) {
      //
      content.push(
        <span>
          <Basic.Icon
            icon="fa:warning"
            style={{ marginLeft: 3, color: '#337ab7' }}
            title={ this.i18n('entity.IdentityRole.duplicate.label') }/>
        </span>
      );
    }
    return content;
  }

  /**
   * Generate cell with actions (buttons)
   */
  renderConceptActionsCell({rowIndex, data}) {
    const {readOnly} = this.props;
    const {showLoadingActions} = this.state;

    const actions = [];
    const value = data[rowIndex];
    const manualRole = !value.automaticRole && !value.directRole;
    const operation = value.operation;
    //
    actions.push(
      <Basic.Button
        level="danger"
        onClick={ this._internalDelete.bind(this, data[rowIndex]) }
        className="btn-xs"
        disabled={ readOnly || !manualRole }
        showLoading={ showLoadingActions }
        role="group"
        title={ this.i18n('button.delete') }
        titlePlacement="bottom"
        icon="trash"/>
    );
    if (operation !== 'REMOVE') {
      actions.push(
        <Basic.Button
          level="warning"
          showLoading={ showLoadingActions }
          onClick={ this._showDetail.bind(this, data[rowIndex], true, false) }
          className="btn-xs"
          disabled={ readOnly || !manualRole }
          role="group"
          title={ this.i18n('button.edit') }
          titlePlacement="bottom"
          icon="edit"/>
      );
    }
    return (
      <div className="btn-group" role="group">
        { actions }
      </div>
    );
  }

  renderConceptAttributesCell({rowIndex, data}) {
    const value = data[rowIndex];
    const result = [];
    if (value
      && value._eav
      && value._eav.length === 1
      && value._eav[0].formDefinition) {
      const formInstance = value._eav[0];
      const _formInstance = new FormInstance(formInstance.formDefinition, formInstance.values);
      result.push(
        <div onClick={ !value._removed ? this._showDetail.bind(this, value, true, false) : null }>
          <Advanced.EavForm
            key={ _.uniqueId(`${rowIndex}-${value.id}`) }
            ref="eavForm"
            formInstance={ _formInstance }
            validationErrors={ formInstance.validationErrors }
            readOnly
            useDefaultValue={ false }/>
        </div>
      );
    }
    return (
      <Basic.Div className="abstract-form condensed" style={{minWidth: 150, padding: 0}}>
        { result }
      </Basic.Div>
    );
  }

  render() {
    const {
      readOnly,
      request,
      identityId,
      showRowSelection,
      showEnvironment
    } = this.props;
    const {
      showChangesOnly,
      detail,
      showRoleByIdentitySelect,
      validationErrors
    } = this.state;

    const identityUsername = request && request.applicant;
    let forceSearchParameters = new SearchParameters();
    if (request) {
      forceSearchParameters = forceSearchParameters.setFilter('roleRequestId', request.id);
    }
    if (identityId) {
      forceSearchParameters = forceSearchParameters.setFilter('identityId', identityId);
    }
    forceSearchParameters = forceSearchParameters.setFilter('onlyChanges', showChangesOnly);
    //
    const showLoading = this.props.showLoading || this.state.showLoading;
    const contractForceSearchparameters = new SearchParameters().setFilter('identity', identityUsername);
    //
    return (
      <div>
        <Basic.Panel rendered={ request !== null}>
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Basic.Toolbar>
            <div>
              <div className="pull-left">
                <Basic.AbstractForm
                  ref="formShowChangesOnly"
                  style={{padding: '0px'}}
                  data={{switchShowChangesOnly: showChangesOnly}}>
                  <Basic.ToggleSwitch
                    ref="switchShowChangesOnly"
                    label={this.i18n('switchShowChangesOnly')}
                    onChange={this._toggleShowChangesOnly.bind(this)}
                  />
                </Basic.AbstractForm>
              </div>
              <div className="pull-right">
                <Basic.Button
                  level="success"
                  className="btn-xs"
                  disabled={readOnly}
                  onClick={this._addConcept.bind(this)}
                  icon="fa:plus"
                  text={ this.i18n('button.add') }/>
                <Basic.Button
                  level="success"
                  className="btn-xs"
                  disabled={ readOnly }
                  onClick={ this._showRoleByIdentitySelect.bind(this) }
                  icon="fa:plus"
                  text={ this.i18n('addByIdentity.header') }
                  style={{ marginLeft: 3 }}/>
              </div>
            </div>
          </Basic.Toolbar>
          <Advanced.Table
            ref="table"
            uiKey="request-identity-role-table"
            hover={ false }
            manager={requestIdentityRoleManager}
            showRowSelection={ showRowSelection ? this._showRowSelection.bind(this) : false}
            actions={
              [{
                value: 'delete',
                niceLabel: this.i18n('action.delete.action'),
                action: this.onDelete.bind(this),
                disabled: readOnly
              }]
            }
            rowClass={this._getRowClass}
            forceSearchParameters={forceSearchParameters}
            filter={
              <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className="last">
                    <Basic.Col lg={ 3 }>
                      <Advanced.Filter.RoleSelect
                        ref="roleId"
                        label={ null }
                        placeholder={ this.i18n('content.identity.roles.filter.role.placeholder') }
                        header={ this.i18n('content.identity.roles.filter.role.placeholder') }/>
                    </Basic.Col>
                    <Basic.Col lg={ 3 } className={ showEnvironment ? '' : 'hidden'}>
                      <Advanced.CodeListSelect
                        ref="roleEnvironment"
                        code="environment"
                        label={ null }
                        placeholder={ this.i18n('entity.Role.environment.label') }
                        multiSelect/>
                    </Basic.Col>
                    <Basic.Col lg={ showEnvironment ? 3 : 6 }>
                      <Advanced.Filter.SelectBox
                        ref="identityContractId"
                        placeholder={ this.i18n('entity.IdentityRole.identityContract.title') }
                        manager={ identityContractManager }
                        forceSearchParameters={ contractForceSearchparameters }
                        niceLabel={ (entity) => identityContractManager.getNiceLabel(entity, false) }/>
                    </Basic.Col>
                    <Basic.Col lg={ 3 } className="text-right">
                      <Basic.Button onClick={ this.cancelFilter.bind(this) } style={{ marginRight: 5 }}>
                        { this.i18n('button.filter.cancel') }
                      </Basic.Button>
                      <Basic.Button level="primary" onClick={ this.useFilter.bind(this) } >
                        { this.i18n('button.filter.use') }
                      </Basic.Button>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            _searchParameters={ this.getSearchParameters() }>
            <Advanced.Column
              header=""
              className="detail-button"
              cell={ this.renderDetailCell.bind(this) }/>
            <Advanced.Column
              property="role.name"
              title={ this.i18n('entity.Role.name') }
              sort
              header={ this.i18n('entity.IdentityRole.role') }
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data }) => {
                  const role = data[rowIndex]._embedded.role;
                  if (!role) {
                    return '';
                  }
                  return (
                    <Advanced.EntityInfo
                      entityType="role"
                      entityIdentifier={ role.id }
                      entity={ role }
                      face="popover"
                      showIcon
                      showCode={ false }
                      showEnvironment={ false }/>
                  );
                }
              }/>
            <Advanced.Column
              property="_embedded.role.baseCode"
              sortProperty="role.baseCode"
              rendered={showEnvironment}
              face="text"
              header={ this.i18n('entity.Role.baseCode.label') }
              sort/>
            <Advanced.Column
              property="systemState"
              width={75}
              face="text"
              header={this.i18n('systemState')}
              rendered={showChangesOnly}
              cell={
                ({ rowIndex, data }) => {
                  const entity = data[rowIndex];
                  return (
                    <Advanced.OperationResult value={ entity.systemState }/>
                  );
                }
              }/>
            <Advanced.Column
              property="_embedded.role.environment"
              rendered={showEnvironment}
              sortProperty="role.environment"
              face="text"
              header={ this.i18n('entity.Role.environment.label') }
              sort/>
            <Advanced.Column
              header={this.i18n('content.task.IdentityRoleConceptTable.identityRoleAttributes.header')}
              cell={
                ({rowIndex, data}) => this.renderConceptAttributesCell({ rowIndex, data })
              }/>
            <Advanced.Column
              header={ this.i18n('entity.IdentityRole.identityContract.title') }
              cell={
                ({rowIndex, data}) => {
                  const contract = data[rowIndex]._embedded.identityContract;
                  if (!contract) {
                    return '';
                  }
                  return (
                    <Advanced.IdentityContractInfo
                      entityIdentifier={ contract.id }
                      entity={ contract }
                      showIdentity={ false }
                      showIcon
                      face="popover" />
                  );
                }
              }/>
            <Advanced.Column
              sort
              property="validFrom"
              face="date"
              header={this.i18n('entity.ConceptRoleRequest.validFrom')}/>
            <Advanced.Column
              sort
              property="validTill"
              face="date"
              header={this.i18n('entity.ConceptRoleRequest.validTill')}/>
            <Advanced.Column
              property="directRole"
              header={ this.i18n('entity.IdentityRole.directRole.label') }
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data, property }) => {
                  if (!data[rowIndex][property]) {
                    return null;
                  }
                  //
                  return (
                    <Advanced.EntityInfo
                      entityType="identityRole"
                      entityIdentifier={ data[rowIndex][property] }
                      entity={ data[rowIndex]._embedded[property] }
                      showIdentity={ false }
                      face="popover" />
                  );
                }
              }
              width={ 150 }/>
            <Advanced.Column
              sort
              property="automaticRole"
              header={ <Basic.Icon value="component:automatic-role" title={ this.i18n('entity.IdentityRole.automaticRole.help') }/> }
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data }) => <Basic.BooleanCell
                  propertyValue={ data[rowIndex].automaticRole !== null }
                  className="column-face-bool"/>
              }/>
            <Advanced.Column
              header={ this.i18n('label.action') }
              className="action"
              cell={ this.renderConceptActionsCell.bind(this) }/>
          </Advanced.Table>
        </Basic.Panel>
        <Basic.Modal
          bsSize="large"
          show={showRoleByIdentitySelect}
          onHide={ this._hideRoleByIdentitySelect.bind(this) }
          backdrop="static"
          keyboard={!showLoading}>
          <Basic.Modal.Header
            closeButton={ !showLoading }
            text={ this.i18n('create.headerByIdentity') }
            rendered={ Utils.Entity.isNew(detail.entity) }/>
          <Basic.Modal.Body>
            <RoleSelectByIdentity
              ref="roleSelectByIdentity"
              showLoading={ showLoading }
              identityUsername={identityUsername}
              request={request}/>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              showLoading={ showLoading }
              onClick={ this._hideRoleByIdentitySelect.bind(this) }>
              { this.i18n('button.close') }
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoading={ showLoading }
              onClick={ this._createConceptsByIdentity.bind(this) }
              showLoadingIcon>
              { this.i18n('button.set') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={ this._closeDetail.bind(this) }
          backdrop="static"
          keyboard={!showLoading}>
          <form onSubmit={ this._internalSave.bind(this) }>
            <Basic.Modal.Header
              closeButton={ !showLoading }
              text={ this.i18n('create.header') }
              rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              closeButton={ !showLoading }
              text={ this.i18n('edit.header', { role: detail.entity.role }) }
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <RoleConceptDetail
                ref="roleConceptDetail"
                showEnvironment={showEnvironment}
                identityUsername={identityUsername}
                showLoading={showLoading}
                readOnly={readOnly}
                entity={detail.entity}
                isEdit={detail.edit}
                multiAdd={detail.add}
                validationErrors={ validationErrors }/>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this._closeDetail.bind(this) }
                showLoading={ showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={ showLoading }
                showLoadingIcon
                rendered={ detail.edit && !readOnly }>
                { this.i18n('button.set') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

RequestIdentityRoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  identityId: PropTypes.string.isRequired,
  className: PropTypes.string,
  request: PropTypes.object,
  readOnly: PropTypes.bool,
  showEnvironment: PropTypes.bool,
  replaceUrl: PropTypes.func
};

RequestIdentityRoleTable.defaultProps = {
  showLoading: false,
  showRowSelection: true,
  readOnly: false,
  showEnvironment: true
};

export default RequestIdentityRoleTable;