module.exports = {
  module: 'acc',
  childRoutes: [
    {
      path: 'connector-types',
      component: require('./src/content/wizard/ConnectorTypes'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
    },
    {
      path: 'systems',
      component: require('./src/content/system/Systems'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
    },
    {
      path: 'system/:entityId/',
      component: require('./src/content/system/System'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/system/SystemContent'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'entities',
          component: require('./src/content/system/SystemEntities'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-configs',
          component: require('./src/content/system/SystemSynchronizationConfigs'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'accounts',
          component: require('./src/content/system/SystemAccounts'),
          access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['SYSTEM_READ', 'ACCOUNT_READ'] } ]
        },
        {
          path: 'owner',
          component: require('./src/content/system/SystemOwners'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEMOWNER_READ', 'SYSTEMOWNERROLE_READ'] } ]
        },
        {
          path: 'connector',
          component: require('./src/content/system/SystemConnector'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'object-classes',
          component: require('./src/content/system/SchemaObjectClasses'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'object-classes/:objectClassId/detail',
          component: require('./src/content/system/SchemaObjectClassDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'object-classes/:objectClassId/new',
          component: require('./src/content/system/SchemaObjectClassDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'schema-attributes/:attributeId/detail',
          component: require('./src/content/system/SchemaAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'schema-attributes/:attributeId/new',
          component: require('./src/content/system/SchemaAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'mappings',
          component: require('./src/content/system/SystemMappings'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'mappings/:mappingId/detail',
          component: require('./src/content/system/SystemMappingDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'mappings/:mappingId/new',
          component: require('./src/content/system/SystemMappingDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'attribute-mappings/:attributeId/detail',
          component: require('./src/content/system/SystemAttributeMappingDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'attribute-mappings/:attributeId/new',
          component: require('./src/content/system/SystemAttributeMappingDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-configs/:configId/detail',
          component: require('./src/content/system/SystemSynchronizationConfigDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-configs/:configId/new',
          component: require('./src/content/system/SystemSynchronizationConfigDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE'] } ]
        },
        {
          path: 'synchronization-logs/:logId/detail',
          component: require('./src/content/system/SystemSynchronizationLogDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-action-logs/:logActionId/detail',
          component: require('./src/content/system/SystemSyncActionLogDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'synchronization-item-logs/:logItemId/detail',
          component: require('./src/content/system/SystemSyncItemLogDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'provisioning',
          component: require('./src/content/system/SystemProvisioningOperations'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
        },
        {
          path: 'break-configs',
          component: require('./src/content/system/SystemProvisioningBreakConfigs'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'break-configs/:configId/new',
          component: require('./src/content/system/SystemProvisioningBreakConfigDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_CREATE'] } ]
        },
        {
          path: 'break-configs/:configId/detail',
          component: require('./src/content/system/SystemProvisioningBreakConfigDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'roles',
          component: require('./src/content/system/SystemRoles'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
        },
        {
          path: 'roles/:roleSystemId/new',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
        },
        {
          path: 'roles/:roleSystemId/detail',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'roles/:roleSystemId/attributes/:attributeId/detail',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'roles/:roleSystemId/attributes/:attributeId/new',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
        }
      ]
    },
    {
      path: 'system/:entityId/new',
      component: require('./src/content/system/SystemContent'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_CREATE'] } ]
    },
    {
      path: 'accounts/',
      component: require('./src/content/account/AccountRoutes'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
      childRoutes: [
        {
          path: 'accounts-all',
          component: require('./src/content/account/Accounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ]
        },
        {
          path: 'accounts-personal',
          component: require('./src/content/account/AccountsPersonal'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ]
        }
      ]
    },
    {
      path: 'account/:entityId',
      component: require('./src/content/account/AccountDetailRoutes'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/account/AccountDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ]
        },
        {
          path: 'roles',
          component: require('./src/content/account/AccountRoles'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ]
        },
        {
          path: 'audit',
          component: require('./src/content/account/AccountAudit'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ]
        },
        {
          path: 'provisioning',
          component: require('./src/content/account/AccountProvisioning'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ]
        },
        {
          path: 'other',
          component: require('./src/content/account/AccountOtherSetting'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ]
        }
      ]
    },
    {
      path: 'connector-servers/',
      component: require('./src/content/connectorserver/ConnectorServers'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['REMOTESERVER_READ'] } ]
    },
    {
      path: 'remote-servers/:entityId',
      component: require('./src/content/connectorserver/RemoteServerRoutes'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['REMOTESERVER_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/connectorserver/RemoteServerDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['REMOTESERVER_READ'] } ]
        },
        {
          path: 'systems',
          component: require('./src/content/connectorserver/RemoteServerSystems'),
          access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['REMOTESERVER_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'connectors',
          component: require('./src/content/connectorserver/RemoteServerConnectors'),
          access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['REMOTESERVER_READ'] } ]
        }
      ]
    },
    {
      path: 'identity/:entityId/',
      component: require('czechidm-core/src/content/identity/Identity'),
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/identity/IdentityAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITYACCOUNT_READ'] } ]
        },
        {
          path: 'provisioning',
          component: require('./src/content/identity/IdentityProvisioningOperations'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
        }
      ]
    },
    {
      path: '/identity/:identityId/identity-contract/:entityId',
      component: require('czechidm-core/src/content/identity/contract/IdentityContract'),
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/contract/ContractAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTACCOUNT_READ'] } ]
        }
      ]
    },
    {
      path: '/identity/:identityId/contract-slice/:entityId',
      component: require('czechidm-core/src/content/identity/contractSlice/ContractSlice'),
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/contractSlice/ContractSliceAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTSLICEACCOUNT_READ'] } ]
        }
      ]
    },
    {
      path: 'password-policies/',
      component: require('czechidm-core/src/content/passwordpolicy/PasswordPolicyRoutes'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDPOLICY_READ'] } ],
      childRoutes: [
        {
          path: ':entityId/systems',
          component: require('./src/content/passwordpolicy/PasswordPolicySystems'),
          access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['PASSWORDPOLICY_READ', 'SYSTEM_READ'] } ]
        }
      ]
    },
    {
      path: 'role/:entityId/',
      component: require('czechidm-core/src/content/role/Role'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ'] } ],
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/role/RoleAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEACCOUNT_READ'] } ]
        },
        {
          path: 'systems',
          component: require('./src/content/role/RoleSystems'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/new',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/detail',
          component: require('./src/content/role/RoleSystemDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/attributes/:attributeId/detail',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
        },
        {
          path: 'systems/:roleSystemId/attributes/:attributeId/new',
          component: require('./src/content/role/RoleSystemAttributeDetail'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
        }
      ]
    },
    {
      path: 'requests/:requestId',
      component: require('czechidm-core/src/content/request/RequestInfo'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ'] } ],
      childRoutes: [
        {
          path: 'role/:entityId',
          component: require('czechidm-core/src/content/role/Role'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ'] } ],
          childRoutes: [
            {
              path: 'accounts',
              component: require('./src/content/role/RoleAccounts'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEACCOUNT_READ'] } ]
            },
            {
              path: 'systems',
              component: require('./src/content/role/RoleSystems'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
            },
            {
              path: 'systems/:roleSystemId/new',
              component: require('./src/content/role/RoleSystemDetail'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
            },
            {
              path: 'systems/:roleSystemId/detail',
              component: require('./src/content/role/RoleSystemDetail'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
            },
            {
              path: 'systems/:roleSystemId/attributes/:attributeId/detail',
              component: require('./src/content/role/RoleSystemAttributeDetail'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
            },
            {
              path: 'systems/:roleSystemId/attributes/:attributeId/new',
              component: require('./src/content/role/RoleSystemAttributeDetail'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_UPDATE', 'SYSTEM_READ'] } ]
            }
          ]
        }
      ]
    },
    {
      path: 'tree',
      childRoutes: [
        {
          path: 'nodes/:entityId',
          component: require('czechidm-core/src/content/tree/node/Node'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TREENODE_READ'] } ],
          childRoutes: [
            {
              path: 'accounts',
              component: require('./src/content/tree/TreeAccounts'),
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TREEACCOUNT_READ'] } ]
            }
          ]
        }
      ]
    },
    {
      path: 'role-catalogue/:entityId',
      component: require('czechidm-core/src/content/rolecatalogue/RoleCatalogue'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUE_READ'] } ],
      childRoutes: [
        {
          path: 'accounts',
          component: require('./src/content/rolecatalogue/RoleCatalogueAccounts'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUEACCOUNT_READ'] } ]
        }
      ]
    },
    {
      path: 'provisioning',
      component: require('./src/content/provisioning/AuditProvisioningOperations'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ],
    },
    {
      path: 'uniform-password/:entityId/new',
      component: require('./src/content/uniformpassword/UniformPasswordContent'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_CREATE'] } ],
    },
    {
      path: 'uniform-password',
      component: require('./src/content/uniformpassword/UniformPasswords'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ]
    },
    {
      path: 'uniform-password/:entityId/',
      component: require('./src/content/uniformpassword/UniformPassword'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/uniformpassword/UniformPasswordContent'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ]
        },
        {
          path: 'systems',
          component: require('./src/content/uniformpassword/UniformPasswordSystems'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDFILTER_READ'] } ]
        }
      ]
    },
    {
      path: 'system-groups/:entityId/new',
      component: require('./src/content/systemgroup/SystemGroupContent'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_GROUP_CREATE'] } ],
    },
    {
      path: 'system-groups',
      component: require('./src/content/systemgroup/SystemGroups'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_GROUP_READ'] } ]
    },
    {
      path: 'system-groups/:entityId/',
      component: require('./src/content/systemgroup/SystemGroup'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_GROUP_READ'] } ],
      childRoutes: [
        {
          path: 'detail',
          component: require('./src/content/systemgroup/SystemGroupContent'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_GROUP_READ'] } ]
        },
        {
          path: 'systems',
          component: require('./src/content/systemgroup/SystemGroupSystems'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_GROUP_READ'] } ]
        }
      ]
    },
    {
      path: 'scripts/:entityId/',
      component: require('czechidm-core/src/content/script/Script'),
      childRoutes: [
        {
          path: 'references-mapping',
          component: require('./src/content/script/ScriptReferencesMapping'),
          access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCRIPT_READ'] } ]
        }
      ]
    }
  ]
};
