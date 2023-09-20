import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Immutable from 'immutable';
import classNames from 'classnames';
import _ from 'lodash';
//
import { withStyles } from '@material-ui/core/styles';
//
import * as Basic from '../../basic';
import * as Domain from '../../../domain';
import * as Utils from '../../../utils';
import DetailButton from '../Table/DetailButton';
import {
  collapseTreeNode,
  expandTreeNode,
  selectTreeNode
} from '../../../redux/ui/actions';
import {
  getExpandedTreeNodes, getSelectedTreeNodes,
  isTreeNodeExpanded
} from '../../../redux/ui/selectors';
import { TreeNode } from './TreeNode';
import { getUiState } from '../../../utils/UiUtils';

const BASE_ICON_WIDTH = 15; // TODO: how to get dynamic padding from css?

const styles = theme => ({
  treeNodeRow: {
    '&.selected': {
      backgroundColor: 'rgba(0, 0, 0, 0.08)',
      borderLeftColor: theme.palette.secondary.main,
      borderLeftWidth: 3,
      borderLeftStyle: 'solid',
      paddingLeft: 12,
    },
    '& .basic-button.embedded': {
      textAlign: 'left',
      padding: 0,
      minWidth: 'auto',
    },
  },
});

/**
 * Advanced tree component
 *
 * TODO: use redux state to prevent reload whole tree after active operations
 * TODO: search
 *
 * @author Radek Tomiška
 */
