import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Utils, Domain, Managers, Enums } from 'czechidm-core';
import { RoleSystemAttributeManager, RoleSystemManager, SystemAttributeMappingManager} from '../../redux';
import AttributeMappingStrategyTypeEnum from '../../domain/AttributeMappingStrategyTypeEnum';

const uiKey = 'role-system-attribute';
let roleSystemAttributeManager = null;
let roleSystemManager = null;
let roleManager = null;
const systemAttributeMappingManager = new SystemAttributeMappingManager();
const scriptManager = new Managers.ScriptManager();

/**
 * Attribute mapping can be overriden.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
class RoleSystemAttributeDetail extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      strategyType: AttributeMappingStrategyTypeEnum.findKeyBySymbol(AttributeMappingStrategyTypeEnum.MERGE)
    };
  }

  getUiKey() {
    return uiKey;
  }

  getManager() {
    return roleSystemAttributeManager;
  }

  getContentKey() {
    return 'acc:content.role.roleSystemAttributeDetail';
  }

  getNavigationKey() {
    if (this._isSystemMenu()) {
      return 'system-roles';
    }
    return this.getRequestNavigationKey('role-systems', this.props.match.params);
  }

  _isMenu(menu = 'role') {
    // TODO: better alghoritm
    return this.props.location.pathname.lastIndexOf(`/${menu}`, 0) === 0;
  }

  _isSystemMenu() {
    return this._isMenu('system');
  }

  // @Deprecated - since V10 ... replaced by dynamic key in Route
  // UNSAFE_componentWillReceiveProps(nextProps) {
  //   const { attributeId} = nextProps.match.params;
  //   if (attributeId && attributeId !== this.props.match.params.attributeId) {
  //     this._initComponent(nextProps);
  //   }
  // }

  // Did mount only call initComponent method
  componentDidMount() {
    super.componentDidMount();
    //
    this._initComponent(this.props);
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    // Init managers - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    roleSystemAttributeManager = this.getRequestManager(props.match.params, new RoleSystemAttributeManager());
    roleSystemManager = this.getRequestManager(props.match.params, new RoleSystemManager());
    roleManager = this.getRequestManager(props.match.params, new Managers.RoleManager());

    const {roleSystemId, attributeId} = props.match.params;
    if (this._getIsNew(props)) {
      this.setState({attribute: {
        roleSystem: roleSystemId,
        strategyType: AttributeMappingStrategyTypeEnum.findKeyBySymbol(AttributeMappingStrategyTypeEnum.SET)
      },
      mappingId: props.location.query.mappingId});
    } else {
      this.context.store.dispatch(roleSystemAttributeManager.fetchEntity(attributeId));
    }
  }

  _strategyTypeChange(value) {
    if (!value) {
      return;
    }
    this.setState({
      strategyType: value.value
    });
  }

  _getIsNew(nextProps) {
    if ((nextProps && nextProps.location) || this.props.location) {
      const { query } = nextProps ? nextProps.location : this.props.location;
      return (query) ? query.new : null;
    }
    return false;
  }

  save(event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      super.afterSave();
    } else {
      this.addError(error);
    }
  }

  closeDetail() {
    this.refs.form.processEnded();
    this.context.history.goBack();
  }

  _uidChanged(event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    this.refs.uid.setState({value: checked}, () => {
      this.forceUpdate();
    });
  }

  _disabledChanged(key, event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    this.refs[key].setState({value: checked}, () => {
      this.forceUpdate();
    });
  }

  _checkboxChanged(key, disableKey, event) {
    const checked = event.currentTarget.checked;
    // I need set value direct to checkbox (this event is run befor state is set, but I need him in render mothod now)
    if (checked) {
      this.refs[disableKey].setState({value: false});
    }
    this.refs[key].setState({value: checked}, () => {
      this.forceUpdate();
    });
  }

  _schemaAttributeChange(value) {
    // Copy values from parent attribute
    this.refs.name.setValue(value.name);
    this.refs.disabledDefaultAttribute.setValue(value.disabledAttribute);
    this.refs.strategyType.setValue(value.strategyType);
    this.refs.uid.setValue(value.uid);
    this.refs.sendAlways.setValue(value.sendAlways);
    this.refs.sendOnlyIfNotNull.setValue(value.sendOnlyIfNotNull);
    this.refs.extendedAttribute.setValue(value.extendedAttribute);
    this.refs.entityAttribute.setValue(value.entityAttribute);
    this.refs.confidentialAttribute.setValue(value.confidentialAttribute);
    const idmPropertyName = value.idmPropertyName;
    if (idmPropertyName) {
      this.refs.idmPropertyEnum.setValue(
        Enums.IdentityAttributeEnum.getEnum(idmPropertyName)
      );
    }
    this.refs.idmPropertyName.setValue(value.idmPropertyName);
    this.refs.disabledDefaultAttribute.setValue(value.disabled);
    this.refs.transformScript.setValue(value.transformToResourceScript);
  }

  _onChangeEntityEnum(item) {
    if (item) {
      const field = Enums.IdentityAttributeEnum.getField(item.value);
      this.refs.idmPropertyName.setValue(field);
    } else {
      this.refs.idmPropertyName.setValue(null);
    }
  }

  _scriptChange(scriptArea, component, value, event) {
    if (event) {
      event.preventDefault();
    }
    //
    if (!value) {
      return;
    }
    // TODO: set into cursor?
    const currentValue = this.refs[scriptArea].getValue() ? this.refs[scriptArea].getValue() : '';
    this.context.store.dispatch(scriptManager.fetchEntity(value.id, value.id, (entity) => {
      this.refs[scriptArea].setValue(currentValue + entity.template);
    }));
    // TODO: script area focus not working
    // this.refs[scriptArea].focus();
  }

  render() {
    const { _showLoading, _attribute, _systemMappingId} = this.props;
    const { mappingId, strategyType } = this.state;
    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    const _mappingId = isNew ? mappingId : _systemMappingId;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemMappingId', _mappingId ? _mappingId : Domain.SearchParameters.BLANK_UUID);

    if (!roleSystemAttributeManager || !attribute) {
      return null;
    }
    const _isDisabled = this.refs.disabledDefaultAttribute ? this.refs.disabledDefaultAttribute.getValue() : false;
    const _isEntityAttribute = this.refs.entityAttribute ? this.refs.entityAttribute.getValue() : false;
    const _isExtendedAttribute = this.refs.extendedAttribute ? this.refs.extendedAttribute.getValue() : false;
    const _showNoRepositoryAlert = (!_isExtendedAttribute && !_isEntityAttribute);
    const _isRequiredIdmField = (_isEntityAttribute || _isExtendedAttribute) && !_isDisabled;
    const strategyTypeTemp = strategyType ? strategyType : attribute.strategyType;
    const isMerge = strategyTypeTemp === AttributeMappingStrategyTypeEnum.findKeyBySymbol(AttributeMappingStrategyTypeEnum.MERGE);
    const isAuthoritativeMerge = strategyTypeTemp === AttributeMappingStrategyTypeEnum.findKeyBySymbol(AttributeMappingStrategyTypeEnum.AUTHORITATIVE_MERGE);

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header', attribute ? { name: attribute.name} : {})}}/>
        </Basic.ContentHeader>
        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className="no-border last">
            <Basic.AbstractForm
              ref="form"
              data={attribute}
              readOnly={!roleManager.canSave()}
              showLoading={_showLoading}>
              <Basic.Checkbox
                ref="disabledDefaultAttribute"
                onChange={this._disabledChanged.bind(this, 'disabledDefaultAttribute')}
                tooltip={this.i18n('acc:entity.RoleSystemAttribute.disabledDefaultAttributeTooltip')}
                label={this.i18n('acc:entity.RoleSystemAttribute.disabledDefaultAttribute')}/>
              <Basic.SelectBox
                ref="roleSystem"
                manager={roleSystemManager}
                label={this.i18n('acc:entity.RoleSystemAttribute.roleSystem')}
                readOnly
                required/>
              <Basic.SelectBox
                ref="systemAttributeMapping"
                manager={systemAttributeMappingManager}
                onChange={this._schemaAttributeChange.bind(this)}
                forceSearchParameters={forceSearchParameters}
                label={this.i18n('acc:entity.RoleSystemAttribute.systemAttributeMapping')}
                required/>
              <Basic.TextField
                ref="name"
                label={this.i18n('acc:entity.RoleSystemAttribute.name.label')}
                helpBlock={this.i18n('acc:entity.RoleSystemAttribute.name.help')}
                required
                max={255}/>
              <Basic.EnumSelectBox
                ref="strategyType"
                enum={AttributeMappingStrategyTypeEnum}
                onChange={this._strategyTypeChange.bind(this)}
                label={this.i18n('acc:entity.RoleSystemAttribute.strategyType')}
                required
                readOnly/>
              <Basic.Checkbox
                ref="uid"
                readOnly = {_isDisabled}
                onChange={this._uidChanged.bind(this)}
                tooltip={this.i18n('acc:entity.SystemAttributeMapping.uid.tooltip')}
                label={this.i18n('acc:entity.SystemAttributeMapping.uid.label')}/>
              <Basic.Checkbox
                ref="sendAlways"
                tooltip={this.i18n('acc:entity.SystemAttributeMapping.sendAlways.tooltip')}
                label={this.i18n('acc:entity.SystemAttributeMapping.sendAlways.label')}
                readOnly = {_isDisabled}/>
              <Basic.Checkbox
                ref="sendOnlyIfNotNull"
                tooltip={this.i18n('acc:entity.SystemAttributeMapping.sendOnlyIfNotNull.tooltip')}
                label={this.i18n('acc:entity.SystemAttributeMapping.sendOnlyIfNotNull.label')}
                readOnly = {_isDisabled}/>
              <Basic.Checkbox
                ref="extendedAttribute"
                onChange={this._checkboxChanged.bind(this, 'extendedAttribute', 'entityAttribute')}
                readOnly = {_isDisabled}
                label={ this.i18n('acc:entity.SystemAttributeMapping.extendedAttribute.label') }
                helpBlock={ this.i18n('acc:entity.SystemAttributeMapping.extendedAttribute.help', { escape: false }) }/>
              <Basic.Checkbox
                ref="entityAttribute"
                onChange={this._checkboxChanged.bind(this, 'entityAttribute', 'extendedAttribute')}
                readOnly = {_isDisabled}
                label={this.i18n('acc:entity.SystemAttributeMapping.entityAttribute')}/>
              <Basic.Checkbox
                ref="confidentialAttribute"
                readOnly = {_isDisabled || !_isRequiredIdmField}
                label={this.i18n('acc:entity.SystemAttributeMapping.confidentialAttribute')}/>
              <Basic.Row>
                <div className="col-lg-6">
                  <Basic.EnumSelectBox
                    ref="idmPropertyEnum"
                    readOnly = {_isDisabled || !_isEntityAttribute}
                    enum={Enums.IdentityAttributeEnum}
                    onChange={this._onChangeEntityEnum.bind(this)}
                    label={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyEnum')}
                    />
                </div>
                <div className="col-lg-6">
                  <Basic.TextField
                    ref="idmPropertyName"
                    readOnly = {_isDisabled || !_isRequiredIdmField || _isEntityAttribute}
                    label={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.help')}
                    required = {_isRequiredIdmField}
                    max={255}/>
                </div>
              </Basic.Row>
              <Basic.LabelWrapper label=" ">
                <Basic.Alert
                   rendered={_showNoRepositoryAlert}
                   key="no-repository-alert"
                   icon="exclamation-sign"
                   className="no-margin"
                   text={this.i18n('acc:content.system.attributeMappingDetail.alertNoRepository')}/>
              </Basic.LabelWrapper>

              <Basic.SelectBox
                ref="transformToResourceScriptSelectBox"
                readOnly = {_isDisabled}
                label={this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScriptSelectBox.label')}
                helpBlock={this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScriptSelectBox.help')}
                onChange={this._scriptChange.bind(this, 'transformScript', 'transformToResourceScriptSelectBox')}
                hidden={isMerge}
                forceSearchParameters={
                  scriptManager.getDefaultSearchParameters().setFilter('category', Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.TRANSFORM_TO))}
                manager={scriptManager} />
              <Basic.Checkbox
                ref="skipValueIfExcluded"
                hidden={!(isMerge || isAuthoritativeMerge)}
                helpBlock={this.i18n('acc:entity.SystemAttributeMapping.notUseIfConctractExcluded.help')}
                label={this.i18n('acc:entity.SystemAttributeMapping.notUseIfConctractExcluded.label')}
                readOnly = {_isDisabled}/>
              <Advanced.ScriptArea
                ref="transformScript"
                readOnly = {_isDisabled}
                showScriptSelection={!isMerge}
                scriptCategory={[Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.DEFAULT), Enums.ScriptCategoryEnum.findKeyBySymbol(Enums.ScriptCategoryEnum.TRANSFORM_TO)]}
                headerText={isMerge ? '' : this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScriptSelectBox.label')}
                helpBlock={isMerge ? '' : this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScriptSelectBox.help')}
                label={this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScript.label')}
                scriptManager={scriptManager} 
               />
            </Basic.AbstractForm>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link"
                onClick={this.context.history.goBack}
                showLoading={_showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this)}
                rendered={roleManager.canSave()}
                level="success"
                type="submit"
                showLoading={_showLoading}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

RoleSystemAttributeDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
RoleSystemAttributeDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  if (!roleSystemAttributeManager) {
    return {};
  }
  const entity = Utils.Entity.getEntity(state, roleSystemAttributeManager.getEntityType(), component.match.params.attributeId);
  let systemMappingId = null;
  if (entity) {
    entity.roleSystem = entity._embedded && entity._embedded.roleSystem ? entity._embedded.roleSystem : null;
    entity.systemAttributeMapping = entity._embedded && entity._embedded.systemAttributeMapping ? entity._embedded.systemAttributeMapping : null;
    entity.idmPropertyEnum = Enums.IdentityAttributeEnum.getEnum(entity.idmPropertyName);
    systemMappingId = entity._embedded && entity._embedded.roleSystem ? entity._embedded.roleSystem.systemMapping : null;
  }
  return {
    _attribute: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _systemMappingId: systemMappingId
  };
}

export default connect(select)(RoleSystemAttributeDetail);
