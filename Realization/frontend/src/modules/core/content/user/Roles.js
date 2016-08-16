import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import SearchParameters from '../../domain/SearchParameters';
import { IdentityRoleManager, IdentityManager, RoleManager, WorkflowProcessInstanceManager, DataManager } from '../../redux';
import AuthoritiesPanel from '../role/AuthoritiesPanel';
import authorityHelp from '../role/AuthoritiesPanel_cs.md';

const uiKey = 'identity-roles';
const uiKeyAuthorities = 'identity-roles';
const roleManager = new RoleManager();
const identityRoleManager = new IdentityRoleManager();
const identityManager = new IdentityManager();
const workflowProcessInstanceManager = new WorkflowProcessInstanceManager();

class Roles extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.user.roles';
  }

  componentDidMount() {
    this.selectSidebarItem('profile-roles');
    const { userID } = this.props.params;
    this.context.store.dispatch(identityRoleManager.fetchRoles(userID, `${uiKey}-${userID}`));
    this.context.store.dispatch(identityManager.fetchAuthorities(userID, `${uiKeyAuthorities}-${userID}`));
  }

  componentWillReceiveProps(nextProps) {
    const { _addRoleProcessIds } = nextProps;
    if (_addRoleProcessIds && _addRoleProcessIds !== this.props._addRoleProcessIds) {
      for (const idProcess of _addRoleProcessIds) {
        const processEntity = workflowProcessInstanceManager.getEntity(this.context.store.getState(), idProcess);
        if (processEntity && processEntity.processVariables.roleIdentifier && !roleManager.isShowLoading(this.context.store.getState(), `role-${processEntity.processVariables.roleIdentifier}`)) {
          this.context.store.dispatch(roleManager.fetchEntityIfNeeded(processEntity.processVariables.roleIdentifier, `role-${processEntity.processVariables.roleIdentifier}`));
        }
      }
    }
  }

  showDetail(entity) {
    const entityFormData = _.merge({}, entity, {
      role: entity.id ? entity._embedded.role.name : null
    });

    this.setState({
      detail: {
        show: true,
        showLoading: false,
        entity: entityFormData
      }
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.role.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        ... this.state.detail,
        show: false
      }
    });
  }

  showProcessDetail(entity) {
    this.context.router.push('workflow/history/processes/' + entity.id);
  }

  /**
   * Compute background color row (added, removed, changed)
   */
  _rowClass({rowIndex, data}) {
    if (data[rowIndex].processVariables.operationType === 'add') {
      return 'bg-success';
    }
    if (data[rowIndex].processVariables.operationType === 'remove') {
      return 'bg-danger';
    }
    if (data[rowIndex].processVariables.operationType === 'change') {
      return 'bg-warning';
    }
    return null;
  }


  save(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const entity = this.refs.form.getData();
    const { userID } = this.props.params;
    const role = roleManager.getEntity(this.context.store.getState(), entity.role);
    entity.identity = identityManager.getSelfLink(userID);
    entity.role = role._links.self.href;
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(identityRoleManager.createEntity(entity, `${uiKey}-${userID}`, (savedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('create.success', { role: role.name, username: userID }) });
          this._afterSave(error);
        } else if (error.statusCode === 202) {
          this.addMessage({ level: 'info', message: this.i18n('create.accepted', { role: role.name, username: userID }) });
          this.refs.tableProcesses.getWrappedInstance().reload();
          this.closeDetail();
        } else {
          this._afterSave(error);
        }
      }));
    } else {
      this.context.store.dispatch(identityRoleManager.patchEntity(entity, `${uiKey}-${userID}`, (savedEntity, error) => {
        this._afterSave(error);
        if (!error) {
          this.addMessage({ message: this.i18n('edit.success', { role: role.name, username: userID }) });
        }
      }));
    }
  }

  _afterSave(error) {
    if (error) {
      this.refs.form.processEnded();
      this.addError(error);
      return;
    }
    const { userID } = this.props.params;
    this.context.store.dispatch(identityManager.fetchAuthorities(userID, `${uiKeyAuthorities}-${userID}`));
    this.closeDetail();
  }

  _onDelete(entity, event) {
    if (event) {
      event.preventDefault();
    }
    const { userID } = this.props.params;
    this.refs['confirm-delete'].show(
      this.i18n(`action.delete.message`, { count: 1, record: entity._embedded.role.name }),
      this.i18n(`action.delete.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(identityRoleManager.deleteEntity(entity, `${uiKey}-${userID}`, (deletedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('delete.success', { role: deletedEntity._embedded.role.name, username: userID }) });
          this.context.store.dispatch(identityManager.fetchAuthorities(userID, `${uiKeyAuthorities}-${userID}`));
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // Rejected
    });
  }

  _onDeleteAddRoleProcessInstance(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-delete'].show(
      this.i18n('content.user.roles.changeRoleProcesses.deleteConfirm', {'processId': entity.id}),
      this.i18n(`action.delete.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(workflowProcessInstanceManager.deleteEntity(entity, null, (deletedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n('content.user.roles.changeRoleProcesses.deleteSuccess', {'processId': entity.id})});
        } else {
          this.addError(error);
        }
        this.refs.tableProcesses.getWrappedInstance().reload();
        this.refs.tablePermissionProcesses.getWrappedInstance().reload();
      }));
    }, () => {
      // Rejected
    });
  }

  _roleNameCell({ rowIndex, data }) {
    const role = roleManager.getEntity(this.context.store.getState(), data[rowIndex].processVariables.roleIdentifier);
    if (role) {
      return role.name;
    }
    return null;
  }

  _changePermissions() {
    const { userID } = this.props.params;
    this.setState({
      showLoading: true
    });
    const promise = identityManager.getService().changePermissions(userID);
    promise.then((json) => {
      this.setState({
        showLoading: false
      });
      this.context.router.push(`/task/${json.id}`);
    }).catch(ex => {
      this.setState({
        showLoading: false
      });
      this.addError(ex);
      this.refs.tableProcesses.getWrappedInstance().reload();
    });
  }

  render() {
    const { userID } = this.props.params;
    const { _entities, _showLoading, authorities } = this.props;
    const { detail } = this.state;
    let force = new SearchParameters();
    force = force.setFilter('identity', userID);
    force = force.setFilter('category', 'eu.bcvsolutions.role.approve');
    let forcePermissions = new SearchParameters();
    forcePermissions = forcePermissions.setFilter('identity', userID);
    forcePermissions = forcePermissions.setFilter('category', 'eu.bcvsolutions.identity.roles.change');

    //
    // sort entities by role name
    // TODO: add sort by validFrom?
    const entities = _.slice(_entities).sort((a, b) => {
      return a._embedded.role.name > b._embedded.role.name;
    });
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Helmet title={this.i18n('title')} />

        <Basic.Row>
          <div className="col-lg-8">
            <Basic.Panel style={{ marginTop: 15 }}>
              <Basic.PanelHeader text={this.i18n('navigation.menu.roles.title')}/>
              {
                _showLoading
                ?
                <Basic.Loading showLoading className="static"/>
                :
                <div>
                  <Basic.Toolbar>
                    <div className="pull-right">
                      <Basic.Button
                        style={{display: 'block'}}
                        level="warning"
                        onClick={this._changePermissions.bind(this)}>
                        <Basic.Icon type="fa" icon="key"/>
                        {' '}
                        { this.i18n('changePermissions') }
                      </Basic.Button>
                    </div>
                    <div className="clearfix"></div>
                  </Basic.Toolbar>
                  <Basic.Table
                    data={entities}
                    showRowSelection={false}>
                    <Basic.Column
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
                    <Basic.Column
                      header={this.i18n('entity.IdentityRole.role')}
                      property="_embedded.role.name"
                      />
                    <Basic.Column
                      property="validFrom"
                      header={this.i18n('label.validFrom')}
                      cell={<Basic.DateCell format={this.i18n('format.date')}/>}
                      />
                    <Basic.Column
                      property="validTill"
                      header={this.i18n('label.validTill')}
                      cell={<Basic.DateCell format={this.i18n('format.date')}/>}/>
                    </Basic.Table>
                  </div>
                }
              </Basic.Panel>
            </div>

            <div className="col-lg-4">
              <Basic.Panel style={{ marginTop: 15 }}>
                <Basic.PanelHeader help={authorityHelp}>
                  <h3><span dangerouslySetInnerHTML={{ __html: this.i18n('authorities') }}/></h3>
                </Basic.PanelHeader>
                <Basic.PanelBody>
                  <AuthoritiesPanel
                    roleManager={roleManager}
                    authorities={authorities}
                    disabled/>
                </Basic.PanelBody>
              </Basic.Panel>
            </div>
          </Basic.Row>
          <Basic.Panel>
            <Basic.PanelHeader text={this.i18n('changeRoleProcesses.header')}/>
            <Advanced.Table
              ref="tableProcesses"
              uiKey="table-processes"
              rowClass={this._rowClass}
              forceSearchParameters={force}
              manager={workflowProcessInstanceManager}
              pagination={false}>
              <Advanced.Column
                property="detail"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <Advanced.DetailButton
                        title={this.i18n('button.detail')}
                        onClick={this.showProcessDetail.bind(this, data[rowIndex])}/>
                    );
                  }
                }
                header={' '}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="currentActivityName"
                header={this.i18n('content.roles.processRoleChange.currentActivity')}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="processVariables.roleIdentifier"
                cell={this._roleNameCell.bind(this)}
                header={this.i18n('content.roles.processRoleChange.roleName')}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="processVariables.validFrom"
                header={this.i18n('content.roles.processRoleChange.roleValidFrom')}
                sort={false}
                face="date"/>
              <Advanced.Column
                property="processVariables.validTill"
                header={this.i18n('content.roles.processRoleChange.roleValidTill')}
                sort={false}
                face="date"/>
              <Advanced.Column
                property="id"
                header={this.i18n('label.id')}
                sort={false}
                face="text"/>
              <Advanced.Column
                header={this.i18n('label.action')}
                className="action"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <Basic.Button
                        level="danger"
                        onClick={this._onDeleteAddRoleProcessInstance.bind(this, data[rowIndex])}
                        className="btn-xs"
                        title={this.i18n('button.delete')}
                        titlePlacement="bottom">
                        <Basic.Icon icon="trash"/>
                      </Basic.Button>
                    );
                  }
                }/>
            </Advanced.Table>
          </Basic.Panel>
          <Basic.Panel>
            <Basic.PanelHeader text={this.i18n('changePermissionProcesses.header')}/>
            <Advanced.Table
              ref="tablePermissionProcesses"
              uiKey="table-permission-processes"
              forceSearchParameters={forcePermissions}
              manager={workflowProcessInstanceManager}
              pagination={false}>
              <Advanced.Column
                property="detail"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <Advanced.DetailButton
                        title={this.i18n('button.detail')}
                        onClick={this.showProcessDetail.bind(this, data[rowIndex])}/>
                    );
                  }
                }
                header={' '}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="processVariables.processInstanceName"
                header={this.i18n('content.roles.processPermissionChange.processInstanceName')}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="currentActivityName"
                header={this.i18n('content.roles.processPermissionChange.currentActivity')}
                sort={false}
                face="text"/>
              <Advanced.Column
                property="id"
                header={this.i18n('label.id')}
                sort={false}
                face="text"/>
              <Advanced.Column
                header={this.i18n('label.action')}
                className="action"
                cell={
                  ({ rowIndex, data }) => {
                    return (
                      <Basic.Button
                        level="danger"
                        onClick={this._onDeleteAddRoleProcessInstance.bind(this, data[rowIndex])}
                        className="btn-xs"
                        title={this.i18n('button.delete')}
                        titlePlacement="bottom">
                        <Basic.Icon icon="trash"/>
                      </Basic.Button>
                    );
                  }
                }/>
            </Advanced.Table>
          </Basic.Panel>
          <Basic.Modal
            bsSize="default"
            show={detail.show}
            onHide={this.closeDetail.bind(this)}
            backdrop="static"
            keyboard={!_showLoading}>

            <form onSubmit={this.save.bind(this)}>
              <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={detail.entity.id === undefined}/>
              <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { role: detail.entity.role })} rendered={detail.entity.id !== undefined}/>
              <Basic.Modal.Body>
                <Basic.AbstractForm ref="form" showLoading={_showLoading} readOnly className="form-horizontal">
                  <Basic.SelectBox
                    ref="role"
                    manager={roleManager}
                    label={this.i18n('entity.IdentityRole.role')}
                    required/>
                  <Basic.DateTimePicker
                    mode="date"
                    ref="validFrom"
                    label={this.i18n('label.validFrom')}/>
                  <Basic.DateTimePicker
                    mode="date"
                    ref="validTill"
                    label={this.i18n('label.validTill')}/>
                </Basic.AbstractForm>
              </Basic.Modal.Body>

              <Basic.Modal.Footer>
                <Basic.Button
                  level="link"
                  onClick={this.closeDetail.bind(this)}
                  showLoading={_showLoading}>
                  {this.i18n('button.close')}
                </Basic.Button>
              </Basic.Modal.Footer>
            </form>
          </Basic.Modal>
        </div>
      );
  }
}

Roles.propTypes = {
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(React.PropTypes.object),
  authorities: PropTypes.arrayOf(React.PropTypes.object)
};
Roles.defaultProps = {
  _showLoading: true,
  _entities: [],
  authorities: []
};

function select(state, component) {
  let addRoleProcessIds;
  if (state.data.ui['table-processes'] && state.data.ui['table-processes'].items) {
    addRoleProcessIds = state.data.ui['table-processes'].items;
  }

  return {
    _showLoading: identityRoleManager.isShowLoading(state, `${uiKey}-${component.params.userID}`),
    _entities: identityRoleManager.getEntities(state, `${uiKey}-${component.params.userID}`),
    _addRoleProcessIds: addRoleProcessIds,
    authorities: DataManager.getData(state, `${uiKeyAuthorities}-${component.params.userID}`)
  };
}

export default connect(select)(Roles);
