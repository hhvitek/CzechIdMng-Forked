export const UI_ACTIONS = {
  EXPAND_TREE_NODE: 'UI_EXPAND_TREE_NODE',
  COLLAPSE_TREE_NODE: 'UI_COLLAPSE_TREE_NODE',
  SELECT_TREE_NODE: 'UI_SELECT_TREE_NODE',
  UNSELECT_TREE_NODE: 'UI_UNSELECT_TREE_NODE'
};

export const expandTreeNode = (uiKey, nodeId) => {
  return {
    type: UI_ACTIONS.EXPAND_TREE_NODE,
    uiKey,
    payload: {
      nodeId
    }
  };
};

export const collapseTreeNode = (uiKey, nodeId) => {
  return {
    type: UI_ACTIONS.COLLAPSE_TREE_NODE,
    uiKey,
    payload: {
      nodeId
    }
  };
};

export const selectTreeNode = (uiKey, nodeId, opts = {
  add: false,
  toggle: false
}) => {
  return {
    type: UI_ACTIONS.SELECT_TREE_NODE,
    uiKey,
    payload: {
      nodeId,
      opts
    }
  };
};

export const unselectTreeNode = (uiKey, nodeId) => {
  return {
    type: UI_ACTIONS.UNSELECT_TREE_NODE,
    uiKey,
    payload: {
      nodeId
    }
  };
};