class Tree extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    //
    let nodes = new Immutable.Map();
    if (props.roots) {
      const nodeIds = [];
      props.roots.forEach(node => {
        let id = node;
        if (_.isObject(node)) {
          id = node.id;
          if (id && !this._getNode(id)) {
            this.context.store.dispatch(this.getManager().receiveEntity(id, node));
          }
        }
        if (id) {
          nodeIds.push(id);
        }
      });
      nodes = nodes.set(null, nodeIds);
    }
    //
    this.state = {
      activeNodeId: null, // selected node for traverse (last clicked node)
      selected: new Immutable.OrderedSet(props.selected), // contains ids
      nodes, // loaded node ids
      ui: new Immutable.Map(), // ui state (loading decorator, last search parameters, total)
      filterOpened: false,
    };
  }

  componentDidMount() {
    this._loadNodes(null);
  }

  UNSAFE_componentWillReceiveProps(newProps) {
    if (!Domain.SearchParameters.is(newProps.forceSearchParameters, this.props.forceSearchParameters)
      || this.props.rendered !== newProps.rendered) {
      this.reload(newProps);
    } else {
      const newState = {};
      // roots changed
      if (this.props.roots !== newProps.roots) {
        const nodeIds = [];
        if (newProps.roots) {
          newProps.roots.forEach(node => {
            let id = node;
            if (_.isObject(node)) {
              id = node.id;
              if (id && !this._getNode(id)) {
                this.context.store.dispatch(this.getManager().receiveEntity(id, node));
              }
            }
            if (id) {
              nodeIds.push(id);
            }
          });
        }
        newState.nodes = this.state.nodes.set(null, nodeIds);
      }
      // selected changed
      if (this.props.selected !== newProps.selected) {
        newState.selected = new Immutable.OrderedSet(newProps.selected);
      }
      //
      this.setState(newState);
    }
  }

  getComponentKey() {
    return 'component.advanced.Tree';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  /**
   * Manager for entity in tree
   */
  getManager() {
    return this.props.manager;
  }

  /**
   * Reload tree
   */
  reload(props = null) {
    const {onChange, onSelect, roots, selected} = props || this.props;
    //
    let nodes = new Immutable.Map();
    if (roots) {
      nodes = nodes.set(null, roots
        .map(node => {
          let id = node;
          if (_.isObject(node)) {
            id = node.id;
          }
          return id;
        })
        .filter(node => node !== null));
    }
    //
    this.setState({
      activeNodeId: null,
      selected: new Immutable.OrderedSet(selected), // contains ids
      nodes,
      ui: new Immutable.Map(),
    }, () => {
      if (!roots) {
        this._loadNodes();
      }
      if (onChange) {
        onChange(null);
      }
      // @deprecated
      if (onSelect) {
        onSelect(null);
      }
    });
  }

  onNextPage(nodeId, event) {
    if (event) {
      event.preventDefault();
    }
    //
    this._loadNodes(nodeId);
  }

  onSelect(nodeId, event) {
    if (event) {
      event.preventDefault();
    }
    const {onChange, onSelect, traverse, multiSelect, clearable} = this.props;
    const {nodes} = this.state;
    const {shiftKey} = event;
    let {selected} = this.state;
    //
    if (!multiSelect || !shiftKey) {
      if (!selected.has(nodeId)) {
        if (!multiSelect) {
          selected = selected.clear();
        }
        selected = selected.add(nodeId);
      } else if (clearable) {
        selected = selected.delete(nodeId);
      }
    } else { // shift and multiselect
      selected = selected.add(nodeId);
      // add first selected till last selected child node in the same parent
      const node = this._getNode(nodeId); // root can be given
      const brothers = nodes.has(node.parent) ? nodes.get(node.parent) : [];
      let min = null;
      let max = null;
      let selectedIndex = null;
      for (let index = 0; index < brothers.length; index++) {
        if (selected.has(brothers[index])) {
          if (min === null) {
            min = index;
          }
          if (max == null || max <= index) {
            max = index;
          }
        }
        if (brothers[index] === nodeId) {
          selectedIndex = index;
        }
      }
      if (selectedIndex < max) {
        max = selectedIndex;
      }
      if (min !== null && max !== null) {
        selected = selected.clear();
        for (let index = min; index <= max; index++) {
          selected = selected.add(brothers[index]);
        }
      }
    }
    let result = true;
    if (onChange) {
      if (multiSelect) {
        result = onChange(selected.toArray());
      } else {
        result = onChange(selected.size > 0 ? selected.first() : null);
      }
    }
    // @deprecated - remove after v.10.x release
    if (result !== false && onSelect) {
      result = onSelect(nodeId);
    }
    // if onChange or onSelect listener returns false, then we can end
    // TODO: what about traverse? Go down without selection?
    if (result === false) {
      return;
    }
    //
    this.setState({
      activeNodeId: nodeId,
      selected,
    }, () => {
      const node = this._getNode(nodeId); // root can be given
      if (traverse && (nodeId == null || node.childrenCount > 0) && !nodes.has(nodeId)) {
        // reload
        this._loadNodes(nodeId);
      }
    });
  }

  onDoubleClick(nodeId, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const {onDoubleClick} = this.props;
    if (onDoubleClick) {
      onDoubleClick(nodeId);
    }
  }

  onDetail(nodeId, event) {
    if (event) {
      event.preventDefault();
    }
    const {onDetail} = this.props;
    if (!onDetail) {
      return;
    }
    //
    onDetail(nodeId);
  }

  showFilter(event) {
    if (event) {
      event.preventDefault();
    }
    //
    this.setState({
      filterOpened: true,
    });
  }

  hideFilter(event) {
    if (event) {
      event.preventDefault();
    }
    //
    this.setState({
      filterOpened: false,
    });
  }

  toogleFilter(filterOpened, event) {
    if (event) {
      event.preventDefault();
    }
    //
    this.setState({
      filterOpened: !filterOpened,
    });
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    //
    this._loadNodes(null, null, this.refs.filter.getValue());
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    //
    this.refs.filter.setValue(null);
  }

  getValue() {
    const {selected} = this.state;
    return selected.toArray();
  }

  _loadNodes(nodeId = null, props = null, filter = null) {
    const _props = props || this.props;
    const {
      roots,
      forceSearchParameters,
      paginationRootSize,
      paginationNodeSize,
    } = _props;
    if (!_props.rendered) {
      // component is not rendered ... loading is not needed
      return;
    }
    //
    if (nodeId === null && roots) {
      // roots are loaded already
      return;
    }
    //
    let searchParameters;
    let uiState = {};
    if (this.state.ui.has(nodeId)) {
      uiState = this.state.ui.get(nodeId);
    }

    searchParameters = null;
    if (uiState.searchParameters) {
      // next page
      searchParameters = uiState.searchParameters.setPage(uiState.searchParameters.getPage() + 1);
    } else if (nodeId === null) {
      // load roots
      searchParameters = this.getManager().getService().getRootSearchParameters();
      if (paginationRootSize) { // only if given, default will be used from underlying service by default otherwise
        searchParameters = searchParameters.setSize(paginationRootSize);
      }
    } else {
      searchParameters = this.getManager().getService().getTreeSearchParameters().setFilter('parent', nodeId);
      if (paginationNodeSize) { // only if given, default will be used from underlying service by default otherwise
        searchParameters = searchParameters.setSize(paginationNodeSize);
      }
    }
    if (filter) {
      searchParameters = searchParameters.setFilter('text', filter);
    }
    //
    const {ui} = this.state;
    this.setState({
      ui: ui.set(nodeId, {
        ...uiState,
        searchParameters,
        showLoading: true,
      }),
    }, () => {
      let _forceSearchParameters = null;
      if (forceSearchParameters) {
        _forceSearchParameters = forceSearchParameters.setSize(null).setPage(null); // we dont want override setted pagination
      }
      searchParameters = this.getManager().mergeSearchParameters(searchParameters, _forceSearchParameters);
      this.context.store.dispatch(
        this.getManager().fetchEntities(
          searchParameters,
          nodeId === null ? this.getUiKey() : `${this.getUiKey()}-${nodeId}`,
          (json, error) => {
            let {nodes, ui} = this.state;
            if (!error) {
              let data = json._embedded[this.getManager().getCollectionType()] || [];
              data = data.map(node => node.id); // only ids are stored in state; TODO: move state to redux store (e.g. Data)
              const currentChildrenCount = json.page ? json.page.totalElements : data.length; // actual real children count
              //
              // check and fill parents children count, if its not defined
              if (nodeId) { // null => roots
                const parentNode = this._getNode(nodeId);
                if (parentNode && (!parentNode.childrenCount || parentNode.childrenCount !== currentChildrenCount)) {
                  parentNode.childrenCount = currentChildrenCount;
                }
              }
              //
              if (nodes.has(nodeId) && searchParameters.getPage() > 0) {
                // push at end
                nodes = nodes.set(nodeId, nodes.get(nodeId).concat(data));
              } else if (currentChildrenCount > 0 || nodeId === null) { // we need to know roots is null
                nodes = nodes.set(nodeId, data); // parentId -> children
              }
              //
              // set the ui state for the next page loading
              ui = ui.set(nodeId, {
                searchParameters,
                total: currentChildrenCount,
                showLoading: false,
              });
            } else {
              this.addErrorMessage({
                level: 'error',
                key: 'error-tree-load',
              }, error);
            }
            this.setState({
              nodes,
              ui,
            });
          },
        ),
      );
    });
  }

  /**
   * Get node from redux store
   *
   * @param  {string} nodeId
   * @return {object}
   */
  _getNode(nodeId) {
    return this.getManager().getEntity(this.context.store.getState(), nodeId);
  }

  getNodeSearchParams(nodeId) {
    const state = this.context.store.getState();
    const uiKey = this.getUiKey();
    const expanded = isTreeNodeExpanded(state, uiKey, nodeId);
    const uiState = getUiState(state, uiKey, nodeId);

    const {
      paginationRootSize,
      paginationNodeSize,
      forceSearchParameters
    } = this.props;
    let searchParameters = null;
    const filter = null;

    if (uiState?.searchParameters && expanded) {
      // next page
      searchParameters = uiState.searchParameters.setPage(uiState.searchParameters.getPage() + 1);
    } else if (!nodeId) {
      // load roots
      searchParameters = this.getManager().getService().getRootSearchParameters();
      if (paginationRootSize) { // only if given, default will be used from underlying service by default otherwise
        searchParameters = searchParameters.setSize(paginationRootSize);
      }
    } else {
      searchParameters = this.getManager().getService().getTreeSearchParameters().setFilter('parent', nodeId);
      if (paginationNodeSize) { // only if given, default will be used from underlying service by default otherwise
        searchParameters = searchParameters.setSize(paginationNodeSize);
      }
    }

    if (filter) {
      searchParameters = searchParameters.setFilter('text', filter);
    }

    let _forceSearchParameters = null;
    if (forceSearchParameters) {
      _forceSearchParameters = forceSearchParameters.setSize(null).setPage(null); // we dont want override setted pagination
    }

    searchParameters = this.getManager().mergeSearchParameters(searchParameters, _forceSearchParameters);

    return searchParameters;
  }

  loadNode(nodeId) {
    const searchParameters = this.getNodeSearchParams(nodeId);
    const uiKey = this.getUiKey();
    const manager = this.getManager();

    return manager.fetchEntities(
      searchParameters,
      `${uiKey}${nodeId ? `-${nodeId}` : ''}`
    );
  }

  handleNodeExpand = (nodeId) => {
    const uiKey = this.getUiKey();
    this.context.store.dispatch(dispatch => {
      dispatch(this.loadNode(nodeId));
      dispatch(expandTreeNode(uiKey, nodeId));
    });
  }

  handleNodeCollapse = (nodeId) => {
    const uiKey = this.getUiKey();
    this.context.store.dispatch(collapseTreeNode(uiKey, nodeId));
  }

  handleNodeClick = (nodeId, e) => {
    const {
      onChange,
      onSelect,
      multiSelect,
      clearable,
      selectedTreeNodes
    } = this.props;
    const uiKey = this.getUiKey();

    const {shiftKey} = e;

    this.context.store.dispatch(selectTreeNode(uiKey, nodeId, {
      add: multiSelect && shiftKey,
      toggle: clearable
    }));

    if (onSelect) {
      onSelect(nodeId);
    }

    if (onChange) {
      const changeId = selectedTreeNodes?.has(nodeId) && clearable ? null : nodeId;
      onChange(changeId, e);
    }
  }

  _renderHeader() {
    const {header, multiSelect} = this.props;
    const {selected, activeNodeId} = this.state;
    //
    if (header === null) { // => not rendered
      return header;
    }
    //
    // TODO: render count, when multiSelect?
    if (!multiSelect && selected.size > 0) {
      const selectedNode = activeNodeId ? this._getNode(activeNodeId) : this._getNode(selected.first());
      const parents = [];
      let _node = selectedNode;
      while (_node !== null && _node.parent !== null) {
        _node = this._getNode(_node.parent);
        if (!_node) {
          // just for sure - redux store doesn't contain parent node
          break;
        }
        parents.push(_node);
      }
      //
      return (
        <ol
          className="breadcrumb"
          style={{
            padding: '0px 2px',
            marginBottom: 0,
            marginRight: 3,
            backgroundColor: 'transparent',
          }}>
          <li>
            <Basic.Button
              level="link"
              className="embedded"
              onClick={this.onSelect.bind(this, null)}
              title={this.i18n('root.link.title')}
              titlePlacement="bottom">
              {this.i18n('root.link.label')}
            </Basic.Button>
          </li>
          {
            !parents.length > 0
            ||
            <li>
              <Basic.Button
                level="link"
                className="embedded"
                onClick={this.onSelect.bind(this, parents[0].id)}>
                <Basic.ShortText
                  text={this.getManager().getNiceLabel(parents[0])}/>
              </Basic.Button>
            </li>
          }
          <li>
            <Basic.ShortText
              text={this.getManager().getNiceLabel(selectedNode)}/>
          </li>
        </ol>
      );
    }
    //
    if (header) {
      return header;
    }
    return this.i18n('header');
  }

  /**
   * True, when all nodes don't have a children.
   *
   * @param  {arrayOf(nodeIds)}  nodeIds
   * @return {Boolean}
   */
  isLeafs(nodeIds) {
    if (!nodeIds || nodeIds.length === 0) {
      return true;
    }
    const nodeWithChildren = nodeIds.find(nodeId => {
      const node = this._getNode(nodeId);
      if (!node) {
        return false;
      }
      return node.childrenCount > 0;
    });
    return nodeWithChildren === null || nodeWithChildren === undefined;
  }

  /**
   * Returns true, when node is disabled.
   *
   * @param  {IdmTreeNodeDto}  node
   * @return {Boolean}
   */
  _isDisabled(node) {
    const {roots, disableable} = this.props;
    //
    if (disableable === false) {
      return false;
    }
    //
    if (Utils.Entity.isDisabled(node)) {
      return true;
    }
    // try to find in roots
    let result = false;
    if (roots) {
      roots.forEach(rootNode => {
        if (_.isObject(rootNode) && rootNode.id === node.id) {
          // given root is disabled
          result = Utils.Entity.isDisabled(rootNode);
        }
      });
    }
    return result;
  }

  render() {
    const {
      rendered,
      header,
      showLoading,
      traverse,
      onDetail,
      className,
      style,
      bodyStyle,
      bodyClassName,
      showRefreshButton,
    } = this.props;
    const {
      nodes,
      ui,
      activeNodeId,
      filterOpened,
    } = this.state;
    //
    const selectedNode = this._getNode(activeNodeId);
    let root = null; // root
    let parent = null; // root
    let uiState = {};
    if (traverse && selectedNode) {
      if (selectedNode.childrenCount > 0) {
        root = selectedNode.id;
        parent = selectedNode.parent;
      } else {
        root = selectedNode.parent; // parent or null as root
        const parentNode = this._getNode(selectedNode.parent);
        if (parentNode) {
          parent = parentNode.parent;
        } else {
          parent = selectedNode.parent;
        }
      }
      if (ui.has(activeNodeId)) {
        uiState = ui.get(activeNodeId);
      }
    }
    //
    if (!rendered) {
      return null;
    }
    const _showLoading = !nodes.has(null) || showLoading;
    //
    return (
      <div className={classNames('advanced-tree', className)} style={style}>
        {
          header === null
          ||
          <>
            <div className="basic-toolbar tree-header">
              <div className="tree-header-text">
                {this._renderHeader()}
              </div>
              <div className="tree-header-buttons">
                <DetailButton
                  rendered={!!(selectedNode !== null && onDetail)}
                  onClick={this.onDetail.bind(this, activeNodeId)}
                  title={this.i18n('detail.link.title')}/>

                {/* RT: search prepare  */}
                <Basic.Button
                  className="hidden"
                  buttonSize="xs"
                  showLoading={_showLoading}
                  icon="filter"
                  style={{marginLeft: 3}}
                  onClick={this.toogleFilter.bind(this, filterOpened)}>
                  <Basic.Icon
                    icon={!filterOpened ? 'triangle-bottom' : 'triangle-top'}
                    style={{fontSize: '0.85em'}}/>
                </Basic.Button>

                <Basic.Button
                  title={this.i18n('reload')}
                  titlePlacement="bottom"
                  buttonSize="xs"
                  onClick={this.reload.bind(this, null)}
                  showLoading={_showLoading}
                  rendered={showRefreshButton}
                  icon="fa:refresh"
                  style={{marginLeft: 3}}/>
              </div>
            </div>
            {/* RT: search prepare */}
            <Basic.Collapse in={filterOpened}>
              <div>
                <form
                  className="basic-toolbar"
                  style={{display: 'flex', marginBottom: 0}}
                  onSubmit={this.useFilter.bind(this)}>
                  <div style={{flex: 1}}>
                    <Basic.TextField
                      ref="filter"
                      label={null}
                      placeholder={this.i18n('component.basic.SelectBox.searchingText')}
                      className="small"
                      style={{marginBottom: 0}}/>
                  </div>
                  <div className="text-right">
                    <Basic.Button
                      type="submit"
                      className="btn-xs"
                      showLoading={_showLoading}
                      icon="fa:check"
                      style={{marginLeft: 3}}/>
                    <Basic.Button
                      className="btn-xs"
                      showLoading={_showLoading}
                      icon="remove"
                      style={{marginLeft: 3}}
                      onClick={this.cancelFilter.bind(this)}/>
                  </div>
                </form>
              </div>
            </Basic.Collapse>
          </>
        }
        <div
          className={classNames('tree-body', bodyClassName)}
          style={bodyStyle}>
          {
            _showLoading
              ?
              <Basic.Loading isStatic show/>
              :
              <div>
                {
                  !traverse || !root
                  ||
                  <Basic.Button
                    level="link"
                    className="embedded parent-link"
                    onClick={this.onSelect.bind(this, parent)}
                    showLoading={uiState && uiState.showLoading}
                    title={this.i18n('parent.link.title')}>
                    <Basic.Icon
                      value="fa:level-down"
                      className="fa-rotate-180 parent-icon"/>
                    [{this.i18n('parent.link.label')}]
                    <Basic.Icon
                      value="refresh"
                      showLoading
                      rendered={uiState && uiState.showLoading === true}
                      style={{marginLeft: 5}}/>
                  </Basic.Button>
                }
                <Basic.Tree>
                  {this.props.roots?.length > 0 ? this.props.roots.map(entity => (
                    <TreeNode
                      key={entity.id}
                      id={entity.id}
                      uiKey={this.getUiKey()}
                      entityType={this.getManager().getEntityType()}
                      onClick={this.handleNodeClick}
                      onExpand={this.handleNodeExpand}
                      onCollapse={this.handleNodeCollapse}
                      onDoubleClick={this.onDoubleClick}
                      renderNode={this.props.renderNode}
                      nodeIcon={this.props.nodeIcon}/>)) : (
                    <TreeNode
                      uiKey={this.getUiKey()}
                      entityType={this.getManager().getEntityType()}
                      onClick={this.handleNodeClick}
                      onExpand={this.handleNodeExpand}
                      onCollapse={this.handleNodeCollapse}
                      onDoubleClick={this.onDoubleClick}
                      renderNode={this.props.renderNode}
                      nodeIcon={this.props.nodeIcon}
                      expanded/>
                  )}
                </Basic.Tree>
              </div>
          }
        </div>
      </div>
    );
  }
}

