import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Domain, Managers, Utils } from 'czechidm-core';
import { SystemEntityManager, SystemManager, SystemEntityTypeManager } from '../../redux';
import AttributeTable from '../account/AttributeTable';

const uiKey = 'system-entities-table';
const manager = new SystemEntityManager();
const systemManager = new SystemManager();
const systemEntityTypeManager = new SystemEntityTypeManager();

/**
 * Entities in target system.
 *
 * @author Radek Tomiška
 */
class SystemEntitiesContent extends Advanced.AbstractTableContent {

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.entities';
  }

  getNavigationKey() {
    return 'system-entities';
  }

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      system: entity._embedded && entity._embedded.system ? entity._embedded.system.id : this.props.match.params.entityId
    });
    if (!Utils.Entity.isNew(entity)) {
      manager.getService()
        .getConnectorObject(entity.id)
        .then(json => {
          const detail = _.merge({}, this.state.detail);
          detail.connectorObject = json;
          this.setState({detail});
        })
        .catch(error => {
          this.addError(error);
        });
    }
    //
    super.showDetail(entityFormData, () => {
      this.refs.uid.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { name: entity.uid }) });
      super.afterSave();
    } else {
      this.addError(error);
    }
  }

  render() {
    const { entityId } = this.props.match.params;
    const { _showLoading } = this.props;
    const { detail } = this.state;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          { this.i18n('header', { escape: false }) }
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={this.getManager()}
            forceSearchParameters={forceSearchParameters}
            showRowSelection={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}
            actions={
              Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])
              ?
              [{ value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }]
              :
              null
            }
            buttons={
              [
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={this.showDetail.bind(this, { entityType: 'IDENTITY' })}
                  rendered={Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE'])}>
                  <Basic.Icon type="fa" icon="plus"/>
                  {' '}
                  {this.i18n('button.add')}
                </Basic.Button>
              ]
            }
            filter={
              <Filter
                ref="filterForm"
                onSubmit={ this.useFilter.bind(this) }
                onCancel={ this.cancelFilter.bind(this) } />
            }
            _searchParameters={ this.getSearchParameters() }>
            <Advanced.Column
              property=""
              header=""
              className="detail-button"
              rendered={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_READ']) }
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={this.i18n('button.detail')}
                      onClick={this.showDetail.bind(this, data[rowIndex])}/>
                  );
                }
              }/>
            <Advanced.ColumnLink
              to={
                ({ rowIndex, data }) => {
                  this.showDetail(data[rowIndex]);
                }
              }
              property="uid"
              header={this.i18n('acc:entity.SystemEntity.uid')}
              sort />
            <Advanced.Column
              property="entityType"
              width={ 75 }
              header={ this.i18n('acc:entity.SystemEntity.entityType') }
              sort
              face="text"/>
            <Advanced.Column
              property="wish"
              sort
              face="boolean" />
          </Advanced.Table>
        </Basic.Panel>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={ Utils.Entity.isNew(detail.entity) }/>
          <Basic.Modal.Header
            closeButton={!_showLoading}
            text={this.i18n('edit.header', { name: detail.entity.name })}
            rendered={ !Utils.Entity.isNew(detail.entity) }/>
          <Basic.Modal.Body>
            <form onSubmit={ this.save.bind(this, {}) }>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading }
                readOnly={ !Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }>
                <Basic.SelectBox
                  ref="system"
                  manager={systemManager}
                  label={this.i18n('acc:entity.SystemEntity.system')}
                  readOnly
                  required/>
                <Basic.TextField
                  ref="uid"
                  label={this.i18n('acc:entity.SystemEntity.uid')}
                  required
                  max={1000}/>
                <Basic.SelectBox
                  ref="entityType"
                  manager={ systemEntityTypeManager }
                  label={this.i18n('acc:entity.SystemEntity.entityType')}
                  required/>
                <Basic.Checkbox
                  ref="wish"
                  label={ this.i18n('acc:entity.SystemEntity.wish.label') }
                  helpBlock={ this.i18n('acc:entity.SystemEntity.wish.help', { escape: false }) }/>
              </Basic.AbstractForm>
              {/* onEnter action - is needed because footer submit button is outside form */}
              <input type="submit" className="hidden"/>
            </form>
            <Basic.ContentHeader text={ this.i18n('acc:entity.SystemEntity.attributes') } rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <AttributeTable
              connectorObject={ detail ? detail.connectorObject : null }
              rendered={ !Utils.Entity.isNew(detail.entity) }
            />
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
              rendered={ Managers.SecurityManager.hasAnyAuthority(['SYSTEM_UPDATE']) }
              onClick={ this.save.bind(this, {}) }>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
      </div>
    );
  }
}

SystemEntitiesContent.propTypes = {
  system: PropTypes.object,
  _showLoading: PropTypes.bool,
};
SystemEntitiesContent.defaultProps = {
  system: null,
  _showLoading: false,
};

function select(state, component) {
  return {
    system: Utils.Entity.getEntity(state, systemManager.getEntityType(), component.match.params.entityId),
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, uiKey)
  };
}

export default connect(select)(SystemEntitiesContent);

/**
 * Table filter component
 *
 * @author Radek Tomiška
 */
class Filter extends Advanced.Filter {

  render() {
    const { onSubmit, onCancel } = this.props;
    //
    return (
      <Advanced.Filter onSubmit={ onSubmit }>
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row className="last">
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.TextField
                ref="text"
                placeholder={ this.i18n('acc:content.system.entities.filter.text.placeholder') }/>
            </Basic.Col>
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.SelectBox
                ref="entityType"
                placeholder={ this.i18n('acc:entity.SystemEntity.entityType') }
                manager={ systemEntityTypeManager }/>
            </Basic.Col>
            <Basic.Col lg={ 4 } className="text-right">
              <Advanced.Filter.FilterButtons cancelFilter={ onCancel }/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }
}
