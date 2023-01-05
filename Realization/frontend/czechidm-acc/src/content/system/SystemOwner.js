import React from "react";
import { connect } from "react-redux";
import { Advanced, Basic, Managers } from "czechidm-core";
import SystemOwnerManager from "../../redux/SystemOwnerManager";
import _ from "lodash";
import { SystemManager } from "../../redux";
import SystemSelect from "../../components/SystemSelect/SystemSelect";

const manager = new SystemOwnerManager();
const identityManager = new Managers.IdentityManager();
const systemManager = new SystemManager();

class SystemOwner extends Advanced.AbstractTableContent {
  constructor(props) {
    super(props);
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getContentKey() {
    return "acc:content.system.owner";
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getNavigationKey() {
    return this.getRequestNavigationKey(
      "system-owner",
      this.props.match.params
    );
  }

  showDetail(entity) {
    super.showDetail(entity, () => {
    });
  }

  save = (entity, event) => {
    const formEntity = this.refs.form.getData();
    super.save(formEntity, event);
  };

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('action.save.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
    }
    //
    super.afterSave(entity, error);
  }

  render() {
    const { uiKey,_showLoading,forceSearchParameters } = this.props;
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger" />
        <Advanced.Table
          ref="table"
          manager={manager}
          uiKey = {uiKey}
          forceSearchParameters={forceSearchParameters}
          showRowSelection={manager.canDelete()}
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'),  action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={[
            <Basic.Button
              level="success"
              key="add_button"
              className="btn-xs"
              onClick={this.showDetail.bind(this, {system: this.props.match.params.entityId,})}>
              <Basic.Icon type="fa" icon="plus" /> {this.i18n("button.add")}
            </Basic.Button>,
          ]}>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={({ rowIndex, data }) => {
              return (
                <Advanced.DetailButton
                  title={this.i18n("button.detail")}
                  onClick={this.showDetail.bind(this, data[rowIndex])}
                  sort={false}
                />
              );
            }}>
            </Advanced.Column>
          <Advanced.Column
            property="owner"
            sortProperty="owner.name"
            face="text"
            header={this.i18n("acc:entity.SystemOwner.owner.label")}
            sort={false}
            cell={({ rowIndex, data }) => {
              const entity = data[rowIndex];
              return (
                <Advanced.EntityInfo
                  entityType="identity"
                  entityIdentifier={entity.owner}
                  entity={entity._embedded.owner}
                  face="popover"
                  showIcon
                />
              );
            }}
          />
          <Advanced.Column></Advanced.Column>
        </Advanced.Table>
        <Basic.Modal
          bsSize="large"
          show={this.state.detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>
          <form onSubmit={this.save.bind({}, this)}>
            <Basic.Modal.Header
              closeButton={!_showLoading}
              text={this.i18n("create.header")}/>
            <Basic.Modal.Header
              closeButton={!_showLoading}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={_showLoading}>
                <SystemSelect
                  ref="system"
                  manager={systemManager}
                  label={this.i18n("acc:entity.SystemOwner.system.label")}
                  readOnly
                  required/>
                <Advanced.IdentitySelect
                  ref="owner"
                  manager={identityManager}
                  label={this.i18n("acc:entity.SystemOwner.owner.label")}
                  helpBlock={this.i18n("acc:entity.SystemOwner.owner.help")}
                  required/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n("button.close")}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n("button.saving")}>
                {this.i18n("button.save")}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </Basic.Div>
    );
  }
}
function select(state, props) {
  return {
  };
}
export default connect(select)(SystemOwner);

