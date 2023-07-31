import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { makeStyles } from '@material-ui/core/styles';
import {
  faSquareMinus,
  faSquarePlus
} from '@fortawesome/free-regular-svg-icons';
import Collapse from '@material-ui/core/Collapse';
import Button from '../Button/Button';
import TreeItemIcon from './TreeItemIcon';
import {
  TreeItemGroupContextProvider,
  useTreeItemGroupContext
} from './TreeItemGroupContext';

const useStyles = makeStyles(theme => ({
  root: {
    margin: 0,
    padding: 0,
    listStyle: 'none',
    outline: 'none'
  },
  content: props => {
    let basePadding = props.level * 16;

    if (!props.expandable && !React.isValidElement(props.children)) {
      basePadding += 16;
    }

    const base = {
      display: 'flex',
      alignItems: 'center',
      paddingLeft: `${basePadding}px`,
      whiteSpace: 'nowrap'
    };

    if (props.selected) {
      return {
        ...base,
        paddingLeft: `${basePadding - 3}px`,
        backgroundColor: 'rgba(0, 0, 0, 0.08)',
        borderLeftColor: theme.palette.secondary.main,
        borderLeftWidth: 3,
        borderLeftStyle: 'solid'
      };
    }

    return base;
  },
  expandIcon: {
    marginRight: '5px',
    color: '#7898b5',
    cursor: 'pointer'
  },
  group: {
    margin: 0,
    padding: 0
  }
}));

export function TreeItem(props) {
  const {
    nodeId,
    children,
    className,
    classNameContent,
    classNameLabel,
    label,
    loading,
    expanded,
    selected,
    expandable,
    onExpand,
    onCollapse,
    onClick,
    onDoubleClick,
    childrenCount,
    ...restProps
  } = props;

  const treeItemGroupCtx = useTreeItemGroupContext();
  const classes = useStyles({...props, level: treeItemGroupCtx?.level || 0});
  const canExpand = (children && React.isValidElement(children)) || expandable;
  const showExpanded = expanded && !loading;

  if (!label) {
    return <>{children}</>;
  }

  return (
    <li
      role="treeitem"
      className={classes.root}
      {...restProps}>
      <div className={classes.content}>
        {canExpand && (
          <FontAwesomeIcon
            icon={expanded ? faSquareMinus : faSquarePlus}
            onClick={expanded ? onCollapse : onExpand}
            className={classes.expandIcon}
          />
        )}
        <Button
          level="link"
          className="embedded"
          onClick={onClick}
          onDoubleClick={onDoubleClick}>
          <TreeItemIcon
            expandable={canExpand}
            expanded={expanded}
            loading={loading}/>
          {label}
        </Button>
        {childrenCount > 0 &&
          <small className="tree-item-children-count">({childrenCount})</small>}
      </div>

      {canExpand && (
        <TreeItemGroupContextProvider parentId={nodeId}>
          <Collapse
            component="ul"
            role="group"
            in={showExpanded}
            className={classes.group}>
            {children}
          </Collapse>
        </TreeItemGroupContextProvider>
      )}
    </li>
  );
}

TreeItem.defaultProps = {};

export default TreeItem;
