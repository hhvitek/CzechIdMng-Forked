import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import { TreeItemGroupContextProvider } from '../TreeItem/TreeItemGroupContext';

const useStyles = makeStyles({
  root: {
    margin: 0,
    padding: 0,
    listStyle: 'none',
    outline: 'none'
  }
});

export function Tree({children}) {
  const classes = useStyles();

  return (
    <ul role="tree" className={classes.root}>
      <TreeItemGroupContextProvider>
        {children}
      </TreeItemGroupContextProvider>
    </ul>
  );
}

export default Tree;
