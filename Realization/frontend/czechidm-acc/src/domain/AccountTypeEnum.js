import { Enums } from 'czechidm-core';
import IdentityAccountTypeEnum from "./IdentityAccountTypeEnum";

/**
 * OperationType for adit operation etc.
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
