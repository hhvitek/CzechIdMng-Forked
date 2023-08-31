package eu.bcvsolutions.idm.vs.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.SysValueChangeType;
import eu.bcvsolutions.idm.acc.dto.SysAttributeDifferenceDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.filter.api.IcResultsHandler;
import eu.bcvsolutions.idm.ic.impl.IcObjectClassImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import eu.bcvsolutions.idm.vs.TestHelper;
import eu.bcvsolutions.idm.vs.VirtualSystemModuleDescriptor;
import eu.bcvsolutions.idm.vs.connector.basic.BasicVirtualConfiguration;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.VsAccountDto;
import eu.bcvsolutions.idm.vs.dto.VsConnectorObjectDto;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.dto.VsSystemDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.dto.filter.VsSystemImplementerFilter;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.entity.VsRequest;
import eu.bcvsolutions.idm.vs.evaluator.VsRequestByImplementerEvaluator;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;
import java.io.Serializable;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Virtual system request test
 * + request filters
 *
 * @author Svanda
 * @author Patrik Stloukal
 * @author Ondrej Husnik
 */
public class DefaultVsRequestServiceIntegrationTest extends AbstractIntegrationTest {

	private static final String USER_ONE_NAME = "vsUserOne";
	private static final String USER_IMPLEMENTER_NAME = "vsUserImplementer";
	private static final String ROLE_ONE_NAME = "vsRoleOne";
	private static final String USER_ONE_CHANGED_NAME = "vsUserOneChanged";

