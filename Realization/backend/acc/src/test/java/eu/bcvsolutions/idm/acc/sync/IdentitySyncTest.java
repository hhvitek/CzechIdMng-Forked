package eu.bcvsolutions.idm.acc.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.core.api.config.datasource.CoreEntityManager;
import org.activiti.engine.ProcessEngine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.beust.jcommander.internal.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.config.domain.ProvisioningConfiguration;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationInactiveOwnerBehaviorType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncActionLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultSynchronizationServiceTest;
import eu.bcvsolutions.idm.acc.service.impl.IdentitySynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.ScriptAuthorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormProjectionService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.dto.DependentTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Identity synchronization tests (basic tests for identity sync are in
 * {@link DefaultSynchronizationServiceTest})
 * 
 * @author Svanda
 * @author Ondrej Husnik
 *
 */
public class IdentitySyncTest extends AbstractIntegrationTest {

	private static final String IDENTITY_ONE = "identityOne";
	private static final String IDENTITY_TWO = "identityTwo";
	private static final String IDENTITY_ONE_EMAIL = "email@test.cz";
	private static final String SYNC_CONFIG_NAME = "syncConfigNameContract";
	private static final String ATTRIBUTE_NAME = "__NAME__";
	private static final String ATTRIBUTE_EMAIL = "email";
	private static final String ATTRIBUTE_EMAIL_TWO = "emailTwo";
	private static final String ATTRIBUTE_DESCRIP = "DESCRIP";
	private static final String IDM_IDENTITY_STATE = "state";

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired
	private SysSystemEntityService systemEntityService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private SysSyncLogService syncLogService;
	@Autowired
	private SysSyncItemLogService syncItemLogService;
	@Autowired
	private SysSyncActionLogService syncActionLogService;
	@Autowired
	private AccAccountService accAccountService;
	@Autowired
	@CoreEntityManager
	private EntityManager entityManager;
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmScriptAuthorityService scriptAuthrotityService;
	@Autowired
	private IdmScriptService scriptService;
	@Autowired
	private TestIdentityProcessor testIdentityProcessor;
	@Autowired
	private FormService formService;
	@Autowired
	private SchedulerManager manager;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private SynchronizationService synchronizationService;
	@Autowired
	private PrivateIdentityConfiguration identityConfiguration;
	@Autowired
	private ProcessEngine processEngine;
	@Autowired
	private IdmFormProjectionService formProjectionService;

