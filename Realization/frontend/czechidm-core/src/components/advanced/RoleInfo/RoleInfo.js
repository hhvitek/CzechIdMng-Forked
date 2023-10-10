import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import {
  RoleManager,
  ConfigurationManager,
  SecurityManager
} from '../../../redux';
import AbstractEntityInfo from '../EntityInfo/AbstractEntityInfo';
import RolePriorityEnum from '../../../enums/RolePriorityEnum';
import Tree from '../Tree/Tree';
import CodeListValue from '../CodeListValue/CodeListValue';

const manager = new RoleManager();

/**
 * Role basic information (info card)
 *
 * @author Radek Tomiška
 */
export class RoleInfo extends AbstractEntityInfo {

  getManager() {
    return manager;
  }

  getNiceLabel(entity) {
    const {showEnvironment, showCode} = this.props;
    const _entity = this.getEntity(entity);
    //
    return this.getManager().getNiceLabel(_entity, showEnvironment, showCode);
  }

  showLink() {
    if (!super.showLink()) {
      return false;
    }
    //
    // evaluate authorization policies
    const {_permissions} = this.props;
    if (!manager.canRead(this.getEntity(), _permissions)) {
      return false;
    }
    return true;
  }

  /**
   * Get link to detail (`url`).
   *
   * @return {string}
   */
  getLink(entity) {
    return `/role/${encodeURIComponent(this.getEntityId(entity))}/detail`;
  }

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
  getEntityIcon(entity) {
    if (entity && entity.childrenCount > 0) {
      return 'component:business-role';
    }
    return 'component:role';
  }

  /**
   * Returns popovers title
   *
   * @param  {object} entity
   */
  getPopoverTitle() {
    return this.i18n('entity.Role._type');
  }

  getTableChildren() {
    // component are used in #getPopoverContent => skip default column resolving
    return [
      <Basic.Column property="label"/>,
      <Basic.Column property="value"/>
    ];
  }

  _renderIcon() {
    const {showTree} = this.props;
    if (showTree) {
      return null;
    }
    return super._renderIcon();
  }

  _renderPopover() {
    const {showTree} = this.props;

    if (!showTree) {
      return super._renderPopover();
    }
    if (!SecurityManager.hasAuthority('ROLE_AUTOCOMPLETE')) {
      return super._renderPopover();
    }
    //
    const _entity = this.getEntity();
    //
    return (
      <Tree
        uiKey={`role-info-${this.getEntityId()}`}
        onMouseDown={this._stopPropagationMouseDown.bind(this)}
        manager={this.getManager()}
        roots={[_entity]}
        header={null}
        className="role-info-tree"
        bodyClassName="role-info-tree-body"
        onChange={() => false}
        nodeIcon={({node}) => (this.props.showIcon ? this.getEntityIcon(node) : null)}
        nodeStyle={{paddingLeft: 0}}
        nodeIconClassName={null}
        renderNode={({node}) => {
          // FIXME: maxWidth + inline-block for IE - find a way, how to fix overflowX
          // TODO: maxWidth configurable
          return (
            <span className="spanRole">{super._renderPopover(node)}</span>
          );
        }}
      />
    );
  }

  /**
   * Returns popover info content
   *
   * @param  {array} table data
   */
  getPopoverContent(entity) {
    const content = [
      {
        label: this.i18n('entity.name.label'),
        value: entity.name
      },
      {
        label: this.i18n('entity.Role.code.label'),
        value: entity.baseCode
      }
    ];
    if (entity.environment && this.props.showEnvironment) {
      content.push({
        label: this.i18n('entity.Role.environment.label'),
        value: (<CodeListValue code="environment" value={entity.environment}/>)
      });
    }
    //
    content.push({
      label: this.i18n('entity.Role.priorityEnum'),
      value: (
        <Basic.EnumValue
          enum={RolePriorityEnum}
          value={RolePriorityEnum.findKeyBySymbol(RolePriorityEnum.getKeyByPriority(entity.priority))}/>
      )
    });
    //
    if (entity.description) {
      content.push({
        label: this.i18n('entity.Role.description'),
        value: (
          <Basic.ShortText value={entity.description} maxLength={100}/>
        )
      });
    }
    //
    return content;
  }
}

RoleInfo.propTypes = {
  ...AbstractEntityInfo.propTypes,
  /**
   * Selected entity - has higher priority
   */
  entity: PropTypes.object,
  /**
   * Selected entity's id - entity will be loaded automatically
   */
  entityIdentifier: PropTypes.string,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ])
};
RoleInfo.defaultProps = {
  ...AbstractEntityInfo.defaultProps,
  entity: null,
  showTree: true,
  face: 'link',
  _showLoading: true,
};

function select(state, component) {
  const {entityIdentifier, entity, showEnvironment, showCode} = component;
  let entityId = entityIdentifier;
  if (!entityId && entity) {
    entityId = entity.id;
  }
  return {
    showEnvironment: ConfigurationManager.getPublicValueAsBoolean(
      state,
      'idm.pub.app.show.environment',
      Utils.Ui.isEmpty(showEnvironment) ? true : showEnvironment
    ),
    showCode: ConfigurationManager.getPublicValueAsBoolean(
      state,
      'idm.pub.app.show.role.baseCode',
      Utils.Ui.isEmpty(showCode) ? true : showCode
    ),
    _entity: manager.getEntity(state, entityId),
    _showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(RoleInfo);
