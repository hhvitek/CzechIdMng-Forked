import { shallowEqual, useSelector } from 'react-redux';

export function useShallowEqualSelector(selector) {
  return useSelector(selector, shallowEqual);
}
