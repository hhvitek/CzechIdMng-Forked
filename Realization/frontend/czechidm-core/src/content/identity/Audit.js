import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import EntityAuditTable from '../audit/EntityAuditTable';
import SearchParameters from '../../domain/SearchParameters';
import { IdentityManager } from '../../redux/data';
//
const identityManager = new IdentityManager();

/**
 * Identity audit tab.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class Audit extends Basic.AbstractContent {

  getContentKey() {
    return 'content.audit';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    this.context.store.dispatch(identityManager.fetchEntityIfNeeded(entityId));
  }

  getNavigationKey() {
    return 'profile-audit-profile';
  }

  render() {
    const { identity } = this.props; // ~ codeable support
    //
    if (!identity) {
      return (
        <Basic.Loading isStatic show/>
      );
    }
    //
    const forceSearchParameters = new SearchParameters()
      .setFilter('withVersion', true)
      .setFilter('ownerType', 'eu.bcvsolutions.idm.core.model.entity.IdmIdentity')
      .setFilter('ownerId', identity.id);
    //
    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        <EntityAuditTable
          uiKey="identity-audit-table"
          forceSearchParameters={ forceSearchParameters }/>
      </div>
    );
  }
}

Audit.propTypes = {
};

Audit.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(Audit);
