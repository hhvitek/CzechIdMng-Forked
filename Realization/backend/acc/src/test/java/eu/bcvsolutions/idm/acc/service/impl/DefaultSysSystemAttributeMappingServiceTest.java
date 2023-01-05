package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ErrorModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Searching entities, using filters
 *
 * @author Petr Hanák
 * @author Vít Švanda
 *
 */
@Transactional
public class DefaultSysSystemAttributeMappingServiceTest extends AbstractIntegrationTest {

	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired
	private SysSchemaAttributeService attributeService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private TestHelper testHelper;
	@Autowired
	private DefaultSysSystemMappingService mappingService;
	@Autowired
	private FormService formService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void moreAttributesOnSchemaAttributeSyncTest() {
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.SET;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		// For synchronization is enabled more mapped attributes for one schema
		// attribute
		systemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		systemMapping = mappingService.save(systemMapping);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		attributeMapping1.setName("attributeOne");
		attributeMapping1 = attributeMappingService.save(attributeMapping1);

		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		attributeMapping2.setName("attributeTwo");
		attributeMapping2 = attributeMappingService.save(attributeMapping2);

		assertNotNull(attributeMapping1.getId());
		assertNotNull(attributeMapping2.getId());
	}

	@Test(expected = ResultCodeException.class)
	public void moreAttributesOnSchemaAttributeProvTest() {
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.SET;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		// For provisioning is NOT enabled more mapped attributes for one schema
		// attribute
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping = mappingService.save(systemMapping);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		attributeMapping1.setName("attributeOne");
		attributeMapping1 = attributeMappingService.save(attributeMapping1);

		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		attributeMapping2.setName("attributeTwo");
		attributeMapping2 = attributeMappingService.save(attributeMapping2);
	}

	@Test(expected = ResultCodeException.class)
	public void moreAttributesOnSchemaAttChangeMappingTypeTest() {
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.SET;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		// For synchronization is enabled more mapped attributes for one schema
		// attribute
		systemMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		systemMapping = mappingService.save(systemMapping);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		attributeMapping1.setName("attributeOne");
		attributeMapping1 = attributeMappingService.save(attributeMapping1);

		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		attributeMapping2.setName("attributeTwo");
		attributeMapping2 = attributeMappingService.save(attributeMapping2);

		assertNotNull(attributeMapping1.getId());
		assertNotNull(attributeMapping2.getId());

		// For provisioning is NOT enabled more mapped attributes for one schema
		// attribute. On change operation type form SYNC to PROVISIONING, has to be
		// throw exception!
		systemMapping.setOperationType(SystemOperationType.PROVISIONING);
		systemMapping = mappingService.save(systemMapping);
	}

