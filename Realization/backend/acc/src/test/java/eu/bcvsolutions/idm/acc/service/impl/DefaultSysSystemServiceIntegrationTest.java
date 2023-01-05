package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorKeyDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
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
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.event.SystemEvent;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.acc.rest.impl.SysSystemController;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyGenerateType;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.service.api.CheckLongPollingResult;
import eu.bcvsolutions.idm.core.model.service.api.LongPollingManager;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultLongPollingManager;
import eu.bcvsolutions.idm.core.rest.DeferredResultWrapper;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcObjectPoolConfiguration;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInstanceImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Target system tests.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultSysSystemServiceIntegrationTest extends AbstractIntegrationTest {
	
	// TODO: remove + transactional
	private static final String SYSTEM_NAME_ONE = "test_system_one_" + System.currentTimeMillis();
	private static final String SYSTEM_NAME_TWO = "test_system_two_" + System.currentTimeMillis();
	
	@Autowired private ApplicationContext context;
	@Autowired private TestHelper helper;
	@Autowired private IdmFormDefinitionService formDefinitionService;	
	@Autowired private IdmFormAttributeService formAttributeService;	
	@Autowired private FormService formService;	
	@Autowired private IcConfigurationFacade icConfigurationAggregatorService;
	@Autowired private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired private SysSchemaAttributeService schemaAttributeService;
	@Autowired private SysRoleSystemService roleSystemService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired private SysRoleSystemAttributeService roleSystemAttributeService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemEntityService systemEntityService;
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private SysSyncLogService syncLogService;
	@Autowired private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired private SysProvisioningOperationService provisioningOperationService;
	@Autowired private LongPollingManager longPollingManager;
	@Autowired private SysSyncItemLogService syncItemLogService;
	@Autowired private SysSyncActionLogService syncActionLogService;
	@Autowired private SysSystemController systemController;
	@Autowired private IdmPasswordPolicyService passwordPolicyService;
	@Autowired private ConfidentialStorage confidentialStorage;
	//
	private DefaultSysSystemService systemService;
	
	@Before
	public void login() {
		systemService = context.getAutowireCapableBeanFactory().createBean(DefaultSysSystemService.class);
		//
		loginAsAdmin();
	}
	
	@After 
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testReferentialIntegrity() {
		SysSystemDto system = new SysSystemDto();
		String systemName = getHelper().createName();
		system.setName(systemName);
		system = systemService.save(system);
		// object class
		SysSchemaObjectClassDto objectClass = new SysSchemaObjectClassDto();
		objectClass.setSystem(system.getId());
		objectClass.setObjectClassName("obj_class");
		objectClass = schemaObjectClassService.save(objectClass);	
		SysSchemaObjectClassFilter objectClassFilter = new SysSchemaObjectClassFilter();
		objectClassFilter.setSystemId(system.getId());
		// schema attribute
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setObjectClass(objectClass.getId());
		schemaAttribute.setName("name");
		schemaAttribute.setClassType("class");
		schemaAttribute = schemaAttributeService.save(schemaAttribute);
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());	
		// system entity handling
		SysSystemMappingDto systemMapping = new SysSystemMappingDto();
		systemMapping.setName("default_" + System.currentTimeMillis());
		systemMapping.setObjectClass(objectClass.getId());
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping.setAccountType(AccountType.PERSONAL);
		systemMapping.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemMapping = systemMappingService.save(systemMapping);
		SysSystemMappingFilter entityHandlingFilter = new SysSystemMappingFilter();
		entityHandlingFilter.setSystemId(system.getId());
		// schema attribute handling
		SysSystemAttributeMappingDto schemaAttributeHandling = new SysSystemAttributeMappingDto();
		schemaAttributeHandling.setSchemaAttribute(schemaAttribute.getId());
		schemaAttributeHandling.setSystemMapping(systemMapping.getId());
		schemaAttributeHandling.setName("name");
		schemaAttributeHandling.setIdmPropertyName("name");
		schemaAttributeHandling = systemAttributeMappingService.save(schemaAttributeHandling);
		SysSystemAttributeMappingFilter schemaAttributeHandlingFilter = new SysSystemAttributeMappingFilter(); 
		schemaAttributeHandlingFilter.setSystemId(system.getId());		
		// role system
		IdmRoleDto role = helper.createRole();
		SysRoleSystemDto roleSystem = new SysRoleSystemDto();
		roleSystem.setSystem(system.getId());
		roleSystem.setRole(role.getId());
		roleSystem.setSystemMapping(systemMapping.getId());
		roleSystem = roleSystemService.save(roleSystem);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(role.getId());
		// role system attributes
		SysRoleSystemAttributeDto roleSystemAttribute = new SysRoleSystemAttributeDto();
		roleSystemAttribute.setRoleSystem(roleSystem.getId());
		roleSystemAttribute.setSystemAttributeMapping(schemaAttributeHandling.getId());
		roleSystemAttribute.setName("name");
		roleSystemAttribute.setIdmPropertyName("name");
		roleSystemAttribute = roleSystemAttributeService.save(roleSystemAttribute);
		
		assertEquals(systemName, systemService.getByCode(systemName).getName());
		assertEquals(1, schemaObjectClassService.find(objectClassFilter, null).getTotalElements());
		assertEquals(1, schemaAttributeService.find(schemaAttributeFilter, null).getTotalElements());
		assertEquals(1, systemMappingService.find(entityHandlingFilter, null).getTotalElements());
		assertEquals(1, systemAttributeMappingService.find(schemaAttributeHandlingFilter, null).getTotalElements());
		assertEquals(1, roleSystemService.find(roleSystemFilter, null).getTotalElements());
		assertNotNull(roleSystemAttributeService.get(roleSystemAttribute.getId()));
		
		systemService.delete(system);
		
		assertNull(systemService.getByCode(systemName));
		assertEquals(0, schemaObjectClassService.find(objectClassFilter, null).getTotalElements());
		assertEquals(0, schemaAttributeService.find(schemaAttributeFilter, null).getTotalElements());
		assertEquals(0, systemMappingService.find(entityHandlingFilter, null).getTotalElements());
		assertEquals(0, systemAttributeMappingService.find(schemaAttributeHandlingFilter, null).getTotalElements());
		assertEquals(0, roleSystemService.find(roleSystemFilter, null).getTotalElements());
		assertNull(roleSystemAttributeService.get(roleSystemAttribute.getId()));
	}
	
	@Test
	public void testReferentialIntegrityPasswordGenerationPolicy() {
		SysSystemDto system = helper.createSystem(helper.createName());
		IdmPasswordPolicyDto passPolicy = new IdmPasswordPolicyDto();
		passPolicy.setName("testPolicyName");
		passPolicy.setType(IdmPasswordPolicyType.GENERATE);
		passPolicy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		passPolicy.setMinPasswordLength(5);
		passPolicy.setMaxPasswordLength(12);
		passPolicy = passwordPolicyService.save(passPolicy);
		  
		system.setPasswordPolicyGenerate(passPolicy.getId());
		system = systemService.save(system);
		
		// test that password policy exists in the system it was set to
		assertNotNull(system.getPasswordPolicyGenerate());
		assertNull(system.getPasswordPolicyValidate());
		
		// removing password policy from IdM
		passwordPolicyService.delete(passPolicy);
		// reloading 'system DTO' setting from IdM
		system = systemService.get(system.getId());
		
		// test that reloaded 'system DTO' doesn't have set password policy after its deletion
		assertNull(system.getPasswordPolicyGenerate());
		assertNull(system.getPasswordPolicyValidate());
		
		systemService.delete(system);
	}
	
	@Test
	public void testReferentialIntegrityPasswordValidationPolicy() {
		SysSystemDto system = helper.createSystem(helper.createName());
		IdmPasswordPolicyDto passPolicy = new IdmPasswordPolicyDto();
		passPolicy.setName("testPolicyName");
		passPolicy.setType(IdmPasswordPolicyType.VALIDATE);
		passPolicy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		passPolicy.setMinPasswordLength(5);
		passPolicy.setMaxPasswordLength(12);
		passPolicy = passwordPolicyService.save(passPolicy);
		
		system.setPasswordPolicyValidate(passPolicy.getId());
		system = systemService.save(system);
		
		// test that password policy exists in the system it was set to
		assertNotNull(system.getPasswordPolicyValidate());
		assertNull(system.getPasswordPolicyGenerate());
		
		// removing password policy from IdM
		passwordPolicyService.delete(passPolicy);
		// reloading 'system DTO' setting from IdM
		system = systemService.get(system.getId());
		
		// test that reloaded 'system DTO' doesn't have set password policy after its deletion
		assertNull(system.getPasswordPolicyValidate());
		assertNull(system.getPasswordPolicyGenerate());
		
		systemService.delete(system);		
	} 
	
	@Test
	public void testReferentialIntegritySystemIsPreservedAfterDeletingPasswordPolicy() {
		SysSystemDto system = helper.createSystem(helper.createName());
		IdmPasswordPolicyDto passPolicy = new IdmPasswordPolicyDto();
		passPolicy.setName("testPolicyName");
		passPolicy.setGenerateType(IdmPasswordPolicyGenerateType.RANDOM);
		passPolicy.setMinPasswordLength(5);
		passPolicy.setMaxPasswordLength(12);
		passPolicy = passwordPolicyService.save(passPolicy);
		
		system.setPasswordPolicyValidate(passPolicy.getId());
		system.setPasswordPolicyGenerate(passPolicy.getId());
		system = systemService.save(system);
		
		// test that password policy exists in the system it was set to
		assertNotNull(system.getPasswordPolicyValidate());
		assertNotNull(system.getPasswordPolicyGenerate());
		
		// removing system from IdM
		systemService.delete(system);
		
		// reloading 'password policy DTO' setting from IdM
		passPolicy = passwordPolicyService.get(passPolicy.getId());
		
		// test that policy still exists after system deletion
		assertNotNull(passPolicy);
		
		passwordPolicyService.delete(passPolicy);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityAccountExists() {
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// account
		AccAccountDto account = new AccAccountDto();
		account.setSystem(system.getId());
		account.setUid("test_uid_" + System.currentTimeMillis());
		account = accountService.save(account);
		
		systemService.delete(system);
	}
	
	@Test
	public void testReferentialIntegritySystemEntityExists() {
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);
		// system entity
		SysSystemEntityDto systemEntity = new SysSystemEntityDto();
		systemEntity.setSystem(system.getId());
		systemEntity.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemEntity.setUid("se_uid_" + System.currentTimeMillis());
		systemEntity = systemEntityService.save(systemEntity);
		
		systemService.delete(system);
		
		assertNull(systemService.getByCode(system.getCode()));
		assertNull(systemEntityService.get(systemEntity.getId()));
	}
	
	@Test(expected = ResultCodeException.class)
	public void testReferentialIntegrityProvisioningOperationExists() {
		SysSystemDto system = new SysSystemDto();
		String systemName = "t_s_" + System.currentTimeMillis();
		system.setName(systemName);
		system = systemService.save(system);

		// system entity
		SysSystemEntityDto systemEntity = new SysSystemEntityDto();
		systemEntity.setUid("test");
		systemEntity.setSystem(system.getId());
		systemEntity.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);		
		systemEntity = systemEntityService.save(systemEntity);
		
		SysProvisioningOperationDto provisioningOperation = new SysProvisioningOperationDto();
		provisioningOperation.setSystem(system.getId());
		provisioningOperation.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperation.setOperationType(ProvisioningEventType.CREATE);
		provisioningOperation.setSystemEntity(systemEntity.getId());
		provisioningOperation.setEntityIdentifier(UUID.randomUUID());
		provisioningOperation.setProvisioningContext(new ProvisioningContext());
		provisioningOperation.setResult(new OperationResult());
		provisioningOperationService.save(provisioningOperation);
		//
		systemService.delete(system);
	}
	
	/**
	 * Test add and delete extended attributes to owner
	 */
	@Test
	public void testFormAttributes() {
		// create owner
		SysSystemDto system = new SysSystemDto();
		system.setName(SYSTEM_NAME_ONE);
		system = systemService.save(system);	
		SysSystemDto systemOne = systemService.getByCode(SYSTEM_NAME_ONE);		
		assertEquals(SYSTEM_NAME_ONE, systemOne.getName());
		//
		// create definition one
		IdmFormDefinitionDto formDefinitionOne = new IdmFormDefinitionDto();
		formDefinitionOne.setType(SysSystem.class.getCanonicalName());
		formDefinitionOne.setCode("v1");
		formDefinitionOne = formDefinitionService.save(formDefinitionOne);
		
		IdmFormAttributeDto attributeDefinitionOne = new IdmFormAttributeDto();
		attributeDefinitionOne.setFormDefinition(formDefinitionOne.getId());
		attributeDefinitionOne.setCode("name_" + System.currentTimeMillis());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);			
		attributeDefinitionOne = formAttributeService.save(attributeDefinitionOne);
		formDefinitionOne = formDefinitionService.get(formDefinitionOne.getId());
		//
		// create definition two
		IdmFormDefinitionDto formDefinitionTwo = new IdmFormDefinitionDto();
		formDefinitionTwo.setType(SysSystem.class.getCanonicalName());
		formDefinitionTwo.setCode("v2");
		formDefinitionTwo = formDefinitionService.save(formDefinitionTwo);
		
		IdmFormAttributeDto attributeDefinitionTwo = new IdmFormAttributeDto();
		attributeDefinitionTwo.setFormDefinition(formDefinitionTwo.getId());
		attributeDefinitionTwo.setCode("name_" + System.currentTimeMillis());
		attributeDefinitionTwo.setName(attributeDefinitionTwo.getCode());
		attributeDefinitionTwo.setPersistentType(PersistentType.TEXT);			
		attributeDefinitionTwo = formAttributeService.save(attributeDefinitionTwo);
		formDefinitionTwo = formDefinitionService.get(formDefinitionTwo.getId());
		//		
		IdmFormValueDto value1 = new IdmFormValueDto(attributeDefinitionOne);
		value1.setValue("test1");
		
		IdmFormValueDto value2 = new IdmFormValueDto(attributeDefinitionTwo);
		value2.setValue("test2");
		
		formService.saveValues(system, formDefinitionOne, Lists.newArrayList(value1));
		formService.saveValues(system, formDefinitionTwo, Lists.newArrayList(value2));
		
		assertEquals("test1", formService.getValues(system, formDefinitionOne).get(0).getValue());
		assertEquals("test2", formService.getValues(system, formDefinitionTwo).get(0).getValue());
		assertEquals("test2", formService.getValues(system, formDefinitionTwo, attributeDefinitionTwo.getName()).get(0).getValue());
		//
		// create second owner
		SysSystemDto systemTwo = new SysSystemDto();
		systemTwo.setName(SYSTEM_NAME_TWO);		
		systemTwo = systemService.save(systemTwo);

		assertEquals(0, formService.getValues(systemTwo, formDefinitionOne).size());
		assertEquals(0, formService.getValues(systemTwo, formDefinitionTwo).size());
		assertEquals(1, formService.getValues(system, formDefinitionOne).size());
		assertEquals(1, formService.getValues(system, formDefinitionTwo).size());
		
		systemService.delete(systemTwo);
		
		assertEquals(1, formService.getValues(system, formDefinitionOne).size());
		assertEquals(1, formService.getValues(system, formDefinitionTwo).size());
		
		formService.deleteValues(system, formDefinitionOne);		
		assertEquals(0, formService.getValues(system, formDefinitionOne).size());
		assertEquals("test2", formService.getValues(system, formDefinitionTwo).get(0).getValue());
		
		systemService.delete(system);
	}
	
	@Test
	public void testCreateConnectorConfiguration() {
		// TODO: test system will be moved here, after UI eav form implementation
		@SuppressWarnings("deprecation")
		IcConnectorKey connectorKey = systemService.getTestConnectorKey();
		
		// create connector instance impl with connector key
		IcConnectorInstance connectorInstance = new IcConnectorInstanceImpl(null, connectorKey, false);
		
		IcConnectorConfiguration conf = icConfigurationAggregatorService.getConnectorConfiguration(connectorInstance);
		
		IdmFormDefinitionDto savedFormDefinition = systemService.getConnectorFormDefinition(connectorInstance);
		
		assertEquals(conf.getConfigurationProperties().getProperties().size(), savedFormDefinition.getFormAttributes().size());
		assertEquals(conf.getConfigurationProperties().getProperties().get(3).getDisplayName(), savedFormDefinition.getFormAttributes().get(3).getName());
	}
	
	@Test
	public void testFillConnectorConfiguration() {
		// create owner
		@SuppressWarnings("deprecation")
		SysSystemDto system =  systemService.createTestSystem();		
		IcConnectorConfiguration connectorConfiguration = systemService.getConnectorConfiguration(system);		
		assertEquals(15, connectorConfiguration.getConfigurationProperties().getProperties().size());
		//
		// check all supported data types
		// TODO: add all supported types
		Integer checked = 0;
		for(IcConfigurationProperty property : connectorConfiguration.getConfigurationProperties().getProperties()) {
			switch(property.getName()) {
				case "host": {
					assertEquals("localhost", property.getValue());
					checked++;
					break;
				}
				case "password": {
					assertEquals(new org.identityconnectors.common.security.GuardedString("idmadmin".toCharArray()), property.getValue());
					checked++;
					break;
				}
				case "rethrowAllSQLExceptions": {
					assertEquals(true, property.getValue());
					checked++;
					break;
				}
			}
		};		
		assertEquals(Integer.valueOf(3), checked);
	}
	
	@Test
	public void testDefaultFormDefinitionNotExists() {
		assertNull(formService.getDefinition(SysSystem.class));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testReadValuesFromDefaultFormDefinitionNotExists() {
		SysSystemDto system = new SysSystemDto();
		system.setName(SYSTEM_NAME_ONE + "_" + System.currentTimeMillis());
		system = systemService.save(system);
		formService.getValues(system);
	}
	
	@Test
	public void checkSystemValid() {
		// create test system
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME);
		// do test system
		systemService.checkSystem(system);
	}
	
	@Test(expected = RuntimeException.class)
	public void checkSystemUnValid() {
		// create test system
		SysSystemDto system =  helper.createSystem(TestResource.TABLE_NAME);
		
		// set wrong password
		formService.saveValues(system, "password", ImmutableList.of("wrongPassword"));
		
		// do test system
		systemService.checkSystem(system);
	}
	
	@Test
	public void duplicateSystem(){
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true);
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		// Number of schema attributes on original system
		int numberOfSchemaAttributesOrig = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		SysSystemMappingDto mappingOrig = helper.getDefaultMapping(system);
		// Number of mapping attributes on original system
		int numberOfMappingAttributesOrig = systemAttributeMappingService.findBySystemMapping(mappingOrig).size();
		
		EntityEvent<SysSystemDto> event = new SystemEvent(SystemEventType.DUPLICATE, system);
		SysSystemDto duplicatedSystem = systemService.publish(event).getContent();
		
		// check duplicate
		systemService.checkSystem(duplicatedSystem);
		
		Assert.assertNotEquals(system.getId(), duplicatedSystem.getId());
		
		schemaAttributeFilter.setSystemId(duplicatedSystem.getId());
		// Number of schema attributes on duplicated system
		int numberOfSchemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		Assert.assertEquals(numberOfSchemaAttributesOrig, numberOfSchemaAttributes);
		
		SysSystemMappingDto mapping = helper.getDefaultMapping(duplicatedSystem);
		// Number of mapping attributes on duplicated system
		int numberOfMappingAttributes = systemAttributeMappingService.findBySystemMapping(mapping).size();
		Assert.assertEquals(numberOfMappingAttributesOrig, numberOfMappingAttributes);
	}

	@Test
	public void duplicateSystemWithSynchronization(){
		String syncName = "test-sync-config";
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true);
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		// Number of schema attributes on original system
		int numberOfSchemaAttributesOrig = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		SysSystemMappingDto mappingOrig = helper.getDefaultMapping(system);
		// Number of mapping attributes on original system
		int numberOfMappingAttributesOrig = systemAttributeMappingService.findBySystemMapping(mappingOrig).size();
		
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingOrig.getId());
		
		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto nameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equalsIgnoreCase(TestHelper.ATTRIBUTE_MAPPING_NAME);
		}).findFirst().get();

		SysSystemAttributeMappingDto firstNameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equalsIgnoreCase(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		}).findFirst().get();
		
		SysSystemAttributeMappingDto emailAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equalsIgnoreCase(TestHelper.ATTRIBUTE_MAPPING_EMAIL);
		}).findFirst().get();
		
		// create synchronization config
		AbstractSysSyncConfigDto syncConfigDuplicate = new SysSyncIdentityConfigDto();
		syncConfigDuplicate.setCustomFilter(true);
		syncConfigDuplicate.setSystemMapping(mappingOrig.getId());
		syncConfigDuplicate.setCorrelationAttribute(nameAttribute.getId());
		syncConfigDuplicate.setTokenAttribute(firstNameAttribute.getId());
		syncConfigDuplicate.setFilterAttribute(emailAttribute.getId());
		syncConfigDuplicate.setReconciliation(true);
		syncConfigDuplicate.setName(syncName);
		syncConfigDuplicate.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
		syncConfigDuplicate.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfigDuplicate.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfigDuplicate.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);

		syncConfigDuplicate = syncConfigService.save(syncConfigDuplicate);
		
		EntityEvent<SysSystemDto> event = new SystemEvent(SystemEventType.DUPLICATE, system);
		SysSystemDto duplicatedSystem = systemService.publish(event).getContent();
		
		// check duplicate
		systemService.checkSystem(duplicatedSystem);
		
		Assert.assertNotEquals(system.getId(), duplicatedSystem.getId());
		
		schemaAttributeFilter.setSystemId(duplicatedSystem.getId());
		// Number of schema attributes on duplicated system
		int numberOfSchemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent().size();
		Assert.assertEquals(numberOfSchemaAttributesOrig, numberOfSchemaAttributes);
		
		SysSystemMappingDto mapping = helper.getDefaultMapping(duplicatedSystem);
		// Number of mapping attributes on duplicated system
		int numberOfMappingAttributes = systemAttributeMappingService.findBySystemMapping(mapping).size();
		Assert.assertEquals(numberOfMappingAttributesOrig, numberOfMappingAttributes);
		
		// check synchronization config
		SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
		syncFilter.setSystemId(duplicatedSystem.getId());
		List<AbstractSysSyncConfigDto> configs = syncConfigService.find(syncFilter, null).getContent();
		Assert.assertEquals(1, configs.size());
		
		
		Assert.assertEquals(1, configs.size());
		AbstractSysSyncConfigDto configNew = configs.get(0);
		Assert.assertFalse(configNew.isEnabled());
		
		Assert.assertTrue(configNew.isReconciliation());
		Assert.assertEquals(syncName, configNew.getName());
		Assert.assertTrue(configNew.isCustomFilter());

		Assert.assertEquals(syncConfigDuplicate.getLinkedAction(), configNew.getLinkedAction());
		Assert.assertEquals(syncConfigDuplicate.getUnlinkedAction(), configNew.getUnlinkedAction());
		Assert.assertEquals(syncConfigDuplicate.getMissingEntityAction(), configNew.getMissingEntityAction());
		Assert.assertEquals(syncConfigDuplicate.getMissingAccountAction(), configNew.getMissingAccountAction());

		SysSystemAttributeMappingDto correlationAtt = schemaAttributeMappingService.get(configNew.getCorrelationAttribute());
		SysSystemAttributeMappingDto tokenAtt = schemaAttributeMappingService.get(configNew.getTokenAttribute());
		SysSystemAttributeMappingDto filterAtt = schemaAttributeMappingService.get(configNew.getFilterAttribute());
		
		Assert.assertEquals(nameAttribute.getName(), correlationAtt.getName());
		Assert.assertEquals(nameAttribute.getIdmPropertyName(), correlationAtt.getIdmPropertyName());
		
		Assert.assertEquals(firstNameAttribute.getName(), tokenAtt.getName());
		Assert.assertEquals(firstNameAttribute.getIdmPropertyName(), tokenAtt.getIdmPropertyName());
		
		Assert.assertEquals(emailAttribute.getName(), filterAtt.getName());
		Assert.assertEquals(emailAttribute.getIdmPropertyName(), filterAtt.getIdmPropertyName());
	}
	
	@Test
	public void testPoolingConnectorDefinition(){
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true);
		IdmFormDefinitionDto formDefinition = systemService.getPoolingConnectorFormDefinition(system);
		Assert.assertNotNull(formDefinition);
		
		// Get pooling definition
		IdmFormAttributeDto attributeDto = formDefinition.getMappedAttributeByCode(SysSystemService.POOLING_SUPPORTED_PROPERTY);
		Assert.assertNotNull(attributeDto);
		Assert.assertEquals(String.valueOf(false), attributeDto.getDefaultValue());
		
		attributeDto = formDefinition.getMappedAttributeByCode(SysSystemService.MAX_IDLE_PROPERTY);
		Assert.assertNotNull(attributeDto);
		attributeDto = formDefinition.getMappedAttributeByCode(SysSystemService.MAX_OBJECTS_PROPERTY);
		Assert.assertNotNull(attributeDto);
		attributeDto = formDefinition.getMappedAttributeByCode(SysSystemService.MAX_WAIT_PROPERTY);
		Assert.assertNotNull(attributeDto);
		attributeDto = formDefinition.getMappedAttributeByCode(SysSystemService.MIN_IDLE_PROPERTY);
		Assert.assertNotNull(attributeDto);
		attributeDto = formDefinition.getMappedAttributeByCode(SysSystemService.MIN_TIME_TO_EVIC_PROPERTY);
		Assert.assertNotNull(attributeDto);
	}
	
	@Test
	public void testPoolingConnectorConfiguration(){
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true);
		IdmFormDefinitionDto formDefinition = systemService.getPoolingConnectorFormDefinition(system);
		Assert.assertNotNull(formDefinition);
		systemService.save(system);
		
		List<IdmFormValueDto> values = formService.getValues(system, formDefinition);
		Assert.assertNotNull(values);
		Assert.assertEquals(0, values.size());
		values = Lists.newArrayList();
		
		IdmFormValueDto formValueDto = new IdmFormValueDto(formService.getAttribute(formDefinition, SysSystemService.POOLING_SUPPORTED_PROPERTY));
		// Change value
		formValueDto.setValue(true);
		values.add(formValueDto);
		
		formValueDto = new IdmFormValueDto(formService.getAttribute(formDefinition, SysSystemService.MAX_IDLE_PROPERTY));
		// Change value
		formValueDto.setValue(111);
		values.add(formValueDto);

		formValueDto = new IdmFormValueDto(formService.getAttribute(formDefinition, SysSystemService.MAX_OBJECTS_PROPERTY));
		// Change value
		formValueDto.setValue(222);
		values.add(formValueDto);
		
		formValueDto = new IdmFormValueDto(formService.getAttribute(formDefinition, SysSystemService.MIN_IDLE_PROPERTY));
		// Change value
		formValueDto.setValue(333);
		values.add(formValueDto);
	
		formValueDto = new IdmFormValueDto(formService.getAttribute(formDefinition, SysSystemService.MAX_WAIT_PROPERTY));
		// Change value
		formValueDto.setValue((long)444);
		values.add(formValueDto);
		
		formValueDto = new IdmFormValueDto(formService.getAttribute(formDefinition, SysSystemService.MIN_TIME_TO_EVIC_PROPERTY));
		// Change value
		formValueDto.setValue((long)555);
		values.add(formValueDto);
		
		// Save all values
		formService.saveValues(system, formDefinition, values);
		
		IcConnectorConfiguration connectorConfiguration = systemService.getConnectorConfiguration(system);
		Assert.assertNotNull(connectorConfiguration);
		Assert.assertTrue(connectorConfiguration.isConnectorPoolingSupported());
		
		IcObjectPoolConfiguration poolConfiguration = connectorConfiguration.getConnectorPoolConfiguration();
		Assert.assertNotNull(poolConfiguration);
		
		Assert.assertEquals(111, poolConfiguration.getMaxIdle());
		Assert.assertEquals(222, poolConfiguration.getMaxObjects());
		Assert.assertEquals(333, poolConfiguration.getMinIdle());
		Assert.assertEquals(444, poolConfiguration.getMaxWait());
		Assert.assertEquals(555, poolConfiguration.getMinEvictableIdleTimeMillis());
	}
	
	@Test
	public void testExecuteDeferredResultOnCreateSyncItemLog() {
		DefaultLongPollingManager defaultPollingManager = (DefaultLongPollingManager) longPollingManager;
		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();
		
		// create test system and sync
		SysSystemDto system = helper.createTestResourceSystem(true);
		AbstractSysSyncConfigDto syncConfig = createSync(system);

		DeferredResult<OperationResultDto> result = new DeferredResult<OperationResultDto>(1000000l,
				new OperationResultDto(OperationState.NOT_EXECUTED));
		DeferredResultWrapper wrapper = new DeferredResultWrapper(system.getId(), system.getClass(), result);
		wrapper.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result,
					LongPollingSubscriber subscriber) {
				systemController.checkDeferredRequest(result, subscriber);
			}
		});

		Queue<DeferredResultWrapper> suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(0, suspendedRequests.size());

		longPollingManager.addSuspendedResult(wrapper);

		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(1, suspendedRequests.size());

		longPollingManager.checkDeferredRequests(SysSystemDto.class);
		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(1, suspendedRequests.size());
		helper.startSynchronization(syncConfig);

		SysSyncConfigFilter logFilter = new SysSyncConfigFilter();
		logFilter.setId(syncConfig.getId());
		logFilter.setIncludeLastLog(Boolean.TRUE);
		List<AbstractSysSyncConfigDto> configs = syncConfigService.find(logFilter, null).getContent();
		Assert.assertEquals(1, configs.size());
		SysSyncLogDto log = new SysSyncLogDto();
		log.setSynchronizationConfig(syncConfig.getId());
		log = syncLogService.save(log);
	
		SysSyncActionLogDto mockAction = new SysSyncActionLogDto();
		mockAction.setSyncAction(SynchronizationActionType.IGNORE);
		mockAction.setSyncLog(log.getId());
		mockAction.setOperationResult(OperationResultType.IGNORE);
		mockAction = syncActionLogService.save(mockAction);

		// Sync item log created -> must be detected
		SysSyncItemLogDto mockItemLogDto = new SysSyncItemLogDto();
		mockItemLogDto.setSyncActionLog(mockAction.getId());
		mockItemLogDto.setIdentification(helper.createName());
		mockItemLogDto = syncItemLogService.save(mockItemLogDto);

		// Check must be called twice, because first detect the change and second remove
		// ended deferred result (from some reason is not invoked method
		// result.onCompleted)
		longPollingManager.checkDeferredRequests(SysSystemDto.class);
		longPollingManager.checkDeferredRequests(SysSystemDto.class);

		suspendedRequests = defaultPollingManager.getSuspendedRequests();
		assertEquals(0, suspendedRequests.size());

		// Clear deferred result and subscribers
		defaultPollingManager.getSuspendedRequests().clear();
		defaultPollingManager.getRegistredSubscribers().clear();
	}
	
	/**
	 * Add connector attribute into form definition after definition exist (e.g. attribute was added into connector configuration).
	 */
	@Test
	@Transactional
	public void testAddConnectorAttributeDefensivelly() {
		// tested on AD connector
		SysSystemDto system = new SysSystemDto();
		system.setName(getHelper().createName());
		//
		IcConnectorKeyImpl key = new IcConnectorKeyImpl();
		key.setFramework("connId");
		key.setConnectorName("net.tirasa.connid.bundles.ad.ADConnector");
		key.setBundleName("net.tirasa.connid.bundles.ad");
		key.setBundleVersion("1.3.4.27");
		//
		system.setConnectorKey(new SysConnectorKeyDto(key));
		system = systemService.save(system);

		IdmFormDefinitionDto formDefinition = systemService.getConnectorFormDefinition(system);
		//
		// remove some attribute (e.g. first one)
		Assert.assertFalse(formDefinition.getFormAttributes().isEmpty());
		int attributesCount = formDefinition.getFormAttributes().size();
		IdmFormAttributeDto lastAttribute = formDefinition.getFormAttributes().get(attributesCount - 1);
		//
		// delete attribute
		formService.deleteAttribute(lastAttribute);
		Assert.assertEquals(attributesCount - 1, formService.getDefinition(formDefinition.getId()).getFormAttributes().size());
		//
		formDefinition = systemService.getConnectorFormDefinition(system);
		Assert.assertEquals(attributesCount, formDefinition.getFormAttributes().size());
		//
		IdmFormAttributeDto recreatedLastAttribute = formDefinition.getFormAttributes().get(attributesCount - 1);
		//
		Assert.assertEquals(lastAttribute.getCode(), recreatedLastAttribute.getCode());
		Assert.assertEquals(lastAttribute.getName(), recreatedLastAttribute.getName());
		Assert.assertEquals(lastAttribute.getDefaultValue(), recreatedLastAttribute.getDefaultValue());
		Assert.assertEquals(lastAttribute.getDescription(), recreatedLastAttribute.getDescription());
		Assert.assertEquals(lastAttribute.getFaceType(), recreatedLastAttribute.getFaceType());
		Assert.assertEquals(lastAttribute.getPersistentType(), recreatedLastAttribute.getPersistentType());
		Assert.assertEquals(lastAttribute.getPlaceholder(), recreatedLastAttribute.getPlaceholder());
		Assert.assertEquals(lastAttribute.getRegex(), recreatedLastAttribute.getRegex());
		Assert.assertEquals(lastAttribute.getValidationMessage(), recreatedLastAttribute.getValidationMessage());
		Assert.assertEquals(lastAttribute.getMax(), recreatedLastAttribute.getMax());
		Assert.assertEquals(lastAttribute.getMin(), recreatedLastAttribute.getMin());
		Assert.assertEquals(lastAttribute.getSeq(), recreatedLastAttribute.getSeq());
		Assert.assertEquals(lastAttribute.isConfidential(), recreatedLastAttribute.isConfidential());
		Assert.assertEquals(lastAttribute.isRequired(), recreatedLastAttribute.isRequired());
		Assert.assertEquals(lastAttribute.isReadonly(), recreatedLastAttribute.isReadonly());
		Assert.assertEquals(lastAttribute.isMultiple(), recreatedLastAttribute.isMultiple());
		Assert.assertEquals(lastAttribute.isUnique(), recreatedLastAttribute.isUnique());
		Assert.assertEquals(lastAttribute.isUnmodifiable(), recreatedLastAttribute.isUnmodifiable());
	}
	
	/**
	 * Test that password of the remote server is not lost after getting a system with remote server.
	 */
	@Test
	@SuppressWarnings("deprecation") // one test to cover old way, how to set remote server
	public void testPasswordNotOverriden() {
		final String testPassword = "myPassword123456";
		final String name = helper.createName();
		SysSystemDto system = helper.createSystem(name);
		system.setRemote(true);
		system.setVirtual(false);
		system.getConnectorServer().setPassword(new GuardedString(testPassword));
		system = systemService.save(system);

		// Prove that confidential storage contains correct password
		String storedPassword = confidentialStorage.getGuardedString(system.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString();
		Assert.assertEquals(testPassword, storedPassword);
		
		// Get stored system and prove it doesn't contain readable password 
		SysSystemDto systemTmp = systemService.get(system.getId());		
		GuardedString obtainedPassword = systemTmp.getConnectorServer().getPassword();
		Assert.assertTrue(obtainedPassword == null);				
		systemTmp = systemService.save(systemTmp);

		// Prove that confidential storage still contains correct password
		storedPassword = confidentialStorage.getGuardedString(system.getId(),
				SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD).asString();
		Assert.assertEquals(testPassword, storedPassword);
	}

	private AbstractSysSyncConfigDto createSync(SysSystemDto system) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		SysSystemMappingDto mappingOrig = helper.getDefaultMapping(system);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingOrig.getId());

		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto nameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equalsIgnoreCase(TestHelper.ATTRIBUTE_MAPPING_NAME);
		}).findFirst().get();

		SysSystemAttributeMappingDto firstNameAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equalsIgnoreCase(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME);
		}).findFirst().get();

		SysSystemAttributeMappingDto emailAttribute = attributes.stream().filter(attribute -> {
			return attribute.getName().equalsIgnoreCase(TestHelper.ATTRIBUTE_MAPPING_EMAIL);
		}).findFirst().get();

		// create synchronization config
		AbstractSysSyncConfigDto syncConfig = new SysSyncIdentityConfigDto();
		syncConfig.setCustomFilter(true);
		syncConfig.setSystemMapping(mappingOrig.getId());
		syncConfig.setCorrelationAttribute(nameAttribute.getId());
		syncConfig.setTokenAttribute(firstNameAttribute.getId());
		syncConfig.setFilterAttribute(emailAttribute.getId());
		syncConfig.setReconciliation(true);
		syncConfig.setName(system.getName());
		syncConfig.setLinkedAction(SynchronizationLinkedActionType.IGNORE);
		syncConfig.setUnlinkedAction(SynchronizationUnlinkedActionType.IGNORE);
		syncConfig.setMissingEntityAction(SynchronizationMissingEntityActionType.CREATE_ENTITY);
		syncConfig.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);
		syncConfig = syncConfigService.save(syncConfig);
		return syncConfig;
	}
}
