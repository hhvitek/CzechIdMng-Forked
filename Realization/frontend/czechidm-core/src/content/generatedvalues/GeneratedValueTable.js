import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Joi from 'joi';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Domain from '../../domain';
import * as Advanced from '../../components/advanced';
import { SecurityManager, DataManager, GeneratedValueManager } from '../../redux';

const MAX_DESCRIPTION_LENGTH = 60;

/**
 * Table with definitions of generated values
 *
 * @author Ondřej Kopr
 */
export class GeneratedValueTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: props.filterOpened,
      generatorType: null,
      _generatorTypes: [],
      detail: {
        show: false
      }
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.context.store.dispatch(this.getManager().fetchAvailableGenerators());
    this.context.store.dispatch(this.getManager().fetchSupportedEntities());
  }

  getManager() {
    return this.props.manager;
  }

  getContentKey() {
    return 'content.generatedValues';
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

  onChangeEntityType(entityType) {
    this._setGeneratorTypeByEntityType(entityType ? entityType.value : null);
  }

  onChangeGeneratorType(selectedGeneratorType) {
    this._setGeneratorPropertiesByGeneratorType(selectedGeneratorType ? selectedGeneratorType.value : null);
  }

  /**
   * Set generator types by entity type. Generator types is used in selecbox.
   */
  _setGeneratorTypeByEntityType(entityType) {
    const { availableGenerators } = this.props;

    const generators = [];
    if (entityType) {
      if (availableGenerators) {
        availableGenerators.forEach(generator => {
          if (generator.entityType === entityType) {
            generators.push({
              value: generator.generatorType,
              niceLabel: this.i18n(this.getManager().getLocalizationPrefixForGenerator() + generator.name + '.label')
            });
          }
        });
      }
    }
    this.setState({
      _generatorTypes: generators
    });
  }

  /**
   * Set generator properties by generator java type
   */
  _setGeneratorPropertiesByGeneratorType(generatorJavaType) {
    const { availableGenerators } = this.props;
    let generatorType = null;
    if (generatorJavaType) {
      availableGenerators.forEach(generator => {
        if (generator.generatorType === generatorJavaType) {
          generatorType = generator;
        }
      });
    }
    //
    this.setState({
      generatorType
    });
  }

  /**
   * Recive new form for create new type else show detail for existing org.
   */
  showDetail(entity, event) {
    const { uiKey } = this.props;
    //
    if (event) {
      event.preventDefault();
    }
    //
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${uiKey}-detail`));
    }
    //
    this._setGeneratorPropertiesByGeneratorType(entity.generatorType);
    this._setGeneratorTypeByEntityType(entity.entityType);
    this.setState({
      detail: {
        show: true,
        entity
      }
    }, () => {
      this.refs.form.setData(entity);
      this.refs.generatorType.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        ...this.state.detail,
        show: false
      },
      generatorType: null,
      _generatorTypes: []
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
    //
    const formEntity = this.refs.form.getData();
    // transform generator properties
    if (this.refs.formInstance) {
      formEntity.generatorProperties = this.refs.formInstance.getProperties();
    }
    if (formEntity.entityType) {
      formEntity.entityType = formEntity.entityType.value;
    }
    //
    super.save(formEntity, event);
  }

  /**
   * Return niceLabel for supported entities
   */
  _getSupportedEntitiesNiceLabel(entityName) {
    if (entityName) {
      return Utils.Ui.getSimpleJavaType(entityName);
    }
    return entityName;
  }

  /**
   * Return localization key for generator name
   * @return {[type]} [description]
   */
  _getLocalizationKeyForGeneratorJavaType(generatorType) {
    const { availableGenerators } = this.props;
    if (generatorType && availableGenerators) {
      const generator = availableGenerators.get(generatorType);
      if (generator) {
        return 'eav.value-generator.' + generator.name + '.label';
      }
    }
    // generator type not found
    return generatorType;
  }

  render() {
    const { uiKey, manager, supportedEntities, _showLoading, _permissions } = this.props;
    const { filterOpened, detail, _generatorTypes, generatorType } = this.state;
    //
    let _entityTypes = [];
    if (supportedEntities) {
      _entityTypes = supportedEntities._embedded.strings.map(item => { return {value: item.content, niceLabel: this._getSupportedEntitiesNiceLabel(item.content) }; });
      _entityTypes.sort((one, two) => {
        return one.niceLabel.localeCompare(two.niceLabel);
      });
    }
    //
    let formInstance = new Domain.FormInstance({});
    if (generatorType && generatorType.formDefinition && detail.entity) {
      formInstance = new Domain.FormInstance(generatorType.formDefinition).setProperties(detail.entity.generatorProperties);
    }
    const showProperties = generatorType && generatorType.formDefinition && generatorType.formDefinition.formAttributes.length > 0;
    const noProperties = generatorType && generatorType.formDefinition && generatorType.formDefinition.formAttributes.length === 0;
    let generatorDescription = null;
    if (showProperties) {
      generatorDescription = this.i18n(this.getManager().getLocalizationPrefixForGenerator() + generatorType.name + '.description');
    }
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={manager}
          showLoading={_showLoading}
          showRowSelection={SecurityManager.hasAuthority('GENERATEDVALUE_DELETE')}
          rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm
                ref="filterForm">
                <Basic.Row>
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.EnumSelectBox
                      ref="entityType"
                      searchable
                      showLoading={_showLoading}
                      placeholder={this.i18n('filter.entityType')}
                      options={ _entityTypes }/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons showLoading={_showLoading} cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={ filterOpened }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={this.showDetail.bind(this, {regenerateValue: false, seq: 11})}
                rendered={SecurityManager.hasAuthority('GENERATEDVALUE_CREATE')}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column property="seq" sort />
          <Advanced.Column property="entityType" sort cell={ ({ rowIndex, data }) => {
            return Utils.Ui.getSimpleJavaType(data[rowIndex].entityType);
          }}/>
          <Advanced.Column property="generatorType" sort cell={ ({ rowIndex, data }) => {
            return this.i18n(this._getLocalizationKeyForGeneratorJavaType(data[rowIndex].generatorType));
          }}/>
          <Advanced.Column property="description" cell={ ({ rowIndex, data }) => {
            if (data[rowIndex] && data[rowIndex].description !== null) {
              const description = data[rowIndex].description.replace(/<(?:.|\n)*?>/gm, '');
              return Utils.Ui.substringByWord(description, MAX_DESCRIPTION_LENGTH, '...');
            }
            return '';
          }}/>
        </Advanced.Table>
        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header')} rendered={!Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                readOnly={!manager.canSave(detail.entity, _permissions)}
                showLoading={_showLoading}>
                <Basic.Row>
                  <Basic.Col lg={ 6 }>
                    <Basic.EnumSelectBox
                      ref="entityType"
                      options={ _entityTypes }
                      onChange={ this.onChangeEntityType.bind(this) }
                      label={ this.i18n('entity.GeneratedValue.entityType.label') }
                      palceholder={ this.i18n('entity.GeneratedValue.entityType.placeholder') }
                      helpBlock={ this.i18n('entity.GeneratedValue.entityType.help') }
                      searchable
                      required
                      useObject/>
                    <Basic.Alert
                      rendered={ _generatorTypes.length < 0 }
                      text={ this.i18n('noGenerators') }/>
                    <Basic.EnumSelectBox
                      ref="generatorType"
                      options={ _generatorTypes }
                      onChange={ this.onChangeGeneratorType.bind(this) }
                      label={ this.i18n('entity.GeneratedValue.generatorType.label') }
                      palceholder={ this.i18n('entity.GeneratedValue.generatorType.placeholder') }
                      readOnly={ _generatorTypes.length === 0 }
                      searchable
                      required/>
                    <Basic.TextField
                      ref="seq"
                      validation={Joi.number().integer().min(0).max(9999)}
                      label={ this.i18n('entity.GeneratedValue.seq.label') }
                      help={ this.i18n('entity.GeneratedValue.seq.help') }/>
                    <Basic.TextArea
                      ref="description"
                      label={this.i18n('entity.GeneratedValue.description.label')}
                      max={2000}/>
                    <Basic.Checkbox
                      ref="regenerateValue"
                      label={ this.i18n('entity.GeneratedValue.regenerateValue.label') }
                      helpBlock={ this.i18n('entity.GeneratedValue.regenerateValue.help') }/>
                    <Basic.Checkbox
                      ref="disabled"
                      label={ this.i18n('entity.GeneratedValue.disabled.label') }
                      helpBlock={ this.i18n('entity.GeneratedValue.disabled.help') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 }>
                    <Basic.Alert
                      rendered={ noProperties }
                      text={ this.i18n('noProperties') }/>
                    <Basic.Alert
                      rendered={ !showProperties }
                      text={ this.i18n('selectGenerator') }/>

                    <Basic.Alert
                      rendered={ generatorDescription }
                      text={ generatorDescription }/>

                    <div style={ showProperties ? {} : { display: 'none' }}>
                      <Basic.ContentHeader text={ this.i18n('generatorProperties.title') }/>
                      <Advanced.EavForm
                        useDefaultValue
                        ref="formInstance"
                        readOnly={!manager.canSave(detail.entity, _permissions)}
                        formInstance={ formInstance }/>
                    </div>

                  </Basic.Col>
                </Basic.Row>

              </Basic.AbstractForm>
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
                readOnly={manager.canSave(detail.entity, _permissions)}
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

GeneratedValueTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};

GeneratedValueTable.defaultProps = {
  _permissions: null
};

function select(state, component) {
  const perm = Utils.Permission.getPermissions(state, `${component.uiKey}-detail`);
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`)
                || Utils.Ui.isShowLoading(state, GeneratedValueManager.UI_KEY_SUPPORTED_ENTITIES)
                || Utils.Ui.isShowLoading(state, GeneratedValueManager.UI_KEY_AVAILABLE_GENERATORS),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    supportedEntities: DataManager.getData(state, GeneratedValueManager.UI_KEY_SUPPORTED_ENTITIES),
    availableGenerators: DataManager.getData(state, GeneratedValueManager.UI_KEY_AVAILABLE_GENERATORS),
    _permissions: perm
  };
}

export default connect(select)(GeneratedValueTable);
