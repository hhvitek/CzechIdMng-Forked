import { useSelector } from 'react-redux';
import { isUiLoading } from '../../../utils/UiUtils';

export function useIsUiStateLoading(uiKey, id = null) {
  return useSelector(state => isUiLoading(state, uiKey, id));
}
