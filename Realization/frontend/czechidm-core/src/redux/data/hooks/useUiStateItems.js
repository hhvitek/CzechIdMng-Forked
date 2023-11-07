import { useShallowEqualSelector } from '../../hooks';
import { getUiItems } from '../../../utils/UiUtils';

export function useUiStateItems(uiKey, id = null) {
  return useShallowEqualSelector(state => getUiItems(state, uiKey, id));
}
