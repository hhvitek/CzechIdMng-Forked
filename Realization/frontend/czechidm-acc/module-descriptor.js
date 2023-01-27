module.exports = {
  id: 'acc',
  npmName: 'czechidm-acc',
  backendId: 'acc',
  disableable: true,
  name: 'Account managment',
  description: 'Module for account managment',
  mainStyleFile: 'src/css/main.less',
  mainRouteFile: 'routes.js',
  mainComponentDescriptorFile: 'component-descriptor.js',
  mainLocalePath: 'src/locales/',
  navigation: {
    items: [
      {
        id: 'sys-systems-main-menu',
        type: 'DYNAMIC',
        labelKey: 'acc:content.systems.title',
        titleKey: 'acc:content.systems.title',
        order: 1100,
        priority: 0,
        path: '/systems',
        icon: 'component:systems',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ],
        items: [
          {
            id: 'sys-systems',
            type: 'MAIN-MENU',
            labelKey: 'acc:content.systems.title',
            titleKey: 'acc:content.systems.title',
            order: 10,
            priority: 0,
            path: '/systems',
            icon: 'component:systems',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ],
            items: [
              {
                id: 'system-detail',
                type: 'TAB',
                labelKey: 'acc:content.system.detail.basic',
                order: 1,
                path: '/system/:entityId/detail',
                icon: 'fa:newspaper-o',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-connector',
                type: 'TAB',
                labelKey: 'acc:content.system.connector.title',
                order: 2,
                path: '/system/:entityId/connector',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-provisioning-break-config',
                type: 'TAB',
                icon: 'far:stop-circle',
                labelKey: 'acc:content.provisioningBreakConfig.title',
                order: 10,
                path: '/system/:entityId/break-configs',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-accounts',
                type: 'TAB',
                labelKey: 'acc:content.system.accounts.title',
                order: 20,
                path: '/system/:entityId/accounts',
                icon: 'fa:external-link',
                access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['SYSTEM_READ', 'ACCOUNT_READ'] } ]
              },
              {
                id: 'system-owner',
                type: 'TAB',
                labelKey: 'acc:content.system.owner.title',
                order: 15,
                path: '/system/:entityId/owner',
                icon: 'fa:user',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEMOWNER_READ', 'SYSTEMOWNERROLE_READ'] } ]
              },
              {
                id: 'system-entities',
                type: 'TAB',
                labelKey: 'acc:content.system.entities.title',
                order: 30,
                path: '/system/:entityId/entities',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'schema-object-classes',
                type: 'TAB',
                icon: 'fa:object-group',
                labelKey: 'acc:content.system.schemaObjectClasses.title',
                order: 40,
                path: '/system/:entityId/object-classes',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-mappings',
                type: 'TAB',
                icon: 'list-alt',
                labelKey: 'acc:content.system.mappings.title',
                order: 50,
                path: '/system/:entityId/mappings',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-roles',
                type: 'TAB',
                icon: 'component:roles',
                labelKey: 'acc:content.systemRoles.title',
                titleKey: 'acc:content.systemRoles.header',
                order: 55,
                path: '/system/:entityId/roles',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-synchronization-configs',
                type: 'TAB',
                icon: 'component:synchronizations',
                labelKey: 'acc:content.system.systemSynchronizationConfigs.title',
                order: 60,
                path: '/system/:entityId/synchronization-configs',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_READ'] } ]
              },
              {
                id: 'system-provisioning-operations',
                type: 'TAB',
                labelKey: 'acc:content.provisioningOperations.label',
                titleKey: 'acc:content.provisioningOperations.title',
                order: 70,
                path: '/system/:entityId/provisioning',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
              }
            ]
          },
          {
            id: 'accounts',
            labelKey: 'acc:content.accounts.label',
            titleKey: 'acc:content.accounts.title',
            order: 15,
            priority: 0,
            icon: 'fa:external-link',
            path: '/accounts/accounts-all',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
            items: [{
              id: 'accounts-all',
              type: 'TAB',
              labelKey: 'acc:content.accounts.all.label',
              titleKey: 'acc:content.accounts.all.title',
              order: 15,
              icon: '',
              path: '/accounts/accounts-all',
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
              items: []
            },
            {
              id: 'accounts-personal',
              type: 'TAB',
              labelKey: 'acc:content.accounts.personal.label',
              titleKey: 'acc:content.accounts.personal.title',
              order: 20,
              icon: '',
              path: '/accounts/accounts-personal',
              access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
              items: []
            }
            ]
          },
          {
            id: 'sys-connector-servers',
            labelKey: 'acc:content.connector-servers.label',
            titleKey: 'acc:content.connector-servers.title',
            order: 15,
            priority: 0,
            icon: 'component:servers',
            path: '/connector-servers',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['REMOTESERVER_READ'] } ],
            items: [
              {
                id: 'sys-remote-server-detail',
                type: 'TAB',
                labelKey: 'acc:content.remote-servers.detail.title',
                icon: 'component:basic-info',
                order: 10,
                path: '/remote-servers/:entityId/detail',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['REMOTESERVER_READ'] } ]
              },
              {
                id: 'sys-remote-server-connectors',
                type: 'TAB',
                labelKey: 'acc:content.remote-servers.connectors.label',
                titleKey: 'acc:content.remote-servers.connectors.title',
                icon: 'component:default-connector',
                order: 20,
                path: '/remote-servers/:entityId/connectors',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['REMOTESERVER_READ'] } ]
              },
              {
                id: 'sys-remote-server-systems',
                type: 'TAB',
                labelKey: 'acc:content.remote-servers.systems.label',
                titleKey: 'acc:content.remote-servers.systems.title',
                icon: 'component:system',
                order: 20,
                path: '/remote-servers/:entityId/systems',
                access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['REMOTESERVER_READ', 'SYSTEM_READ'] } ]
              }
            ]
          },
          {
            id: 'uniform-password',
            labelKey: 'acc:content.uniformPassword.label',
            titleKey: 'acc:content.uniformPassword.title',
            order: 20,
            priority: 0,
            path: '/uniform-password',
            icon: 'fa:key',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['UNIFORMPASSWORD_READ'] } ],
            items: [
              {
                id: 'uniform-password-detail',
                type: 'TAB',
                labelKey: 'acc:content.uniformPassword.basic',
                order: 10,
                path: '/uniform-password/:entityId/detail',
                icon: 'fa:key',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['UNIFORMPASSWORD_READ'] } ]
              },
              {
                id: 'uniform-password-system',
                type: 'TAB',
                labelKey: 'acc:content.uniformPasswordSystem.detail',
                order: 20,
                path: '/uniform-password/:entityId/systems',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['UNIFORMPASSWORD_READ'] } ]
              }
            ]
          },
          {
            id: 'system-groups',
            labelKey: 'acc:content.systemGroup.label',
            titleKey: 'acc:content.systemGroup.title',
            order: 20,
            priority: 0,
            path: '/system-groups',
            icon: 'fa:layer-group',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_GROUP_READ'] } ],
            items: [
              {
                id: 'system-groups-detail',
                type: 'TAB',
                labelKey: 'acc:content.systemGroup.basic',
                order: 10,
                path: '/system-groups/:entityId/detail',
                icon: 'fa:layer-group',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_GROUP_READ'] } ]
              },
              {
                id: 'system-group-systems',
                type: 'TAB',
                labelKey: 'acc:content.systemGroupSystem.detail',
                order: 20,
                path: '/system-groups/:entityId/systems',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SYSTEM_GROUP_READ'] } ]
              }
            ]
          }
        ]
      },
      {
        id: 'identity-accounts',
        parentId: 'identity-profile',
        type: 'TAB',
        labelKey: 'acc:content.identity.accounts.title',
        order: 100,
        priority: 0,
        path: '/identity/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITYACCOUNT_READ'] } ]
      },
      {
        id: 'identity-provisioning-operations',
        parentId: 'identity-profile',
        type: 'TAB',
        labelKey: 'acc:content.provisioningOperations.label',
        titleKey: 'acc:content.provisioningOperations.title',
        order: 110,
        path: '/identity/:entityId/provisioning',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ'] } ]
      },
      {
        id: 'identity-contract-accounts',
        parentId: 'profile-contracts',
        type: 'TAB',
        labelKey: 'acc:content.contract.accounts.title',
        order: 100,
        priority: 0,
        path: '/identity/:identityId/identity-contract/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTACCOUNT_READ'] } ]
      },
      {
        id: 'contract-slice-accounts',
        parentId: 'identity-contract-slices',
        type: 'TAB',
        labelKey: 'acc:content.contract-slice.accounts.title',
        order: 100,
        priority: 0,
        path: '/identity/:identityId/contract-slice/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTSLICEACCOUNT_READ'] } ]
      },
      {
        id: 'password-policies-systems',
        parentId: 'password-policies',
        type: 'TAB',
        labelKey: 'acc:content.passwordPolicy.system.tab',
        order: 100,
        priority: 0,
        path: '/password-policies/:entityId/systems',
        icon: 'component:systems',
        access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['PASSWORDPOLICY_READ', 'SYSTEM_READ'] } ]
      },
      {
        id: 'role-systems',
        type: 'TAB',
        parentId: 'roles',
        labelKey: 'acc:content.role.systems.title',
        titleKey: 'acc:content.role.systems.title',
        order: 500,
        path: '/role/:entityId/systems',
        icon: 'link',
        access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
      },
      {
        id: 'request-role-systems',
        type: 'TAB',
        parentId: 'request-roles',
        labelKey: 'acc:content.role.systems.title',
        titleKey: 'acc:content.role.systems.title',
        order: 500,
        path: 'requests/:requestId/role/:entityId/systems',
        icon: 'link',
        access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['ROLE_READ', 'SYSTEM_READ'] } ]
      },
      {
        id: 'provisioning-operations',
        parentId: 'audit',
        labelKey: 'acc:content.provisioningOperations.label',
        titleKey: 'acc:content.provisioningOperations.title',
        order: 200,
        path: '/provisioning',
        access: [
          {
            type: 'HAS_ANY_AUTHORITY',
            authorities: ['PROVISIONINGOPERATION_READ', 'PROVISIONINGARCHIVE_READ']
          }
        ]
      },
      {
        id: 'role-accounts',
        parentId: 'roles',
        type: 'TAB',
        labelKey: 'acc:content.identity.accounts.title',
        order: 500,
        priority: 0,
        path: '/role/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEACCOUNT_READ'] } ]
      },
      {
        id: 'tree-accounts',
        parentId: 'tree-nodes',
        type: 'TAB',
        labelKey: 'acc:content.identity.accounts.title',
        order: 500,
        priority: 0,
        path: '/tree/nodes/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TREEACCOUNT_READ'] } ]
      },
      {
        id: 'role-catalogue-accounts',
        parentId: 'role-catalogues',
        type: 'TAB',
        labelKey: 'acc:content.identity.accounts.title',
        order: 500,
        priority: 0,
        path: '/role-catalogue/:entityId/accounts',
        icon: 'fa:external-link',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUEACCOUNT_READ'] } ]
      },
      {
        id: 'references-mapping',
        parentId: 'scripts',
        type: 'TAB',
        labelKey: 'acc:content.script.references.title',
        order: 500,
        icon: '',
        path: '/scripts/:entityId/references-mapping',
        priority: 0,
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCRIPT_READ'] } ]
      },
      {
        id: 'account',
        type: 'TAB',
        labelKey: 'acc:content.accounts.label',
        titleKey: 'acc:content.accounts.title',
        order: 15,
        priority: 0,
        icon: 'fa:external-link',
        path: '/account/:entityId',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
        items: [
          {
            id: 'account-detail',
            type: 'TAB',
            labelKey: 'acc:content.accounts.detail.tabs.account',
            titleKey: 'acc:content.accounts.detail.tabs.account',
            order: 15,
            priority: 0,
            icon: 'fa:external-link',
            path: '/account/:entityId/detail',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
            items: []
          },
          {
            id: 'account-roles',
            type: 'TAB',
            labelKey: 'acc:content.accounts.detail.tabs.roles',
            titleKey: 'acc:content.accounts.detail.tabs.roles',
            order: 15,
            priority: 0,
            icon: 'fa:key',
            path: '/account/:entityId/roles',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
            items: []
          },
          {
            id: 'account-audit',
            type: 'TAB',
            labelKey: 'acc:content.accounts.detail.tabs.audit',
            titleKey: 'acc:content.accounts.detail.tabs.audit',
            order: 15,
            priority: 0,
            icon: 'fa:history',
            path: '/account/:entityId/audit',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
            items: []
          },
          {
            id: 'account-provisioning',
            type: 'TAB',
            labelKey: 'acc:content.accounts.detail.tabs.provisioning',
            titleKey: 'acc:content.accounts.detail.tabs.provisioning',
            order: 15,
            priority: 0,
            icon: 'fa:circle-o',
            path: '/account/:entityId/provisioning',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
            items: []
          },
          {
            id: 'account-other-setting',
            type: 'TAB',
            labelKey: 'acc:content.accounts.detail.tabs.other',
            titleKey: 'acc:content.accounts.detail.tabs.other',
            order: 15,
            priority: 0,
            icon: 'fa:cog',
            path: '/account/:entityId/other',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ACCOUNT_READ'] } ],
            items: []
          }
        ]
      }
    ]
  }
};
