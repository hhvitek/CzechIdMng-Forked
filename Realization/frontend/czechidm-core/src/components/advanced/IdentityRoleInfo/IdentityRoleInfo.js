import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import { IdentityRoleManager, IdentityContractManager, RoleManager, IdentityManager } from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import DateValue from '../DateValue/DateValue';
import EntityInfo from '../EntityInfo/EntityInfo';

const manager = new IdentityRoleManager();
const identityContractManager = new IdentityContractManager();
const roleManager = new RoleManager();
const identityManager = new IdentityManager();

/**
 * Assigned role basic information (info card)
 *
 * @author Radek Tomiška
 */
export class IdentityRoleInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  getNiceLabel() {
    const _entity = this.getEntity();
    const { showIdentity } = this.props;
    //
    return this.getManager().getNiceLabel(_entity, showIdentity);
  }

  _isNotLoaded(permissions) {
    return permissions === undefined || permissions === null || permissions === false;
  }

  onEnter() {
    super.onEnter();
    //
    const entity = this.getEntity();
    if (entity && entity.identityContract) {
      const _contractPermissions = identityContractManager.getPermissions(this.context.store.getState(), null, entity.identityContract);
      if (this._isNotLoaded(_contractPermissions)) {
        this.context.store.dispatch(identityContractManager.fetchPermissions(entity.identityContract));
      }
      const _rolePermissions = roleManager.getPermissions(this.context.store.getState(), null, entity.role);
      if (this._isNotLoaded(_rolePermissions)) {
        this.context.store.dispatch(roleManager.fetchPermissions(entity.role));
      }
      if (entity._embedded && entity._embedded.identityContract) {
        const _identityPermissions = identityManager.getPermissions(this.context.store.getState(), null, entity._embedded.identityContract.identity);
        if (this._isNotLoaded(_identityPermissions)) {
          this.context.store.dispatch(identityManager.fetchPermissions(entity._embedded.identityContract.identity));
        }
      }
    }
  }

  showLink() {
    // TODO: assigned role modal detail should be open automatically ...
    return false;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    return null;
    /* TODO: assigned role modal detail should be open automatically ...
    const entity = this.getEntity();
    if (!entity._embedded || !entity._embedded.identityContract || !entity._embedded.identityContract._embedded || !entity._embedded.identityContract._embedded.identity) {
      return null;
    }
    //
    return `/identity/${encodeURIComponent(entity._embedded.identityContract._embedded.identity.username)}/roles`;
    */
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'component:identity-role';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.IdentityRole._type');
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label"/>,
      <Basic.Column property="value"/>
    ];
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    const content = [];
    content.push(
      {
        label: this.i18n('entity.Identity._type'),
        value: !entity._embedded || !entity._embedded.identityContract || !entity._embedded.identityContract._embedded ||
          <EntityInfo
            entityType="identity"
            entity={ entity._embedded.identityContract._embedded.identity }
            entityIdentifier={ entity._embedded.identityContract.identity }
            face="link" />
      },
      {
        label: this.i18n('entity.IdentityContract._type'),
        value: (
          <EntityInfo
            entityType="identityContract"
            entity={ entity._embedded ? entity._embedded.identityContract : null }
            entityIdentifier={ entity.identityContract }
            showIdentity={ !entity._embedded }
            face="link" />
        )
      }
    );
    //
    content.push(
      {
        label: this.i18n('entity.Role._type'),
        value: (
          <EntityInfo
            entityType="role"
            entity={ entity._embedded ? entity._embedded.role : null }
            entityIdentifier={ entity.role }
            face="link" />
        )
      }
    );
    //
    content.push(
      {
        label: this.i18n('entity.validFrom'),
        value: (<DateValue value={ entity.validFrom }/>)
      },
      {
        label: this.i18n('entity.validTill'),
        value: (<DateValue value={ entity.validTill }/>)
      }
    );
    return content;
  }
}

IdentityRoleInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  /**
   * Show asigned role's identity
   */
  showIdentity: PropTypes.bool,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ])
};
IdentityRoleInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
  showIdentity: true
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier),
    _permissions: manager.getPermissions(state, null, component.entityIdentifier)
  };
}
export default connect(select)(IdentityRoleInfo);
