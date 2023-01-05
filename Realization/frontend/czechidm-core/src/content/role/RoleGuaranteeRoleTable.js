import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { RoleGuaranteeRoleManager, RoleManager, CodeListManager } from '../../redux';

let manager = new RoleGuaranteeRoleManager();
let roleManager = new RoleManager();
const codeListManager = new CodeListManager();

/**
* Table of role guarantees - by roles.
*
* @author Radek Tomiška
*/
export class RoleGuaranteeRoleTable extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.role.guarantees';
  }

  getUiKey() {
    return this.props.uiKey;
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
      this.refs.guaranteeRole.focus();
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
    const { uiKey, forceSearchParameters, _showLoading, _permissions, className, guaranteeTypes } = this.props;
    const { detail } = this.state;
    const role = forceSearchParameters.getFilters().get('role');
    //
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ manager.canDelete() }
          className={ className }
          _searchParameters={ this.getSearchParameters() }
          rowClass={({rowIndex, data}) => {
            const embedded = data[rowIndex]._embedded;
            if (embedded) {
              return Utils.Ui.getRowClass(embedded.guaranteeRole);
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
                rendered={ manager.canSave() }
                icon="fa:plus">
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
            property="guaranteeRole"
            sortProperty="guaranteeRole.name"
            face="text"
            header={ this.i18n('entity.RoleGuaranteeRole.guaranteeRole.label') }
            sort
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={ entity.guaranteeRole }
                    entity={ entity._embedded.guaranteeRole }
                    face="popover"
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            property="type"
            width={ 125 }
            face="text"
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
            <Basic.Modal.Header closeButton={ !_showLoading }
            text={ this.i18n('create.header')} 
            rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={ this.i18n('edit.header', { name: manager.getNiceLabel(detail.entity) }) }
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading }
                readOnly={ !manager.canSave(detail.entity, _permissions) }>
                <Advanced.RoleSelect
                  ref="role"
                  manager={ roleManager }
                  label={ this.i18n('entity.RoleGuaranteeRole.role.label') }
                  readOnly
                  required/>
                <Advanced.RoleSelect
                  ref="guaranteeRole"
                  manager={ roleManager }
                  label={ this.i18n('entity.RoleGuaranteeRole.guaranteeRole.label') }
                  helpBlock={ this.i18n('entity.RoleGuaranteeRole.guaranteeRole.help') }
                  required/>
                <Advanced.CodeListSelect
                  ref="type"
                  code="guarantee-type"
                  showOnlyIfOptionsExists
                  label={ this.i18n('entity.RoleGuaranteeRole.type.label') }
                  helpBlock={ this.i18n(`entity.RoleGuaranteeRole.type.help`) }
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
      </Basic.Div>
    );
  }
}

RoleGuaranteeRoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  //
  _showLoading: PropTypes.bool
};

RoleGuaranteeRoleTable.defaultProps = {
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

export default connect(select)(RoleGuaranteeRoleTable);
