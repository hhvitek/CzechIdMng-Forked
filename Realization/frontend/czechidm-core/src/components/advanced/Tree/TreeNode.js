import React from 'react';
import { useSelector } from 'react-redux';
import { isFunction } from 'swiss-knife-utils';
import {
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
  onClick,
  onDoubleClick,
  onExpand,
  onCollapse,
  expanded: expandedProp,
  selected: selectedProp
}) {
  const loading = useIsUiStateLoading(uiKey, id);
  const childNodes = useUiStateItems(uiKey, id);
  const label = useSelector(state => getTreeNodeLabel(state, id));
  const childrenCount = useSelector(state => getTreeNodeChildrenCount(state, id));
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
    >
      {childNodes?.map(childNodeId => (
        <TreeNode
          key={childNodeId}
          id={childNodeId}
          uiKey={uiKey}
          onClick={onClick}
          onDoubleClick={onDoubleClick}
          onExpand={onExpand}
          onCollapse={onCollapse}
        />
      ))}
    </TreeItem>
  );
}

TreeNode.defaultProps = {
  level: 0
};
