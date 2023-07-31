import React from 'react';
import classnames from 'classnames';
import Icon from '../Icon/Icon';

export function TreeItemIcon({
  children,
  className,
  expandable,
  expanded,
  loading,
  ...props
}) {
  const leaf = !expandable;
  const icon = leaf ? 'fa:file-o' : expanded ? 'fa:folder-open' : 'fa:folder';

  return (
    <Icon
      value={icon}
      showLoading={loading}
      className={classnames('tree-item-icon', className, {leaf, parent: !leaf})}
      {...props}>
      {children}
    </Icon>
  );
}

export default TreeItemIcon;
