import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Utils from '../../../utils';
import { IdentityContractManager, SecurityManager, IdentityManager, TreeTypeManager} from '../../../redux/';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import DateValue from '../DateValue/DateValue';

const manager = new IdentityContractManager();
const identityManager = new IdentityManager();
const treeTypeManager = new TreeTypeManager();


/**
 * Component for rendering nice identifier for identity contracts, similar function as roleInfo
 *
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
export class IdentityContractInfo extends AbstractEntityInfo {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    if (!SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITYCONTRACT_READ']})) {
      return false;
    }
    return true;
  }

  /**
   * Returns true, when disabled decorator has to be used
   *
   * @param  {object} entity
   * @return {bool}
   */
  isDisabled(entity) {
    return !Utils.Entity.isValid(entity);
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink() {
    const { entityIdentifier } = this.props;
    const _entity = this.getEntity();
    //
    return `/identity/${encodeURIComponent(_entity._embedded.identity.username)}/identity-contract/${entityIdentifier}/detail`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon() {
    return 'fa:building';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.IdentityContract._type');
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    // idenity nice label
    let identityNiceLabel = '';
    if (entity && entity._embedded) {
      identityNiceLabel = identityManager.getNiceLabel(entity._embedded.identity);
    }

    // working position nice label
    let workingPositionNiceLable = '';
    if (entity && entity._embedded && entity._embedded.workPosition
        && entity._embedded.workPosition._embedded) {
      workingPositionNiceLable = treeTypeManager.getNiceLabel(entity._embedded.workPosition._embedded.treeType);
    }
    return [
      {
        label: this.i18n('entity.Identity._type'),
        value: identityNiceLabel
      },
      {
        label: this.i18n('entity.IdentityContract.position'),
        value: manager.getNiceLabel(entity, false)
      },
      {
        label: this.i18n('entity.TreeType._type'),
        value: workingPositionNiceLable
      },
      {
        label: this.i18n('entity.validFrom'),
        value: (<DateValue value={ entity.validFrom }/>)
      },
      {
        label: this.i18n('entity.validTill'),
        value: (<DateValue value={ entity.validTill }/>)
      }
    ];
  }
}

IdentityContractInfo.propTypes = {
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
   * Show contract's identity
   */
  showIdentity: PropTypes.bool,
  //
  _showLoading: PropTypes.bool,
};
IdentityContractInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  face: 'link',
  _showLoading: true,
  showIdentity: true,
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.entityIdentifier),
    _showLoading: manager.isShowLoading(state, null, component.entityIdentifier)
  };
}
export default connect(select)(IdentityContractInfo);
