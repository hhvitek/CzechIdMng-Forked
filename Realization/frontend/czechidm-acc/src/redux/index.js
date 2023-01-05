import SystemManager from './SystemManager';
import RoleSystemManager from './RoleSystemManager';
import RoleSystemAttributeManager from './RoleSystemAttributeManager';
import SystemEntityManager from './SystemEntityManager';
import AccountManager from './AccountManager';
import IdentityAccountManager from './IdentityAccountManager';
import SchemaObjectClassManager from './SchemaObjectClassManager';
import SchemaAttributeManager from './SchemaAttributeManager';
import SystemAttributeMappingManager from './SystemAttributeMappingManager';
import SystemMappingManager from './SystemMappingManager';
import SynchronizationLogManager from './SynchronizationLogManager';
import SynchronizationConfigManager from './SynchronizationConfigManager';
import ProvisioningOperationManager from './ProvisioningOperationManager';
import ProvisioningArchiveManager from './ProvisioningArchiveManager';
import SyncActionLogManager from './SyncActionLogManager';
import SyncItemLogManager from './SyncItemLogManager';
import RoleAccountManager from './RoleAccountManager';
import TreeAccountManager from './TreeAccountManager';
import RoleCatalogueAccountManager from './RoleCatalogueAccountManager';
import ProvisioningBreakConfigManager from './ProvisioningBreakConfigManager';
import ProvisioningBreakRecipientManager from './ProvisioningBreakRecipientManager';
import ContractAccountManager from './ContractAccountManager';
import ContractSliceAccountManager from './ContractSliceAccountManager';
import AttributeControlledValueManager from './AttributeControlledValueManager';
import UniformPasswordManager from './UniformPasswordManager';
import UniformPasswordSystemManager from './UniformPasswordSystemManager';
import RemoteServerManager from './RemoteServerManager';
import SystemGroupManager from './SystemGroupManager';
import SystemGroupSystemManager from './SystemGroupSystemManager';
import SystemOwnerRoleManager from './SystemOwnerRoleManager';
import SystemOwnerManager from './SystemOwnerManager';
import SystemEntityTypeManager from './SystemEntityTypeManager';
import RequestAccountRoleManager from "./RequestAccountRoleManager";

const ManagerRoot = {
  SystemManager,
  RoleSystemManager,
  SystemEntityManager,
  AccountManager,
  IdentityAccountManager,
  SchemaObjectClassManager,
  SchemaAttributeManager,
  SystemAttributeMappingManager,
  SystemMappingManager,
  RoleSystemAttributeManager,
  SynchronizationLogManager,
  SynchronizationConfigManager,
  ProvisioningOperationManager,
  ProvisioningArchiveManager,
  SyncActionLogManager,
  SyncItemLogManager,
  RoleAccountManager,
  TreeAccountManager,
  RoleCatalogueAccountManager,
  ProvisioningBreakConfigManager,
  ProvisioningBreakRecipientManager,
  ContractAccountManager,
  ContractSliceAccountManager,
  AttributeControlledValueManager,
  UniformPasswordManager,
  UniformPasswordSystemManager,
  RemoteServerManager,
  SystemGroupManager,
  RequestAccountRoleManager,
  SystemGroupSystemManager,
  SystemOwnerRoleManager,
  SystemOwnerManager,
  SystemEntityTypeManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;