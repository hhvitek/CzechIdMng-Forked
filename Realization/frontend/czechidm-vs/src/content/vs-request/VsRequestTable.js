import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import uuid from 'uuid';
import { Link } from 'react-router-dom';
//
import { Basic, Advanced, Utils, Domain, Managers} from 'czechidm-core';
import { VsRequestManager} from '../../redux';
import VsOperationType from '../../enums/VsOperationType';
import VsRequestState from '../../enums/VsRequestState';

const accManagers = require('czechidm-acc').Managers;
const coreManagers = require('czechidm-core').Managers;

const manager = new VsRequestManager();
const systemManager = new accManagers.SystemManager();
const identityManager = new coreManagers.IdentityManager();

/**
* Table of virtula system requests
*
* @author Vít Švanda
*
*/
export class VsRequestTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.vs-requests';
  }

  /**
   * Base manager for this agenda (used in `AbstractTableContent`)
   */
  getManager() {
    return manager;
  }

  /**
   * Submit filter action
   */
  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  /**
   * Cancel filter action
   */
  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  /**
   * Link to detail / create
   */
  showDetail(entity) {
    if (Utils.Entity.isNew(entity)) {
      const uuidId = uuid.v1();
      this.context.history.push(`/vs/request/${uuidId}/new?new=1`);
    } else {
      this.context.history.push(`/vs/request/${entity.id}/detail`);
    }
  }

  /**
  * Mark virtual system request as realized (changes will be propagated to VsAccount)
  */
  realize(bulkActionValue, ids, event) {
    return manager.realize(bulkActionValue, ids, this, null, event);
  }

  /**
  * Cancel virtual system request
  */
  cancel(bulkActionValue, ids, event) {
    return manager.cancel(bulkActionValue, ids, this, null, event);
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    return Utils.Ui.getSimpleJavaType(name);
  }

  _getSystemCell({ rowIndex, data }) {
    return (
      <Advanced.EntityInfo
        entityType="system"
        entityIdentifier={ data[rowIndex].system }
        entity={ data[rowIndex]._embedded.system}
        face="popover" />
    );
  }

  _getUidCell({ rowIndex, data }) {
    return (
      <Advanced.EntityInfo
        entityType="vs-request"
        entityIdentifier={ data[rowIndex].id }
        entity={ data[rowIndex] }
        face="link" />
    );
  }

  renderTargetEntity({rowIndex, data}) {
    if (!data[rowIndex].targetEntity) {
      return data[rowIndex].uid;
    }
    return (
      <Advanced.EntityInfo
        entityType={ this._getType(data[rowIndex].targetEntityType) }
        entity={ data[rowIndex].targetEntity }
        showIcon
        face="popover"
        showEntityType/>
    );
  }

  _getImplementersCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.implementers) {
      return '';
    }
    const identities = [];
    for (const implementer of entity.implementers) {
      identities.push(implementer.id);
    }

    return (
      <Advanced.IdentitiesInfo identities={identities}/>
    );
  }

  _getButtonsCell({ rowIndex, data}) {
    return (
      <span>
        <Basic.Button
          ref="realizeButton"
          type="button"
          level="success"
          rendered={Managers.SecurityManager.hasAnyAuthority(['VSREQUEST_UPDATE'])}
          title={this.i18n('vs:content.vs-request.detail.button.request.realize')}
          titlePlacement="bottom"
          style={{ marginRight: 5 }}
          onClick={this.realize.bind(this, 'realize', [data[rowIndex].id])}
          disabled={ data[rowIndex].state !== 'IN_PROGRESS' }
          buttonSize="xs"
          icon="fa:check"/>
        <Basic.Button
          ref="cancelButton"
          type="button"
          level="danger"
          rendered={Managers.SecurityManager.hasAnyAuthority(['VSREQUEST_UPDATE'])}
          title={this.i18n('vs:content.vs-request.detail.button.request.cancel')}
          titlePlacement="bottom"
          onClick={this.cancel.bind(this, 'cancel', [data[rowIndex].id])}
          disabled={ data[rowIndex].state !== 'IN_PROGRESS' }
          buttonSize="xs"
          icon="fa:remove"/>
      </span>
    );
  }

  render() {
    const {
      uiKey,
      columns,
      forceSearchParameters,
      showRowSelection, showFilter,
      showToolbar,
      showPageSize,
      showId,
      className,
      defaultSearchParameters
    } = this.props;
    const { filterOpened, showLoading} = this.state;

    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Basic.Confirm ref="confirm-realize" level="success">
          <Basic.Div style={{ marginTop: 20 }}>
            <Basic.AbstractForm ref="realize-form" uiKey="confirm-realize" >
              <Basic.TextArea
                ref="realize-reason"
                placeholder={this.i18n('vs:content.vs-requests.realize-reason.placeholder')}
              />
            </Basic.AbstractForm>
          </Basic.Div>
        </Basic.Confirm>
        <Basic.Confirm ref="confirm-cancel" level="danger">
          <Basic.Div style={{ marginTop: 20 }}>
            <Basic.AbstractForm ref="cancel-form" uiKey="confirm-cancel" >
              <Basic.TextArea
                ref="cancel-reason"
                placeholder={this.i18n('vs:content.vs-requests.cancel-reason.placeholder')}
                required/>
            </Basic.AbstractForm>
          </Basic.Div>
        </Basic.Confirm>

        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          showFilter={showFilter}
          showToolbar={showToolbar}
          showPageSize={showPageSize}
          showId={showId}
          condensed
          filterOpened={filterOpened}
          showLoading={showLoading}
          forceSearchParameters={ forceSearchParameters }
          defaultSearchParameters={defaultSearchParameters}
          showRowSelection={showRowSelection}
          className={ className }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.FilterDate
                      ref="fromTill"
                      facePlaceholder={this.i18n('vs:entity.VsRequest.created.label')}
                      fromProperty="createdAfter"
                      tillProperty="createdBefore"/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.FilterDate
                      ref="fromTillmodified"
                      facePlaceholder={this.i18n('vs:entity.VsRequest.modified.label')}
                      fromProperty="modifiedAfter"
                      tillProperty="modifiedBefore" />
                  </Basic.Col>
                  <Basic.Col lg={ 4 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
                <Basic.Row className="last">
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.SelectBox
                      ref="systemId"
                      placeholder={this.i18n('acc:entity.System._type')}
                      multiSelect={false}
                      forceSearchParameters={new Domain.SearchParameters()
                        .setName(Domain.SearchParameters.NAME_AUTOCOMPLETE)
                        .setFilter('virtual', true)}
                      manager={systemManager}/>
                  </Basic.Col>
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.SelectBox
                      ref="implementers"
                      placeholder={this.i18n('filter.implementers.placeholder')}
                      multiSelect
                      manager={identityManager}/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          _searchParameters={ this.getSearchParameters() }
        >

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
            property="uid"
            header={this.i18n('vs:entity.VsRequest.uid.label')}
            rendered={_.includes(columns, 'uid')}
            sort
            cell={this._getUidCell.bind(this)}/>
          <Advanced.Column
            property="targetEntity"
            rendered={ _.includes(columns, 'targetEntity') }
            header={ this.i18n('vs:content.vs-request.detail.accountOwner') }
            cell={ this.renderTargetEntity.bind(this) }
          />
          <Advanced.Column
            header={this.i18n('acc:entity.System.name')}
            rendered={_.includes(columns, 'systemId')}
            cell={this._getSystemCell.bind(this)}/>
          <Advanced.Column
            property="operationType"
            width={ 125 }
            sort
            face="enum"
            enumClass={VsOperationType}
            rendered={_.includes(columns, 'operationType')}/>
          <Advanced.Column
            property="state"
            width={ 125 }
            sort
            face="enum"
            enumClass={VsRequestState}
            rendered={_.includes(columns, 'state')}/>
          <Advanced.Column
            property="executeImmediately"
            width={ 75 }
            sort
            face="bool"
            rendered={_.includes(columns, 'executeImmediately')}/>
          <Advanced.Column
            header={this.i18n('vs:entity.VsRequest.implementers.label')}
            rendered={_.includes(columns, 'implementers')}
            cell={this._getImplementersCell.bind(this)}/>
          <Advanced.Column
            property="created"
            width={ 125 }
            sort
            face="datetime"
            rendered={_.includes(columns, 'created')}/>
          <Advanced.Column
            property="creator"
            width={ 125 }
            sort
            face="text"
            rendered={_.includes(columns, 'creator')}/>
          <Advanced.Column
            property="modified"
            width={ 125 }
            sort
            face="datetime"
            rendered={_.includes(columns, 'modified')}/>
          <Advanced.Column
            property="modifier"
            width={ 125 }
            sort
            face="text"
            rendered={_.includes(columns, 'modifier')}/>
          <Advanced.Column
            property="roleRequestId"
            header={ this.i18n('acc:entity.ProvisioningOperation.roleRequestId.label') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (!entity || !entity.roleRequestId) {
                  return null;
                }
                return (
                  <Link
                    to={ `/role-requests/${encodeURIComponent(entity.roleRequestId)}/detail` }
                    title={ this.i18n('acc:entity.ProvisioningOperation.roleRequestId.help') }>
                    <Basic.Icon value="fa:key" style={{marginLeft: '25px'}}/>
                  </Link>
                );
              }
            }
            rendered={ _.includes(columns, 'roleRequestId') } />
          <Advanced.Column
            property=""
            header=""
            rendered={_.includes(columns, 'operations')}
            width={ 75 }
            cell={this._getButtonsCell.bind(this)}/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

VsRequestTable.propTypes = {
  /**
   * Entities, permissions etc. fro this content are stored in redux under given key
   */
  uiKey: PropTypes.string.isRequired,
  /**
   * Rendered columns (all by default)
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * Show filter or collapse
   */
  filterOpened: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Show row selection for bulk actions
   */
  showRowSelection: PropTypes.bool
};

VsRequestTable.defaultProps = {
  columns: ['state',
    'systemId',
    'operationType',
    'executeImmediately',
    'implementers',
    'created',
    'creator',
    'operations',
    'roleRequestId',
    'targetEntity',
    'uid'],
  filterOpened: false,
  forceSearchParameters: new Domain.SearchParameters(),
  showAddButton: true,
  showRowSelection: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey), // persisted filter state in redux
  };
}

export default connect(select, null, null, { forwardRef: true })(VsRequestTable);
