import PropTypes from 'prop-types';
import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import RecursionTypeEnum from '../../enums/RecursionTypeEnum';
import { RoleTreeNodeManager, AutomaticRoleRequestManager, SecurityManager} from '../../redux';
import AutomaticRoleRequestTableComponent, { AutomaticRoleRequestTable } from '../automaticrolerequest/AutomaticRoleRequestTable';
import SearchParameters from '../../domain/SearchParameters';

const manager = new RoleTreeNodeManager();
const automaticRoleRequestManager = new AutomaticRoleRequestManager();

/**
* Table of automatic roles by tree structure.
*
* @author Radek Tomiška
*/
export class RoleTreeNodeTable extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.role.tree-nodes';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    return manager;
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
    const { forceSearchParameters } = this.props;
    //
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    let roleId = null;
    let treeNodeId = null;
    if (forceSearchParameters) {
      if (forceSearchParameters.getFilters().has('roleId')) {
        roleId = forceSearchParameters.getFilters().get('roleId');
      }
      if (forceSearchParameters.getFilters().has('treeNodeId')) {
        treeNodeId = forceSearchParameters.getFilters().get('treeNodeId');
      }
    }
    //
    const entityFormData = _.merge({}, entity, {
      role: entity._embedded && entity._embedded.role ? entity._embedded.role : roleId,
      treeNode: entity._embedded && entity._embedded.treeNode ? entity._embedded.treeNode : treeNodeId
    });
    //
    super.showDetail(entityFormData, () => {
      if (forceSearchParameters && forceSearchParameters.getFilters().has('roleId')) {
        this.refs.name.focus();
      } else {
        this.refs.role.focus();
      }
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ level: 'info', message: this.i18n('save.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
    }
    //
    super.afterSave(entity, error);
    this.refs['automatic-role-requests-table'].reload();
  }

  afterDelete() {
    super.afterDelete();
    this.refs['automatic-role-requests-table'].reload();
  }

  _onChangeSelectRole(id, value) {
    const roleCode = value ? value.name : '';
    const name = this.refs.name.getValue();
    if (!name || this.state.oldRoleCode === name) {
      this.setState({
        oldRoleCode: roleCode
      }, () => {
        this.refs.name.setValue(roleCode);
      });
    }
  }

  render() {
    const { uiKey, columns, forceSearchParameters, _showLoading, _permissions } = this.props;
    const { detail } = this.state;

    let roleId = null;
    if (forceSearchParameters) {
      if (forceSearchParameters.getFilters().has('roleId')) {
        roleId = forceSearchParameters.getFilters().get('roleId');
      }
    }
    let requestForceSearch = new SearchParameters();
    requestForceSearch = requestForceSearch.setFilter('roleId', roleId);
    requestForceSearch = requestForceSearch.setFilter('requestType', 'TREE');
    requestForceSearch = requestForceSearch.setFilter('states', ['IN_PROGRESS', 'CONCEPT', 'EXCEPTION']);
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
                onClick={ this.showDetail.bind(this, { recursionType: RecursionTypeEnum.findKeyBySymbol(RecursionTypeEnum.NO) }) }
                rendered={ manager.canSave() }>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('filter.text.placeholder') }
                      help={ Advanced.Filter.getTextHelp() }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TreeNodeSelect
                      ref="treeNodeId"
                      label={ null }
                      placeholder={ this.i18n('filter.treeNodeId.placeholder') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
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
          <Advanced.Column
            property="name"
            header={ this.i18n('entity.AutomaticRole.name.label') }
            face="text"
            width="20%"
            rendered={ _.includes(columns, 'name') }
            sort/>
          <Advanced.Column
            property="_embedded.role.name"
            width="25%"
            header={ this.i18n('entity.RoleTreeNode.role') }
            sort
            sortProperty="role.name"
            face="text"
            rendered={ _.includes(columns, 'role') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={ entity.role }
                    entity={ entity._embedded.role }
                    face="popover"
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            property="_embedded.treeNode.name"
            width="40%"
            header={ this.i18n('entity.RoleTreeNode.treeNode') }
            face="text"
            sort
            sortProperty="treeNode.name"
            rendered={ _.includes(columns, 'treeNode') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="treeNode"
                    entityIdentifier={ entity.treeNode }
                    entity={ entity._embedded.treeNode }
                    face="popover"/>
                );
              }
            }/>
          <Advanced.Column
            property="recursionType"
            header={ this.i18n('entity.RoleTreeNode.recursionType') }
            face="enum"
            enumClass={ RecursionTypeEnum }
            sort
            rendered={ _.includes(columns, 'recursionType') }/>
        </Advanced.Table>

        {
          !SecurityManager.hasAuthority('AUTOMATICROLEREQUEST_READ')
          ||
          <Basic.Div className="tab-pane-table-body">
            <Basic.ContentHeader style={{ marginBottom: 0 }} text={ this.i18n('content.automaticRoles.request.header') }/>
            <AutomaticRoleRequestTableComponent
              ref="automatic-role-requests-table"
              uiKey="role-automatic-role-requests-table"
              forceSearchParameters={ requestForceSearch }
              columns={
                _.difference(
                  AutomaticRoleRequestTable.defaultProps.columns,
                  roleId
                  ?
                  ['role', 'executeImmediately', 'startRequest', 'createNew']
                  :
                  ['executeImmediately', 'startRequest', 'createNew', 'wf_name', 'modified']
                )
              }
              showFilter={ false }
              manager={ automaticRoleRequestManager }/>
          </Basic.Div>
        }

        <Basic.Modal
          bsSize="large"
          show={ detail.show }
          onHide={ this.closeDetail.bind(this) }
          backdrop="static"
          keyboard={ !_showLoading }>

          <form onSubmit={ this.save.bind(this, {}) }>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={ this.i18n('create.header') }
              rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              closeButton={ !_showLoading }
              text={ this.i18n('edit.header', { name: detail.entity.name }) }
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading }
                readOnly={ !manager.canSave(detail.entity, _permissions) }>
                <Advanced.RoleSelect
                  ref="role"
                  label={ this.i18n('entity.RoleTreeNode.role') }
                  readOnly={ !Utils.Entity.isNew(detail.entity) || !_.includes(columns, 'role') }
                  required
                  onChange={ this._onChangeSelectRole.bind(this) }/>
                <Basic.TextField
                  ref="name"
                  required
                  readOnly={ !Utils.Entity.isNew(detail.entity) || !_.includes(columns, 'name') }
                  label={ this.i18n('entity.AutomaticRole.name.label') }
                  helpBlock={ this.i18n('entity.AutomaticRole.name.help') }/>
                <Advanced.TreeNodeSelect
                  ref="treeNode"
                  label={ this.i18n('entity.RoleTreeNode.treeNode')}
                  readOnly={ !Utils.Entity.isNew(detail.entity) || !_.includes(columns, 'treeNode') }
                  required/>
                <Basic.EnumSelectBox
                  ref="recursionType"
                  enum={ RecursionTypeEnum }
                  label={ this.i18n('entity.RoleTreeNode.recursionType') }
                  readOnly={ !Utils.Entity.isNew(detail.entity) }
                  required/>
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
                rendered={ Utils.Entity.isNew(detail.entity) && manager.canSave(detail.entity, _permissions) }
                showLoading={ _showLoading}
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }>
                { this.i18n('button.save') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}

RoleTreeNodeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  //
  _showLoading: PropTypes.bool
};

RoleTreeNodeTable.defaultProps = {
  columns: ['name', 'role', 'treeNode', 'recursionType'],
  forceSearchParameters: null,
  _showLoading: false
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(RoleTreeNodeTable);
