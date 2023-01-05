import * as Managers from './src/redux';
import {SystemTable} from './src/content/system/SystemTable';
import AccountSelect from "./src/components/AccountSelect/AccountSelect";
import AccountTypeEnum from './src/domain/AccountTypeEnum';
import SystemOperationTypeEnum from './src/domain/SystemOperationTypeEnum';

const ModuleRoot = {
  Managers,
  SystemTable,
  AccountSelect,
  SystemTable,
  Enums: {
    AccountTypeEnum,
    SystemOperationTypeEnum
  }
};

ModuleRoot.version = '0.0.1';
module.exports = ModuleRoot;
