import { Enums } from 'czechidm-core';
import IdentityAccountTypeEnum from "./IdentityAccountTypeEnum";

/**
 * Enum containing all supported account types. It extends IdentityAccountTypeEnum with support for technical accounts.
 */
export default class AccountTypeEnum extends IdentityAccountTypeEnum {

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.TECHNICAL: {
        return 'primary';
      }
      default: {
        return super.getLevel(key);
      }
    }
  }
}

AccountTypeEnum.TECHNICAL = Symbol('TECHNICAL');
