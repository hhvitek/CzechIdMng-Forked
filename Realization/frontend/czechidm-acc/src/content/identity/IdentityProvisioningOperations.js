import React from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Domain, Managers } from 'czechidm-core';
import ProvisioningOperations from '../provisioning/ProvisioningOperations';
import { ProvisioningOperationTable } from '../provisioning/ProvisioningOperationTable';

const identityManager = new Managers.IdentityManager();

/**
 * Identity's provisioning operations (active and archived)
 *
 * @author Radek Tomiška
 */
class IdentityProvisioningOperations extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'acc:content.provisioningOperations';
  }

  getNavigationKey() {
    return 'identity-provisioning-operations';
  }

  render() {
    const { entity } = this.props;
    if (!entity) {
      return (
        <Basic.Loading isStatic show />
      );
    }
    const forceSearchParameters = new Domain.SearchParameters()
      .setFilter('entityIdentifier', entity.id)
      .setFilter('entityType', 'IDENTITY');
    let columns = ProvisioningOperationTable.defaultProps.columns;
    columns = _.difference(columns, ['entityType', 'entityIdentifier']);
    //
    return (
      <Basic.Div>
        <Basic.ContentHeader text={ this.i18n('header', { escape: false }) }/>

        <ProvisioningOperations
          uiKey="identity-provisioning-operation-table"
          forceSearchParameters={ forceSearchParameters }
          columns={ columns }
          showDeleteAllButton={ false }/>
      </Basic.Div>
    );
  }
}

function select(state, component) {
  return {
    entity: identityManager.getEntity(state, component.match.params.entityId)
  };
}

export default connect(select)(IdentityProvisioningOperations);
