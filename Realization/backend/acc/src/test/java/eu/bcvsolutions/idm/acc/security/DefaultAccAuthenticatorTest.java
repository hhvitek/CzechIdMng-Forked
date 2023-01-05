package eu.bcvsolutions.idm.acc.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.config.domain.AuthenticatorConfiguration;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.security.authentication.impl.DefaultAccAuthenticator;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.IdentitySynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Default test for {@link DefaultAccAuthenticator} against new system.
 *
 * @author Svanda
 * @author Ondrej Kopr
 * @author Roman Kucera
 */
@SuppressWarnings("deprecation")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultAccAuthenticatorTest extends AbstractIntegrationTest {

	private static final String USERNAME = "test_authnetication_user";
	private static final String USERNAME_DIFFERENT_AUTH_ATTRIBUTE = "test_authnetication_user_different_auth";
	private static final String PASSWORD = "test1234";
	private static final String ROLE_NAME = "role";
	private static final String ROLE_NAME_DIFFERENT_AUTH_ATTRIBUTE = "roleDifferentAuthAttribute";

	@Autowired
	private TestHelper helper;

	@Autowired
	private SysSystemService sysSystemService;

	@Autowired
	private SysSystemMappingService systemEntityHandlingService;

	@Autowired
	private SysSystemAttributeMappingService schemaAttributeHandlingService;

	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	private SysSchemaAttributeService schemaAttributeService;

	@Autowired
	private IdmRoleService roleService;

	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private IdmIdentityContractService identityContractService;

	@Autowired
	private SysRoleSystemService roleSystemService;

	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;

	@Autowired
	private ProvisioningService provisioningService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private AccIdentityAccountService identityAccountService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	@Override
	public void logout() {
		super.logout();
	}

	@Test
	public void A_loginAgainstSystem() {
		initData();
		IdmIdentityDto identity = identityService.getByUsername(USERNAME);
		IdmRoleDto role = roleService.getByCode(ROLE_NAME);

		logInAgainstSystem(identity, role, USERNAME);
	}

	@Test
	public void loginAgainstTwoAccount() {
		IdmIdentityDto identity = identityService.getByUsername(USERNAME);

		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();

		// get account distinct for identityAccounts
		List<String> accountIds = new ArrayList<>();
		for (AccIdentityAccountDto identityAccount : identityAccounts) {
			if (!accountIds.contains(identityAccount.getAccount().toString())) {
				accountIds.add(identityAccount.getAccount().toString());
			}
		}

		assertEquals(1, accountIds.size());
		assertEquals(1, identityAccounts.size());

		IdmRoleDto role2 = roleService.getByCode(ROLE_NAME + "2");

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(role2.getId());

		irdto = identityRoleService.save(irdto);

		identityAccounts = identityAccountService.find(filter, null).getContent();

		// get account distinct for identityAccounts
		accountIds = new ArrayList<>();
		for (AccIdentityAccountDto identityAccount : identityAccounts) {
			if (!accountIds.contains(identityAccount.getAccount().toString())) {
				accountIds.add(identityAccount.getAccount().toString());
			}
		}

		assertEquals(2, accountIds.size());
		assertEquals(2, identityAccounts.size());

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		List<String> accs = new ArrayList<>();
		accs.add(accountIds.get(0));
		passwordChangeDto.setAccounts(accs);
		passwordChangeDto.setAll(false);
		passwordChangeDto.setNewPassword(new GuardedString("1234"));
		// change password for system
		provisioningService.changePassword(identity, passwordChangeDto);

		passwordChangeDto = new PasswordChangeDto();
		accs = new ArrayList<>();
		accs.add(accountIds.get(1));
		passwordChangeDto.setAccounts(accs);
		passwordChangeDto.setAll(false);
		passwordChangeDto.setNewPassword(new GuardedString("4321"));
		// change password for system
		provisioningService.changePassword(identity, passwordChangeDto);

		// bough password are right
		LoginDto loginDto1 = new LoginDto();
		loginDto1.setUsername(USERNAME);
		loginDto1.setPassword(new GuardedString("1234"));
		loginDto1 = authenticationManager.authenticate(loginDto1);

		LoginDto loginDto2 = new LoginDto();
		loginDto2.setUsername(USERNAME);
		loginDto2.setPassword(new GuardedString("4321"));
		loginDto2 = authenticationManager.authenticate(loginDto2);

		assertNotNull(loginDto2);
		assertNotNull(loginDto2.getAuthentication());
		assertEquals("acc", loginDto2.getAuthenticationModule());

		assertNotNull(loginDto1);
		assertNotNull(loginDto1.getAuthentication());
		assertEquals("acc", loginDto1.getAuthenticationModule());
	}

	@Test
	public void loginAgainstIdm() {
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername("test_login_1");
		identity.setLastName("test_login_1");
		identity.setPassword(new GuardedString("test1234"));
		identity = identityService.save(identity);

		LoginDto loginDto = new LoginDto();
		loginDto.setPassword(new GuardedString("test1234"));
		loginDto.setUsername("test_login_1");

		loginDto = authenticationManager.authenticate(loginDto);
		assertNotNull(loginDto);
		assertNotNull(loginDto.getAuthentication());
		assertEquals("core", loginDto.getAuthenticationModule());
	}

	@Test(expected = IdmAuthenticationException.class)
	public void loginViaManagerBadCredentials() {
		IdmIdentityDto identity = identityService.getByUsername(USERNAME);

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAll(true);
		passwordChangeDto.setIdm(false);
		passwordChangeDto.setNewPassword(new GuardedString(PASSWORD));
		// change password for system
		provisioningService.changePassword(identity, passwordChangeDto);

		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(USERNAME);
		loginDto.setPassword(new GuardedString("test"));
		authenticationManager.authenticate(loginDto);
	}

	@Test
	public void testDifferentAuthenticationAttribute() {
		initDataDifferentAuthAttribute();
		IdmIdentityDto identity = identityService.getByUsername(USERNAME_DIFFERENT_AUTH_ATTRIBUTE);
		IdmRoleDto role = roleService.getByCode(ROLE_NAME_DIFFERENT_AUTH_ATTRIBUTE);

		logInAgainstSystem(identity, role, USERNAME_DIFFERENT_AUTH_ATTRIBUTE);
	}

	private void logInAgainstSystem(IdmIdentityDto identity, IdmRoleDto role, String username) {
		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(role.getId());
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		irdto = identityRoleService.save(irdto);
		//
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> accounts = identityAccountService.find(filter, null).getContent();

		assertEquals(1, accounts.size());

		List<String> accs = new ArrayList<>();
		accs.add(accounts.get(0).getId().toString());

		PasswordChangeDto passwordChangeDto = new PasswordChangeDto();
		passwordChangeDto.setAccounts(accs);
		passwordChangeDto.setAll(true);
		passwordChangeDto.setNewPassword(new GuardedString("test"));
		// change password for system
		provisioningService.changePassword(identity, passwordChangeDto);

		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(username);
		loginDto.setPassword(new GuardedString("test"));
		loginDto = authenticationManager.authenticate(loginDto);
		//
		assertNotNull(loginDto);
		assertNotNull(loginDto.getAuthentication());
		assertEquals("acc", loginDto.getAuthenticationModule());
	}

	private void initData() {
		SysSystemDto system = createTestSystem();
		List<SysSchemaObjectClassDto> objectClasses = sysSystemService.generateSchema(system);

		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(USERNAME);
		identity.setLastName(USERNAME);
		identity.setPassword(new GuardedString(PASSWORD));
		identity = identityService.save(identity);

		// Create mapped attributes to schema
		SysSystemMappingDto systemMapping = new SysSystemMappingDto();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0).getId());
		systemMapping.setAccountType(AccountType.PERSONAL);
		final SysSystemMappingDto entityHandlingResult = systemEntityHandlingService.save(systemMapping);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		SysSystemAttributeMappingDto attributeHandlingLastName = new SysSystemAttributeMappingDto();
		SysSystemAttributeMappingDto attributeHandlingPassword = new SysSystemAttributeMappingDto();
		SysSystemAttributeMappingDto attributeHandlingUsername = new SysSystemAttributeMappingDto();

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		for (SysSchemaAttributeDto schemaAttr : schemaAttributesPage) {
			if ("__NAME__".equals(schemaAttr.getName())) {
				attributeHandlingUsername.setUid(true);
				attributeHandlingUsername.setEntityAttribute(true);
				attributeHandlingUsername.setAuthenticationAttribute(true);
				attributeHandlingUsername.setIdmPropertyName("username");
				attributeHandlingUsername.setTransformToResourceScript("if(attributeValue){return \"x\"+ attributeValue;}");
				attributeHandlingUsername.setName(schemaAttr.getName());
				attributeHandlingUsername.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingUsername.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingUsername = schemaAttributeHandlingService.save(attributeHandlingUsername);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				attributeHandlingLastName.setIdmPropertyName("lastName");
				attributeHandlingLastName.setName(schemaAttr.getName());
				attributeHandlingLastName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingLastName.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingLastName = schemaAttributeHandlingService.save(attributeHandlingLastName);

			} else if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				attributeHandlingPassword.setIdmPropertyName("password");
				attributeHandlingPassword.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingPassword.setName(schemaAttr.getName());
				attributeHandlingPassword.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingPassword = schemaAttributeHandlingService.save(attributeHandlingPassword);

			}
		}

		// create two roles with same system and different override username
		IdmRoleDto role1 = getHelper().createRole(ROLE_NAME);
		SysRoleSystemDto role1System = new SysRoleSystemDto();
		role1System.setRole(role1.getId());
		role1System.setSystem(system.getId());
		role1System.setSystemMapping(entityHandlingResult.getId());
		role1System = roleSystemService.save(role1System);


		IdmRoleDto role2 = getHelper().createRole(ROLE_NAME + "2");
		role2 = roleService.save(role2);
		SysRoleSystemDto roleSystem2 = new SysRoleSystemDto();
		roleSystem2.setSystem(system.getId());
		roleSystem2.setSystemMapping(entityHandlingResult.getId());
		roleSystem2.setRole(role2.getId());
		roleSystem2 = roleSystemService.save(roleSystem2);

		SysRoleSystemAttributeDto overloadedRole2 = new SysRoleSystemAttributeDto();
		overloadedRole2.setSystemAttributeMapping(attributeHandlingUsername.getId());
		overloadedRole2.setUid(true);
		overloadedRole2.setEntityAttribute(true);
		overloadedRole2.setTransformScript("return \"z" + USERNAME + "\";");
		overloadedRole2.setIdmPropertyName("username");
		overloadedRole2.setName("username");
		overloadedRole2.setRoleSystem(roleSystem2.getId());

		overloadedRole2 = roleSystemAttributeService.save(overloadedRole2);
	}

	private void initDataDifferentAuthAttribute() {
		SysSystemDto system = createTestSystem();
		List<SysSchemaObjectClassDto> objectClasses = sysSystemService.generateSchema(system);

		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(USERNAME_DIFFERENT_AUTH_ATTRIBUTE);
		identity.setLastName(USERNAME_DIFFERENT_AUTH_ATTRIBUTE);
		identity.setPassword(new GuardedString(PASSWORD));
		identity = identityService.save(identity);

		// Create mapped attributes to schema
		SysSystemMappingDto systemMapping = new SysSystemMappingDto();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setObjectClass(objectClasses.get(0).getId());
		systemMapping.setAccountType(AccountType.PERSONAL);
		final SysSystemMappingDto entityHandlingResult = systemEntityHandlingService.save(systemMapping);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		SysSystemAttributeMappingDto attributeHandlingLastName = new SysSystemAttributeMappingDto();
		SysSystemAttributeMappingDto attributeHandlingPassword = new SysSystemAttributeMappingDto();
		SysSystemAttributeMappingDto attributeHandlingUsername = new SysSystemAttributeMappingDto();

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		for (SysSchemaAttributeDto schemaAttr : schemaAttributesPage) {
			if ("__NAME__".equals(schemaAttr.getName())) {
				attributeHandlingUsername.setAuthenticationAttribute(true);
				attributeHandlingUsername.setEntityAttribute(true);
				attributeHandlingUsername.setIdmPropertyName("username");
				attributeHandlingUsername.setTransformToResourceScript("if(attributeValue){return \"x\"+ attributeValue;}");
				attributeHandlingUsername.setName(schemaAttr.getName());
				attributeHandlingUsername.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingUsername.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingUsername = schemaAttributeHandlingService.save(attributeHandlingUsername);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				attributeHandlingLastName.setUid(true);
				attributeHandlingLastName.setEntityAttribute(true);
				attributeHandlingLastName.setIdmPropertyName("lastName");
				attributeHandlingLastName.setName(schemaAttr.getName());
				attributeHandlingLastName.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingLastName.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingLastName = schemaAttributeHandlingService.save(attributeHandlingLastName);

			} else if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equalsIgnoreCase(schemaAttr.getName())) {
				attributeHandlingPassword.setIdmPropertyName("password");
				attributeHandlingPassword.setSchemaAttribute(schemaAttr.getId());
				attributeHandlingPassword.setName(schemaAttr.getName());
				attributeHandlingPassword.setSystemMapping(entityHandlingResult.getId());
				attributeHandlingPassword = schemaAttributeHandlingService.save(attributeHandlingPassword);

			}
		}

		// create one role for system
		IdmRoleDto role1 = getHelper().createRole(ROLE_NAME_DIFFERENT_AUTH_ATTRIBUTE);
		SysRoleSystemDto role1System = new SysRoleSystemDto();
		role1System.setRole(role1.getId());
		role1System.setSystem(system.getId());
		role1System.setSystemMapping(entityHandlingResult.getId());
		role1System = roleSystemService.save(role1System);
	}

	private SysSystemDto createTestSystem() {
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		system = sysSystemService.save(system);

		// set system id to application property
		configurationService.setValue(AuthenticatorConfiguration.PROPERTY_AUTH_SYSTEM_ID, system.getId().toString());
		return system;
	}
}
