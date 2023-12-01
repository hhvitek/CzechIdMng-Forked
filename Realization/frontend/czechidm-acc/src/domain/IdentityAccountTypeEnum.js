import { Enums } from 'czechidm-core';

/**
 * OperationType for adit operation etc.
 */
export default class IdentityAccountTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.AccountTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.PERSONAL: {
        return 'success';
      }
      case this.PERSONAL_OTHER: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }
}

IdentityAccountTypeEnum.PERSONAL = Symbol('PERSONAL');
IdentityAccountTypeEnum.PERSONAL_OTHER = Symbol('PERSONAL_OTHER');