	@Autowired
	private TestHelper helper;
	@Autowired
	private VsRequestService requestService;
	@Autowired
	private VsAccountService accountService;
	@Autowired
	private FormService formService;
	@Autowired
	private LoginService loginService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private IdmFormAttributeService formAttributeService;
	@Autowired
	private SysSystemEntityService systemEntityService;
	@Autowired
	private IcConnectorFacade connectorFacade;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	@Autowired
	private RoleConfiguration roleConfiguration;
	@Autowired
	private VsSystemImplementerService systemImplementerService;
	@Autowired
	private AccAccountService accAccountService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		this.deleteAll(USER_ONE_NAME, USER_ONE_CHANGED_NAME, USER_IMPLEMENTER_NAME, ROLE_ONE_NAME);
		super.logout();
	}

	@Test
	public void createAndRealizeRequestTest() {

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		UUID requestId = request.getId();
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try realize the request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		request = requestService.realize(request);
		Assert.assertEquals(VsRequestState.REALIZED, request.getState());
		account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNotNull("Account cannot be null, because request was realized!", account);

		// test referential integrity - delete system 
		String virtualSystemKey = MessageFormat.format("{0}:systemId={1}", system.getConnectorKey().getFullName(), system.getId()); // TODO: move to api
		Assert.assertNotNull(formService.getDefinition(VsAccount.class, virtualSystemKey));
		VsSystemImplementerFilter implementerFilter = new VsSystemImplementerFilter();
		implementerFilter.setSystemId(system.getId());
		Assert.assertNotEquals(0, systemImplementerService.count(implementerFilter));
		// clean up acc accounts
		AccAccountFilter accAccountFilter = new AccAccountFilter();
		accAccountFilter.setSystemId(system.getId());
		accAccountService.find(accAccountFilter, null).forEach(a -> {
			accAccountService.delete(a);
		});
		requestService.find(requestFilter, null).forEach(r -> {
			// delete account creates request in progress
			if (!r.getId().equals(requestId)) {
				requestService.delete(r);
			}
		});
		Assert.assertNotNull(requestService.get(request));
		systemService.delete(system);
		Assert.assertNull(formService.getDefinition(VsAccount.class, virtualSystemKey));
		Assert.assertNull(requestService.get(request));
		Assert.assertNull(accountService.get(account));
		Assert.assertEquals(0, systemImplementerService.count(implementerFilter));
	}

	@Test
	public void disableRequestTest() {

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		IdmIdentityDto identity = helper.createIdentity(USER_ONE_NAME);
		this.assignRoleSystem(system, identity, ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try realize the request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		request = requestService.realize(request);
		Assert.assertEquals(VsRequestState.REALIZED, request.getState());
		account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNotNull("Account cannot be null, because request was realized!", account);
		Assert.assertEquals(Boolean.TRUE, account.isEnable());

		super.logout();
		loginAsAdmin();
		// Disable the identity
		identity.setState(IdentityState.DISABLED_MANUALLY);
		identityService.save(identity);

		// Find created requests
		requests = requestService.find(requestFilter, null).getContent().stream()
				.filter(r -> VsRequestState.IN_PROGRESS == r.getState()).collect(Collectors.toList());
		Assert.assertEquals(1, requests.size());
		request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.UPDATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		// We try realize the request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		request = requestService.realize(request);
		Assert.assertEquals(VsRequestState.REALIZED, request.getState());
		account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNotNull("Account cannot be null, because request was realized!", account);
		Assert.assertEquals(Boolean.FALSE, account.isEnable());
	}

	@Test
	public void systemAccountFilterTest() {

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try realize the request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		request = requestService.realize(request);
		Assert.assertEquals(VsRequestState.REALIZED, request.getState());
		account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNotNull("Account cannot be null, because request was realized!", account);

		IcConnectorConfiguration configuration = systemService.getConnectorConfiguration(system);
		IcObjectClass objectClass = new IcObjectClassImpl("__ACCOUNT__");
		List<String> uids = new ArrayList<>();
		connectorFacade.search(systemService.getConnectorInstance(system), configuration, objectClass, null, new IcResultsHandler() {

			@Override
			public boolean handle(IcConnectorObject connectorObject) {
				uids.add(connectorObject.getUidValue());
				return true;
			}
		});
		Assert.assertEquals(1, uids.size());
		Assert.assertEquals(USER_ONE_NAME, uids.get(0));
	}

	@Test
	public void createAndCancelRequestTest() {
		String reason = "cancel \"request\" reason!";

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try cancel the request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		request = requestService.cancel(request, reason);
		Assert.assertEquals(VsRequestState.CANCELED, request.getState());
		Assert.assertEquals(reason, request.getReason());
		account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was canceled!", account);
	}

	@Test(expected = ForbiddenEntityException.class)
	public void realizeRequestWithouRightTest() {
		String reason = "cancel \"request\" reason!";

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try cancel the request
		super.logout();
		loginService.login(new LoginDto(USER_ONE_NAME, new GuardedString("password")));
		request = requestService.cancel(request, reason);
	}

	@Test
	public void createMoreRequestsTest() {
		String changed = "changed";

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);

		IdmIdentityDto userOne = identityService.getByUsername(USER_ONE_NAME);
		userOne.setFirstName(changed);
		userOne.setLastName(changed);
		identityService.save(userOne);
		// Duplicated save ... not invoke provisioning
		identityService.save(userOne);

		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(2, requests.size());
		VsRequestDto changeRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.UPDATE == req.getOperationType())
				.findFirst().orElse(null);
		Assert.assertNotNull("Request with change not found!", changeRequest);
	}

	@Test
	public void realizeUpdateAndDeleteRequestsTest() {
		String changed = "changed";

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);

		IdmIdentityDto userOne = identityService.getByUsername(USER_ONE_NAME);
		userOne.setFirstName(changed);
		userOne.setLastName(changed);
		identityService.save(userOne);
		// Delete identity
		identityService.delete(userOne);

		// Test read rights (none requests can be returned for UserOne)
		IdmIdentityDto userTwo = helper.createIdentity("vsUserTwo");
		super.logout();
		loginService.login(new LoginDto(userTwo.getUsername(), new GuardedString("password")));
		requests = requestService.find(requestFilter, null, IdmBasePermission.READ).getContent();
		Assert.assertEquals("We found request without correct rights!", 0, requests.size());

		// Test read rights (3 requests must be returned for UserImplementer)
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		requests = requestService.find(requestFilter, null, IdmBasePermission.READ).getContent();
		Assert.assertEquals(3, requests.size());
		VsRequestDto changeRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.UPDATE == req.getOperationType())
				.findFirst().orElse(null);
		Assert.assertNotNull("Request with change not found!", changeRequest);
		VsRequestDto deleteRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.DELETE == req.getOperationType())
				.findFirst().orElse(null);
		Assert.assertNotNull("Request with delete not found!", deleteRequest);
		VsRequestDto createRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.CREATE == req.getOperationType())
				.findFirst().orElse(null);
		Assert.assertNotNull("Request with create not found!", createRequest);

		// Realize create request
		request = requestService.realize(createRequest);
		// Realize update request
		request = requestService.realize(changeRequest);
		// Realize delete request
		request = requestService.realize(deleteRequest);

		// Find only archived
		requestFilter.setOnlyArchived(Boolean.TRUE);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(3, requests.size());
		boolean foundNotRealized = requests.stream().filter(req -> VsRequestState.REALIZED != req.getState())
				.findFirst().isPresent();
		Assert.assertTrue("Found not realized requests!", !foundNotRealized);
		
		//Delete
		identityService.delete(userTwo);
	}
	
	
	/**
	 * Relation on identity (target entity) after delete account must exists in vs-request.
	 */
	@Test
	public void realizeRequestOwnereRelationAfterDeleteTest() {
		String changed = "changed";

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		requestFilter.setIncludeOwner(true);
		
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		assertEquals(USER_ONE_NAME, request.getUid());
		assertEquals(VsOperationType.CREATE, request.getOperationType());
		assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);

		IdmIdentityDto userOne = identityService.getByUsername(USER_ONE_NAME);
		userOne.setFirstName(changed);
		userOne.setLastName(changed);
		identityService.save(userOne);
		
		// Delete identity roles for userOne.
		IdmRoleRequestDto roleRequestDelete = roleRequestService.createRequest(helper.getPrimeContract(userOne.getId()));
		identityRoleService.findAllByIdentity(userOne.getId()).forEach(identityRole -> {
			roleRequestService.createConcept(roleRequestDelete, 
					helper.getPrimeContract(userOne.getId()), identityRole.getId(), identityRole.getRole(), ConceptRoleRequestOperation.REMOVE);
		});
		this.getHelper().executeRequest(roleRequestDelete, false, true);
		
		requests = requestService.find(requestFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(3, requests.size());
		VsRequestDto changeRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.UPDATE == req.getOperationType())
				.findFirst().orElse(null);
		assertNotNull("Request with change not found!", changeRequest);
		VsRequestDto deleteRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.DELETE == req.getOperationType())
				.findFirst().orElse(null);
		assertNotNull("Request with delete not found!", deleteRequest);
		
		VsRequestDto createRequest = requests.stream().filter(
				req -> VsRequestState.IN_PROGRESS == req.getState() && VsOperationType.CREATE == req.getOperationType())
				.findFirst().orElse(null);
		assertNotNull("Request with create not found!", createRequest);
		
		// Target entity must exists.
		assertNotNull(deleteRequest.getTargetEntity());
		assertNotNull(deleteRequest.getTargetEntityType());
		assertEquals(userOne.getId(), deleteRequest.getTargetEntity().getId());

		// Realize create request
		request = requestService.realize(createRequest);
		// Realize update request
		request = requestService.realize(changeRequest);
		// Realize delete request
		request = requestService.realize(deleteRequest);

		// Find only archived
		requestFilter.setOnlyArchived(Boolean.TRUE);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(3, requests.size());
		boolean foundNotRealized = requests.stream().anyMatch(req -> VsRequestState.REALIZED != req.getState());
		Assert.assertFalse("Found not realized requests!", foundNotRealized);

		//Delete
		identityService.delete(userOne);
	}

	@Test
	public void checkMultivalueInWishObjectTest() {
		String ldapGroupsName = "ldapGroups";
		String changed = "changed";
		List<String> attributes = new ArrayList<>(Lists.newArrayList(BasicVirtualConfiguration.DEFAULT_ATTRIBUTES));
		attributes.add(ldapGroupsName);

		// Create virtual system with extra attribute (ldapGroups)
		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, attributes);

		// Search attribute definition for ldapGroups and set him to multivalue
		String virtualSystemKey = MessageFormat.format("{0}:systemId={1}", system.getConnectorKey().getFullName(),
				system.getId().toString());
		String type = VsAccount.class.getName();
		IdmFormDefinitionDto definition = this.formService.getDefinition(type, virtualSystemKey);
		IdmFormAttributeDto ldapGroupsFormAttr = formAttributeService.findAttribute(VsAccount.class.getName(),
				definition.getCode(), ldapGroupsName);
		Assert.assertNotNull("Ldap attribute muste exist!", ldapGroupsFormAttr);
		ldapGroupsFormAttr.setMultiple(true);
		// Change the name of this attribute. We want to check that logic no depends on the attribute name.
		ldapGroupsFormAttr.setName(helper.createName());
		formService.saveAttribute(ldapGroupsFormAttr);

		// Generate schema for system (we need propagate multivalue setting)
		SysSchemaObjectClassDto schema = systemService.generateSchema(system).get(0);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null)
				.getContent();
		SysSystemMappingFilter systemMappingFilter = new SysSystemMappingFilter();
		systemMappingFilter.setSystemId(system.getId());
		systemMappingFilter.setObjectClassId(schema.getId());
		SysSystemMappingDto mapping = systemMappingService.find(systemMappingFilter, null).getContent().get(0);
		for (SysSchemaAttributeDto schemaAttr : schemaAttributes) {
			if (ldapGroupsName.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(false);
				attributeMapping.setEntityAttribute(false);
				attributeMapping.setExtendedAttribute(true);
				attributeMapping.setIdmPropertyName(ldapGroupsName);
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(mapping.getId());
				systemAttributeMappingService.save(attributeMapping);
			}
		}
		IdmIdentityDto userOne = helper.createIdentity(USER_ONE_NAME);
		List<Serializable> initList = ImmutableList.of("TEST1", "TEST2", "TEST3");
		formService.saveValues(userOne, ldapGroupsName, initList);

		this.assignRoleSystem(system, userOne, ROLE_ONE_NAME);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto createRequest = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, createRequest.getUid());
		Assert.assertEquals(VsOperationType.CREATE, createRequest.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, createRequest.getState());

		VsConnectorObjectDto wish = requestService.getWishConnectorObject(createRequest);
		boolean findAttributeWithouChange = wish.getAttributes().stream().filter(attribute -> !attribute.isChanged())
				.findFirst().isPresent();
		Assert.assertTrue(!findAttributeWithouChange);

		// Check on exist ldapGroups attribute with three values
		SysAttributeDifferenceDto ldapGroupAttribute = wish.getAttributes().stream()
				.filter(attribute -> ldapGroupsName.equals(attribute.getName())).findFirst().get();
		Assert.assertTrue(ldapGroupAttribute.isMultivalue());
		Assert.assertEquals(3, ldapGroupAttribute.getValues().size());

		// Change multivalue attribute
		List<Serializable> changeList = ImmutableList.of("TEST1", changed, "TEST3");
		formService.saveValues(userOne, ldapGroupsName, changeList);
		// Invoke provisioning
		identityService.save(userOne);

		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(2, requests.size());
		VsRequestDto changeRequest = requests.stream().filter(req -> VsOperationType.UPDATE == req.getOperationType())
				.findFirst().get();
		wish = requestService.getWishConnectorObject(changeRequest);
		ldapGroupAttribute = wish.getAttributes().stream()
				.filter(attribute -> ldapGroupsName.equals(attribute.getName())).findFirst().get();
		Assert.assertTrue(ldapGroupAttribute.isMultivalue());
		// Wish must contains three values (all add) ... because previous create
		// request is not realize yet. Wish show changes versus reals state in
		// VsAccount.
		Assert.assertEquals(3, ldapGroupAttribute.getValues().size());

		// We realize the create request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		requestService.realize(createRequest);
		// Refresh wish
		wish = requestService.getWishConnectorObject(changeRequest);
		ldapGroupAttribute = wish.getAttributes().stream()
				.filter(attribute -> ldapGroupsName.equals(attribute.getName())).findFirst().get();
		Assert.assertTrue(ldapGroupAttribute.isMultivalue());
		// Wish must contains four values ... two without change, one delete and
		// one add value
		Assert.assertEquals(4, ldapGroupAttribute.getValues().size());

		// Find unchanged value
		boolean findCorrectTest1Value = ldapGroupAttribute
				.getValues().stream().filter(value -> value.getValue().equals(initList.get(0))
						&& value.getOldValue().equals(initList.get(0)) && value.getChange() == null)
				.findFirst().isPresent();
		Assert.assertTrue(findCorrectTest1Value);

		// Find deleted value
		boolean findCorrectDeletedTest2Value = ldapGroupAttribute.getValues().stream()
				.filter(value -> value.getValue().equals(initList.get(1)) && value.getOldValue().equals(initList.get(1))
						&& SysValueChangeType.REMOVED == value.getChange())
				.findFirst().isPresent();
		Assert.assertTrue(findCorrectDeletedTest2Value);

		// Find added value
		boolean findCorrectCreatedChangedValue = ldapGroupAttribute.getValues().stream()
				.filter(value -> value.getValue().equals(changed) && value.getOldValue() == null
						&& SysValueChangeType.ADDED == value.getChange())
				.findFirst().isPresent();
		Assert.assertTrue(findCorrectCreatedChangedValue);
	}

	@Test
	public void checkSinglevalueInWishObjectTest() {
		String changed = "changed";
		String firstName = "firstName";
		String lastName = "lastName";
		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);

		IdmIdentityDto userOne = helper.createIdentity(USER_ONE_NAME);
		userOne.setFirstName(firstName);
		userOne.setLastName(lastName);
		identityService.save(userOne);

		this.assignRoleSystem(system, userOne, ROLE_ONE_NAME);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		requestFilter.setState(VsRequestState.IN_PROGRESS);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto createRequest = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, createRequest.getUid());
		Assert.assertEquals(VsOperationType.CREATE, createRequest.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, createRequest.getState());

		VsConnectorObjectDto wish = requestService.getWishConnectorObject(createRequest);
		boolean findAttributeWithouChange = wish.getAttributes().stream().filter(attribute -> !attribute.isChanged())
				.findFirst().isPresent();
		Assert.assertTrue(!findAttributeWithouChange);

		// Change singlevalue attributes
		userOne.setFirstName(changed);
		userOne.setLastName(changed);
		// Invoke provisioning
		identityService.save(userOne);

		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(2, requests.size());

		// We realize the create request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		requestService.realize(createRequest);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		// get wish
		wish = requestService.getWishConnectorObject(requests.get(0));
		Assert.assertEquals(2, wish.getAttributes().stream().filter(attr -> attr.isChanged()).count());

		// Find change for firstName value
		boolean findCorrectChangedFirstName = wish.getAttributes().stream()
				.filter(attr -> attr.getValue() != null && attr.getValue().getValue().equals(changed)
						&& attr.getValue().getOldValue().equals(firstName)
						&& SysValueChangeType.UPDATED == attr.getValue().getChange())
				.findFirst().isPresent();
		Assert.assertTrue(findCorrectChangedFirstName);

		// Find change for lastName value
		boolean findCorrectChangedLastName = wish.getAttributes().stream()
				.filter(attr -> attr.getValue() != null && attr.getValue().getValue().equals(changed)
						&& attr.getValue().getOldValue().equals(lastName)
						&& SysValueChangeType.UPDATED == attr.getValue().getChange())
				.findFirst().isPresent();
		Assert.assertTrue(findCorrectChangedLastName);

	}

	@Test
	public void changeUidTest() {
		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);

		IdmIdentityDto userOne = helper.createIdentity(USER_ONE_NAME);
		identityService.save(userOne);

		this.assignRoleSystem(system, userOne, ROLE_ONE_NAME);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		requestFilter.setState(VsRequestState.IN_PROGRESS);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto createRequest = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, createRequest.getUid());
		Assert.assertEquals(VsOperationType.CREATE, createRequest.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, createRequest.getState());

		VsConnectorObjectDto wish = requestService.getWishConnectorObject(createRequest);
		boolean findAttributeWithouChange = wish.getAttributes().stream().filter(attribute -> !attribute.isChanged())
				.findFirst().isPresent();
		Assert.assertTrue(!findAttributeWithouChange);

		// Change username attributes
		userOne.setUsername(USER_ONE_CHANGED_NAME);
		// Invoke provisioning
		identityService.save(userOne);

		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(2, requests.size());

		// We realize the create request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		requestService.realize(createRequest);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		// get wish
		wish = requestService.getWishConnectorObject(requests.get(0));
		Assert.assertEquals(1, wish.getAttributes().stream().filter(attr -> attr.isChanged()).count());

		// Find change for firstName value
		boolean findCorrectChangedUserName = wish.getAttributes().stream()
				.filter(attr -> attr.getValue().getValue().equals(USER_ONE_CHANGED_NAME)
						&& attr.getValue().getOldValue().equals(USER_ONE_NAME)
						&& SysValueChangeType.UPDATED == attr.getValue().getChange())
				.findFirst().isPresent();
		Assert.assertTrue(findCorrectChangedUserName);

		SysSystemEntityFilter systemEntityFilter = new SysSystemEntityFilter();
		systemEntityFilter.setSystemId(system.getId());
		systemEntityFilter.setUid(USER_ONE_NAME);
		boolean oldUserNameExist = !systemEntityService.find(systemEntityFilter, null).getContent().isEmpty();
		Assert.assertTrue(oldUserNameExist);
		// Realize change username
		requestService.realize(requests.get(0));
		// We expects change UID in SystemEntity.UID
		oldUserNameExist = !systemEntityService.find(systemEntityFilter, null).getContent().isEmpty();
		Assert.assertTrue(!oldUserNameExist);
		systemEntityFilter.setUid(USER_ONE_CHANGED_NAME);
		boolean changedUserNameExist = !systemEntityService.find(systemEntityFilter, null).getContent().isEmpty();
		Assert.assertTrue(changedUserNameExist);
	}

	@Test
	public void dateTest() {
		SysSystemDto virtualSystem = helper.createVirtualSystem(helper.createName());
		IdmRoleDto roleOne = helper.createRole();
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);

		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		requestFilter.setUid(identity.getUsername());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());

		requestFilter.setCreatedAfter(ZonedDateTime.now().minusSeconds(10));
		requestFilter.setCreatedBefore(ZonedDateTime.now());
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());

		requestFilter.setCreatedAfter(ZonedDateTime.now().plusMinutes(10));
		requestFilter.setCreatedBefore(ZonedDateTime.now().plusMinutes(11));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(0, requests.size());

		requestFilter.setCreatedAfter(ZonedDateTime.now().minusMinutes(10));
		requestFilter.setCreatedBefore(ZonedDateTime.now().minusMinutes(9));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(0, requests.size());
	}

	@Test
	public void modifiedDateTest() {
		SysSystemDto virtualSystem = helper.createVirtualSystem(helper.createName());
		IdmRoleDto roleOne = helper.createRole();
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);

		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		requestFilter.setUid(identity.getUsername());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());

		requestFilter.setModifiedAfter(ZonedDateTime.now().minusSeconds(10));
		requestFilter.setModifiedBefore(ZonedDateTime.now());
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());

		requestFilter.setModifiedAfter(ZonedDateTime.now().plusMinutes(10));
		requestFilter.setModifiedBefore(ZonedDateTime.now().plusMinutes(11));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(0, requests.size());

		requestFilter.setModifiedAfter(ZonedDateTime.now().minusMinutes(10));
		requestFilter.setModifiedBefore(ZonedDateTime.now().minusMinutes(9));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(0, requests.size());
	}

	@Test
	public void systemTest() {
		SysSystemDto virtualSystem = helper.createVirtualSystem(helper.createName());
		IdmRoleDto roleOne = helper.createRole(helper.createName());
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = helper.createIdentity((GuardedString) null);
		IdmIdentityDto identity3 = helper.createIdentity((GuardedString) null);
		IdmIdentityDto identity4 = helper.createIdentity((GuardedString) null);

		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity2.getId()), false, roleOne);

		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(2, requests.size());

		helper.assignRoles(helper.getPrimeContract(identity3.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity4.getId()), false, roleOne);

		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(4, requests.size());

		requestFilter.setUid(identity.getUsername());
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());// identity uid filter test

	}

	@Test
	public void filterTest() {
		SysSystemDto virtualSystem = helper.createVirtualSystem(helper.createName());
		IdmRoleDto roleOne = helper.createRole();
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);
		IdmIdentityDto identity2 = helper.createIdentity((GuardedString) null);
		IdmIdentityDto identity3 = helper.createIdentity((GuardedString) null);
		IdmIdentityDto identity4 = helper.createIdentity((GuardedString) null);

		// Assign system to role
		helper.createRoleSystem(roleOne, virtualSystem);
		helper.assignRoles(helper.getPrimeContract(identity.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity2.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity3.getId()), false, roleOne);
		helper.assignRoles(helper.getPrimeContract(identity4.getId()), false, roleOne);

		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(virtualSystem.getId());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(4, requests.size());

		VsRequestDto request = requests.get(0);
		requestService.realize(request);
		Assert.assertEquals(VsRequestState.REALIZED, request.getState());

		requestFilter.setOnlyArchived(true);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());

		requestFilter.setOnlyArchived(null);
		requestFilter.setState(VsRequestState.IN_PROGRESS);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(3, requests.size());

		requestFilter.setConnectorKey(request.getConnectorKey());
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(3, requests.size());
	}
	
	@Test
	public void filterByImplementersTest () {
		IdmIdentityDto implementerIdentity1 = helper.createIdentity(helper.createName());
		IdmIdentityDto implementerIdentity2 = helper.createIdentity(helper.createName());
		IdmRoleDto implementerRole = helper.createRole(helper.createName());
		IdmIdentityDto userIdentity = helper.createIdentity(helper.createName());
		IdmRoleDto userRole = helper.createRole(helper.createName());
		// system with implementer by identity
		VsSystemDto vsConfig1 = new VsSystemDto();
		vsConfig1.setName(helper.createName());
		vsConfig1.setImplementers(Arrays.asList(implementerIdentity1.getId()));
		SysSystemDto system1 = helper.createVirtualSystem(vsConfig1);
		helper.createRoleSystem(userRole, system1);
		// system with implementer by role
		VsSystemDto vsConfig2 = new VsSystemDto();
		vsConfig2.setName(helper.createName());
		vsConfig2.setImplementerRoles(Arrays.asList(implementerRole.getId()));
		SysSystemDto system2 = helper.createVirtualSystem(vsConfig2);
		helper.createRoleSystem(userRole, system2);
		// assign user and implementer roles
		helper.assignRoles(helper.getPrimeContract(implementerIdentity2.getId()), false, implementerRole);
		helper.assignRoles(helper.getPrimeContract(userIdentity.getId()), false, userRole);
		
		// no filter used, find all requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertTrue(requests.size() >= 2);
		
		// find requests defined by implementer identity 
		requestFilter = new VsRequestFilter();
		requestFilter.setImplementers(Arrays.asList(implementerIdentity1.getId()));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		Assert.assertEquals(requests.get(0).getSystem(), system1.getId());
		
		// find requests defined by implementer role 
		requestFilter = new VsRequestFilter();
		requestFilter.setImplementers(Arrays.asList(implementerIdentity2.getId()));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		Assert.assertEquals(requests.get(0).getSystem(), system2.getId());
		
		// prove that no requests are found when identity is not an implementer 
		requestFilter = new VsRequestFilter();
		requestFilter.setImplementers(Arrays.asList(userIdentity.getId()));
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(0, requests.size());
 	} 

	@Test
	public void createAndRealizeRequestWithNoteTest() {

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);
		// We try realize the request
		super.logout();
		loginService.login(new LoginDto(USER_IMPLEMENTER_NAME, new GuardedString("password")));
		String note = helper.createName();
		request = requestService.realize(request, note);
		Assert.assertEquals(VsRequestState.REALIZED, request.getState());
		account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNotNull("Account cannot be null, because request was realized!", account);
		request = requestService.get(request.getId());
		Assert.assertEquals(note, request.getReason());
	}

	@Test
	public void testNotificationForCreateRequest() {

		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(USER_ONE_NAME);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(USER_ONE_NAME, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(USER_ONE_NAME, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);

		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(USER_IMPLEMENTER_NAME);
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		//
		assertEquals(1, notifications.size());
		assertEquals(VirtualSystemModuleDescriptor.TOPIC_VS_REQUEST_CREATED, notifications.get(0).getTopic());
	}

	@Test
	public void testNotificationForCreateRequestDiffUid() {
		String suffix="_suffix";
		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);

		SysSystemAttributeMappingFilter filterAttribute = new SysSystemAttributeMappingFilter();
		filterAttribute.setSystemId(system.getId());

		SysSystemAttributeMappingDto attributeUid = systemAttributeMappingService.find(filterAttribute, null)
				.getContent()
				.stream()
				.filter(attribute -> attribute.isUid())
				.findFirst()
				.orElse(null);

		attributeUid.setTransformToResourceScript("return attributeValue+\""+suffix+"\"");
		attributeUid = systemAttributeMappingService.save(attributeUid);
		String uid = USER_ONE_NAME + suffix;

		this.assignRoleSystem(system, helper.createIdentity(USER_ONE_NAME), ROLE_ONE_NAME);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setUid(uid);
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(uid, request.getUid());
		Assert.assertEquals(VsOperationType.CREATE, request.getOperationType());
		Assert.assertEquals(VsRequestState.IN_PROGRESS, request.getState());

		VsAccountDto account = accountService.findByUidSystem(uid, system.getId());
		Assert.assertNull("Account must be null, because request was not realized yet!", account);

		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(USER_IMPLEMENTER_NAME);
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		//
		assertEquals(1, notifications.size());
		assertEquals(VirtualSystemModuleDescriptor.TOPIC_VS_REQUEST_CREATED, notifications.get(0).getTopic());
	}

	/**
	 * Method for create role, assign role to system and to user.
	 *
	 * @return
	 */
	public SysSystemDto assignRoleSystem(SysSystemDto system, IdmIdentityDto userOne, String roleOneName) {
		IdmRoleDto roleOne = helper.createRole(roleOneName);

		// Create policy for vs evaluator and user role
		helper.createAuthorizationPolicy(
				roleConfiguration.getDefaultRoleId(),
				VirtualSystemGroupPermission.VSREQUEST, 
				VsRequest.class, 
				VsRequestByImplementerEvaluator.class,
				IdmBasePermission.ADMIN
		);

		// Assign system to role
		helper.createRoleSystem(roleOne, system);
		helper.assignRoles(helper.getPrimeContract(userOne.getId()), false, roleOne);
		return system;
	}

	public SysSystemDto createVirtualSystem(String userImplementerName, List<String> attributes) {
		IdmIdentityDto userImplementer = helper.createIdentity(userImplementerName);
		VsSystemDto config = new VsSystemDto();
		config.setName(helper.createName());
		config.setImplementers(ImmutableList.of(userImplementer.getId()));
		if (attributes != null) {
			config.setAttributes(attributes);
		}
		SysSystemDto system = helper.createVirtualSystem(config);
		Assert.assertNotNull(system);
		return system;
	}

	public void deleteAll(String userOneName, String userOneChangedName, String userImplementerName,
			String roleOneName) {
		if (identityService.getByUsername(userOneName) != null) {
			identityService.delete(identityService.getByUsername(userOneName));
		}
		if (identityService.getByUsername(userOneChangedName) != null) {
			identityService.delete(identityService.getByUsername(userOneChangedName));
		}
		if (identityService.getByUsername(userImplementerName) != null) {
			identityService.delete(identityService.getByUsername(userImplementerName));
		}
		if (roleService.getByCode(roleOneName) != null) {
			roleService.delete(roleService.getByCode(roleOneName));
		}
	}

	@Test
	public void testChangeUid() {
		SysSystemDto system = this.createVirtualSystem(USER_IMPLEMENTER_NAME, null);
		IdmIdentityDto identity = helper.createIdentity((GuardedString) null);
		String roleCode = getHelper().createName();
		this.assignRoleSystem(system, identity, roleCode);
		// Find created requests
		VsRequestFilter requestFilter = new VsRequestFilter();
		requestFilter.setSystemId(system.getId());
		requestFilter.setState(VsRequestState.IN_PROGRESS);
		requestFilter.setUid(identity.getUsername());
		List<VsRequestDto> requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto request = requests.get(0);
		Assert.assertEquals(identity.getUsername(), request.getUid());
		request = requestService.realize(request);

		identity.setLastName(getHelper().createName());
		identity = identityService.save(identity);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(1, requests.size());
		VsRequestDto requestBefore = requests.get(0);

		String newUid = this.getHelper().createName();
		identity.setUsername(newUid);
		identity = identityService.save(identity);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(2, requests.size());
		VsRequestDto requestWithUUID = requests.stream().filter(r -> !r.getId().equals(requestBefore.getId())).findFirst().orElseThrow();

		identity.setFirstName(getHelper().createName());
		identity = identityService.save(identity);
		requests = requestService.find(requestFilter, null).getContent();
		Assert.assertEquals(3, requests.size());
		VsRequestDto requestAfter = requests.stream().filter(r -> !r.getId().equals(requestBefore.getId()) && !r.getId().equals(requestWithUUID.getId())).findFirst().orElseThrow();

		requestService.realize(requestWithUUID);

		VsRequestDto requestForCheck = requestService.get(requestBefore.getId());
		Assert.assertNotEquals(requestForCheck.getUid(), requestBefore.getUid());
		Assert.assertEquals(newUid, requestForCheck.getUid());

		requestForCheck = requestService.get(requestAfter.getId());
		Assert.assertNotEquals(requestForCheck.getUid(), requestAfter.getUid());
		Assert.assertEquals(newUid, requestForCheck.getUid());
	}
}