Tree.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Key prefix in redux (loading / store data).
   */
  uiKey: PropTypes.string.isRequired,
  /**
   * EntityManager for fetching entities in tree. Manager's underlying service should support methods:
   * - getRootSearchParameters() - returns search parameters to find roots
   * - getTreeSearchParameters() - returns search parameters to find children with 'parent' paremeter filter.
   */
  manager: PropTypes.object.isRequired,
  /**
   * "Hard roots" - roots can be loaded from outside and given as parameter, then root will not be loaded by method getRootSearchParameters().
   * Roots can be given as array of ids only - entities has to be loaded in redux store!
   * Search is disabled, if roots are given.
   */
  roots: PropTypes.arrayOf(PropTypes.oneOfType(
    PropTypes.string,
    PropTypes.object,
  )),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Node icon - single icon for all nodes (string) or callback - named parameters "node" and "opened" will be given.
   * { null } can be given - disable default icons.
   */
  nodeIcon: PropTypes.oneOfType(
    PropTypes.func,
  ),
  /**
   * Node icon class name - string or callback - named parameters "node" and "opened" will be given.
   * { null } can be given - disable default icon class names.
   */
  nodeIconClassName: PropTypes.oneOfType(
    PropTypes.string,
    PropTypes.func,
  ),
  /**
   * Node style - single style for all nodes (string) or callback - named parameters "node" and "opened" will be given.
   */
  nodeStyle: PropTypes.oneOfType(
    PropTypes.string,
    PropTypes.func,
  ),
  /**
   * Override whole node content - all listeners will be disabled (onSelect ...), just node icon remains.
   * Can be used, if tree is used just as decorator without selected value holder.
   */
  renderNode: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.element,
  ]),
  /**
   * Node label. Manager's nice label is used by default.
   */
  nodeNiceLabel: PropTypes.func,
  /**
   * Single (false) or multi selection.
   */
  multiSelect: PropTypes.bool,
  /**
   * onChange callback. Selected node (or array of nodes, if multiSelect is true) is given as parameter.
   */
  onChange: PropTypes.func,
  /**
   * On double click node callback. Selected node is given as parameter
   */
  onDoubleClick: PropTypes.func,
  /**
   * Show detail function.
   */
  onDetail: PropTypes.func,
  /**
   * Traverse to selected folder
   */
  traverse: PropTypes.bool,
  /**
   * Tree header. If ``null`` is given, then header is not rendered.
   */
  header: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.element,
  ]),
  /**
   * If tree roots are empty, then this text will be shown
   */
  noData: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * Tree css
   */
  className: PropTypes.string,
  /**
   * Tree styles
   */
  style: PropTypes.object,
  /**
   * Tree body css
   */
  bodyClassName: PropTypes.string,
  /**
   * Tree body styles
   */
  bodyStyle: PropTypes.object,
  /**
   * Selected options can be cleared
   */
  clearable: PropTypes.bool,
  /**
   * Show refresh button
   */
  showRefreshButton: PropTypes.bool,
  /**
   * If disabled option can be selected.
   */
  disableable: PropTypes.bool,
  /**
   * Pagination - number of items for roots.
   *
   * @since 10.7.0
   */
  paginationRootSize: PropTypes.number,
  /**
   * Pagination - number of items for nodes (children).
   *
   * @since 10.7.0
   */
  paginationNodeSize: PropTypes.number,
  /**
   * Show description in node title on hover.
   *
   * @since 10.7.0
   */
  showNodeDescription: PropTypes.bool,
};

Tree.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  traverse: false,
  multiSelect: false,
  clearable: true,
  showRefreshButton: true,
  disableable: true,
  showNodeDescription: true,
};

const mapStateToProps = (state, component) => {
  const manager = component.manager;
  const uiKey = component.uiKey;

  return {
    _showLoading: manager.isShowLoading(state, uiKey),
    expandedNodes: getExpandedTreeNodes(state, uiKey),
    selectedTreeNodes: getSelectedTreeNodes(state, uiKey)
  };
};

const mapDispatchToProps = {
  onExpandNode: expandTreeNode,
  onCollapseNode: collapseTreeNode,
};

export default connect(mapStateToProps, mapDispatchToProps, null, {forwardRef: true})(withStyles(styles, {withTheme: true})(Tree));
