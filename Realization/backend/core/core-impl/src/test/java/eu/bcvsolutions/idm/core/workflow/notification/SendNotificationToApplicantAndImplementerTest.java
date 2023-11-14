package eu.bcvsolutions.idm.core.workflow.notification;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.config.WorkflowConfig;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Test for request role notification. Testing if notification is send right
 * with dependency on two boolean flags and if applicant == implementer.
 * 
 * @author Patrik Stloukal
 */
public class SendNotificationToApplicantAndImplementerTest extends AbstractCoreWorkflowIntegrationTest {

	// FIXME: move to api (workflow config constant)
	private static final String APPROVE_BY_SECURITY_ENABLE = "idm.sec.core.wf.approval.security.enabled";
	private static final String APPROVE_BY_MANAGER_ENABLE = "idm.sec.core.wf.approval.manager.enabled";
	private static final String APPROVE_BY_USERMANAGER_ENABLE = "idm.sec.core.wf.approval.usermanager.enabled";
	private static final String APPROVE_BY_HELPDESK_ENABLE = "idm.sec.core.wf.approval.helpdesk.enabled";
	private static String SENT_TO_APPLICANT = "idm.sec.core.wf.notification.applicant.enabled";
	private static String SENT_TO_IMPLEMENTER = "idm.sec.core.wf.notification.implementer.enabled";
	private IdmIdentityDto testUser2;
	private IdmTreeNodeDto organization;

	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private IdmNotificationLogService notificationLogService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmTreeNodeService treeNodeService;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired
	private SecurityService securityService;
	@Autowired 
	private EventConfiguration eventConfiguration;