	@Test
	public void textFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		attributeMapping1.setName("OriginalName01");
		attributeMappingService.save(attributeMapping1);

		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping,
				AttributeMappingStrategyType.CREATE, schemaAttribute.getId());
		attributeMapping2.setName("OriginalName21");
		attributeMappingService.save(attributeMapping2);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setText("OriginalName0");

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping1));
		assertFalse(result.getContent().contains(attributeMapping2));
	}

	@Test
	public void schemaAttributeIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);

		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);
		SysSchemaAttributeDto schemaAttribute2 = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute2.getId());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSchemaAttributeId(schemaAttribute.getId());

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping));
		assertFalse(result.getContent().contains(attributeMapping2));
	}

	@Test
	public void systemMappingIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping1 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSystemMappingDto systemMapping2 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping1, strategyType,
				schemaAttribute.getId());
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping2, strategyType,
				schemaAttribute.getId());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemMappingId(systemMapping1.getId());

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping1));
		assertFalse(result.getContent().contains(attributeMapping2));
	}

	@Test
	public void systemIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system1 = createSystem();
		SysSystemDto system2 = createSystem();
		SysSchemaObjectClassDto objectClass1 = createObjectClass(system1);
		SysSchemaObjectClassDto objectClass2 = createObjectClass(system2);

		SysSystemMappingDto systemMapping1 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass1);
		SysSystemMappingDto systemMapping2 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass2);
		SysSchemaAttributeDto schemaAttribute1 = createSchemaAttribute(objectClass1);
		SysSchemaAttributeDto schemaAttribute2 = createSchemaAttribute(objectClass2);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping1, strategyType,
				schemaAttribute1.getId());
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping2, strategyType,
				schemaAttribute2.getId());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(system1.getId());

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping1));
		assertFalse(result.getContent().contains(attributeMapping2));

	}

	@Test
	public void idmPropertyNameFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping, strategyType,
				schemaAttribute.getId());
		attributeMapping1.setIdmPropertyName("PropName31");
		attributeMappingService.save(attributeMapping1);
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping,
				AttributeMappingStrategyType.CREATE, schemaAttribute.getId());
		attributeMapping2.setIdmPropertyName("PropName4");
		attributeMappingService.save(attributeMapping2);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setIdmPropertyName("PropName4");

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping2));
		assertFalse(result.getContent().contains(attributeMapping1));
	}

	@Test
	public void isUidFitlerTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		AttributeMappingStrategyType strategyType = AttributeMappingStrategyType.MERGE;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemMappingDto systemMapping1 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSystemMappingDto systemMapping2 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSchemaAttributeDto schemaAttribute = createSchemaAttribute(objectClass);

		SysSystemAttributeMappingDto attributeMapping1 = createAttributeMappingSystem(systemMapping1,
				AttributeMappingStrategyType.CREATE, schemaAttribute.getId());
		SysSystemAttributeMappingDto attributeMapping2 = createAttributeMappingSystem(systemMapping2, strategyType,
				schemaAttribute.getId());
		attributeMapping2.setUid(true);
		attributeMappingService.save(attributeMapping2);
		createAttributeMappingSystem(systemMapping1, AttributeMappingStrategyType.SET, schemaAttribute.getId());

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setIsUid(true);
		filter.setSystemId(system.getId());

		Page<SysSystemAttributeMappingDto> result = attributeMappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(attributeMapping2));
		assertFalse(result.getContent().contains(attributeMapping1));
	}
	
	@Test
	public void createExtendedAttributeTest() {
		SysSystemDto system = testHelper.createSystem(TestResource.TABLE_NAME);
		SysSystemMappingDto mapping = testHelper.createMapping(system);

		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());

		SysSchemaAttributeDto descriptionSchemaAttribute = attributeService.find(schemaAttributeFilter, null)
				.getContent() //
				.stream() //
				.filter(schemaAttribute -> TestHelper.ATTRIBUTE_MAPPING_DESCRIPTION.equalsIgnoreCase(schemaAttribute.getName())) //
				.findFirst() //
				.get(); //

		// create EAV attribute
		String propertyName = testHelper.createName();
		SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
		attributeMapping.setExtendedAttribute(true);
		attributeMapping.setName(descriptionSchemaAttribute.getName());
		attributeMapping.setIdmPropertyName(propertyName);
		attributeMapping.setSchemaAttribute(descriptionSchemaAttribute.getId());
		attributeMapping.setSystemMapping(mapping.getId());
		attributeMapping = attributeMappingService.save(attributeMapping);
		
		IdmFormDefinitionDto definition = formService.getDefinition(IdmIdentityDto.class);
		IdmFormAttributeDto formAttributeDto = definition.getFormAttributes() //
				.stream() //
				.filter(formAttribute -> propertyName.equals(formAttribute.getCode())) //
				.findFirst() //
				.get(); //
		
		assertEquals(propertyName, formAttributeDto.getCode());
		assertEquals(testHelper.getSchemaColumnName(TestHelper.ATTRIBUTE_MAPPING_DESCRIPTION),  formAttributeDto.getName());
		assertEquals(false, formAttributeDto.isMultiple());
		assertEquals(false, formAttributeDto.isConfidential());
		assertEquals(false, formAttributeDto.isRequired());
		assertEquals(false, formAttributeDto.isReadonly());
		assertEquals(false, formAttributeDto.isUnique());
		assertEquals(PersistentType.SHORTTEXT, formAttributeDto.getPersistentType());

	}
	
	@Test
	public void transformationFromScriptFailure() {
		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objClass = createObjectClass(system);
		SysSchemaAttributeDto schemaAttr = createSchemaAttribute(objClass);
		SysSystemMappingDto systemMapping = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objClass);
		SysSystemAttributeMappingDto attrMapping = createAttributeMappingSystem(systemMapping,
				AttributeMappingStrategyType.CREATE, schemaAttr.getId());
		
		// script consists of just one missing symbol,
		// which is supposed to be part of error message 
		String script = "xxxxx";
		attrMapping.setTransformFromResourceScript(script);
		systemMapping = mappingService.save(systemMapping);
		
		try {
			attributeMappingService.transformValueFromResource("testValue", attrMapping, new ArrayList<IcAttribute>());
			fail();
		} catch (ResultCodeException ex) {
			ErrorModel errModel = ex.getError().getError();
			String message = (String)errModel.getParameters().get(SysSystemAttributeMappingService.MAPPING_SCRIPT_FAIL_MESSAGE_KEY);
			String idmPath = (String)errModel.getParameters().get(SysSystemAttributeMappingService.MAPPING_SCRIPT_FAIL_IDM_PATH_KEY);
			assertEquals(errModel.getStatusEnum(), AccResultCode.GROOVY_SCRIPT_ATTR_TRANSFORMATION_FAILED.getCode());
			assertTrue(message.contains(script));
			assertTrue(idmPath.contains(system.getCode()));
			assertTrue(idmPath.contains(systemMapping.getName()));
			assertTrue(idmPath.contains(attrMapping.getName()));
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void transformationToScriptFailure() {
		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objClass = createObjectClass(system);
		SysSchemaAttributeDto schemaAttr = createSchemaAttribute(objClass);
		SysSystemMappingDto systemMapping = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objClass);
		SysSystemAttributeMappingDto attrMapping = createAttributeMappingSystem(systemMapping,
				AttributeMappingStrategyType.CREATE, schemaAttr.getId());
		
		// script consists of just one missing symbol,
		// which is supposed to be part of error message
		String script = "xxxxx";
		attrMapping.setTransformToResourceScript(script);
		systemMapping = mappingService.save(systemMapping);
		
		try {
			attributeMappingService.transformValueToResource(null, "testValue", attrMapping, new IdmIdentityDto());
			fail();
		} catch (ResultCodeException ex) {
			ErrorModel errModel = ex.getError().getError();
			String message = (String)errModel.getParameters().get(SysSystemAttributeMappingService.MAPPING_SCRIPT_FAIL_MESSAGE_KEY);
			String idmPath = (String)errModel.getParameters().get(SysSystemAttributeMappingService.MAPPING_SCRIPT_FAIL_IDM_PATH_KEY);
			assertEquals(errModel.getStatusEnum(), AccResultCode.GROOVY_SCRIPT_ATTR_TRANSFORMATION_FAILED.getCode());
			assertTrue(message.contains(script));
			assertTrue(idmPath.contains(system.getCode()));
			assertTrue(idmPath.contains(systemMapping.getName()));
			assertTrue(idmPath.contains(attrMapping.getName()));
		} catch (Exception e) {
			fail();
		}
	}

	private SysSystemDto createSystem() {
		SysSystemDto system = new SysSystemDto();
		system.setName("system_" + UUID.randomUUID());
		return systemService.save(system);
	}

	private SysSchemaObjectClassDto createObjectClass(SysSystemDto system) {
		SysSchemaObjectClassDto objectClass = new SysSchemaObjectClassDto();
		objectClass.setSystem(system.getId());
		objectClass.setObjectClassName("__ACCOUNT__");
		return schemaObjectClassService.save(objectClass);
	}

	private SysSchemaAttributeDto createSchemaAttribute(SysSchemaObjectClassDto objectClass) {
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName("Name" + UUID.randomUUID());
		schemaAttribute.setObjectClass(objectClass.getId());
		schemaAttribute.setClassType("SomeType");
		return attributeService.save(schemaAttribute);
	}

	private SysSystemAttributeMappingDto createAttributeMappingSystem(SysSystemMappingDto systemMapping,
			AttributeMappingStrategyType mappingStrategyType, UUID schemaAttribute) {
		SysSystemAttributeMappingDto attributeMapping = new SysSystemAttributeMappingDto();
		attributeMapping.setName("Name" + UUID.randomUUID());
		attributeMapping.setSystemMapping(systemMapping.getId());
		attributeMapping.setSchemaAttribute(schemaAttribute);
		attributeMapping.setStrategyType(mappingStrategyType);
		attributeMapping.setUid(false);
		return attributeMappingService.save(attributeMapping);
	}

}
