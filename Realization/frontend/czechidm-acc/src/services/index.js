import SystemService from './SystemService';
import RoleSystemService from './RoleSystemService';
import RoleSystemAttributeService from './RoleSystemAttributeService';
import SystemEntityService from './SystemEntityService';
import AccountService from './AccountService';
import IdentityAccountService from './IdentityAccountService';
import SchemaObjectClassService from './SchemaObjectClassService';
import SchemaAttributeService from './SchemaAttributeService';
import SystemAttributeMappingService from './SystemAttributeMappingService';
import SystemMappingService from './SystemMappingService';
import SynchronizationConfigService from './SynchronizationConfigService';
import SynchronizationLogService from './SynchronizationLogService';
import ProvisioningOperationService from './ProvisioningOperationService';
import ProvisioningArchiveService from './ProvisioningArchiveService';
import SyncActionLogService from './SyncActionLogService';
import SyncItemLogService from './SyncItemLogService';
import RoleAccountService from './RoleAccountService';
import TreeAccountService from './TreeAccountService';
import RoleCatalogueAccountService from './RoleCatalogueAccountService';
import ProvisioningBreakConfigService from './ProvisioningBreakConfigService';
import ProvisioningBreakRecipientService from './ProvisioningBreakRecipientService';
import ContractAccountService from './ContractAccountService';
import ContractSliceAccountService from './ContractSliceAccountService';
import AttributeControlledValueService from './AttributeControlledValueService';
import UniformPasswordService from './UniformPasswordService';
import UniformPasswordSystemService from './UniformPasswordSystemService';
import RemoteServerService from './RemoteServerService';
import SystemGroupService from './SystemGroupService';
import SystemGroupSystemService from './SystemGroupSystemService';
import SystemOwnerService from './SystemOwnerService';
import SystemOwnerRoleService from './SystemOwnerRoleService';
import SystemEntityTypeService from './SystemEntityTypeService';
import RequestAccountRoleService from "./RequestAccountRoleService";

const ServiceRoot = {
  SystemService,
  RoleSystemService,
  SystemEntityService,
  AccountService,
  IdentityAccountService,
  SchemaObjectClassService,
  SchemaAttributeService,
  SystemAttributeMappingService,
  SystemMappingService,
  RoleSystemAttributeService,
  SynchronizationConfigService,
  SynchronizationLogService,
  ProvisioningOperationService,
  ProvisioningArchiveService,
  SyncActionLogService,
  SyncItemLogService,
  RoleAccountService,
  TreeAccountService,
  RoleCatalogueAccountService,
  ProvisioningBreakConfigService,
  ProvisioningBreakRecipientService,
  ContractAccountService,
  ContractSliceAccountService,
  AttributeControlledValueService,
  UniformPasswordService,
  UniformPasswordSystemService,
  RemoteServerService,
  SystemGroupService,
  SystemGroupSystemService,
  RequestAccountRoleService,
  SystemGroupSystemService,
  SystemOwnerService,
  SystemOwnerRoleService,
  SystemEntityTypeService
};

ServiceRoot.version = '0.1.0';
module.exports = ServiceRoot;