	@Before
	public void login() {
		getHelper().setConfigurationValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, true);
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, true);
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, true);
		// FIXME: set defaults on the other side 
		getHelper().setConfigurationValue("idm.sec.core.wf.approval.security.role", "Security");
		getHelper().setConfigurationValue("idm.sec.core.wf.approval.helpdesk.role", "Helpdesk");
		getHelper().setConfigurationValue("idm.sec.core.wf.approval.usermanager.role", "Usermanager");
		
		createStructure();
	}

	@After
	public void logout() {
		getHelper().setConfigurationValue(WorkflowConfig.SEND_NOTIFICATION_CONFIGURATION_PROPERTY, false);
		getHelper().setConfigurationValue(APPROVE_BY_SECURITY_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_MANAGER_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_HELPDESK_ENABLE, false);
		getHelper().setConfigurationValue(APPROVE_BY_USERMANAGER_ENABLE, false);
		super.logout();
	}

	@Test
	public void requestRejectedByHelpdeskApplicantImplementerSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskApplicantImplementerNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskApplicantSameTest() {
		Assert.assertFalse(eventConfiguration.isAsynchronous());
		
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		getHelper().setConfigurationValue(SENT_TO_APPLICANT, true);
		getHelper().setConfigurationValue(SENT_TO_IMPLEMENTER, false);
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		request = getHelper().startRequestInternal(request, true, true);
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskApplicantNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestRejectedByHelpdeskImplementerSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskImplementerNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_DISAPPROVE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestRejectedByHelpdeskSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestRejectedByHelpdeskNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "disapprove");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestApprovedApplicantImplementerSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedApplicantImplementerNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedApplicantSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedApplicantNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}
	
	@Test
	public void requestApprovedApplicantNotSameChangeUsernameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		// change username
		test1.setUsername(test1.getUsername() + test1.getUsername());
		identityService.save(test1);
		
		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestApprovedImplementerSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedImplementerNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_CHANGE_IDENTITY_ROLES_IMPLEMENTER, notifications.get(0).getTopic());
	}

	@Test
	public void requestApprovedSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestApprovedNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// SECURITY
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestReturnedByHelpdeskApplicantImplementerSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER,
				notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskApplicantImplementerNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER,
				notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskApplicantSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER,
				notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskApplicantNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES, notifications.get(0).getTopic());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestReturnedByHelpdeskImplementerSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER,
				notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskImplementerNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER,
				notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByHelpdeskSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	@Test
	public void requestReturnedByHelpdeskNotSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "false");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "false");
		//
		IdmIdentityDto testUser3 = createTestUser();
		loginAsAdmin(testUser3.getUsername());
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		//
		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");

		// test notification to applicant
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());

		// test notification to implementer
		filter = new IdmNotificationFilter();
		filter.setRecipient(testUser3.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(0, notifications.size());
	}

	/**
	 * Creates concept role request for assign role to identity
	 * 
	 * @param IdmRoleDto, IdmIdentityContractDto, IdmRoleRequestDto
	 * @return IdmConceptRoleRequestDto
	 */
	private IdmConceptRoleRequestDto createRoleConcept(IdmRoleDto adminRole, IdmIdentityContractDto contract,
			IdmRoleRequestDto request) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(adminRole.getId());
		concept.setIdentityContract(contract.getId());
		return concept;
	}

	/**
	 * Creates request for identity
	 * 
	 * @param IdmIdentityDto
	 * @return IdmRoleRequestDto
	 */
	private IdmRoleRequestDto createRoleRequest(IdmIdentityDto test1) {
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicantInfo(new ApplicantImplDto(test1.getId(), IdmIdentityDto.class.getCanonicalName()));
		request.setExecuteImmediately(false);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		return request;
	}

	/**
	 * Completes one task (as helpdesk/manager/user manager/security)
	 * 
	 * @param taskFilter, userName, decision
	 */
	private void checkAndCompleteOneTask(WorkflowFilterDto taskFilter, String userName, String decision) {
		IdmIdentityDto identity = identityService.getByUsername(userName);
		List<WorkflowTaskInstanceDto> tasks;
		tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(1, tasks.size());
		assertEquals(identity.getId().toString(), tasks.get(0).getApplicant());

		workflowTaskInstanceService.completeTask(tasks.get(0).getId(), decision);
	}

	/**
	 * Creates role
	 * 
	 * @param code
	 * @return IdmRoleDto
	 */
	private IdmRoleDto createRole() {
		IdmRoleDto role = new IdmRoleDto();
		role.setBaseCode(getHelper().createName());
		role.setCanBeRequested(true);
		//
		return roleService.save(role);
	}

	/**
	 * Creates testUser with working position and contract
	 * 
	 * @return IdmIdentityDto
	 */
	private IdmIdentityDto createTestUser() {
		IdmIdentityDto testUser = new IdmIdentityDto();
		testUser.setUsername(getHelper().createName());
		testUser.setPassword(new GuardedString(InitApplicationData.ADMIN_PASSWORD));
		testUser.setFirstName("Test");
		testUser.setLastName("User");
		testUser.setEmail(testUser.getUsername() + "@bscsolutions.eu");
		testUser = this.identityService.save(testUser);

		IdmIdentityContractDto identityWorkPosition2 = new IdmIdentityContractDto();
		identityWorkPosition2.setIdentity(testUser.getId());
		identityWorkPosition2.setWorkPosition(organization.getId());
		identityWorkPosition2 = identityContractService.save(identityWorkPosition2);

		IdmContractGuaranteeDto contractGuarantee = new IdmContractGuaranteeDto();
		contractGuarantee.setIdentityContract(identityWorkPosition2.getId());
		contractGuarantee.setGuarantee(testUser2.getId());
		contractGuaranteeService.save(contractGuarantee);
		return testUser;
	}

	/**
	 * Creates organization's structure and identity testUser2 as manager
	 * 
	 */
	private void createStructure() {
		IdmTreeNodeDto rootOrganization = treeNodeService.findRoots((UUID) null, PageRequest.of(0, 1)).getContent()
				.get(0);

		IdmRoleDto role2 = new IdmRoleDto();
		role2.setCode("TestCustomRole002" + System.currentTimeMillis());
		role2 = this.roleService.save(role2);

		testUser2 = new IdmIdentityDto();
		testUser2.setUsername("Test_user_Manager" + System.currentTimeMillis());
		testUser2.setPassword(new GuardedString(InitApplicationData.ADMIN_PASSWORD));
		testUser2.setFirstName("Test");
		testUser2.setLastName("Second User");
		testUser2.setEmail("test2@bscsolutions.eu");
		testUser2 = this.identityService.save(testUser2);

		IdmTreeTypeDto type = treeTypeService.get(rootOrganization.getTreeType());

		organization = new IdmTreeNodeDto();
		organization.setCode("test" + System.currentTimeMillis());
		organization.setName("Organization Test Notification");
		organization.setParent(rootOrganization.getId());
		organization.setTreeType(type.getId());
		organization = this.treeNodeService.save(organization);
	}

	@Test
	public void requestReturnedByManagerApplicantImplementerSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER,
				notifications.get(0).getTopic());
	}

	@Test
	public void requestReturnedByUserManagerApplicantImplementerSameTest() {
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		configurationService.setValue(SENT_TO_APPLICANT, "true");
		configurationService.setValue(SENT_TO_IMPLEMENTER, "true");
		//
		IdmIdentityDto test1 = createTestUser();
		IdmRoleDto test_role = createRole();
		loginAsAdmin(test1.getUsername());

		IdmIdentityContractDto contract = identityContractService.getPrimeContract(test1.getId());

		IdmRoleRequestDto request = createRoleRequest(test1);
		request = roleRequestService.save(request);

		IdmConceptRoleRequestDto concept = createRoleConcept(test_role, contract, request);
		concept = conceptRoleRequestService.save(concept);

		roleRequestService.startRequestInternal(request.getId(), true);
		request = roleRequestService.get(request.getId());
		assertEquals(RoleRequestState.IN_PROGRESS, request.getState());

		WorkflowFilterDto taskFilter = new WorkflowFilterDto();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		taskFilter.setCreatedAfter(now);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) workflowTaskInstanceService
				.find(taskFilter, null, IdmBasePermission.READ).getContent();
		assertEquals(0, tasks.size());

		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		// HELPDESK
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// MANAGER
		loginAsAdmin(testUser2.getUsername());
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "approve");
		// USER MANAGER
		loginAsAdmin();
		taskFilter.setCandidateOrAssigned(securityService.getCurrentId().toString());
		checkAndCompleteOneTask(taskFilter, test1.getUsername(), "backToApplicant");
		// test notification
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setRecipient(test1.getUsername());
		filter.setNotificationType(IdmNotificationLog.class);
		List<IdmNotificationLogDto> notifications = notificationLogService.find(filter, null).getContent();
		assertEquals(1, notifications.size());

		assertEquals(CoreModuleDescriptor.TOPIC_RETURN_REQUEST_IDENTITY_ROLES_IMPLEMENTER,
				notifications.get(0).getTopic());
	}
}
