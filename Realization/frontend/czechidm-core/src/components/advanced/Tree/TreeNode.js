import React from 'react';
import { useSelector } from 'react-redux';
import { isFunction } from 'swiss-knife-utils';
import {
  getEntity,
  getTreeNodeChildrenCount,
  getTreeNodeLabel,
  isTreeNodeExpanded, isTreeNodeSelected
} from '../../../redux/ui/selectors';
import {
  useIsUiStateLoading,
  useUiStateItems
} from '../../../redux/data/hooks';
import { TreeItem } from '../../basic/TreeItem';

export function TreeNode({
  uiKey,
  id,
  entityType,
  onClick,
  onDoubleClick,
  onExpand,
  onCollapse,
  expanded: expandedProp,
  selected: selectedProp,
  renderNode,
  nodeIcon
}) {
  const loading = useIsUiStateLoading(uiKey, id);
  const childNodes = useUiStateItems(uiKey, id);
  const node = useSelector(state => getEntity(state, id, entityType));
  const label = useSelector(state => getTreeNodeLabel(state, id, entityType));
  const childrenCount = useSelector(state => getTreeNodeChildrenCount(state, id, entityType));
  const stateExpanded = useSelector(state => isTreeNodeExpanded(state, uiKey, id));
  const stateSelected = useSelector(state => isTreeNodeSelected(state, uiKey, id));

  const handleClick = (e) => {
    if (isFunction(onClick)) {
      onClick(id, e);
    }
  };

  const handleDoubleClick = (e) => {
    if (isFunction(onDoubleClick)) {
      onDoubleClick(id, e);
    }
  };

  const handleExpand = () => {
    if (isFunction(onExpand)) {
      onExpand(id);
    }
  };

  const handleCollapse = () => {
    if (isFunction(onCollapse)) {
      onCollapse(id);
    }
  };

  const expanded = stateExpanded || expandedProp;
  const selected = stateSelected || selectedProp;

  return (
    <TreeItem
      nodeId={id}
      loading={loading}
      label={label}
      expandable={childrenCount > 0}
      childrenCount={childrenCount}
      expanded={expanded}
      selected={selected}
      onClick={handleClick}
      onDoubleClick={handleDoubleClick}
      onExpand={handleExpand}
      onCollapse={handleCollapse}
      renderNode={renderNode}
      nodeIcon={nodeIcon}
      node={node}
    >
      {childNodes?.map(childNodeId => (
        <TreeNode
          key={childNodeId}
          id={childNodeId}
          uiKey={uiKey}
          entityType={entityType}
          onClick={onClick}
          onDoubleClick={onDoubleClick}
          onExpand={onExpand}
          onCollapse={onCollapse}
          renderNode={renderNode}
          nodeIcon={nodeIcon}
        />
      ))}
    </TreeItem>
  );
}

TreeNode.defaultProps = {
  level: 0
};
