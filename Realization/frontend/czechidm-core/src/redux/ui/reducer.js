import merge from 'object-assign';
import Immutable from 'immutable';
import { UI_ACTIONS } from './actions';

export const TREE_NODE_ENTITY_TYPE = 'TreeNode';

export function uiReducer(state = {}, action) {
  const uiKey = action.uiKey;

  if (!uiKey) {
    return state;
  }

  const uiState = state[uiKey];
  const payload = action.payload;
  let stateUpdate;

  switch (action.type) {
    case UI_ACTIONS.EXPAND_TREE_NODE: {
      const {nodeId} = payload;
      let expandedTreeNodes = uiState?.expandedTreeNodes || new Immutable.Set();
      expandedTreeNodes = expandedTreeNodes.add(nodeId);

      stateUpdate = {
        expandedTreeNodes
      };

      break;
    }
    case UI_ACTIONS.COLLAPSE_TREE_NODE: {
      const {nodeId} = payload;
      let expandedTreeNodes = uiState?.expandedTreeNodes;

      if (!expandedTreeNodes?.has(nodeId)) {
        break;
      }

      expandedTreeNodes = expandedTreeNodes.remove(nodeId);

      stateUpdate = {
        expandedTreeNodes
      };

      break;
    }
    case UI_ACTIONS.SELECT_TREE_NODE: {
      const {nodeId, opts} = payload;

      const shouldAdd = opts.add;
      const shouldToggle = opts.toggle;

      let selectedTreeNodes = uiState?.selectedTreeNodes || new Immutable.Set();

      if (selectedTreeNodes.has(nodeId)) {
        if (shouldToggle) {
          selectedTreeNodes = selectedTreeNodes.delete(nodeId);
        }

        stateUpdate = {
          selectedTreeNodes
        };

        break;
      }

      if (!shouldAdd) {
        selectedTreeNodes = selectedTreeNodes.clear();
      }

      selectedTreeNodes = selectedTreeNodes.add(nodeId);

      stateUpdate = {
        selectedTreeNodes
      };

      break;
    }
    case UI_ACTIONS.UNSELECT_TREE_NODE: {
      const {nodeId} = payload;

      let selectedTreeNodes = uiState?.selectedTreeNodes;

      if (!selectedTreeNodes?.has(nodeId)) {
        break;
      }

      selectedTreeNodes = selectedTreeNodes.remove(nodeId);

      stateUpdate = {
        selectedTreeNodes
      };

      break;
    }
    default:
      return state;
  }

  if (!stateUpdate) {
    return state;
  }

  return merge({}, state, {
    [uiKey]: merge({}, uiState, stateUpdate)
  });
}

export default uiReducer;
