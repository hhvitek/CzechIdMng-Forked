import React, { useContext, useMemo } from 'react';

const TreeItemGroupContextDefaultValue = {
  parentId: null,
  level: 0
};

export const TreeItemGroupContext = React.createContext(TreeItemGroupContextDefaultValue);

export function useTreeItemGroupContext() {
  return useContext(TreeItemGroupContext);
}

export function TreeItemGroupContextProvider({children, parentId}) {
  const treeItemGroupContext = useTreeItemGroupContext();
  const level = treeItemGroupContext?.level || 0;

  const ctxValue = useMemo(() => ({
    level: level + 1,
    parentId
  }), [parentId, level]);

  return (
    <TreeItemGroupContext.Provider value={ctxValue}>
      {children}
    </TreeItemGroupContext.Provider>
  );
}
