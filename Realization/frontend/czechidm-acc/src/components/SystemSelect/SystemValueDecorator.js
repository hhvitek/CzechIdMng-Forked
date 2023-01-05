import {Basic, Utils} from 'czechidm-core';
/**
 * Identity select value decorator.
 *
 * @author Evgeniya Gorina
 * @since 13.0.0
 */
export default class SystemValueDecorator extends Basic.SelectBox.ValueDecorator {

  /**
   * Returns entity icon (null by default - icon will not be rendered)
   *
   * @param  {object} entity
   */
   getEntityIcon(entity) {
    if (!entity) {
      return 'component:system';
    }
    if (Utils.Entity.isDisabled(entity) || (entity._disabled && entity._disabled === true)) {
      return 'component:disabled-system';
    }
    return 'component:enabled-system';
  }

}