	@After
	public void logout() {
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_ONE);
		if (identity != null) {
			identityService.delete(identity);
			// clean up account in protection. There should be max. 1 after every test.
			deleteProtectedAccount(IDENTITY_ONE);
		}
		IdmIdentityDto identityTwo = identityService.getByUsername(IDENTITY_TWO);
		if (identityTwo != null) {
			identityService.delete(identityTwo);
			// clean up account in protection. There should be max. 1 after every test.
			deleteProtectedAccount(IDENTITY_TWO);
		}
	}

	@Test
	public void createIdentityWithDefaultRoleTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		// Have to be in the warning state, because default role cannot be assigned for
		// new identity, because sync do not creates the default contract. See
		// IdmIdentityContractService.SKIP_CREATION_OF_DEFAULT_POSITION.
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
				OperationResultType.WARNING);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identities.get(0).getId());
		Assert.assertEquals(0, roles.size());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testDifferntialSyncNoChanges() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Different sync is disabled by default.
		Assert.assertFalse(config.isDifferentialSync());

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		// Start sync for create identity
		helper.startSynchronization(config);
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
//		IdmIdentityDto identity = identities.get(0);
		
		// Start sync with disable different sync - no change was made on
		// identity, but standard update should be made.
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		// Identity had to be modified by sync!
//		ZonedDateTime originalModified = identity.getModified();
//		ZonedDateTime modified = identityService.get(identity.getId()).getModified();
//		Assert.assertNull(originalModified);
//		Assert.assertNotNull(modified);
		
		// Enable different sync.
		config.setDifferentialSync(true);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		Assert.assertTrue(config.isDifferentialSync());
		
		// Start sync with enable different sync - no change was made on
		// identity, so only ignore update should be made.
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.IGNORE);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		// Identity was not updated!
//		originalModified = modified;
//		modified = identityService.get(identity.getId()).getModified();
//		Assert.assertNotNull(originalModified);
//		Assert.assertNotNull(modified);
//		Assert.assertTrue(modified.isEqual(originalModified));

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testIdentityStateSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		
		// no identity is in system, will be created during sync
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());
		// DESCRIP attribute is used for holding STATE value 
		setDescriptionMappedToState(system, false);

		// Start sync for create identity
		// set required STATE value in database table for test record
		this.getBean().setStateToDescriptionValue(IDENTITY_ONE,"VALID");
		helper.startSynchronization(config);
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(IdentityState.VALID,identities.get(0).getState());
		syncLogService.delete(log);
				
		// update sync with the new value of STATE in DB 
		this.getBean().setStateToDescriptionValue(IDENTITY_ONE,"NO_CONTRACT");
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(IdentityState.NO_CONTRACT,identities.get(0).getState());
		syncLogService.delete(log);
		
		// update sync with the new value test case insensitivity
		this.getBean().setStateToDescriptionValue(IDENTITY_ONE,"lEfT");
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(IdentityState.LEFT,identities.get(0).getState());
		syncLogService.delete(log);
		
		// update sync with set transformation - always returns CREATED state
		setDescriptionMappedToState(system, true);
		this.getBean().setStateToDescriptionValue(IDENTITY_ONE,"NONEXISTENT_STATE");
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(IdentityState.CREATED,identities.get(0).getState());
						
		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	
	@Test
	public void testDifferntialSyncWithEntityChange() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Different sync is disabled by default.
		Assert.assertFalse(config.isDifferentialSync());

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		// Start sync for create identity
		helper.startSynchronization(config);
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		
		// Enable different sync.
		config.setDifferentialSync(true);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		Assert.assertTrue(config.isDifferentialSync());
		
		// Start sync with enable different sync - no change was made on
		// identity, so only ignore update should be made.
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.IGNORE);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		identity.setFirstName(getHelper().createName());
		identity = identityService.save(identity);
		
		// Start sync with enable different sync - first name was changed
		// -> standard update should be made.
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		String originalFirstName = identity.getFirstName();
		String firstName = identityService.get(identity.getId()).getFirstName();
		Assert.assertNotEquals(originalFirstName, firstName);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testDifferntialSyncWithEAVChange() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Different sync is disabled by default.
		Assert.assertFalse(config.isDifferentialSync());

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		// Start sync for create identity
		helper.startSynchronization(config);
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		
		// Enable different sync.
		config.setDifferentialSync(true);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		Assert.assertTrue(config.isDifferentialSync());
		
		// Start sync with enable different sync - no change was made on
		// identity, so only ignore update should be made.
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.IGNORE);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());
		
		List<IdmFormValueDto> values = formService.getValues(identity, ATTRIBUTE_EMAIL_TWO);
		Assert.assertEquals(1, values.size());
		IdmFormValueDto emailTwo = values.get(0);
		Assert.assertEquals(identity.getEmail(), emailTwo.getValue());
		
		String emailTwoValue = getHelper().createName();
		formService.saveValues(identity, ATTRIBUTE_EMAIL_TWO, Lists.newArrayList(emailTwoValue));
		
		// Start sync with enable different sync - email two in EAV was changed
		// -> standard update should be made.
		helper.startSynchronization(config);
		log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1,
				OperationResultType.SUCCESS);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		values = formService.getValues(identity, ATTRIBUTE_EMAIL_TWO);
		Assert.assertEquals(1, values.size());
		emailTwo = values.get(0);
		Assert.assertEquals(identity.getEmail(), emailTwo.getValue());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testCreateIdentityWithDefaultContractAndRoleSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();
		//
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config.setCreateDefaultContract(true);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		//
		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		// Have to be in the success state, because default role will be assigned to the default contract.
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identities.get(0).getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto assignedRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), assignedRole.getRole());
		
		// check only one identity account is created
		AccIdentityAccountFilter accountFilter = new AccIdentityAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(accountFilter, null).getContent();
		Assert.assertEquals(1, identityAccounts.size());
		Assert.assertEquals(assignedRole.getId(), identityAccounts.get(0).getIdentityRole());
		
		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testDefaultRoleToAllContractsWithDefaultContract() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();
		//
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config.setCreateDefaultContract(true);
		config.setAssignDefaultRoleToAll(true);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		//
		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		// Have to be in the success state, because default role will be assigned to the default contract.
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identities.get(0).getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto assignedRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), assignedRole.getRole());
		
		// check only one identity account is created
		AccIdentityAccountFilter accountFilter = new AccIdentityAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(accountFilter, null).getContent();
		Assert.assertEquals(1, identityAccounts.size());
		Assert.assertEquals(assignedRole.getId(), identityAccounts.get(0).getIdentityRole());
		
		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testDefaultRoleToAllContracts() {
		IdmIdentityDto identityDto = helper.createIdentity((GuardedString) null);
		SysSystemDto system = initData(identityDto.getUsername());
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();
		//
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config.setCreateDefaultContract(false);
		config.setAssignDefaultRoleToAll(true);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		//
		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);
		
		
		IdmIdentityContractDto validContract = helper.getPrimeContract(identityDto);
		IdmIdentityContractDto validFutureContract = helper.createContract(identityDto, null, LocalDate.now().plusDays(10), null);
		helper.createContract(identityDto, null, null, LocalDate.now().minusDays(1));
		
		List<IdmIdentityContractDto> contracts = contractService.findAllByIdentity(identityDto.getId());
		Assert.assertEquals(3, contracts.size());

		helper.startSynchronization(config);

		// Have to be in the success state, because default role will be assigned to the valid contracts.
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		IdmIdentityDto identity = identityService.get(identityDto);
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, roles.size());
		
		long identityRolesWithDefaultRole = roles.stream()
			.filter(role -> role.getRole().equals(defaultRole.getId()))
			.count();
		Assert.assertEquals(2, identityRolesWithDefaultRole);
		
		// Valid contract must have default role
		long identityRolesWithValidContract = roles.stream()
			.filter(role -> role.getIdentityContract().equals(validContract.getId()))
			.count();
		Assert.assertEquals(1, identityRolesWithValidContract);
		
		// Future contract must have default role
		long identityRolesWithFutureContract = roles.stream()
			.filter(role -> role.getIdentityContract().equals(validFutureContract.getId()))
			.count();
		Assert.assertEquals(1, identityRolesWithFutureContract);
		
		// Check only one identity account is created.
		// Only one identity-account relation can exists, because only one
		// current valid identity-role exists now (the second is future valid).
		AccIdentityAccountFilter accountFilter = new AccIdentityAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(accountFilter, null).getContent();
		
		// !!!!To delete - Test doesn't pass on the Jenkins, we need to more information
		if (identityAccounts.size() > 1) {
			identityAccounts.forEach(identityAccountDtoOne -> {
				System.out.println("Id - identityAccount: " + identityAccountDtoOne.getId());
				System.out.println("Account: " + identityAccountDtoOne.getAccount());
				System.out.println("RoleSystem: " + identityAccountDtoOne.getRoleSystem());
				System.out.println("Identity: " + identityAccountDtoOne.getIdentity());
				System.out.println("IdentityRole: " + identityAccountDtoOne.getIdentityRole());
				System.out.println("----");
			});
		}
		// !!!
		Assert.assertEquals(1, identityAccounts.size());
		
		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testCreateIdentityWithDefaultContractAndRoleAsync() {
		try {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, true);
			SysSystemDto system = initData();
			Assert.assertNotNull(system);
			IdmRoleDto defaultRole = helper.createRole();
			
			SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
			// Set default role to sync configuration
			config.setDefaultRole(defaultRole.getId());
			config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
			config.setCreateDefaultContract(true);
			config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
			
			// create default mapping for provisioning
			helper.createMapping(system);
			helper.createRoleSystem(defaultRole, system);
	
			IdmIdentityFilter identityFilter = new IdmIdentityFilter();
			identityFilter.setUsername(IDENTITY_ONE);
			List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
			Assert.assertEquals(0, identities.size());
	
			helper.startSynchronization(config);
	
			// Have to be in the success state, because default role will be assigned to the default contract.
			SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1, OperationResultType.SUCCESS);
	
			Assert.assertFalse(log.isRunning());
			Assert.assertFalse(log.isContainsError());
	
			identities = identityService.find(identityFilter, null).getContent();
			Assert.assertEquals(1, identities.size());
			IdmIdentityDto identity = identities.get(0);
			List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identities.get(0).getId());
			Assert.assertEquals(1, roles.size());
			IdmIdentityRoleDto assignedRole = roles.get(0);
			Assert.assertEquals(defaultRole.getId(), assignedRole.getRole());
			
			// Check only one identity account is created.
			// Only one identity-account relation can exists, because only one
			// current valid identity-role exists now (the second is future valid).
			AccIdentityAccountFilter accountFilter = new AccIdentityAccountFilter();
			accountFilter.setIdentityId(identity.getId());
			List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(accountFilter, null).getContent();
			
			// !!!!To delete - Test doesn't pass on the Jenkins, we need to more information
			if (identityAccounts.size() > 1) {
				identityAccounts.forEach(identityAccountDtoOne -> {
					System.out.println("Account: " + identityAccountDtoOne.getAccount());
					System.out.println("RoleSystem: " + identityAccountDtoOne.getRoleSystem());
					System.out.println("Identity: " + identityAccountDtoOne.getIdentity());
					System.out.println("IdentityRole: " + identityAccountDtoOne.getIdentityRole());
				});
			}
			// !!!
			
			Assert.assertEquals(1, identityAccounts.size());
			Assert.assertEquals(assignedRole.getId(), identityAccounts.get(0).getIdentityRole());
			
			// Delete log
			syncLogService.delete(log);
			syncConfigService.delete(config);
		} finally {
			getHelper().setConfigurationValue(EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED, false);
		}
	}

	@Test
	public void mappingTwoAttributesOnSchemaAttributeTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);

		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
				OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		List<IdmFormValueDto> emailValues = formService.getValues(identity, ATTRIBUTE_EMAIL_TWO);
		Assert.assertEquals(1, emailValues.size());
		Assert.assertEquals(IDENTITY_ONE_EMAIL, emailValues.get(0).getValue());
		Assert.assertEquals(IDENTITY_ONE_EMAIL, identity.getEmail());

		// Delete log
		syncLogService.delete(log);
	}

	@Test
	public void updateIdentityWithDefaultRoleTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityDto identityOne = helper.createIdentity(IDENTITY_ONE);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identityOne.getId());
		Assert.assertEquals(1, roles.size());
		Assert.assertEquals(defaultRole.getId(), roles.get(0).getRole());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void updateIdentityWithUserTypeTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		
		// Create projection
		IdmFormProjectionDto projection = new IdmFormProjectionDto();
		projection.setOwnerType(IdmIdentity.class.getCanonicalName());
		projection.setCode(getHelper().createName());
		projection.setDisabled(false);
		projection = formProjectionService.save(projection);
		
		SysSchemaAttributeDto someSchemaAttribute = schemaAttributeService.find(schemaAttributeFilter, null).getContent().get(0);
		Assert.assertNotNull(someSchemaAttribute);
		
		// Create attribute returns code of projection.
		SysSystemAttributeMappingDto projectionAttribute = new SysSystemAttributeMappingDto();
		projectionAttribute.setIdmPropertyName(IdmIdentity_.formProjection.getName());
		projectionAttribute.setEntityAttribute(true);
		projectionAttribute.setExtendedAttribute(false);
		projectionAttribute.setName(IdmIdentity_.formProjection.getName());
		projectionAttribute.setSchemaAttribute(someSchemaAttribute.getId());
		projectionAttribute.setSystemMapping(config.getSystemMapping());
		projectionAttribute.setTransformFromResourceScript(MessageFormat.format("return \"{0}\";", projection.getCode()));
		projectionAttribute = schemaAttributeMappingService.save(projectionAttribute);

		IdmIdentityDto identityOne = helper.createIdentity(IDENTITY_ONE);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK_AND_UPDATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identityOne.getId());
		Assert.assertEquals(1, roles.size());
		Assert.assertEquals(defaultRole.getId(), roles.get(0).getRole());
		
		identityOne = identityService.get(identityOne.getId());
		
		// Identity must have sets projection.
		Assert.assertNotNull(identityOne);
		Assert.assertNotNull(identityOne.getFormProjection());
		Assert.assertEquals(projection.getId(), identityOne.getFormProjection());

		// Delete log
		syncLogService.delete(log);
		// Delete projection attribute and projection.
		schemaAttributeMappingService.delete(projectionAttribute);
		formProjectionService.delete(projection);
	}

	@Test
	public void updateIdentityWithInvalidContractTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityDto identityOne = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identityOne.getId());
		Assert.assertNotNull(primeContract);
		primeContract.setValidTill(LocalDate.now().minusDays(10));
		primeContract = contractService.save(primeContract);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.WARNING);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identityOne.getId());
		Assert.assertEquals(0, roles.size());

		// Delete log
		syncLogService.delete(log);
	}

	@Test
	public void updateIdentityPropagateValidityTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		IdmIdentityDto identityOne = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identityOne.getId());
		Assert.assertNotNull(primeContract);

		LocalDate validTill = LocalDate.now().plusDays(10);
		LocalDate validFrom = LocalDate.now().plusDays(-10);
		primeContract.setValidFrom(validFrom);
		primeContract.setValidTill(validTill);
		primeContract = contractService.save(primeContract);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identityOne.getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto identityRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), identityRole.getRole());
		Assert.assertEquals(identityRole.getValidFrom(), validFrom);
		Assert.assertNull(identityRole.getValidTill());

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityRoleId(identityRole.getId());
		Assert.assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Delete log
		syncLogService.delete(log);

	}

	@Test
	public void deleteDefaulRoleIntegrityTest() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		Assert.assertNotNull(config.getDefaultRole());
		// Delete default role
		roleService.delete(defaultRole);
		config = (SysSyncIdentityConfigDto) syncConfigService.get(config.getId());
		Assert.assertNull(config.getDefaultRole());
	}

	@Test
	public void testSynchronizationCache() {
		SysSystemDto system = initData();
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		this.getBean().deleteAllResourceData();
		String testLastName = "test-last-name-same-" + System.currentTimeMillis();
		String testFirstName = "test-first-name";

		String userOne = "test-1-" + System.currentTimeMillis();
		this.getBean().setTestData(userOne, testFirstName, testLastName);

		String userTwo = "test-2-" + System.currentTimeMillis();
		this.getBean().setTestData(userTwo, testFirstName, testLastName);

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		mappingFilter.setSystemId(system.getId());
		mappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemMappingDto> mappings = systemMappingService.find(mappingFilter, null).getContent();
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto defaultMapping = mappings.get(0);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(defaultMapping.getId());

		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();

		SysSystemAttributeMappingDto firstNameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getIdmPropertyName().equals(IdmIdentity_.firstName.getName());
		}).findFirst().orElse(null);

		Assert.assertNotNull(firstNameAttribute);
		StringBuilder scriptGenerateUuid = new StringBuilder();
		scriptGenerateUuid.append("import java.util.UUID;");
		scriptGenerateUuid.append(System.lineSeparator());
		scriptGenerateUuid.append("return UUID.randomUUID();");

		String scriptName = "generateUuid";
		IdmScriptDto scriptUuid = new IdmScriptDto();
		scriptUuid.setCategory(IdmScriptCategory.TRANSFORM_FROM);
		scriptUuid.setCode(scriptName);
		scriptUuid.setName(scriptName);
		scriptUuid.setScript(scriptGenerateUuid.toString());
		scriptUuid = scriptService.save(scriptUuid);

		IdmScriptAuthorityDto scriptAuth = new IdmScriptAuthorityDto();
		scriptAuth.setClassName("java.util.UUID");
		scriptAuth.setType(ScriptAuthorityType.CLASS_NAME);
		scriptAuth.setScript(scriptUuid.getId());
		scriptAuth = scriptAuthrotityService.save(scriptAuth);

		// we must call script
		StringBuilder transformationScript = new StringBuilder();

		transformationScript.append("return scriptEvaluator.evaluate(");
		transformationScript.append(System.lineSeparator());
		transformationScript.append("scriptEvaluator.newBuilder()");
		transformationScript.append(System.lineSeparator());
		transformationScript.append(".setScriptCode('" + scriptName + "')");
		transformationScript.append(System.lineSeparator());
		transformationScript.append(".build());");
		transformationScript.append(System.lineSeparator());

		firstNameAttribute.setTransformFromResourceScript(transformationScript.toString());
		firstNameAttribute.setCached(true);
		firstNameAttribute = schemaAttributeMappingService.save(firstNameAttribute);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2,
				OperationResultType.WARNING);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setLastName(testLastName);
		List<IdmIdentityDto> identities = identityService.find(filter, null).getContent();
		assertEquals(2, identities.size());
		//
		IdmIdentityDto identityOne = identities.get(0);
		IdmIdentityDto identityTwo = identities.get(1);
		//
		assertNotEquals(identityOne.getFirstName(), identityTwo.getFirstName());
	}

	@Test
	public void testEnableAutomaticRoleDuringSynchronization() {
		// default initialization of system and all necessary things
		SysSystemDto system = initData();
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config.setStartAutoRoleRec(true); // we want start recalculation after synchronization
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		this.getBean().deleteAllResourceData();

		String testLastName = "test-last-name-same-" + System.currentTimeMillis();
		String testFirstName = "test-first-name";

		String user1 = "test-1-" + System.currentTimeMillis();
		this.getBean().setTestData(user1, testFirstName, testLastName);

		String user2 = "test-2-" + System.currentTimeMillis();
		this.getBean().setTestData(user2, testFirstName, testLastName);

		String user3 = "test-3-" + System.currentTimeMillis();
		this.getBean().setTestData(user3, testFirstName, testLastName);

		IdmRoleDto role1 = helper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = helper.createAutomaticRole(role1.getId());
		helper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, user1);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3,
				OperationResultType.WARNING);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		IdmIdentityDto identity1 = identityService.getByUsername(user1);
		IdmIdentityDto identity2 = identityService.getByUsername(user2);
		IdmIdentityDto identity3 = identityService.getByUsername(user3);

		// we must change username, after create contract is also save identity (change
		// state)
		identity1.setUsername(user1 + System.currentTimeMillis());
		identity1 = identityService.save(identity1);

		helper.createContract(identity1);
		helper.createContract(identity2);
		helper.createContract(identity3);

		List<IdmIdentityRoleDto> identityRoles1 = identityRoleService.findAllByIdentity(identity1.getId());
		List<IdmIdentityRoleDto> identityRoles2 = identityRoleService.findAllByIdentity(identity2.getId());
		List<IdmIdentityRoleDto> identityRoles3 = identityRoleService.findAllByIdentity(identity3.getId());

		assertEquals(0, identityRoles1.size());
		assertEquals(0, identityRoles2.size());
		assertEquals(0, identityRoles3.size());

		// enable test processor
		testIdentityProcessor.enable();
		helper.startSynchronization(config);

		identityRoles1 = identityRoleService.findAllByIdentity(identity1.getId());
		identityRoles2 = identityRoleService.findAllByIdentity(identity2.getId());
		identityRoles3 = identityRoleService.findAllByIdentity(identity3.getId());

		assertEquals(1, identityRoles1.size());
		assertEquals(0, identityRoles2.size());
		assertEquals(0, identityRoles3.size());

		IdmIdentityRoleDto foundIdentityRole = identityRoles1.get(0);
		assertEquals(automaticRole.getId(), foundIdentityRole.getAutomaticRole());

		// synchronization immediately recalculate is disabled
		int size = testIdentityProcessor.getRolesByUsername(user1).size();
		assertEquals(0, size);
		size = testIdentityProcessor.getRolesByUsername(user2).size();
		assertEquals(0, size);
		size = testIdentityProcessor.getRolesByUsername(user3).size();
		assertEquals(0, size);
	}

	@Test
	public void testDisableAutomaticRoleDuringSynchronization() {
		// default initialization of system and all necessary things
		SysSystemDto system = initData();
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		IdmRoleDto defaultRole = helper.createRole();

		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK);
		config.setStartAutoRoleRec(false); // we want start recalculation after synchronization
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		this.getBean().deleteAllResourceData();

		String testLastName = "test-last-name-same-" + System.currentTimeMillis();
		String testFirstName = "test-first-name";

		String user1 = "test-1-" + System.currentTimeMillis();
		this.getBean().setTestData(user1, testFirstName, testLastName);

		String user2 = "test-2-" + System.currentTimeMillis();
		this.getBean().setTestData(user2, testFirstName, testLastName);

		String user3 = "test-3-" + System.currentTimeMillis();
		this.getBean().setTestData(user3, testFirstName, testLastName);

		IdmRoleDto role1 = helper.createRole();
		IdmAutomaticRoleAttributeDto automaticRole = helper.createAutomaticRole(role1.getId());
		helper.createAutomaticRoleRule(automaticRole.getId(), AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY, IdmIdentity_.username.getName(), null, user1);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 3,
				OperationResultType.WARNING);
		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		IdmIdentityDto identity1 = identityService.getByUsername(user1);
		IdmIdentityDto identity2 = identityService.getByUsername(user2);
		IdmIdentityDto identity3 = identityService.getByUsername(user3);

		// we must change username, after create contract is also save identity (change
		// state)
		identity1.setUsername(user1 + System.currentTimeMillis());
		identity1 = identityService.save(identity1);

		helper.createContract(identity1);
		helper.createContract(identity2);
		helper.createContract(identity3);

		List<IdmIdentityRoleDto> identityRoles1 = identityRoleService.findAllByIdentity(identity1.getId());
		List<IdmIdentityRoleDto> identityRoles2 = identityRoleService.findAllByIdentity(identity2.getId());
		List<IdmIdentityRoleDto> identityRoles3 = identityRoleService.findAllByIdentity(identity3.getId());

		assertEquals(0, identityRoles1.size());
		assertEquals(0, identityRoles2.size());
		assertEquals(0, identityRoles3.size());

		// enable test processor
		testIdentityProcessor.enable();
		helper.startSynchronization(config);

		identityRoles1 = identityRoleService.findAllByIdentity(identity1.getId());
		identityRoles2 = identityRoleService.findAllByIdentity(identity2.getId());
		identityRoles3 = identityRoleService.findAllByIdentity(identity3.getId());

		assertEquals(0, identityRoles1.size());
		assertEquals(0, identityRoles2.size());
		assertEquals(0, identityRoles3.size());

		// synchronization immediately recalculate is disabled
		int size = testIdentityProcessor.getRolesByUsername(user1).size();
		assertEquals(0, size);
		size = testIdentityProcessor.getRolesByUsername(user2).size();
		assertEquals(0, size);
		size = testIdentityProcessor.getRolesByUsername(user3).size();
		assertEquals(0, size);
	}

	@Test
	public void testLinkAndUpdateIdentity() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		this.getBean().deleteAllResourceData();

		String testLastName = "test-last-name-same-" + System.currentTimeMillis();
		String testFirstName = "test-first-name-";

		String user1 = "test-1-" + System.currentTimeMillis();
		IdmIdentityDto identity1 = helper.createIdentity(user1);
		this.getBean().setTestData(user1, testFirstName + 1, testLastName);

		String user2 = "test-2-" + System.currentTimeMillis();
		IdmIdentityDto identity2 = helper.createIdentity(user2);
		this.getBean().setTestData(user2, testFirstName + 2, testLastName);

		String user3 = "test-3-" + System.currentTimeMillis();
		IdmIdentityDto identity3 = helper.createIdentity(user3);
		this.getBean().setTestData(user3, testFirstName + 3, testLastName);

		assertNotEquals(testFirstName + 1, identity1.getFirstName());
		assertNotEquals(testFirstName + 2, identity2.getFirstName());
		assertNotEquals(testFirstName + 3, identity3.getFirstName());

		assertNotEquals(testLastName, identity1.getLastName());
		assertNotEquals(testLastName, identity2.getLastName());
		assertNotEquals(testLastName, identity3.getLastName());

		testIdentityProcessor.enable();
		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK_AND_UPDATE_ENTITY, 3,
				OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		IdmIdentityDto updatedIdentity1 = identityService.getByUsername(user1);
		IdmIdentityDto updatedIdentity2 = identityService.getByUsername(user2);
		IdmIdentityDto updatedIdentity3 = identityService.getByUsername(user3);

		assertNotEquals(updatedIdentity1.getFirstName(), identity1.getFirstName());
		assertNotEquals(updatedIdentity2.getFirstName(), identity2.getFirstName());
		assertNotEquals(updatedIdentity3.getFirstName(), identity3.getFirstName());

		assertNotEquals(updatedIdentity1.getLastName(), identity1.getLastName());
		assertNotEquals(updatedIdentity2.getLastName(), identity2.getLastName());
		assertNotEquals(updatedIdentity3.getLastName(), identity3.getLastName());

		assertNotEquals(updatedIdentity1.getModified(), identity1.getModified());
		assertNotEquals(updatedIdentity2.getModified(), identity2.getModified());
		assertNotEquals(updatedIdentity3.getModified(), identity3.getModified());

		assertEquals(testFirstName + 1, updatedIdentity1.getFirstName());
		assertEquals(testFirstName + 2, updatedIdentity2.getFirstName());
		assertEquals(testFirstName + 3, updatedIdentity3.getFirstName());

		assertEquals(testLastName, updatedIdentity1.getLastName());
		assertEquals(testLastName, updatedIdentity2.getLastName());
		assertEquals(testLastName, updatedIdentity3.getLastName());
	}

	@Test
	public void testTaskExecution() throws InterruptedException {
		getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
		try {
			SysSystemDto system = initData();
			Assert.assertNotNull(system);
			SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
	
			config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
	
			IdmIdentityFilter identityFilter = new IdmIdentityFilter();
			identityFilter.setUsername(IDENTITY_ONE);
			List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
			Assert.assertEquals(0, identities.size());
	
			Task initiatorTask = createSyncTask(config.getId());
			ObserveLongRunningTaskEndProcessor.listenTask(initiatorTask.getId());
			// Execute
			manager.runTask(initiatorTask.getId());
			ObserveLongRunningTaskEndProcessor.waitForEnd(initiatorTask.getId());
	
			SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
					OperationResultType.SUCCESS);
	
			Assert.assertFalse(log.isRunning());
			Assert.assertFalse(log.isContainsError());
	
			identities = identityService.find(identityFilter, null).getContent();
			Assert.assertEquals(1, identities.size());
			IdmIdentityDto identity = identities.get(0);
			List<IdmFormValueDto> emailValues = formService.getValues(identity, ATTRIBUTE_EMAIL_TWO);
			Assert.assertEquals(1, emailValues.size());
			Assert.assertEquals(IDENTITY_ONE_EMAIL, emailValues.get(0).getValue());
			Assert.assertEquals(IDENTITY_ONE_EMAIL, identity.getEmail());
	
			// Delete log
			syncLogService.delete(log);
		} finally {
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
		}
	}

	@Test
	public void testDependentTaskExecution() throws InterruptedException {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);

		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		SysSystemDto systemTwo = initData();
		Assert.assertNotNull(systemTwo);
		SysSyncIdentityConfigDto configTwo = doCreateSyncConfig(systemTwo);

		configTwo = (SysSyncIdentityConfigDto) syncConfigService.save(configTwo);

		Task initiatorTask = createSyncTask(config.getId());
		Task dependentTask = createSyncTask(configTwo.getId());

		ObserveLongRunningTaskEndProcessor.listenTask(initiatorTask.getId());
		ObserveLongRunningTaskEndProcessor.listenTask(dependentTask.getId());

		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(initiatorTask.getId());
		//
		// execute initiator
		manager.createTrigger(dependentTask.getId(), trigger);
		manager.runTask(initiatorTask.getId());

		ZonedDateTime startOne = ZonedDateTime.now();
		ObserveLongRunningTaskEndProcessor.waitForEnd(initiatorTask.getId());
		ZonedDateTime endOne = ZonedDateTime.now();
		ObserveLongRunningTaskEndProcessor.waitForEnd(dependentTask.getId());
		ZonedDateTime endTwo = ZonedDateTime.now();
		long durationOne = endOne.toInstant().toEpochMilli() - startOne.toInstant().toEpochMilli();
		long durationTwo = endTwo.toInstant().toEpochMilli() - endOne.toInstant().toEpochMilli();

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1,
				OperationResultType.SUCCESS);

		SysSyncLogDto logTwo = checkSyncLog(configTwo, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		long syncDurationOne = log.getEnded().toInstant().toEpochMilli() - log.getStarted().toInstant().toEpochMilli();
		long syncDurationTwo = logTwo.getEnded().toInstant().toEpochMilli()
				- logTwo.getStarted().toInstant().toEpochMilli();

		// We want to check if was the task ended after sync end.
		assertTrue(durationOne > syncDurationOne);
		assertTrue(durationTwo > syncDurationTwo);

		//
		assertEquals(OperationState.EXECUTED,
				ObserveLongRunningTaskEndProcessor.getResult(initiatorTask.getId()).getState());
		assertEquals(OperationState.EXECUTED,
				ObserveLongRunningTaskEndProcessor.getResult(dependentTask.getId()).getState());

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		Assert.assertFalse(logTwo.isRunning());
		Assert.assertFalse(logTwo.isContainsError());

		// Delete log
		syncLogService.delete(logTwo);
	}

	@Test(expected = ProvisioningException.class)
	public void testStopSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		// Stop sync - Sync is not running, so exception will be throw
		synchronizationService.stopSynchronization(config);
	}
	
	@Test
	public void testSyncWithWfSituationMissingEntity() {
		
		final String wfExampleKey =  "syncActionExampl";
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setLinkedActionWfKey(wfExampleKey);
		config.setMissingAccountActionWfKey(wfExampleKey);
		config.setMissingEntityActionWfKey(wfExampleKey);
		config.setUnlinkedActionWfKey(wfExampleKey);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());
		
		long countOfHistoricProcesses = processEngine.getHistoryService() //
				.createHistoricProcessInstanceQuery() //
				.processDefinitionKey(wfExampleKey) //
				.finished() //
				.count(); //

		// Start sync
		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.MISSING_ENTITY, 1,
				OperationResultType.WF);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		List<IdmFormValueDto> emailValues = formService.getValues(identity, ATTRIBUTE_EMAIL_TWO);
		Assert.assertEquals(1, emailValues.size());
		Assert.assertEquals(IDENTITY_ONE_EMAIL, emailValues.get(0).getValue());
		Assert.assertEquals(IDENTITY_ONE_EMAIL, identity.getEmail());
		
		long countOfHistoricProcessesAfter = processEngine.getHistoryService() //
				.createHistoricProcessInstanceQuery() //
				.processDefinitionKey(wfExampleKey) //
				.finished() //
				.count(); //

		// We deleting a historic processes created during sync -> count must be same!
		Assert.assertEquals(countOfHistoricProcesses, countOfHistoricProcessesAfter);

		// Delete log
		syncLogService.delete(log);
	}
	
	@Test
	public void testSyncWithWfSituationUnlinkEntity() {
		
		final String wfExampleKey =  "syncActionExampl";
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setLinkedActionWfKey(wfExampleKey);
		config.setMissingAccountActionWfKey(wfExampleKey);
		config.setMissingEntityActionWfKey(wfExampleKey);
		config.setUnlinkedActionWfKey(wfExampleKey);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());
		getHelper().createIdentity(IDENTITY_ONE);
		
		long countOfHistoricProcesses = processEngine.getHistoryService() //
				.createHistoricProcessInstanceQuery() //
				.processDefinitionKey(wfExampleKey) //
				.finished() //
				.count(); //

		// Start sync
		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.UNLINKED, 1,
				OperationResultType.WF);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		List<IdmFormValueDto> emailValues = formService.getValues(identity, ATTRIBUTE_EMAIL_TWO);
		Assert.assertEquals(1, emailValues.size());
		Assert.assertEquals(IDENTITY_ONE_EMAIL, emailValues.get(0).getValue());
		Assert.assertEquals(IDENTITY_ONE_EMAIL, identity.getEmail());
		
		long countOfHistoricProcessesAfter = processEngine.getHistoryService() //
				.createHistoricProcessInstanceQuery() //
				.processDefinitionKey(wfExampleKey) //
				.finished() //
				.count(); //

		// We deleting a historic processes created during sync -> count must be same!
		Assert.assertEquals(countOfHistoricProcesses, countOfHistoricProcessesAfter);

		// Delete log
		syncLogService.delete(log);
	}
	
	@Test
	public void testSyncWithWfSituationLinkedEntity() {
		SysSystemDto system = initData();
		
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());
		
		// Create identity and account
		IdmIdentityDto identity = getHelper().createIdentity(IDENTITY_ONE);
		helper.createIdentityAccount(system, identity);
		Assert.assertNotEquals(IDENTITY_ONE_EMAIL, identity.getEmail());
		
		final String wfExampleKey =  "syncActionExampl";
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setLinkedActionWfKey(wfExampleKey);
		config.setMissingAccountActionWfKey(wfExampleKey);
		config.setMissingEntityActionWfKey(wfExampleKey);
		config.setUnlinkedActionWfKey(wfExampleKey);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		
		long countOfHistoricProcesses = processEngine.getHistoryService() //
				.createHistoricProcessInstanceQuery() //
				.processDefinitionKey(wfExampleKey) //
				.finished() //
				.count(); //

		// Start sync
		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINKED, 1,
				OperationResultType.WF);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		identity = identities.get(0);
		List<IdmFormValueDto> emailValues = formService.getValues(identity, ATTRIBUTE_EMAIL_TWO);
		Assert.assertEquals(1, emailValues.size());
		Assert.assertEquals(IDENTITY_ONE_EMAIL, emailValues.get(0).getValue());
		Assert.assertEquals(IDENTITY_ONE_EMAIL, identity.getEmail());
		
		long countOfHistoricProcessesAfter = processEngine.getHistoryService() //
				.createHistoricProcessInstanceQuery() //
				.processDefinitionKey(wfExampleKey) //
				.finished() //
				.count(); //

		// We deleting a historic processes created during sync -> count must be same!
		Assert.assertEquals(countOfHistoricProcesses, countOfHistoricProcessesAfter);

		// Delete log
		syncLogService.delete(log);
	}
	
	@Test
	public void testSyncWithWfSituationMissingAccount() {
		SysSystemDto system = initData();
		
		// Create identity and account (this account is not on the target system)
		// We need remove test from the name of identity ... because the WF create approving task for identity begins with 'test'.
		IdmIdentityDto identity = getHelper().createIdentity(getHelper().createName().substring(5));
		helper.createIdentityAccount(system, identity);
		// Delete all data on the target system
		this.getBean().deleteAllResourceData();
		
		final String wfExampleKey =  "syncActionExampl";
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setMissingAccountActionWfKey(wfExampleKey);
		config.setMissingAccountAction(ReconciliationMissingAccountActionType.CREATE_ACCOUNT);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// Start sync
		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.MISSING_ACCOUNT, 1,
				OperationResultType.WF);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Delete log
		syncLogService.delete(log);
	}

	@Test
	public void syncIdentityWithDefaultContract() {
		// application property for creating default contract is allowed and also is allowed create 
		configurationService.setBooleanValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED, Boolean.TRUE);
		try {
			SysSystemDto system = initData();
			this.getBean().deleteAllResourceData();
			
			String usernameOne = getHelper().createName();
			this.getBean().setTestData(usernameOne, getHelper().createName(), getHelper().createName());
			
			String usernameTwo = getHelper().createName();
			this.getBean().setTestData(usernameTwo, getHelper().createName(), getHelper().createName());
	
			SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
			config.setCreateDefaultContract(true);
			config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
			
			assertTrue(identityConfiguration.isCreateDefaultContractEnabled());
			
			helper.startSynchronization(config);
			
			SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2,
					OperationResultType.SUCCESS);
			
			Assert.assertFalse(log.isRunning());
			Assert.assertFalse(log.isContainsError());
			
			IdmIdentityDto identityOne = identityService.getByUsername(usernameOne);
			assertNotNull(identityOne);
			List<IdmIdentityContractDto> allByIdentity = contractService.findAllByIdentity(identityOne.getId());
			assertEquals(1, allByIdentity.size());
	
			IdmIdentityDto identityTwo = identityService.getByUsername(usernameTwo);
			assertNotNull(identityTwo);
			allByIdentity = contractService.findAllByIdentity(identityTwo.getId());
			assertEquals(1, allByIdentity.size());
		} finally {
			configurationService.deleteValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		}
	}

	@Test
	public void syncIdentityWithoutDefaultContract() {
		configurationService.setBooleanValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED, Boolean.TRUE);
		try {
			SysSystemDto system = initData();
			this.getBean().deleteAllResourceData();
			
			String usernameOne = getHelper().createName();
			this.getBean().setTestData(usernameOne, getHelper().createName(), getHelper().createName());
			
			String usernameTwo = getHelper().createName();
			this.getBean().setTestData(usernameTwo, getHelper().createName(), getHelper().createName());
	
			SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
			config.setCreateDefaultContract(false);
			config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
			
			assertTrue(identityConfiguration.isCreateDefaultContractEnabled());
			
			helper.startSynchronization(config);
			
			SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2,
					OperationResultType.SUCCESS);
			
			Assert.assertFalse(log.isRunning());
			Assert.assertFalse(log.isContainsError());
			
			IdmIdentityDto identityOne = identityService.getByUsername(usernameOne);
			assertNotNull(identityOne);
			List<IdmIdentityContractDto> allByIdentity = contractService.findAllByIdentity(identityOne.getId());
			assertEquals(0, allByIdentity.size());
	
			IdmIdentityDto identityTwo = identityService.getByUsername(usernameTwo);
			assertNotNull(identityTwo);
			allByIdentity = contractService.findAllByIdentity(identityTwo.getId());
			assertEquals(0, allByIdentity.size());
		} finally {
			configurationService.deleteValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		}
	}

	@Test
	public void syncIdentityWithDefaultContractDisableByProperty() {
		configurationService.setBooleanValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED, Boolean.FALSE);
		try {
			SysSystemDto system = initData();
			this.getBean().deleteAllResourceData();
			
			String usernameOne = getHelper().createName();
			this.getBean().setTestData(usernameOne, getHelper().createName(), getHelper().createName());
			
			String usernameTwo = getHelper().createName();
			this.getBean().setTestData(usernameTwo, getHelper().createName(), getHelper().createName());
	
			SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
			config.setCreateDefaultContract(true);
			config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
			
			assertFalse(identityConfiguration.isCreateDefaultContractEnabled());
			
			helper.startSynchronization(config);
			
			SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 2,
					OperationResultType.SUCCESS);
			
			Assert.assertFalse(log.isRunning());
			Assert.assertFalse(log.isContainsError());
			
			IdmIdentityDto identityOne = identityService.getByUsername(usernameOne);
			assertNotNull(identityOne);
			List<IdmIdentityContractDto> allByIdentity = contractService.findAllByIdentity(identityOne.getId());
			assertEquals(0, allByIdentity.size());
	
			IdmIdentityDto identityTwo = identityService.getByUsername(usernameTwo);
			assertNotNull(identityTwo);
			allByIdentity = contractService.findAllByIdentity(identityTwo.getId());
			assertEquals(0, allByIdentity.size());
		} finally {
			configurationService.deleteValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		}
	}
	
	//-------------------------------------- Inactive owner behavior tests --------------------------------------------------------

	// check settings - default role is set, inactive owner behavior not set => synchronization didn't start
	// check settings - link protected, account protection is not set => synchronization didn't start

	// new identity, no default contract, don't link => identity is not created
	// new identity, no default contract, link protected => created identity, account in protection
	// new identity, default contract, don't link => created identity, account is assigned by role
	// new identity, default contract, link protected => created identity, account is assigned by role
	// unlinked, valid contract, behavior don't link => role is assigned, account is assigned by role
	// unlinked, valid contract, link protected => role is assigned, account is assigned by role
	// unlinked, invalid contract, don't link => role is not assigned, account is not linked, identity is not updated
	// unlinked, invalid contract, link protected => role is not assigned, account is in protection
	// unlinked, invalid contract, link protected, account protection is 30 days => role is not assigned, account is in protection to "contract end + 31 days"
	// unlinked, future contract, link protected, account protection is 30 days => role is not assigned, account is in protection to "now + 31 days"
	// unlinked, multiple invalid contracts, link protected, account protection is 30 days => account is in protection to "last contract end + 31 days"
	// unlinked, no contract, don't link => role is not assigned, account is not linked
	// unlinked, no contract, link protected => role is not assigned, account is in protection
	// unlinked, no contract, link protected, account protection is 30 days => role is not assigned, account is in protection to "now + 31 days"
	
	// check settings - default role is set, inactive owner behavior not set => synchronization didn't start
	@Test(expected = ResultCodeException.class)
	public void testDefaultRoleWithoutInactiveOwnerBehaviorSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(null);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);

		helper.startSynchronization(config);
	}

	// check settings - link protected, account protection is not set => synchronization didn't start
	@Test(expected = ResultCodeException.class)
	public void testDefaultRoleLinkProtectedWithoutProtectionSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);

		helper.startSynchronization(config);
	}

	// new identity, no default contract, don't link => identity is not created
	@Test
	public void testCreateIdentityWithoutDefaultContractDontLinkSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Don't link without owner
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
		// Don't create default contract
		config.setCreateDefaultContract(false);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		// Identity didn't have contract to link the account -> it wasn't created at all and was ignored
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.MISSING_ENTITY, 1, OperationResultType.IGNORE);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());
		
		// check that AccAccount doesn't exist
		checkAccAccount(0, false, null);
		
		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// new identity, no default contract, link protected => created identity, account in protection
	@Test
	public void testCreateIdentityWithoutDefaultContractLinkProtectedSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link accounts to protection
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		// Don't create default contract
		config.setCreateDefaultContract(false);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		// create mapping for provisioning with protection enabled
		createMappingWithProtection(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		// Has to be success - identity didn't have contract, but we expected this and used Link protected
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		// default role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identities.get(0).getId());
		Assert.assertEquals(0, roles.size());

		// account is in protection
		checkAccAccount(1, true, null);

		// identity account is in protection and not assigned by any role
		checkIdentityAccount(identity, 1, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// new identity, default contract, don't link => created identity, account is assigned by role
	@Test
	public void testCreateIdentityWithDefaultContractDontLinkSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Don't link without owner
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
		// Create default contract
		config.setCreateDefaultContract(true);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		// Has to be success - identity has contract
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		// default role is assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto assignedRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), assignedRole.getRole());

		// account is linked without protection
		checkAccAccount(1, false, null);

		// account is assigned by the role
		checkIdentityAccount(identity, 1, assignedRole.getId());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// new identity, default contract, link protected => created identity, account is assigned by role
	@Test
	public void testCreateIdentityWithDefaultContractLinkProtectedSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		// Create default contract
		config.setCreateDefaultContract(true);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		// create mapping for provisioning with protection enabled
		createMappingWithProtection(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		// Has to be success - identity has contract
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		IdmIdentityDto identity = identities.get(0);
		// default role is assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto assignedRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), assignedRole.getRole());

		// account is linked without protection
		checkAccAccount(1, false, null);

		// account is assigned by the role
		checkIdentityAccount(identity, 1, assignedRole.getId());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, valid contract, behavior don't link => role is assigned, account is assigned by role
	@Test
	public void testLinkIdentityValidContractDontLinkSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Don't link without owner
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		String lastNameBefore = identity.getLastName();
		Assert.assertNotEquals(IDENTITY_ONE, lastNameBefore);
		IdmIdentityContractDto primeValidContract = contractService.getPrimeValidContract(identity.getId());
		Assert.assertNotNull(primeValidContract);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK_AND_UPDATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto assignedRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), assignedRole.getRole());
		Assert.assertEquals(primeValidContract.getId(), assignedRole.getIdentityContract());
		
		// account is linked without protection
		checkAccAccount(1, false, null);

		// account is assigned by the role
		checkIdentityAccount(identity, 1, assignedRole.getId());

		// check that identity was updated
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(IDENTITY_ONE, identities.get(0).getLastName());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, valid contract, link protected => role is assigned, account is assigned by role
	@Test
	public void testLinkIdentityValidContractLinkProtectedSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create mapping for provisioning with protection enabled
		createMappingWithProtection(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeValidContract = contractService.getPrimeValidContract(identity.getId());
		Assert.assertNotNull(primeValidContract);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto assignedRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), assignedRole.getRole());
		Assert.assertEquals(primeValidContract.getId(), assignedRole.getIdentityContract());

		// account is linked without protection
		checkAccAccount(1, false, null);

		// account is assigned by the role
		checkIdentityAccount(identity, 1, assignedRole.getId());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, invalid contract, don't link => role is not assigned, account is not linked, identity is not updated
	@Test
	public void testLinkIdentityInvalidContractDontLinkSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Don't link without owner
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// set invalid contract
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		String lastNameBefore = identity.getLastName();
		Assert.assertNotEquals(IDENTITY_ONE, lastNameBefore);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNotNull(primeContract);
		primeContract.setValidTill(LocalDate.now().minusDays(10));
		primeContract = contractService.save(primeContract);

		helper.startSynchronization(config);

		// has to be ignored, because no valid contract was found, so account wasn't linked
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.UNLINKED, 1, OperationResultType.IGNORE);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// check that AccAccount doesn't exist
		checkAccAccount(0, false, null);

		// account is not linked
		checkIdentityAccount(identity, 0, null);

		// check that identity wasn't updated
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(lastNameBefore, identities.get(0).getLastName());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, invalid contract, link protected => role is not assigned, account is in protection infinitely
	@Test
	public void testLinkIdentityInvalidContractLinkProtectedSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create mapping for provisioning with protection enabled infinitely
		createMappingWithProtection(system, null);
		helper.createRoleSystem(defaultRole, system);

		// set invalid contract
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNotNull(primeContract);
		primeContract.setValidTill(LocalDate.now().minusDays(10));
		primeContract = contractService.save(primeContract);

		helper.startSynchronization(config);

		// has to be success, account was linked and we expected that contract can be invalid
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// account is linked in protection infinitely
		checkAccAccount(1, true, null);

		// account is in protection and not assigned by any role
		checkIdentityAccount(identity, 1, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, invalid contract, link protected, account protection is 30 days => role is not assigned, account is in protection to "contract end + 31 days"
	@Test
	public void testLinkIdentityInvalidContractLinkProtectedFiniteProtectionSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create mapping for provisioning with protection enabled
		createMappingWithProtection(system, 30);
		helper.createRoleSystem(defaultRole, system);

		// set invalid contract
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNotNull(primeContract);
		LocalDate validTill = LocalDate.now().minusDays(10);
		primeContract.setValidTill(validTill);
		primeContract = contractService.save(primeContract);

		helper.startSynchronization(config);

		// has to be success, account was linked and we expected that contract can be invalid
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// account is linked in protection
		ZonedDateTime protectionEnd = validTill.plusDays(31).atStartOfDay(ZoneId.systemDefault());
		checkAccAccount(1, true, protectionEnd);

		// account is in protection and not assigned by any role
		checkIdentityAccount(identity, 1, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, future contract, link protected, account protection is 30 days => role is not assigned, account is in protection to "now + 31 days"
	@Test
	public void testLinkIdentityFutureContractLinkProtectedFiniteProtectionSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create mapping for provisioning with protection enabled
		createMappingWithProtection(system, 30);
		helper.createRoleSystem(defaultRole, system);

		// set future contract
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNotNull(primeContract);
		primeContract.setValidTill(LocalDate.now().plusDays(100));
		primeContract.setValidFrom(LocalDate.now().plusDays(10));
		primeContract = contractService.save(primeContract);

		helper.startSynchronization(config);

		// has to be success, account was linked and we expected that contract can be invalid
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// account is linked in protection
		// end of protection = now + protection interval + 1 day
		ZonedDateTime protectionEnd = LocalDate.now().plusDays(31).atStartOfDay(ZoneId.systemDefault());
		checkAccAccount(1, true, protectionEnd);

		// account is in protection and not assigned by any role
		checkIdentityAccount(identity, 1, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, multiple invalid contracts, link protected, account protection is 30 days => account is in protection to "last contract end + 31 days"
	@Test
	public void testLinkIdentityMultipleInvalidContractsLinkProtectedFiniteProtectionSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create mapping for provisioning with protection enabled
		createMappingWithProtection(system, 30);
		helper.createRoleSystem(defaultRole, system);

		// ends of contracts
		LocalDate validTillOlder = LocalDate.now().minusDays(100);
		LocalDate validTillNewer = LocalDate.now().minusDays(50);

		// set invalid contract
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNotNull(primeContract);
		primeContract.setValidTill(validTillOlder);
		primeContract = contractService.save(primeContract);

		// create second invalid contract, which is newer than the first
		helper.createContract(identity, null, null, validTillNewer);

		helper.startSynchronization(config);

		// has to be success, account was linked and we expected that contract can be invalid
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// account is linked in protection
		// end of protection = validTillNewer + protection interval + 1 day
		ZonedDateTime protectionEnd = validTillNewer.plusDays(31).atStartOfDay(ZoneId.systemDefault());
		checkAccAccount(1, true, protectionEnd);

		// account is in protection and not assigned by any role
		checkIdentityAccount(identity, 1, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, no contract, don't link => role is not assigned, account is not linked
	@Test
	public void testLinkIdentityWithoutContractDontLinkSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Don't link without owner
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create identity without contract
		IdmIdentityDto identity;
		configurationService.setBooleanValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED, Boolean.FALSE);
		try {
			identity = helper.createIdentity(IDENTITY_ONE);
		} finally {
			configurationService.deleteValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		}
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNull(primeContract);

		helper.startSynchronization(config);

		// has to be ignored, because no contract was found, so account wasn't linked
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.UNLINKED, 1, OperationResultType.IGNORE);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// check that AccAccount doesn't exist
		checkAccAccount(0, false, null);

		checkIdentityAccount(identity, 0, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testLinkIdentityWithContractAndIdentityWithoutContractDontLinkSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Don't link without owner
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);
		
		// We want to try If identity with the contract does not affect to the next sync of
		// identity without contract
		this.getBean().deleteAllResourceData();
		this.getBean().setTestData(IDENTITY_TWO, IDENTITY_TWO, IDENTITY_TWO);
		this.getBean().setTestData(IDENTITY_ONE, IDENTITY_ONE, IDENTITY_ONE);
		
		IdmIdentityDto identityTwo = helper.createIdentity(IDENTITY_TWO);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identityTwo.getId());
		Assert.assertNotNull(primeContract);

		// create identity without contract
		configurationService.setBooleanValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED, Boolean.FALSE);
		IdmIdentityDto identity;
		try {
			identity = helper.createIdentity(IDENTITY_ONE);
		} finally {
			configurationService.deleteValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		}
		primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNull(primeContract);

		helper.startSynchronization(config);

		// has to be ignored, because no contract was found, so account wasn't linked
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.UNLINKED, 1, OperationResultType.IGNORE);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// check that AccAccount doesn't exist
		checkAccAccount(0, false, null);

		checkIdentityAccount(identity, 0, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, no contract, link protected => role is not assigned, account is in protection infinitely
	@Test
	public void testLinkIdentityWithoutContractLinkProtectedSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create mapping for provisioning with protection enabled infinitely
		createMappingWithProtection(system, null);
		helper.createRoleSystem(defaultRole, system);

		// create identity without contract
		configurationService.setBooleanValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED, Boolean.FALSE);
		IdmIdentityDto identity;
		try {
			identity = helper.createIdentity(IDENTITY_ONE);
		} finally {
			configurationService.deleteValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		}
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNull(primeContract);

		helper.startSynchronization(config);

		// has to be success, account was linked and we expected that contract can be invalid
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// account is linked in protection infinitely
		checkAccAccount(1, true, null);

		// account is in protection and not assigned by any role
		checkIdentityAccount(identity, 1, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	// unlinked, no contract, link protected => role is not assigned, account is in protection infinitely
	@Test
	public void testLinkIdentityWithContractAndIdentityWithoutContractLinkProtectedSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create mapping for provisioning with protection enabled infinitely
		createMappingWithProtection(system, null);
		helper.createRoleSystem(defaultRole, system);
		
		// We want to try If identity with the contract does not affect to the next sync of
		// identity without contract
		this.getBean().deleteAllResourceData();
		this.getBean().setTestData(IDENTITY_TWO, IDENTITY_TWO, IDENTITY_TWO);
		this.getBean().setTestData(IDENTITY_ONE, IDENTITY_ONE, IDENTITY_ONE);
		
		IdmIdentityDto identityTwo = helper.createIdentity(IDENTITY_TWO);
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identityTwo.getId());
		Assert.assertNotNull(primeContract);

		// create identity without contract
		configurationService.setBooleanValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED, false);
		IdmIdentityDto identity;
		try {
			identity = helper.createIdentity(IDENTITY_ONE);
		} finally {
			configurationService.deleteValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		}
		primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNull(primeContract);

		helper.startSynchronization(config);

		// has to be success, account was linked and we expected that contract can be
		// invalid
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 2, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());
		
		// role is assigned
		roles = identityRoleService.findAllByIdentity(identityTwo.getId());
		Assert.assertEquals(1, roles.size());

		// account is linked in protection infinitely
		checkAccAccount(1, true, null);

		// account is in protection and not assigned by any role
		checkIdentityAccount(identity, 1, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, no contract, link protected, account protection is 30 days => role is not assigned, account is in protection to "now + 31 days"
	@Test
	public void testLinkIdentityWithoutContractLinkProtectedFiniteProtectionSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Link protected
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.LINK_PROTECTED);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create mapping for provisioning with protection enabled
		createMappingWithProtection(system, 30);
		helper.createRoleSystem(defaultRole, system);

		// create identity without contract
		configurationService.setBooleanValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED, false);
		IdmIdentityDto identity;
		try {
			identity = helper.createIdentity(IDENTITY_ONE);
		} finally {
			configurationService.deleteValue(PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		}
		IdmIdentityContractDto primeContract = contractService.getPrimeContract(identity.getId());
		Assert.assertNull(primeContract);

		helper.startSynchronization(config);

		// has to be success, account was linked and we expected that contract can be invalid
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// role is not assigned
		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(0, roles.size());

		// account is linked in protection
		// end of protection = now + protection interval + 1 day
		ZonedDateTime protectionEnd = LocalDate.now().plusDays(31).atStartOfDay(ZoneId.systemDefault());
		checkAccAccount(1, true, protectionEnd);

		// account is in protection and not assigned by any role
		checkIdentityAccount(identity, 1, null);

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	//-------------------------------------- Inactive owner behavior tests end--------------------------------------------------------

	//-------------------------------------- System entity wish tests --------------------------------------------------------
	// new identity, system entity has "wish" => removed "wish"
	// unlinked, system entity has "wish" => removed "wish"
	// linked, system entity has "wish", action Ignore => no change
	// linked, system entity has "wish", action Update entity, automapping is allowed => removed "wish" 
	// linked, system entity has "wish", action Update entity, automapping is not allowed => no change + warning

	// new identity, system entity has "wish" => removed "wish"
	@Test
	public void testCreateIdentityRemoveWishSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);

		// Create system entity with "wish"
		createSystemEntityWish(system);

		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(0, identities.size());

		helper.startSynchronization(config);

		// Has to be success
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// Identity was created
		identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());

		// System entity is no longer "wish"
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, IDENTITY_ONE);
		Assert.assertFalse(systemEntity.isWish());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// unlinked, system entity has "wish" => removed "wish"
	@Test
	public void testLinkIdentityRemoveWishSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);

		// Create system entity with "wish"
		createSystemEntityWish(system);

		helper.createIdentity(IDENTITY_ONE);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// System entity is no longer "wish"
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, IDENTITY_ONE);
		Assert.assertFalse(systemEntity.isWish());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// linked, system entity has "wish", action Ignore => no change
	@Test
	public void testLinkedIdentityIgnoredDontRemoveWishSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// Create system entity with "wish"
		createSystemEntityWish(system);

		// Create identity with account
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		helper.createIdentityAccount(system, identity);

		helper.startSynchronization(config);

		// has to be ignored
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINKED, 1, OperationResultType.IGNORE);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// System entity has "wish"
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, IDENTITY_ONE);
		Assert.assertTrue(systemEntity.isWish());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	// linked, system entity has "wish", action Ignore => no change
	@Test
	public void testLinkedIdentityIgnoredDontRemoveWishSyncNoLog() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setLinkedAction(SynchronizationLinkedActionType.IGNORE_AND_DO_NOT_LOG);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// Create system entity with "wish"
		createSystemEntityWish(system);

		// Create identity with account
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		helper.createIdentityAccount(system, identity);

		helper.startSynchronization(config);

		// has to be ignored
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINKED, 0, OperationResultType.IGNORE);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// System entity has "wish"
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, IDENTITY_ONE);
		Assert.assertTrue(systemEntity.isWish());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	// linked, system entity has "wish", action Update entity, automapping is allowed => removed "wish" 
	@Test
	public void testUpdateIdentityRemoveWishSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);

		// Allow automapping
		configurationService.setBooleanValue(ProvisioningConfiguration.PROPERTY_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT, Boolean.TRUE);

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// Create system entity with "wish"
		createSystemEntityWish(system);

		// Create identity with account
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		helper.createIdentityAccount(system, identity);

		helper.startSynchronization(config);

		// has to be success
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// System entity has no longer "wish"
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, IDENTITY_ONE);
		Assert.assertFalse(systemEntity.isWish());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);

		// Return default
		configurationService.setBooleanValue(ProvisioningConfiguration.PROPERTY_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT,
				ProvisioningConfiguration.DEFAULT_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT);
	}

	// linked, system entity has "wish", action Update entity, automapping is not allowed => no change + warning
	@Test
	public void testUpdateIdentityNoAutomappingDontRemoveWishSync() {
		SysSystemDto system = initData();
		Assert.assertNotNull(system);

		// Disallow automapping
		configurationService.setBooleanValue(ProvisioningConfiguration.PROPERTY_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT, Boolean.FALSE);

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// Create system entity with "wish"
		createSystemEntityWish(system);

		// Create identity with account
		IdmIdentityDto identity = helper.createIdentity(IDENTITY_ONE);
		helper.createIdentityAccount(system, identity);

		helper.startSynchronization(config);

		// has to be warning - automapping is not allowed
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.UPDATE_ENTITY, 1, OperationResultType.WARNING);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		// System entity has "wish"
		SysSystemEntityDto systemEntity = systemEntityService.getBySystemAndEntityTypeAndUid(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, IDENTITY_ONE);
		Assert.assertTrue(systemEntity.isWish());

		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);

		// Return default
		configurationService.setBooleanValue(ProvisioningConfiguration.PROPERTY_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT,
				ProvisioningConfiguration.DEFAULT_ALLOW_AUTO_MAPPING_ON_EXISTING_ACCOUNT);
	}

	//-------------------------------------- System entity wish tests end--------------------------------------------------------
	
	/**
	 *  Unlinked, valid contract, different UID on system and on entity - correlation by email
	 */
	@Test
	public void testLinkWithChangeUIDSync() {
		String uniqueEmail = getHelper().createName() + "@bcv.eu";
		SysSystemDto system = initData(IDENTITY_ONE, uniqueEmail);
		Assert.assertNotNull(system);
		IdmRoleDto defaultRole = helper.createRole();

		SysSyncIdentityConfigDto config = doCreateSyncConfig(system, true);
		config.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
		// Set default role to sync configuration
		config.setDefaultRole(defaultRole.getId());
		// Don't link without owner
		config.setInactiveOwnerBehavior(SynchronizationInactiveOwnerBehaviorType.DO_NOT_LINK);
		config = (SysSyncIdentityConfigDto) syncConfigService.save(config);

		// create default mapping for provisioning
		helper.createMapping(system);
		helper.createRoleSystem(defaultRole, system);

		IdmIdentityDto identity = helper.createIdentity();
		identity.setEmail(uniqueEmail);
		identity = identityService.save(identity);
		
		String lastNameBefore = identity.getLastName();
		Assert.assertNotEquals(IDENTITY_ONE, lastNameBefore);
		String usernameBefore = identity.getLastName();
		Assert.assertNotEquals(IDENTITY_ONE, usernameBefore);
		
		IdmIdentityContractDto primeValidContract = contractService.getPrimeValidContract(identity.getId());
		Assert.assertNotNull(primeValidContract);

		helper.startSynchronization(config);

		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.LINK_AND_UPDATE_ENTITY, 1,
				OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		Assert.assertFalse(log.isContainsError());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, roles.size());
		IdmIdentityRoleDto assignedRole = roles.get(0);
		Assert.assertEquals(defaultRole.getId(), assignedRole.getRole());
		Assert.assertEquals(primeValidContract.getId(), assignedRole.getIdentityContract());

		// account is linked without protection
		checkAccAccount(1, false, null);

		// account is assigned by the role
		checkIdentityAccount(identity, 1, assignedRole.getId());

		// check that identity was updated
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(IDENTITY_ONE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(IDENTITY_ONE, identities.get(0).getLastName());
		IdmIdentityDto currentIdentity = identityService.get(identity.getId());
		Assert.assertEquals(IDENTITY_ONE, currentIdentity.getUsername());
		
		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}
	
	@Test
	public void testCreateIdentityWithAutomaticRoleByEavAttribute() {
		String username = getHelper().createName();
		SysSystemDto system = initData(username, "mockIdentity@idm.eu");
		Assert.assertNotNull(system);
		SysSyncIdentityConfigDto config = doCreateSyncConfig(system);
		config.setCreateDefaultContract(true);
		config.setStartAutoRoleRec(true);
		syncConfigService.save(config);
		//
		// create form definition, roles, automatic role etc.
		IdmRoleDto role = getHelper().createRole();
		IdmRoleDto subRole = getHelper().createRole();
		getHelper().createRoleComposition(role, subRole);
		// sync supports default definition only
		IdmFormAttributeDto formAttribute = new IdmFormAttributeDto(getHelper().createName());
		IdmFormAttributeDto formAttributeIdentity = formService.saveAttribute(IdmIdentityDto.class, formAttribute);
		//
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		getHelper().createAutomaticRoleRule(
				automaticRole.getId(), 
				AutomaticRoleAttributeRuleComparison.EQUALS, 
				AutomaticRoleAttributeRuleType.IDENTITY_EAV, 
				null, 
				formAttributeIdentity.getId(), 
				"mockIdentity@idm.eu");
		//
		// create mapping to eav attribute - leader = eav
		SysSystemMappingDto syncSystemMapping = systemMappingService.get(config.getSystemMapping());
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(syncSystemMapping.getId());
		SysSystemAttributeMappingDto lastnameAttributeMapping = schemaAttributeMappingService
				.findBySystemMappingAndName(syncSystemMapping.getId(), ATTRIBUTE_EMAIL);
		lastnameAttributeMapping.setEntityAttribute(false);
		lastnameAttributeMapping.setExtendedAttribute(true);
		lastnameAttributeMapping.setIdmPropertyName(formAttributeIdentity.getCode());
		schemaAttributeMappingService.save(lastnameAttributeMapping);
		//
		helper.startSynchronization(config);
		
		SysSyncLogDto log = checkSyncLog(config, SynchronizationActionType.CREATE_ENTITY, 1, OperationResultType.SUCCESS);

		Assert.assertFalse(log.isRunning());
		
		IdmIdentityFilter identityFilter = new IdmIdentityFilter();
		identityFilter.setUsername(username);
		identityFilter.setAddEavMetadata(Boolean.TRUE);
		List<IdmIdentityDto> identities = identityService.find(identityFilter, null).getContent();
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals("mockIdentity@idm.eu", identities
				.get(0)
				.getEavs()
				.stream()
				.filter(fi -> fi.getFormDefinition().isMain())
				.findFirst()
				.get()
				.getValues()
				.stream()
				.filter(v -> v.getFormAttribute().equals(formAttributeIdentity.getId()))
				.findFirst()
				.get()
				.getShortTextValue());
		
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identities.get(0).getId());
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.find(identityRoleFilter, null).getContent();
		Assert.assertEquals(2, assignedRoles.size());
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(role.getId())));
		Assert.assertTrue(assignedRoles.stream().anyMatch(ir -> ir.getRole().equals(subRole.getId())));
		
		// Delete log
		syncLogService.delete(log);
		syncConfigService.delete(config);
	}

	private Task createSyncTask(UUID syncConfId) {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(SynchronizationSchedulableTaskExecutor.class);
		task.setDescription("test");
		task.getParameters().put(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID, syncConfId.toString());
		//
		return manager.createTask(task);
	}

	private SysSyncLogDto checkSyncLog(AbstractSysSyncConfigDto config, SynchronizationActionType actionType, int count,
			OperationResultType resultType) {
		SysSyncConfigFilter logFilter = new SysSyncConfigFilter();
		logFilter.setId(config.getId());
		logFilter.setIncludeLastLog(Boolean.TRUE);
		List<AbstractSysSyncConfigDto> configs = syncConfigService.find(logFilter, null).getContent();
		Assert.assertEquals(1, configs.size());
		SysSyncLogDto log = configs.get(0).getLastSyncLog();
		if (actionType == null) {
			return log;
		}

		SysSyncActionLogFilter actionLogFilter = new SysSyncActionLogFilter();
		actionLogFilter.setSynchronizationLogId(log.getId());
		List<SysSyncActionLogDto> actions = syncActionLogService.find(actionLogFilter, null).getContent();

		if (actions.isEmpty()) {
			Assert.assertEquals(count, 0);
			return log;
		}
		
		SysSyncActionLogDto actionLog = actions.stream().filter(action -> {
			return actionType == action.getSyncAction();
		}).findFirst().get();

		Assert.assertEquals(resultType, actionLog.getOperationResult());
		SysSyncItemLogFilter itemLogFilter = new SysSyncItemLogFilter();
		itemLogFilter.setSyncActionLogId(actionLog.getId());
		List<SysSyncItemLogDto> items = syncItemLogService.find(itemLogFilter, null).getContent();
		Assert.assertEquals(count, items.size());

		return log;
	}
	
	public SysSyncIdentityConfigDto doCreateSyncConfig(SysSystemDto system) {
		return this.doCreateSyncConfig(system, false);
	}

	public SysSyncIdentityConfigDto doCreateSyncConfig(SysSystemDto system, boolean correlationByEmail) {

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		mappingFilter.setSystemId(system.getId());
		mappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		List<SysSystemMappingDto> mappings = systemMappingService.find(mappingFilter, null).getContent();
		Assert.assertEquals(1, mappings.size());
		SysSystemMappingDto mapping = mappings.get(0);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mapping.getId());

		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto correlationAttribute = attributes.stream().filter(attribute -> {
			if (correlationByEmail) {
				return ATTRIBUTE_EMAIL.equals(attribute.getIdmPropertyName());
			}
			
			return attribute.isUid();
		}).findFirst().orElse(null);

		// Create default synchronization config
		SysSyncIdentityConfigDto syncConfigCustom = new SysSyncIdentityConfigDto();
		syncConfigCustom.setReconciliation(true);
		syncConfigCustom.setCustomFilter(false);
		syncConfigCustom.setSystemMapping(mapping.getId());
		syncConfigCustom.setCorrelationAttribute(correlationAttribute.getId());
		syncConfigCustom.setName(SYNC_CONFIG_NAME);
		syncConfigCustom.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
		syncConfigCustom.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK);
		syncConfigCustom.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigCustom.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		syncConfigCustom = (SysSyncIdentityConfigDto) syncConfigService.save(syncConfigCustom);

		SysSyncConfigFilter configFilter = new SysSyncConfigFilter();
		configFilter.setSystemId(system.getId());
		Assert.assertEquals(1, syncConfigService.find(configFilter, null).getTotalElements());
		return syncConfigCustom;
	}
	
	private SysSystemDto initData() {
		return this.initData(IDENTITY_ONE);
	}

	private SysSystemDto initData(String username) {
		return this.initData(username, IDENTITY_ONE_EMAIL);
	}
	
	private SysSystemDto initData(String username, String email) {

		// create test system
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		Assert.assertNotNull(system);

		// generate schema for system
		List<SysSchemaObjectClassDto> objectClasses = systemService.generateSchema(system);

		// Create synchronization mapping
		SysSystemMappingDto syncSystemMapping = new SysSystemMappingDto();
		syncSystemMapping.setName(getHelper().createName());
		syncSystemMapping.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		syncSystemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncSystemMapping.setObjectClass(objectClasses.get(0).getId());
		syncSystemMapping.setAccountType(AccountType.PERSONAL);
		final SysSystemMappingDto syncMapping = systemMappingService.save(syncSystemMapping);
		createMapping(system, syncMapping);
		this.getBean().initIdentityData(username, email);
		return system;

	}

	/**
	 * Method set and persist data to test resource
	 */
	@Transactional
	public void setTestData(String name, String firstName, String lastName) {
		TestResource resourceUser = new TestResource();
		resourceUser.setName(name);
		resourceUser.setFirstname(firstName);
		resourceUser.setLastname(lastName);
		entityManager.persist(resourceUser);
	}

	@Transactional
	public void initIdentityData(String username, String email) {
		deleteAllResourceData();

		TestResource resourceUserOne = new TestResource();
		resourceUserOne.setName(username);
		resourceUserOne.setFirstname(username);
		resourceUserOne.setLastname(username);
		resourceUserOne.setEavAttribute("1");
		resourceUserOne.setEmail(email);
		entityManager.persist(resourceUserOne);

	}
	
	@Transactional
	public void setStateToDescriptionValue (String username, String status) {
		//TestResource resourceUser = new TestResource();
		TestResource resourceUser = entityManager.find(TestResource.class, username);
		resourceUser.setDescrip(status);
		//entityManager.persist(resourceUser);
		//entityManager.merge(resourceUser);
	}
	
	private void setDescriptionMappedToState(SysSystemDto system, boolean withTransfor) {
		SysSystemAttributeMappingFilter descAttrMapFilt = new SysSystemAttributeMappingFilter();
		descAttrMapFilt.setSystemId(system.getId());
		descAttrMapFilt.setIdmPropertyName("description");
		List<SysSystemAttributeMappingDto> mapAttrs = schemaAttributeMappingService.find(descAttrMapFilt,null).getContent();
		if (mapAttrs.size() == 0) { // try if re-mapping has been already set
			descAttrMapFilt.setIdmPropertyName(IDM_IDENTITY_STATE);
			mapAttrs = schemaAttributeMappingService.find(descAttrMapFilt,null).getContent();
		}
		Assert.assertEquals(1, mapAttrs.size());
		
		SysSystemAttributeMappingDto descAttrMap = mapAttrs.get(0);
		descAttrMap.setIdmPropertyName(IDM_IDENTITY_STATE);
		descAttrMap.setName(ATTRIBUTE_DESCRIP);
		if (withTransfor) {
			StringBuilder sb = new StringBuilder("import eu.bcvsolutions.idm.core.api.domain.IdentityState;\n");
			sb.append("return IdentityState.CREATED;\n");
			descAttrMap.setTransformFromResourceScript(sb.toString());
		}
		schemaAttributeMappingService.save(descAttrMap);
	}

	private void createMapping(SysSystemDto system, final SysSystemMappingDto entityHandlingResult) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		Page<SysSchemaAttributeDto> schemaAttributesPage = schemaAttributeService.find(schemaAttributeFilter, null);
		schemaAttributesPage.forEach(schemaAttr -> {
			if (ATTRIBUTE_NAME.equals(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setUid(true);
				attributeMapping.setEntityAttribute(true);
				attributeMapping.setIdmPropertyName("username");
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);

			} else if ("firstname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("firstName");
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setName(schemaAttr.getName());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);

			} else if ("lastname".equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("lastName");
				attributeMapping.setName(schemaAttr.getName().toLowerCase());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);

			} else if (ATTRIBUTE_EMAIL.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName(ATTRIBUTE_EMAIL);
				attributeMapping.setName(schemaAttr.getName().toLowerCase());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);
				SysSystemAttributeMappingDto attributeMappingTwo = new SysSystemAttributeMappingDto();
				attributeMappingTwo.setIdmPropertyName(ATTRIBUTE_EMAIL_TWO);
				attributeMappingTwo.setEntityAttribute(false);
				attributeMappingTwo.setExtendedAttribute(true);
				attributeMappingTwo.setName(ATTRIBUTE_EMAIL_TWO);
				attributeMappingTwo.setSchemaAttribute(schemaAttr.getId());
				attributeMappingTwo.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMappingTwo);
			} else if (ATTRIBUTE_DESCRIP.equalsIgnoreCase(schemaAttr.getName())) {
				SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
				attributeMapping.setIdmPropertyName("description");
				attributeMapping.setName(schemaAttr.getName().toLowerCase());
				attributeMapping.setSchemaAttribute(schemaAttr.getId());
				attributeMapping.setSystemMapping(entityHandlingResult.getId());
				schemaAttributeMappingService.save(attributeMapping);
			}
		});
	}

	private SysSystemMappingDto createMappingWithProtection(SysSystemDto system) {
		return createMappingWithProtection(system, null);
	}

	private SysSystemMappingDto createMappingWithProtection(SysSystemDto system, Integer protectionInterval) {
		SysSystemMappingDto systemMapping = helper.createMapping(system);
		systemMapping.setProtectionEnabled(true);
		systemMapping.setProtectionInterval(protectionInterval);
		systemMappingService.save(systemMapping);
		return systemMapping;
	}

	private void createSystemEntityWish(SysSystemDto system) {
		SysSystemEntityDto systemEntity = new SysSystemEntityDto();
		systemEntity.setUid(IDENTITY_ONE);
		systemEntity.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemEntity.setWish(true);
		systemEntity.setSystem(system.getId());
		systemEntity = systemEntityService.save(systemEntity);
	}

	private void checkIdentityAccount(IdmIdentityDto identity, int numberOfAccounts, UUID identityRole) {
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(identityAccountFilter, null).getContent();
		Assert.assertEquals(numberOfAccounts, identityAccounts.size());
		if (numberOfAccounts == 0) {
			return;
		}
		Assert.assertEquals(identityRole, identityAccounts.get(0).getIdentityRole());
	}

	private void checkAccAccount(int numberOfAccounts, boolean inProtection, ZonedDateTime endOfProtection) {
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setUid(IDENTITY_ONE);
		List<AccAccountDto> accAccounts = accAccountService.find(accountFilter, null).getContent();
		Assert.assertEquals(numberOfAccounts, accAccounts.size());
		if (numberOfAccounts == 0) {
			return;
		}
		Assert.assertEquals(inProtection, accAccounts.get(0).isInProtection());
		Assert.assertEquals(endOfProtection, accAccounts.get(0).getEndOfProtection());
	}

	@Transactional
	public void deleteAllResourceData() {
		// Delete all
		Query q = entityManager.createNativeQuery("DELETE FROM " + TestResource.TABLE_NAME);
		q.executeUpdate();
	}

	private void deleteProtectedAccount(String accountUid) {
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setUid(accountUid);
		List<AccAccountDto> accAccounts = accAccountService.find(accountFilter, null).getContent();
		if (accAccounts.size() == 0) {
			return;
		}
		Assert.assertEquals(1, accAccounts.size());
		AccAccountDto account = accAccounts.get(0);
		account.setInProtection(true); // or disable protection on the provisioning mapping
		account.setEndOfProtection(ZonedDateTime.now().minusMonths(1));
		account = accAccountService.save(account);
		accAccountService.delete(account);

		accAccounts = accAccountService.find(accountFilter, null).getContent();
		Assert.assertEquals(0, accAccounts.size());
	}
	
	private IdentitySyncTest getBean() {
		return applicationContext.getAutowireCapableBeanFactory().createBean(this.getClass());
	}
}
