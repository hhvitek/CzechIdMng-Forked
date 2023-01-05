module.exports = {
  id: 'core',
  npmName: 'czechidm-core',
  name: 'Core',
  disableable: false,
  description: 'Core functionallity. Defines basic navigation structure, routes etc. Has lowest priority, could be overriden.',
  mainStyleFile: 'src/css/main.less',
  mainRouteFile: 'routes.js',
  mainComponentDescriptorFile: 'component-descriptor.js',
  mainLocalePath: 'src/locales/',
  navigation: {
    items: [
      {
        id: 'dashboard',
        type: 'DYNAMIC',
        section: 'main',
        labelKey: 'navigation.menu.dashboard.label',
        titleKey: 'navigation.menu.dashboard.title',
        icon: 'user',
        iconColor: '#428BCA',
        order: 10,
        priority: 0,
        path: '/',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ],
      },
      {
        id: 'tasks',
        disabled: false,
        labelKey: 'navigation.menu.tasks.label',
        titleKey: 'navigation.menu.tasks.title',
        icon: 'fa:tasks',
        path: '/tasks/identity/:entityId',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['WORKFLOWTASK_READ'] } ],
        order: 30,
        items: [
          {
            id: 'tasks-identity',
            labelKey: 'content.tasks.identity.label',
            titleKey: 'content.tasks.identity.title',
            order: 10,
            path: '/tasks/identity/:entityId',
            icon: '',
            type: 'TAB',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['WORKFLOWTASK_READ'] } ]
          },
          {
            id: 'tasks-all',
            labelKey: 'content.tasks.all.label',
            titleKey: 'content.tasks.all.title',
            order: 20,
            path: '/tasks/all',
            icon: '',
            type: 'TAB',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['WORKFLOWTASK_ADMIN'] } ]
          }
        ]
      },
      {
        id: 'identities-menu',
        type: 'DYNAMIC',
        labelKey: 'navigation.menu.identities.label',
        titleKey: 'navigation.menu.identities.title',
        icon: 'component:identities',
        order: 1010,
        path: '/identities',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ', 'DELEGATIONDEFINITION_READ'] } ],
        items: [
          {
            id: 'identities',
            labelKey: 'navigation.menu.identities.label',
            titleKey: 'navigation.menu.identities.title',
            icon: 'component:identities',
            type: 'MAIN-MENU',
            order: 1010,
            path: '/identities',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ],
            items: [
              {
                id: 'identity-profile',
                type: 'TAB',
                section: 'main',
                labelKey: 'navigation.menu.profile.label',
                titleKey: 'navigation.menu.profile.title',
                icon: 'user',
                iconColor: '#428BCA',
                order: 10,
                priority: 0,
                path: '/identity/:loggedUsername/profile',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ],
                items: [
                  {
                    id: 'profile-personal',
                    type: 'TAB',
                    labelKey: 'content.identity.sidebar.profile',
                    order: 10,
                    priority: 0,
                    path: '/identity/:entityId/profile',
                    icon: 'user',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ]
                  },
                  {
                    id: 'profile-eav',
                    type: 'TAB',
                    labelKey: 'content.identity.eav.title',
                    order: 11,
                    priority: 0,
                    path: '/identity/:entityId/eav',
                    access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['IDENTITY_READ', 'FORMDEFINITION_AUTOCOMPLETE'] } ]
                  },
                  {
                    id: 'profile-password',
                    type: 'TAB',
                    labelKey: 'content.identity.sidebar.password',
                    order: 20,
                    path: '/identity/:entityId/password/change',
                    icon: 'component:password',
                    conditions: [
                      'todo: eval( canPasswordChange ...)'
                    ],
                    access: [{
                      type: 'HAS_ANY_AUTHORITY', // TODO: PASSWORDRESET is from pwdreset module, implement some conditional iten hidding
                      authorities: ['IDENTITY_PASSWORDCHANGE', 'PASSWORD_READ', 'IDENTITY_PASSWORDRESET']
                    }],
                    items: [
                      {
                        id: 'profile-password-change',
                        type: 'TAB',
                        labelKey: 'content.identity.passwordChange.title',
                        order: 0,
                        icon: '',
                        path: '/identity/:entityId/password/change',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_PASSWORDCHANGE'] } ]
                      },
                      {
                        id: 'profile-password-metadata',
                        type: 'TAB',
                        labelKey: 'content.password.label',
                        order: 100,
                        icon: '',
                        path: '/identity/:entityId/password/detail',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORD_READ'] } ]
                      },
                      {
                        id: 'profile-tokens',
                        type: 'TAB',
                        labelKey: 'content.tokens.header',
                        titleKey: 'content.tokens.title',
                        order: 600,
                        icon: '',
                        path: '/identity/:entityId/password/tokens',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TOKEN_READ'] } ]
                      }
                    ]
                  },
                  {
                    id: 'profile-roles',
                    type: 'TAB',
                    labelKey: 'content.identity.sidebar.roles',
                    order: 30,
                    path: '/identity/:entityId/roles',
                    icon: 'component:identity-roles',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITYROLE_READ'] } ]
                  },
                  {
                    id: 'profile-authority',
                    type: 'TAB',
                    labelKey: 'content.identity.authorities.label',
                    titleKey: 'content.identity.authorities.title',
                    order: 40,
                    icon: 'fa:shield-alt',
                    path: '/identity/:entityId/authority/authorities',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTHORIZATIONPOLICY_READ'] } ],
                    items: [
                      {
                        id: 'profile-authorities',
                        type: 'TAB',
                        labelKey: 'content.identity.authorities.label',
                        titleKey: 'content.identity.authorities.title',
                        order: 10,
                        icon: '',
                        path: '/identity/:entityId/authority/authorities',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTHORIZATIONPOLICY_READ'] } ]
                      },
                      {
                        id: 'profile-authorization-policies',
                        type: 'TAB',
                        labelKey: 'content.identity.authorization-policies.label',
                        titleKey: 'content.identity.authorization-policies.title',
                        order: 10,
                        icon: '',
                        path: '/identity/:entityId/authority/authorization-policies',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTHORIZATIONPOLICY_READ'] } ]
                      }
                    ]
                  },
                  {
                    id: 'profile-contracts',
                    type: 'TAB',
                    labelKey: 'entity.IdentityContract._type',
                    order: 50,
                    path: '/identity/:entityId/contracts',
                    icon: 'component:contracts',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITYCONTRACT_READ'] } ],
                    items: [
                      {
                        id: 'identity-contract-detail',
                        type: 'TAB',
                        labelKey: 'content.identity-contract.detail.label',
                        order: 10,
                        path: '/identity/:identityId/identity-contract/:entityId/detail',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITYCONTRACT_READ'] } ]
                      },
                      {
                        id: 'identity-contract-eav',
                        type: 'TAB',
                        labelKey: 'content.identity-contract.eav.label',
                        order: 20,
                        path: '/identity/:identityId/identity-contract/:entityId/eav',
                        access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['IDENTITYCONTRACT_READ', 'FORMDEFINITION_AUTOCOMPLETE'] } ],
                      },
                      {
                        id: 'identity-contract-guarantees',
                        type: 'TAB',
                        labelKey: 'content.identity-contract.guarantees.label',
                        order: 30,
                        path: '/identity/:identityId/identity-contract/:entityId/guarantees',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITYCONTRACT_READ'] } ],
                      },
                      {
                        id: 'identity-contract-positions',
                        type: 'TAB',
                        labelKey: 'content.identity-contract.positions.label',
                        order: 40,
                        icon: 'component:contract-positions',
                        path: '/identity/:identityId/identity-contract/:entityId/positions',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTPOSITION_READ'] } ],
                      },
                      {
                        id: 'identity-contract-slices',
                        type: 'TAB',
                        labelKey: 'entity.ContractSlice._type',
                        order: 55,
                        path: '/identity/:identityId/identity-contract/:entityId/contract-slices',
                        icon: 'fa:hourglass-half',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTSLICE_READ'] } ],
                        items: [
                          {
                            id: 'contract-slice-detail',
                            type: 'TAB',
                            labelKey: 'content.contract-slice.detail.label',
                            order: 10,
                            path: '/identity/:identityId/contract-slice/:entityId/detail',
                            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTSLICE_READ'] } ]
                          },
                          {
                            id: 'contract-slice-eav',
                            type: 'TAB',
                            labelKey: 'content.contract-slice.eav.label',
                            order: 20,
                            path: '/identity/:identityId/contract-slice/:entityId/eav',
                            access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['CONTRACTSLICE_READ', 'FORMDEFINITION_AUTOCOMPLETE'] } ]
                          },
                          {
                            id: 'contract-slice-guarantees',
                            type: 'TAB',
                            labelKey: 'content.contract-slice.guarantees.label',
                            order: 30,
                            path: '/identity/:identityId/contract-slice/:entityId/guarantees',
                            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONTRACTSLICEGUARANTEE_READ'] } ],
                          }
                        ]
                      },
                    ]
                  },
                  {
                    id: 'profile-audit',
                    labelKey: 'content.audit.label',
                    order: 500,
                    path: '/identity/:entityId/audit/identity',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUDIT_READ'] } ],
                    icon: 'component:audit',
                    items: [
                      {
                        id: 'profile-audit-profile',
                        icon: '',
                        type: 'TAB',
                        labelKey: 'content.audit.label',
                        order: 100,
                        path: '/identity/:entityId/audit/identity',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUDIT_READ'] } ]
                      },
                      {
                        id: 'profile-audit-roles',
                        icon: '',
                        type: 'TAB',
                        labelKey: 'content.audit.identityRoles.label',
                        order: 200,
                        path: '/identity/:entityId/audit/roles',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUDIT_READ'] } ]
                      },
                      {
                        id: 'profile-audit-login',
                        icon: '',
                        type: 'TAB',
                        labelKey: 'content.audit.identityLogin.label',
                        order: 300,
                        path: '/identity/:entityId/audit/login',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUDIT_READ'] } ]
                      },
                      {
                        id: 'profile-audit-password-change',
                        icon: '',
                        type: 'TAB',
                        labelKey: 'content.audit.identityPasswordChange.label',
                        order: 400,
                        path: '/identity/:entityId/audit/password-change',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUDIT_READ'] } ]
                      }
                    ]
                  },
                  {
                    id: 'profile-events',
                    type: 'TAB',
                    labelKey: 'content.entityEvents.label',
                    order: 550,
                    path: '/identity/:entityId/events',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ENTITYEVENT_READ'] } ]
                  },
                  {
                    id: 'profile-delegations',
                    type: 'TAB',
                    icon: 'fa:dolly',
                    labelKey: 'content.delegation-definitions.label',
                    order: 95,
                    path: '/identity/:entityId/delegation-definitions',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['DELEGATIONDEFINITION_READ'] } ],
                    items: [
                      {
                        id: 'identity-delegation-definitions',
                        labelKey: 'content.delegation-definitions.header',
                        titleKey: 'content.delegation-definitions.title',
                        icon: 'fa:dolly',
                        order: 20,
                        path: '/delegation-definitions',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['DELEGATIONDEFINITION_READ'] } ],
                        items: [
                          {
                            id: 'identity-delegation-definition-detail',
                            labelKey: 'content.delegation-definitions.detail.title',
                            order: 10,
                            path: '/identity/:entityId/delegation-definitions/:delegationId/detail',
                            icon: 'fa:dolly',
                            type: 'TAB',
                            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['DELEGATIONDEFINITION_READ'] } ]
                          },
                          {
                            id: 'identity-delegations',
                            type: 'TAB',
                            labelKey: 'content.delegation-definitions.delegations.label',
                            order: 40,
                            icon: 'fa:tasks',
                            path: '/identity/:entityId/delegation-definitions/:delegationId/delegations',
                            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['DELEGATIONDEFINITION_READ'] } ]
                          }
                        ]
                      }
                    ]
                  },
                  {
                    id: 'profile-subordinates',
                    type: 'TAB',
                    labelKey: 'content.identity.subordinates.title',
                    order: 60,
                    path: '/identity/:entityId/subordinates',
                    icon: 'component:identities',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ]
                  },
                  {
                    id: 'profile-garanted-roles',
                    type: 'TAB',
                    labelKey: 'content.identity.garanted-roles.title',
                    order: 70,
                    path: '/identity/:entityId/garanted-roles',
                    icon: 'component:roles',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ'] } ]
                  },
                ]
              }
            ]
          },
          {
            id: 'delegation-definitions',
            labelKey: 'content.delegation-definitions.header',
            titleKey: 'content.delegation-definitions.title',
            icon: 'fa:dolly',
            order: 20,
            path: '/delegation-definitions',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['DELEGATIONDEFINITION_READ'] } ],
            items: [
              {
                id: 'delegation-definition-detail',
                labelKey: 'content.delegation-definitions.detail.title',
                order: 10,
                path: '/delegation-definitions/:delegationId/detail',
                icon: 'fa:dolly',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['DELEGATIONDEFINITION_READ'] } ]
              },
              {
                id: 'delegations',
                type: 'TAB',
                labelKey: 'content.delegation-definitions.delegations.label',
                order: 40,
                icon: 'fa:tasks',
                path: '/delegation-definitions/:delegationId/delegations',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['DELEGATIONDEFINITION_READ'] } ]
              }
            ]
          }
        ]},
      {
        id: 'organizations',
        labelKey: 'content.organizations.label',
        titleKey: 'content.organizations.title',
        icon: 'fa:folder-open',
        order: 1020,
        iconColor: '#419641',
        path: '/organizations',
        access: [
          { type: 'HAS_ANY_AUTHORITY', authorities: ['TREENODE_READ', 'TREETYPE_READ'] },
          { type: 'HAS_ALL_AUTHORITIES', authorities: ['TREETYPE_AUTOCOMPLETE', 'TREENODE_AUTOCOMPLETE'] }
        ],
        items: [
          {
            id: 'tree-nodes',
            type: 'MAIN-MENU',
            labelKey: 'content.organizations.label',
            titleKey: 'content.organizations.title',
            order: 100,
            icon: 'fa:folder-open',
            path: '/organizations',
            access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['TREENODE_READ', 'TREETYPE_AUTOCOMPLETE'] } ],
            items: [
              {
                id: 'tree-node-detail',
                type: 'TAB',
                labelKey: 'content.tree.node.detail.label',
                order: 10,
                path: '/tree/nodes/:entityId/detail',
                icon: 'component:basic-info',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TREENODE_READ'] } ]
              },
              {
                id: 'tree-node-eav',
                type: 'TAB',
                labelKey: 'content.tree.node.eav.title',
                order: 20,
                path: '/tree/nodes/:entityId/eav',
                access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['TREENODE_READ', 'FORMDEFINITION_AUTOCOMPLETE'] } ]
              },
              {
                id: 'tree-node-roles',
                type: 'TAB',
                labelKey: 'content.tree.node.roles.label',
                titleKey: 'content.tree.node.roles.title',
                order: 30,
                icon: 'component:roles',
                path: '/tree/nodes/:entityId/roles',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLETREENODE_READ'] } ]
              },
              {
                id: 'tree-node-identities',
                type: 'TAB',
                labelKey: 'content.tree.node.identities.label',
                titleKey: 'content.tree.node.identities.title',
                order: 40,
                icon: 'component:identities',
                path: '/tree/nodes/:entityId/identities',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ]
              }
            ]
          },
          {
            id: 'tree-types',
            labelKey: 'content.tree.types.title',
            titleKey: 'content.tree.types.title',
            order: 200,
            icon: 'fa:folder-open',
            path: '/tree/types',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TREETYPE_READ'] } ]
          }
        ]
      },
      {
        id: 'roles-menu',
        type: 'DYNAMIC',
        labelKey: 'content.roles.header',
        titleKey: 'content.roles.title',
        icon: 'component:roles',
        iconColor: '#eb9316',
        order: 1030,
        path: '/roles',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'ROLECATALOGUE_READ', 'ROLETREENODE_READ'] } ],
        items: [
          {
            id: 'roles',
            type: 'MAIN-MENU',
            labelKey: 'content.roles.header',
            titleKey: 'content.roles.title',
            icon: 'component:roles',
            iconColor: '#eb9316',
            order: 20,
            path: '/roles',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'AUTOMATICROLEATTRIBUTE_READ', 'ROLETREENODE_READ'] } ],
            items: [
              {
                id: 'role-detail',
                type: 'TAB',
                labelKey: 'content.roles.tabs.basic',
                order: 100,
                path: '/role/:entityId/detail',
                icon: 'component:basic-info'
              },
              {
                id: 'role-eav',
                type: 'TAB',
                labelKey: 'content.role.eav.title',
                order: 110,
                priority: 0,
                path: '/role/:entityId/eav',
                access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['ROLE_READ', 'FORMDEFINITION_AUTOCOMPLETE'] } ]
              },
              {
                id: 'role-form-attributes',
                type: 'TAB',
                labelKey: 'content.role.formAttributes.title',
                icon: 'fa:th-list',
                order: 115,
                path: '/role/:entityId/form-attributes',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEFORMATTRIBUTE_READ'] } ]
              },
              {
                id: 'role-compositions',
                type: 'TAB',
                icon: 'component:business-roles',
                labelKey: 'content.role.compositions.title',
                order: 120,
                path: '/role/:entityId/compositions',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECOMPOSITION_READ'] } ]
              },
              {
                id: 'incompatible-roles',
                type: 'TAB',
                labelKey: 'content.role.incompatible-roles.header',
                titleKey: 'content.role.incompatible-roles.title',
                order: 125,
                icon: 'fa:times-circle',
                path: '/role/:entityId/incompatible-roles',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['INCOMPATIBLEROLE_READ'] } ]
              },
              {
                id: 'role-guarantees',
                type: 'TAB',
                labelKey: 'content.role.guarantees.title',
                icon: 'component:identities',
                order: 130,
                path: '/role/:entityId/guarantees',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEGUARANTEE_READ', 'ROLEGUARANTEEROLE_READ'] } ]
              },
              {
                id: 'role-catalogue-roles',
                type: 'TAB',
                labelKey: 'content.role.catalogues.title',
                icon: 'component:role-catalogue',
                order: 140,
                path: '/role/:entityId/catalogues',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUEROLE_READ'] } ]
              },
              {
                id: 'role-authorization-policies',
                type: 'TAB',
                labelKey: 'content.role.authorization-policies.label',
                titleKey: 'content.role.authorization-policies.title',
                order: 200,
                icon: 'fa:shield-alt',
                path: '/role/:entityId/authorization-policies',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTHORIZATIONPOLICY_READ'] } ]
              },
              {
                id: 'role-automatic-roles',
                type: 'TAB',
                labelKey: 'content.role.tree-nodes.label',
                titleKey: 'content.role.tree-nodes.title',
                icon: 'component:automatic-roles',
                order: 400,
                path: '/role/:entityId/automatic-roles/trees',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLETREENODE_READ'] } ],
                items: [
                  {
                    id: 'role-automatic-role-tree',
                    labelKey: 'content.automaticRoles.tree.title',
                    order: 10,
                    path: '/role/:entityId/automatic-roles/trees',
                    icon: '',
                    type: 'TAB',
                    access: [
                      {
                        type: 'HAS_ANY_AUTHORITY',
                        authorities: ['ROLETREENODE_READ']
                      }
                    ]
                  },
                  {
                    id: 'role-automatic-role-attribute',
                    labelKey: 'content.automaticRoles.attribute.title',
                    order: 20,
                    path: '/role/:entityId/automatic-roles/attributes',
                    icon: '',
                    type: 'TAB',
                    access: [
                      {
                        type: 'HAS_ANY_AUTHORITY',
                        authorities: ['AUTOMATICROLEATTRIBUTE_READ']
                      }
                    ],
                    items: [
                      {
                        id: 'role-automatic-role-attribute-detail',
                        labelKey: 'content.automaticRoles.attribute.basic.title',
                        order: 10,
                        path: '/role/:entityId/automatic-roles/attributes/:automaticRoleId/detail',
                        icon: '',
                        type: 'TAB',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTOMATICROLEATTRIBUTE_READ'] } ]
                      },
                      {
                        id: 'role-automatic-role-attribute-rules',
                        labelKey: 'content.automaticRoles.attribute.rules.title',
                        order: 15,
                        path: '/role/:entityId/automatic-roles/attributes/:automaticRoleId/rules',
                        icon: '',
                        type: 'TAB',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTOMATICROLEATTRIBUTERULE_READ'] } ]
                      },
                      {
                        id: 'role-automatic-role-attribute-identities',
                        labelKey: 'content.automaticRoles.attribute.identities.title',
                        order: 20,
                        path: '/role/:entityId/automatic-roles/attributes/:automaticRoleId/identities',
                        icon: '',
                        type: 'TAB',
                        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ]
                      }
                    ]
                  }
                ]
              },
              {
                id: 'role-identities',
                type: 'TAB',
                labelKey: 'content.role.identities.title',
                order: 400,
                path: '/role/:entityId/identities',
                icon: 'component:identities',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ]
              },
              {
                id: 'role-audit',
                labelKey: 'content.audit.label',
                order: 500,
                path: '/role/:entityId/audit/detail',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUDIT_READ'] } ],
                icon: 'component:audit',
                items: [
                  {
                    id: 'role-audit-detail',
                    icon: '',
                    type: 'TAB',
                    labelKey: 'content.audit.label',
                    order: 100,
                    path: '/role/:entityId/audit/detail',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUDIT_READ'] } ]
                  },
                  {
                    id: 'role-audit-roles',
                    icon: '',
                    type: 'TAB',
                    labelKey: 'content.audit.identityRoles.label',
                    order: 200,
                    path: '/role/:entityId/audit/roles',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUDIT_READ'] } ]
                  }
                ]
              },
            ]
          },
          {
            id: 'request-roles',
            type: 'TAB',
            labelKey: 'content.roles.header',
            titleKey: 'content.roles.title',
            icon: 'component:request-roles',
            iconColor: '#eb9316',
            order: 20,
            path: '/requests/:requestId/roles',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ', 'AUTOMATICROLEATTRIBUTE_READ', 'ROLETREENODE_READ'] } ],
            items: [
              {
                id: 'request-role-detail',
                type: 'TAB',
                labelKey: 'content.roles.tabs.basic',
                order: 100,
                path: '/requests/:requestId/role/:entityId/detail',
                icon: 'component:basic-info'
              },
              {
                id: 'request-role-compositions',
                type: 'TAB',
                icon: 'component:business-roles',
                labelKey: 'content.role.compositions.title',
                order: 120,
                path: '/requests/:requestId/role/:entityId/compositions',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECOMPOSITION_READ'] } ]
              },
              {
                id: 'request-incompatible-roles',
                type: 'TAB',
                labelKey: 'content.role.incompatible-roles.header',
                titleKey: 'content.role.incompatible-roles.title',
                order: 125,
                icon: 'fa:times-circle',
                path: '/requests/:requestId/role/:entityId/incompatible-roles',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['INCOMPATIBLEROLE_READ'] } ]
              },
              {
                id: 'request-role-guarantees',
                type: 'TAB',
                labelKey: 'content.role.guarantees.title',
                icon: 'component:identities',
                order: 130,
                path: '/requests/:requestId/role/:entityId/guarantees',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEGUARANTEE_READ', 'ROLEGUARANTEEROLE_READ'] } ]
              },
              {
                id: 'request-role-form-attributes',
                type: 'TAB',
                labelKey: 'content.role.formAttributes.title',
                icon: 'fa:th-list',
                order: 115,
                path: '/requests/:requestId/role/:entityId/form-attributes',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEFORMATTRIBUTE_READ'] } ]
              },
              {
                id: 'request-role-catalogue-roles',
                type: 'TAB',
                labelKey: 'content.role.catalogues.title',
                icon: 'component:role-catalogue',
                order: 140,
                path: '/requests/:requestId/role/:entityId/catalogues',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUEROLE_READ'] } ]
              },
              {
                id: 'request-role-eav',
                type: 'TAB',
                labelKey: 'content.role.eav.title',
                order: 110,
                priority: 0,
                path: '/requests/:requestId/role/:entityId/eav',
                access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['ROLE_READ', 'FORMDEFINITION_AUTOCOMPLETE'] } ]
              },
              {
                id: 'request-role-authorization-policies',
                type: 'TAB',
                labelKey: 'content.role.authorization-policies.label',
                titleKey: 'content.role.authorization-policies.title',
                order: 200,
                icon: 'fa:shield-alt',
                path: '/requests/:requestId/role/:entityId/authorization-policies',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTHORIZATIONPOLICY_READ'] } ]
              },
              {
                id: 'request-role-automatic-roles',
                type: 'TAB',
                labelKey: 'content.role.tree-nodes.label',
                titleKey: 'content.role.tree-nodes.title',
                order: 400,
                path: '/requests/:requestId/role/:entityId/automatic-roles/trees',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLETREENODE_READ'] } ]
              }
            ]
          },
          {
            id: 'automatic-roles',
            labelKey: 'content.automaticRoles.header',
            titleKey: 'content.automaticRoles.title',
            icon: 'component:automatic-roles',
            order: 70,
            iconColor: '#428BCA',
            path: '/automatic-role/trees',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTOMATICROLEATTRIBUTE_READ', 'ROLETREENODE_READ'] } ],
            items: [
              {
                id: 'automatic-role-tree',
                labelKey: 'content.automaticRoles.tree.title',
                order: 10,
                path: '/automatic-role/trees',
                icon: '',
                type: 'TAB',
                access: [
                  {
                    type: 'HAS_ANY_AUTHORITY',
                    authorities: ['ROLETREENODE_READ']
                  }
                ]
              },
              {
                id: 'automatic-role-attribute',
                labelKey: 'content.automaticRoles.attribute.title',
                order: 20,
                path: '/automatic-role/attributes',
                icon: '',
                type: 'TAB',
                access: [
                  {
                    type: 'HAS_ANY_AUTHORITY',
                    authorities: ['AUTOMATICROLEATTRIBUTE_READ']
                  }
                ],
                items: [
                  {
                    id: 'automatic-role-attribute-detail',
                    labelKey: 'content.automaticRoles.attribute.basic.title',
                    order: 10,
                    path: '/automatic-role/attributes/:automaticRoleId',
                    icon: '',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTOMATICROLEATTRIBUTE_READ'] } ]
                  },
                  {
                    id: 'automatic-role-attribute-rules',
                    labelKey: 'content.automaticRoles.attribute.rules.title',
                    order: 15,
                    path: '/automatic-role/attributes/:automaticRoleId/rules',
                    icon: '',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTOMATICROLEATTRIBUTERULE_READ'] } ]
                  },
                  {
                    id: 'automatic-role-attribute-identities',
                    labelKey: 'content.automaticRoles.attribute.identities.title',
                    order: 20,
                    path: '/automatic-role/attributes/:automaticRoleId/identities',
                    icon: '',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ]
                  }
                ]
              }
            ]
          },
          {
            id: 'role-catalogues',
            labelKey: 'content.roleCatalogues.header',
            titleKey: 'content.roleCatalogues.title',
            icon: 'component:role-catalogue',
            iconColor: '#dad727',
            order: 100,
            path: '/role-catalogues',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUE_READ'] } ],
            items: [
              {
                id: 'role-catalogue-detail',
                type: 'TAB',
                labelKey: 'content.roleCatalogues.tabs.basic',
                order: 200,
                path: '/role-catalogue/:entityId/detail',
                icon: 'component:basic-info',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLECATALOGUE_READ'] } ]
              },
              {
                id: 'role-catalogue-catalogue-roles',
                type: 'TAB',
                labelKey: 'content.roleCatalogues.roles.title',
                icon: 'component:roles',
                order: 300,
                path: '/role-catalogue/:entityId/roles',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLE_READ'] } ]
              },
            ]
          }
        ]
      },
      {
        id: 'audit',
        labelKey: 'content.audit.title',
        titleKey: 'content.audit.title',
        icon: 'component:audit',
        path: '/audit/entities',
        order: 1900,
        items: [
          {
            id: 'workflow-historic-processes',
            labelKey: 'navigation.menu.workflow.historicProcess',
            order: 10,
            icon: 'fa:sitemap',
            iconColor: '#428BCA',
            path: '/workflow/history/processes',
          },
          {
            id: 'role-requests',
            labelKey: 'content.roleRequests.title',
            titleKey: 'content.roleRequests.header',
            icon: 'component:role-requests',
            order: 20,
            path: '/role-requests',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ROLEREQUEST_READ'] } ]
          },
          {
            id: 'requests',
            labelKey: 'content.requests.header',
            titleKey: 'content.requests.title',
            icon: 'fas:exchange-alt',
            order: 25,
            iconColor: '#419641',
            path: '/requests',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['REQUEST_READ'] } ]
          },
          {
            id: 'automatic-role-requests',
            labelKey: 'content.automaticRoles.header',
            titleKey: 'content.automaticRoles.title',
            icon: 'component:automatic-role-requests',
            order: 30,
            iconColor: '#428BCA',
            path: '/automatic-role-requests',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['AUTOMATICROLEREQUEST_READ'] } ]
          },
          {
            id: 'audits',
            labelKey: 'content.audit.title',
            order: 40,
            path: '/audit/entities',
            icon: 'component:audit',
            access: [
              {
                type: 'HAS_ANY_AUTHORITY',
                authorities: ['AUDIT_READ']
              }
            ],
            items: [
              {
                id: 'audit-identities',
                labelKey: 'content.audit.title-identities',
                order: 42,
                path: '/audit/identities',
                icon: '',
                type: 'TAB',
                access: [
                  {
                    type: 'HAS_ANY_AUTHORITY',
                    authorities: ['AUDIT_READ']
                  }
                ]
              },
              {
                id: 'audit-entities',
                labelKey: 'content.audit.title-entities',
                order: 40,
                path: '/audit/entities',
                icon: '',
                type: 'TAB',
                access: [
                  {
                    type: 'HAS_ANY_AUTHORITY',
                    authorities: ['AUDIT_READ']
                  }
                ]
              },
              {
                id: 'audit-identity-roles',
                labelKey: 'content.audit.title-identity-roles',
                order: 45,
                path: '/audit/identity-roles',
                icon: '',
                type: 'TAB',
                access: [
                  {
                    type: 'HAS_ANY_AUTHORITY',
                    authorities: ['AUDIT_READ']
                  }
                ]
              },
              {
                id: 'audit-identity-login',
                labelKey: 'content.audit.title-identity-login',
                order: 50,
                path: '/audit/identity-login',
                icon: '',
                type: 'TAB',
                access: [
                  {
                    type: 'HAS_ANY_AUTHORITY',
                    authorities: ['AUDIT_READ']
                  }
                ]
              },
              {
                id: 'audit-identity-password-change',
                labelKey: 'content.audit.title-identity-password-change',
                order: 55,
                path: '/audit/identity-password-change',
                icon: '',
                type: 'TAB',
                access: [
                  {
                    type: 'HAS_ANY_AUTHORITY',
                    authorities: ['AUDIT_READ']
                  }
                ]
              }
            ]
          },
          {
            id: 'audit-logging-events',
            labelKey: 'content.audit.logging-event.label',
            titleKey: 'content.audit.logging-event.title',
            icon: 'fa:history',
            iconColor: '#eb9316',
            order: 60,
            path: '/audit/logging-events',
            access: [
              {
                type: 'HAS_ANY_AUTHORITY',
                authorities: ['AUDIT_READ']
              }
            ]
          },
          {
            id: 'entity-events',
            labelKey: 'content.entityEvents.label',
            titleKey: 'content.entityEvents.title',
            order: 100,
            path: '/audit/entity-events',
            access: [
              {
                type: 'HAS_ANY_AUTHORITY',
                authorities: ['ENTITYEVENT_READ']
              }
            ]
          },
          {
            id: 'entity-states',
            labelKey: 'content.entityStates.label',
            titleKey: 'content.entityStates.title',
            order: 150,
            path: '/audit/entity-states',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['ENTITYSTATE_READ'] } ]
          }
        ]
      },
      {
        id: 'notification',
        labelKey: 'navigation.menu.notifications.label',
        titleKey: 'navigation.menu.notifications.title',
        icon: 'fa:envelope',
        order: 1910,
        path: '/notification/notifications',
        access: [
          {
            type: 'HAS_ANY_AUTHORITY',
            authorities: ['NOTIFICATION_READ']
          }
        ],
        items: [
          {
            id: 'notification-notifications',
            labelKey: 'content.notifications.label',
            titleKey: 'content.notifications.title',
            order: 30,
            path: '/notification/notifications',
            icon: 'fa:envelope',
            access: [
              {
                type: 'HAS_ANY_AUTHORITY',
                authorities: ['NOTIFICATION_READ']
              }
            ]
          },
          {
            id: 'notification-emails',
            labelKey: 'content.emails.title',
            order: 35,
            path: '/notification/emails',
            icon: 'far:envelope',
            access: [
              {
                type: 'HAS_ANY_AUTHORITY',
                authorities: ['NOTIFICATION_READ']
              }
            ]
          },
          {
            id: 'notification-websockets',
            labelKey: 'content.websockets.title',
            order: 100,
            path: '/notification/websockets',
            _deprecated: '@deprecated @since 9.2.0 websocket notification will be removed',
            access: [
              {
                type: 'DENY_ALL'
              }
            ]
          },
          {
            id: 'notification-sms',
            labelKey: 'content.sms.title',
            order: 200,
            path: '/notification/sms',
            icon: 'far:comment-alt',
            access: [
              {
                type: 'HAS_ANY_AUTHORITY',
                authorities: ['NOTIFICATION_READ']
              }
            ]
          },
          {
            id: 'notification-templates',
            labelKey: 'content.notificationTemplate.title',
            icon: 'fa:envelope-square',
            order: 900,
            path: '/notification/templates',
            access: [
              {
                type: 'HAS_ANY_AUTHORITY',
                authorities: ['NOTIFICATIONTEMPLATE_READ']
              }
            ]
          },
          {
            id: 'notification-configurations',
            labelKey: 'content.notificationConfigurations.label',
            titleKey: 'content.notificationConfigurations.title',
            order: 1000,
            path: '/notification/configurations',
            icon: 'component:setting',
            access: [
              {
                type: 'HAS_ANY_AUTHORITY',
                authorities: ['NOTIFICATIONCONFIGURATION_READ']
              }
            ]
          },
        ]
      },
      {
        id: 'system',
        labelKey: 'navigation.menu.system',
        titleKey: 'navigation.menu.system',
        icon: 'component:setting',
        order: 2000,
        path: '/configurations',
        iconColor: '#c12e2a',
        access: [
          {
            type: 'HAS_ANY_AUTHORITY',
            authorities: [
              'CONFIGURATION_READ', 'MODULE_READ', 'GENERATEVALUE_READ',
              'SCHEDULER_READ', 'FORMDEFINITION_READ', 'FORMPROJECTION_READ', 'PASSWORDPOLICY_READ', 'SCRIPT_READ',
              'CONFIDENTIALSTORAGEVALUE_READ', 'CODELIST_READ', 'EXPORTIMPORT_READ', 'MONITORING_READ', 'MONITORINGRESULT_READ'
            ]
          }
        ],
        items: [
          {
            id: 'system-configuration',
            labelKey: 'navigation.menu.configuration',
            icon: 'component:setting',
            order: 10,
            path: '/configurations',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CONFIGURATION_READ'] } ]
          },
          {
            id: 'monitoring',
            labelKey: 'navigation.menu.monitoring.label',
            icon: 'component:monitoring',
            order: 120,
            path: '/monitoring/monitoring-results',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['MONITORING_READ', 'MONITORINGRESULT_READ'] } ],
            items: [
              {
                id: 'monitoring-results',
                labelKey: 'content.monitoring-results.header',
                titleKey: 'content.monitoring-results.title',
                icon: 'component:monitoring-results',
                order: 10,
                path: '/monitoring/monitoring-results',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['MONITORINGRESULT_READ'] } ]
              },
              {
                id: 'monitorings',
                labelKey: 'content.monitorings.header',
                titleKey: 'content.monitorings.title',
                icon: 'component:monitoring',
                order: 420,
                path: '/monitoring/monitorings',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['MONITORING_READ'] } ],
                items: [
                  {
                    id: 'monitoring-detail',
                    labelKey: 'content.monitorings.header',
                    titleKey: 'content.monitorings.title',
                    order: 10,
                    path: '/monitoring/monitorings/:entityId/detail',
                    icon: 'component:basic-info',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['MONITORING_READ'] } ]
                  },
                  {
                    id: 'monitoring-detail-results',
                    type: 'TAB',
                    labelKey: 'content.monitoring-results.header',
                    titleKey: 'content.monitoring-results.title',
                    order: 40,
                    icon: 'component:monitoring-results',
                    path: '/monitoring/monitorings/:entityId/results',
                    access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['MONITORING_READ', 'MONITORINGRESULT_READ'] } ]
                  },
                ]
              }
            ]
          },
          {
            id: 'modules',
            labelKey: 'content.system.modules.title',
            order: 50,
            path: '/modules/be-modules',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['MODULE_READ'] } ],
            icon: 'fa:puzzle-piece',
            items: [
              {
                id: 'fe-modules',
                labelKey: 'content.system.fe-modules.title',
                order: 20,
                path: '/modules/fe-modules',
                icon: '',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['APP_ADMIN'] } ]
              },
              {
                id: 'be-modules',
                labelKey: 'content.system.be-modules.title',
                order: 10,
                path: '/modules/be-modules',
                icon: '',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['MODULE_READ'] } ]
              },
              {
                id: 'entity-event-processors',
                labelKey: 'content.system.entity-event-processors.title',
                order: 30,
                path: '/modules/entity-event-processors',
                icon: '',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['MODULE_READ'] } ]
              },
              {
                id: 'filter-builders',
                labelKey: 'content.system.filter-builders.title',
                order: 40,
                path: '/modules/filter-builders',
                icon: '',
                type: 'TAB',
                access: [{type: 'HAS_ANY_AUTHORITY', authorities: ['MODULE_READ']}]
              },
              {
                id: 'bulk-actions',
                labelKey: 'content.system.bulk-actions.title',
                order: 45,
                path: '/modules/bulk-actions',
                icon: '',
                type: 'TAB',
                access: [{type: 'HAS_ANY_AUTHORITY', authorities: ['MODULE_READ']}]
              },
              {
                id: 'available-services',
                labelKey: 'content.system.available-services.title',
                order: 50,
                path: '/modules/available-services',
                icon: '',
                type: 'TAB',
                access: [{type: 'IS_AUTHENTICATED'}]
              },
              {
                id: 'caches',
                labelKey: 'content.system.cache.title',
                order: 60,
                path: '/modules/caches',
                icon: '',
                type: 'TAB',
                access: [{type: 'HAS_ANY_AUTHORITY', authorities: ['APP_ADMIN']}]
              }
            ]
          },
          {
            id: 'scheduler',
            labelKey: 'content.scheduler.title',
            order: 15,
            path: '/scheduler/all-tasks',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCHEDULER_READ'] } ],
            icon: 'component:scheduled-tasks',
            items: [
              {
                id: 'scheduler-running-tasks',
                labelKey: 'content.scheduler.running-tasks.title',
                order: 10,
                path: '/scheduler/running-tasks',
                icon: '',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCHEDULER_READ'] } ]
              },
              {
                id: 'scheduler-schedule-tasks',
                labelKey: 'content.scheduler.schedule-tasks.title',
                order: 20,
                path: '/scheduler/schedule-tasks',
                icon: '',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCHEDULER_READ'] } ]
              },
              {
                id: 'scheduler-all-tasks',
                labelKey: 'content.scheduler.all-tasks.title',
                order: 30,
                path: '/scheduler/all-tasks',
                icon: '',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCHEDULER_READ'] } ],
                items: [
                  {
                    id: 'long-running-task-detail',
                    type: 'TAB',
                    labelKey: 'content.scheduler.all-tasks.tabs.basic',
                    order: 100,
                    path: '/scheduler/all-tasks/:entityId/detail',
                    icon: 'component:basic-info'
                  },
                  {
                    id: 'long-running-task-items',
                    labelKey: 'content.scheduler.all-tasks.tabs.items',
                    order: 200,
                    path: '/scheduler/all-tasks/:entityId/items',
                    type: 'TAB',
                    access: [
                      {
                        type: 'HAS_ANY_AUTHORITY',
                        authorities: ['SCHEDULER_READ']
                      }
                    ]
                  },
                  {
                    id: 'long-running-task-queue',
                    labelKey: 'content.scheduler.all-tasks.tabs.queue',
                    order: 300,
                    path: '/scheduler/all-tasks/:entityId/queue',
                    type: 'TAB',
                    access: [
                      {
                        type: 'HAS_ANY_AUTHORITY',
                        authorities: ['SCHEDULER_EXECUTE']
                      }
                    ]
                  },
                  {
                    id: 'long-running-task-audit',
                    labelKey: 'content.scheduler.all-tasks.tabs.audit',
                    order: 400,
                    path: '/scheduler/all-tasks/:entityId/audit',
                    type: 'TAB',
                    icon: 'component:audit',
                    access: [
                      {
                        type: 'DENY_ALL',
                        authorities: ['AUDIT_READ']
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            id: 'workflow-definitions',
            labelKey: 'navigation.menu.workflow.definitions',
            icon: 'fa:sitemap',
            order: 25,
            iconColor: '#428BCA',
            path: '/workflow/definitions',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['APP_ADMIN'] } ]
          },
          {
            id: 'password-policies',
            labelKey: 'content.passwordPolicies.header',
            titleKey: 'content.passwordPolicies.title',
            icon: 'component:password-policies',
            iconColor: '#ff0000',
            order: 60,
            path: '/password-policies',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDPOLICY_READ'] } ],
            items: [
              {
                id: 'password-policies-basic',
                labelKey: 'content.passwordPolicies.basic.title',
                order: 10,
                path: '/password-policies/:entityId',
                icon: 'component:basic-info',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDPOLICY_READ'] } ]
              },
              {
                id: 'password-policies-advanced',
                labelKey: 'content.passwordPolicies.advanced.title',
                order: 15,
                path: '/password-policies/:entityId/advanced',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDPOLICY_READ'] } ]
              },
              {
                id: 'password-policies-characters',
                labelKey: 'content.passwordPolicies.characters.title',
                order: 20,
                path: '/password-policies/:entityId/characters',
                icon: 'fa:language',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PASSWORDPOLICY_READ'] } ]
              }
            ]
          },
          {
            id: 'scripts',
            labelKey: 'content.scripts.header',
            titleKey: 'content.scripts.title',
            icon: 'component:scripts',
            iconColor: '#272fd8',
            order: 30,
            path: '/scripts',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCRIPT_READ'] } ],
            items: [
              {
                id: 'script-detail',
                type: 'TAB',
                labelKey: 'content.scripts.detail.title',
                order: 100,
                path: '/scripts/:entityId/detail',
                icon: '',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCRIPT_READ'] } ]
              },
              {
                id: 'script-authorities',
                type: 'TAB',
                labelKey: 'content.scripts.authorities.title',
                order: 110,
                path: '/scripts/:entityId/authorities',
                icon: '',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCRIPT_READ'] } ]
              },
              {
                id: 'script-references',
                type: 'TAB',
                labelKey: 'content.scripts.references.title',
                order: 120,
                path: '/scripts/:entityId/references',
                icon: '',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['SCRIPT_READ'] } ]
              }
            ]
          },
          {
            id: 'code-lists',
            labelKey: 'content.code-lists.header',
            titleKey: 'content.code-lists.title',
            order: 35,
            path: '/code-lists',
            icon: 'component:codelists',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CODELIST_READ'] } ],
            items: [
              {
                id: 'code-list-detail',
                labelKey: 'content.code-lists.detail.title',
                order: 10,
                path: '/code-lists/:entityId/detail',
                icon: 'component:basic-info',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CODELIST_READ'] } ]
              },
              {
                id: 'code-list-items',
                labelKey: 'content.code-lists.items.header',
                titleKey: 'content.code-lists.items.title',
                order: 100,
                path: '/code-lists/:entityId/items',
                icon: 'component:codelist-items',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['CODELISTITEM_READ'] } ]
              },
              {
                id: 'code-list-attributes',
                labelKey: 'content.code-lists.attributes.title',
                icon: 'component:form-attributes',
                order: 200,
                path: '/code-lists/:entityId/attributes',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMATTRIBUTE_READ'] } ]
              }
            ]
          },
          {
            id: 'forms',
            labelKey: 'content.formDefinitions.header',
            titleKey: 'content.formDefinitions.title',
            icon: 'component:form-definitions',
            order: 55,
            path: '/forms/form-definitions',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMDEFINITION_READ', 'FORMPROJECTION_READ'] } ],
            items: [
              {
                id: 'form-definitions',
                labelKey: 'content.formDefinitions.header',
                titleKey: 'content.formDefinitions.title',
                icon: 'component:form-definitions',
                order: 40,
                path: '/forms/form-definitions',
                type: 'TAB',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMDEFINITION_READ'] } ],
                items: [
                  {
                    id: 'forms-detail',
                    labelKey: 'content.formDefinitions.detail.title',
                    order: 10,
                    path: '/form-definitions/:entityId/detail',
                    icon: 'component:basic-info',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMDEFINITION_READ'] } ]
                  },
                  {
                    id: 'forms-attributes',
                    labelKey: 'content.formDefinitions.attributes.title',
                    order: 100,
                    path: '/form-definitions/:entityId/attributes',
                    icon: 'component:form-attributes',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMATTRIBUTE_READ'] } ],
                    items: [
                      {
                        id: 'forms-attribute-detail',
                        labelKey: 'content.formAttributes.detail.title',
                        order: 10,
                        path: '/form-definitions/attribute/:entityId/detail',
                        icon: 'component:basic-info',
                        type: 'TAB',
                        access: [{ type: 'HAS_ANY_AUTHORITY', authorities: ['FORMDEFINITION_READ'] }]
                      },
                      {
                        id: 'form-attribute-values',
                        labelKey: 'content.formAttributes.form-values.title',
                        order: 505,
                        path: '/form-definitions/attribute/:entityId/values',
                        icon: 'component:form-values',
                        type: 'TAB',
                        access: [{ type: 'HAS_ANY_AUTHORITY', authorities: ['FORMVALUE_READ'] }]
                      }
                    ]
                  },
                  {
                    id: 'form-definition-values',
                    labelKey: 'content.form-values.title',
                    order: 200,
                    path: '/form-definitions/:entityId/values',
                    icon: 'component:form-values',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMVALUE_READ'] } ]
                  },
                  {
                    id: 'forms-localization',
                    labelKey: 'content.formDefinitions.localization.label',
                    titleKey: 'content.formDefinitions.localization.title',
                    order: 900,
                    path: '/form-definitions/:entityId/localization',
                    icon: 'component:localization',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMDEFINITION_READ'] } ]
                  }
                ]
              },
              {
                id: 'form-projections',
                labelKey: 'content.form-projections.header',
                titleKey: 'content.form-projections.title',
                icon: 'component:form-projections',
                order: 45,
                type: 'TAB',
                path: '/forms/form-projections',
                access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMPROJECTION_READ'] } ],
                items: [
                  {
                    id: 'form-projection-detail',
                    labelKey: 'content.form-projections.detail.title',
                    order: 10,
                    path: '/form-projections/:entityId/detail',
                    icon: 'component:basic-info',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMPROJECTION_READ'] } ]
                  },
                  {
                    id: 'form-projection-identities',
                    type: 'TAB',
                    labelKey: 'content.form-projections.identities.label',
                    order: 40,
                    icon: 'component:identities',
                    path: '/form-projections/:entityId/identities',
                    access: [ { type: 'HAS_ALL_AUTHORITIES', authorities: ['FORMPROJECTION_READ', 'IDENTITY_READ'] } ]
                  },
                  {
                    id: 'form-projection-localization',
                    labelKey: 'content.form-projections.localization.title',
                    order: 900,
                    path: '/form-projections/:entityId/localization',
                    icon: 'component:localization',
                    type: 'TAB',
                    access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['FORMPROJECTION_READ'] } ]
                  }
                ]
              }
            ]
          },
          {
            id: 'generate-values',
            labelKey: 'content.generateValues.header',
            titleKey: 'content.generateValues.title',
            icon: 'random',
            iconColor: '#000000',
            order: 50,
            path: '/generate-values',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['GENERATEVALUE_READ'] } ],
          },
          {
            id: 'confidential-storage',
            labelKey: 'content.confidentialStorage.header',
            titleKey: 'content.confidentialStorage.title',
            icon: 'fa:lock',
            iconColor: '#272fd8',
            order: 70,
            path: '/confidential-storage',
            access: [ { type: 'DENY_ALL' } ],
          },
          {
            id: 'export-imports',
            labelKey: 'content.export-imports.header',
            titleKey: 'content.export-imports.title',
            icon: 'export',
            iconColor: '#000000',
            order: 400,
            path: '/export-imports',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['EXPORTIMPORT_READ'] } ],
          },
          {
            id: 'tokens',
            labelKey: 'content.tokens.header',
            titleKey: 'content.tokens.title',
            order: 500,
            path: '/tokens',
            access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['TOKEN_READ'] } ],
          }
        ]
      },
      {
        id: 'messages',
        section: 'system',
        titleKey: 'navigation.menu.messages',
        icon: 'fa:envelope',
        order: 20,
        path: '/messages'
      },
      {
        id: 'logout',
        section: 'system',
        titleKey: 'navigation.menu.logout',
        icon: 'fa:power-off',
        order: 100,
        path: '/logout'
      },
      {
        id: 'password-change',
        section: 'main',
        labelKey: 'content.password.change.title',
        order: 10,
        path: '/password/change',
        icon: false,
        access: [ { type: 'NOT_AUTHENTICATED' } ]
      },
      {
        id: 'identity-menu-detail',
        section: 'identity-menu',
        labelKey: 'component.advanced.IdentityInfo.link.detail.default.label',
        icon: 'fa:angle-double-right',
        order: 10,
        path: '/identity/:loggedUsername/profile',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_READ'] } ],
      },
      {
        id: 'identity-menu-password-change',
        section: 'identity-menu',
        labelKey: 'content.password.change.header',
        icon: 'component:password',
        order: 20,
        path: '/identity/:loggedUsername/password/change',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_PASSWORDCHANGE'] } ]
      },
      {
        id: 'identity-menu-profile-setting',
        section: 'identity-menu',
        labelKey: 'content.identity.profile-setting.header',
        icon: 'fa:cog',
        modal: 'profile-modal',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROFILE_READ'] } ],
        order: 900
      },
      {
        id: 'identity-menu-switch-user',
        section: 'identity-menu',
        labelKey: 'content.identity.switch-user.header',
        icon: 'component:switch-user',
        modal: 'switch-user-modal',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['IDENTITY_SWITCHUSER'] } ],
        order: 800
      },
      {
        id: 'identity-menu-logout-separator',
        section: 'identity-menu',
        type: 'SEPARATOR',
        access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['PROFILE_READ', 'IDENTITY_PASSWORDCHANGE', 'IDENTITY_READ'] } ],
        order: 990
      },
      {
        id: 'identity-menu-logout',
        section: 'identity-menu',
        labelKey: 'navigation.menu.logout',
        icon: 'off',
        order: 1000,
        path: '/logout'
      },
    ]
  }
};
