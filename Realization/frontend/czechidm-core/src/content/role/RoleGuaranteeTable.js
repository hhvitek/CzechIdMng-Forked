import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { RoleGuaranteeManager, IdentityManager, RoleManager, CodeListManager } from '../../redux';

let manager = new RoleGuaranteeManager();
let roleManager = new RoleManager();
const identityManager = new IdentityManager();
const codeListManager = new CodeListManager();

/**
* Table of role guarantees - by identity
*
* @author Radek Tomiška
*/
export class RoleGuaranteeTable extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.role.guarantees';
  }

  getManager() {
    // Init manager - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    manager = this.getRequestManager(this.props.match.params, manager);
    roleManager = this.getRequestManager(this.props.match.params, roleManager);
    return manager;
  }

  showDetail(entity) {
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    super.showDetail(entity, () => {
      this.refs.guarantee.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
    }
    //
    super.afterSave(entity, error);
  }

  render() {
    const { forceSearchParameters, _showLoading, _permissions, className, guaranteeTypes } = this.props;
    const { detail } = this.state;
    const role = forceSearchParameters.getFilters().get('role');
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          className={ className }
          showRowSelection={ manager.canDelete() }
          _searchParameters={ this.getSearchParameters() }
          rowClass={({rowIndex, data}) => {
            const embedded = data[rowIndex]._embedded;
            if (embedded) {
              return Utils.Ui.getRowClass(embedded.guarantee);
            }
            return '';
          }}
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
                onClick={ this.showDetail.bind(this, { role }) }
                rendered={ manager.canSave() }>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }>

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
          <Advanced.Column
            property="guarantee"
            sortProperty="guarantee.name"
            face="text"
            header={ this.i18n('entity.RoleGuarantee.guarantee.label') }
            sort
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="identity"
                    entityIdentifier={ entity.guarantee }
                    entity={ entity._embedded.guarantee }
                    face="popover"
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            property="type"
            width={ 125 }
            face="text"
            header={ this.i18n('entity.RoleGuarantee.type.label') }
            sort
            rendered={ guaranteeTypes.length > 0 }
            cell={
              ({ rowIndex, data, property }) => {
                return (
                  <Advanced.CodeListValue code="guarantee-type" value={ data[rowIndex][property] }/>
                );
              }
            }
          />
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={ !_showLoading } text={ this.i18n('create.header')} rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={ this.i18n('edit.header', { name: manager.getNiceLabel(detail.entity) }) }
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                data={detail.entity}
                showLoading={ _showLoading }
                readOnly={ !manager.canSave(detail.entity, _permissions)}>
                <Advanced.RoleSelect
                  ref="role"
                  manager={ roleManager }
                  label={ this.i18n('entity.RoleGuarantee.role.label') }
                  readOnly
                  required/>
                <Advanced.IdentitySelect
                  ref="guarantee"
                  manager={ identityManager }
                  label={ this.i18n('entity.RoleGuarantee.guarantee.label') }
                  helpBlock={ this.i18n('entity.RoleGuarantee.guarantee.help') }
                  required/>
                <Advanced.CodeListSelect
                  ref="type"
                  code="guarantee-type"
                  showOnlyIfOptionsExists
                  label={ this.i18n('entity.RoleGuarantee.type.label') }
                  helpBlock={ this.i18n(`entity.RoleGuarantee.type.help`) }
                  max={ 255 }/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                rendered={ manager.canSave(detail.entity, _permissions) }
                showLoading={ _showLoading}
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

RoleGuaranteeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  //
  _showLoading: PropTypes.bool
};

RoleGuaranteeTable.defaultProps = {
  forceSearchParameters: null,
  _showLoading: false
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    guaranteeTypes: codeListManager.getCodeList(state, 'guarantee-type') || []
  };
}

export default connect(select)(RoleGuaranteeTable);
