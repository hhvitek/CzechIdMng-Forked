import EntityUtils from '../../utils/EntityUtils';

export const getExpandedTreeNodes = (state, uiKey) => state?.ui[uiKey]?.expandedTreeNodes;
export const isTreeNodeExpanded = (state, uiKey, nodeId) => state?.ui[uiKey]?.expandedTreeNodes?.has(nodeId);
export const getSelectedTreeNodes = (state, uiKey) => state?.ui[uiKey]?.selectedTreeNodes;
export const isTreeNodeSelected = (state, uiKey, nodeId) => state?.ui[uiKey]?.selectedTreeNodes?.has(nodeId);

export const getTreeNodeLabel = (state, id, type) => {
  const nodeEntity = EntityUtils.getEntity(state, type, id, null);

  if (!nodeEntity) {
    return '';
  }

  if (nodeEntity.name === nodeEntity.code) {
    return nodeEntity.name;
  }
  return `${nodeEntity.name} (${nodeEntity.code})`;
};

export const getTreeNodeChildrenCount = (state, id, type) => {
  const nodeEntity = EntityUtils.getEntity(state, type, id, null);
  return nodeEntity?.childrenCount;
};
