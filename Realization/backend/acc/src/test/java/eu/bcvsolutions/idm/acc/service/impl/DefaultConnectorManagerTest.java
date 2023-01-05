package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.connector.AbstractConnectorType;
import eu.bcvsolutions.idm.acc.connector.CsvConnectorType;
import eu.bcvsolutions.idm.acc.connector.DefaultConnectorType;
import eu.bcvsolutions.idm.acc.connector.PostgresqlConnectorType;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent;
import eu.bcvsolutions.idm.acc.rest.impl.SysSystemController;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for connector types
 *
 * @author Vít Švanda
 * @since 10.7.0
 */
@Transactional
public class DefaultConnectorManagerTest extends AbstractIntegrationTest {

	@Autowired
	private CsvConnectorType csvConnectorType;
	@Autowired
	private SysSystemController systemController;
	@Autowired
	private TestHelper helper;
	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private SysSystemMappingService mappingService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testSupportsConnectorTypes() {
		String defaultTableConnectorName = "net.tirasa.connid.bundles.db.table.DatabaseTableConnector";

		Resources<ConnectorTypeDto> supportedTypes = systemController.getSupportedTypes();

		// Find connector without connector type (it is default table connector
		// = table connector has tree connector types and one default connector type).
		DefaultConnectorType mockDefaultConnectorType = new DefaultConnectorType();
		ConnectorTypeDto defaultConnectorTypeDto = supportedTypes.getContent()
				.stream()
				.filter(connectorTypeDto -> defaultTableConnectorName.equals(connectorTypeDto.getId()))
				.findFirst()
				.orElse(null);
		assertNotNull(defaultConnectorTypeDto);
		assertEquals(defaultConnectorTypeDto.getIconKey(), mockDefaultConnectorType.getIconKey());

		// Find PostgreSQL connector (table connector has tree connector types and one default connector type).
		ConnectorTypeDto postgresqlConnectorTypeDto = supportedTypes.getContent()
				.stream()
				.filter(connectorTypeDto -> PostgresqlConnectorType.NAME.equals(connectorTypeDto.getId()))
				.findFirst()
				.orElse(null);
		assertNotNull(postgresqlConnectorTypeDto);

		// Find CSV connector type
		ConnectorTypeDto csvConnectorTypeDto = supportedTypes.getContent()
				.stream()
				.filter(connectorTypeDto -> CsvConnectorType.NAME.equals(connectorTypeDto.getId()))
				.findFirst()
				.orElse(null);
		assertNotNull(csvConnectorTypeDto);

	}

	@Test
	public void testLoadDefaultValuesConnectorType() {
		ConnectorTypeDto mockCsvConnectorTypeDto = new ConnectorTypeDto();
		mockCsvConnectorTypeDto.setReopened(false);
		mockCsvConnectorTypeDto.setId(CsvConnectorType.NAME);
		ResponseEntity<ConnectorTypeDto> responseEntity = systemController.loadWizardType(mockCsvConnectorTypeDto);
		ConnectorTypeDto csvConnectorTypeDto = responseEntity.getBody();
		assertNotNull(csvConnectorTypeDto);

		Map<String, String> metadata = csvConnectorTypeDto.getMetadata();
		Map<String, String> beanMetadata = csvConnectorType.getMetadata();
		assertEquals(beanMetadata.get(CsvConnectorType.FILE_PATH), metadata.get(CsvConnectorType.FILE_PATH));
		assertEquals(beanMetadata.get(CsvConnectorType.SEPARATOR), metadata.get(CsvConnectorType.SEPARATOR));
		assertEquals(beanMetadata.get(CsvConnectorType.SYSTEM_NAME), metadata.get(CsvConnectorType.SYSTEM_NAME));
	}

	@Test
	public void testMappingStep() {

		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		Assert.assertEquals(DefaultConnectorType.NAME, connectorType.getConnectorName());

		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDto.setReopened(true);
		connectorTypeDto = connectorManager.load(connectorTypeDto);
		// One mapping already exists.
		BaseDto mapping = connectorTypeDto.getEmbedded().get(AbstractConnectorType.MAPPING_DTO_KEY);
		Assert.assertTrue(mapping instanceof SysSystemMappingDto);
		// Only one mapping exists now.
		String moreMappings = connectorTypeDto.getMetadata().get(AbstractConnectorType.ALERT_MORE_MAPPINGS);
		Assert.assertNull(moreMappings);

		// Execute mapping step.
		connectorTypeDto.setReopened(false);
		connectorTypeDto.setWizardStepName(AbstractConnectorType.STEP_MAPPING);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SCHEMA_ID, ((SysSystemMappingDto) mapping).getObjectClass().toString());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.OPERATION_TYPE, SystemOperationType.SYNCHRONIZATION.name());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.ENTITY_TYPE, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemController.executeWizardType(connectorTypeDto);

		ConnectorTypeDto connectorTypeDtoAfterMappingStep = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDtoAfterMappingStep.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDtoAfterMappingStep.setReopened(true);
		connectorTypeDtoAfterMappingStep = connectorManager.load(connectorTypeDtoAfterMappingStep);
		// Two mappings have to exists now.
		moreMappings = connectorTypeDtoAfterMappingStep.getMetadata().get(AbstractConnectorType.ALERT_MORE_MAPPINGS);
		Assert.assertEquals(Boolean.TRUE.toString(), moreMappings);
	}
	
	@Test
	public void testAutoSyncMappingInWizard() {

		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		Assert.assertEquals(DefaultConnectorType.NAME, connectorType.getConnectorName());

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(systemDto.getId());
		SysSystemMappingDto sysSystemMappingDto = mappingService.find(mappingFilter, null).getContent().stream().findFirst().orElse(null);
		Assert.assertNotNull(sysSystemMappingDto);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SCHEMA_ID, (sysSystemMappingDto).getObjectClass().toString());
		// Delete a created mapping.
		mappingService.delete(sysSystemMappingDto);

		connectorTypeDto.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDto.setReopened(true);
		connectorTypeDto = connectorManager.load(connectorTypeDto);

		// Execute mapping step.
		connectorTypeDto.setReopened(false);
		connectorTypeDto.setWizardStepName(AbstractConnectorType.STEP_MAPPING);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.OPERATION_TYPE, SystemOperationType.SYNCHRONIZATION.name());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.ENTITY_TYPE, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemController.executeWizardType(connectorTypeDto);

		ConnectorTypeDto connectorTypeDtoAfterMappingStep = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDtoAfterMappingStep.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDtoAfterMappingStep.setReopened(true);
		connectorTypeDtoAfterMappingStep = connectorManager.load(connectorTypeDtoAfterMappingStep);
		// Sync mapping must exist.
		BaseDto mapping = connectorTypeDtoAfterMappingStep.getEmbedded().get(AbstractConnectorType.MAPPING_DTO_KEY);
		Assert.assertTrue(mapping instanceof SysSystemMappingDto);
		SysSystemMappingDto syncMapping = (SysSystemMappingDto) mapping;
		Assert.assertSame(SystemOperationType.SYNCHRONIZATION, syncMapping.getOperationType());

		// Attributes had to be created.
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(syncMapping.getId());
		List<SysSystemAttributeMappingDto> attributeMappingDtos = attributeMappingService.find(attributeMappingFilter, null).getContent();
		Assert.assertEquals(7, attributeMappingDtos.size());
	}
	
	@Test
	public void testAutoSyncMappingOutsideWizard() {

		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		Assert.assertEquals(DefaultConnectorType.NAME, connectorType.getConnectorName());

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(systemDto.getId());
		SysSystemMappingDto sysSystemMappingDto = mappingService.find(mappingFilter, null).getContent().stream().findFirst().orElse(null);
		Assert.assertNotNull(sysSystemMappingDto);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SCHEMA_ID, (sysSystemMappingDto).getObjectClass().toString());
		// Delete a created mapping.
		mappingService.delete(sysSystemMappingDto);

		SysSystemMappingDto syncMapping = new SysSystemMappingDto();
		syncMapping.setObjectClass(sysSystemMappingDto.getObjectClass());
		syncMapping.setName("Mapping");
		syncMapping.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		syncMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncMapping.setAccountType(AccountType.PERSONAL);
		syncMapping = mappingService.publish(
				new SystemMappingEvent(
						SystemMappingEvent.SystemMappingEventType.CREATE,
						syncMapping,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, true)))
				.getContent();

		// Attributes had to be created.
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(syncMapping.getId());
		List<SysSystemAttributeMappingDto> attributeMappingDtos = attributeMappingService.find(attributeMappingFilter, null).getContent();
		Assert.assertEquals(7, attributeMappingDtos.size());
	}
	
	@Test
	public void testAutoProvisioningMappingOutsideWizard() {

		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		Assert.assertEquals(DefaultConnectorType.NAME, connectorType.getConnectorName());

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(systemDto.getId());
		SysSystemMappingDto sysSystemMappingDto = mappingService.find(mappingFilter, null).getContent().stream().findFirst().orElse(null);
		Assert.assertNotNull(sysSystemMappingDto);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SCHEMA_ID, (sysSystemMappingDto).getObjectClass().toString());
		// Delete a created mapping.
		mappingService.delete(sysSystemMappingDto);

		SysSystemMappingDto mappingDto = new SysSystemMappingDto();
		mappingDto.setObjectClass(sysSystemMappingDto.getObjectClass());
		mappingDto.setName("Mapping");
		mappingDto.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		mappingDto.setOperationType(SystemOperationType.PROVISIONING);
		mappingDto.setAccountType(AccountType.PERSONAL);
		mappingDto = mappingService.publish(
				new SystemMappingEvent(
						SystemMappingEvent.SystemMappingEventType.CREATE,
						mappingDto,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, true)))
				.getContent();

		// Attributes had to be created.
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());
		List<SysSystemAttributeMappingDto> attributeMappingDtos = attributeMappingService.find(attributeMappingFilter, null).getContent();
		Assert.assertEquals(7, attributeMappingDtos.size());
	}
	
	@Test
	public void testAutoProvisioningMappingInWizard() {

		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		Assert.assertEquals(DefaultConnectorType.NAME, connectorType.getConnectorName());

		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(systemDto.getId());
		SysSystemMappingDto sysSystemMappingDto = mappingService.find(mappingFilter, null).getContent().stream().findFirst().orElse(null);
		Assert.assertNotNull(sysSystemMappingDto);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SCHEMA_ID, (sysSystemMappingDto).getObjectClass().toString());
		// Delete a created mapping.
		mappingService.delete(sysSystemMappingDto);

		connectorTypeDto.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDto.setReopened(true);
		connectorTypeDto = connectorManager.load(connectorTypeDto);

		// Execute mapping step.
		connectorTypeDto.setReopened(false);
		connectorTypeDto.setWizardStepName(AbstractConnectorType.STEP_MAPPING);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.OPERATION_TYPE, SystemOperationType.PROVISIONING.name());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.ENTITY_TYPE, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		systemController.executeWizardType(connectorTypeDto);

		ConnectorTypeDto connectorTypeDtoAfterMappingStep = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDtoAfterMappingStep.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDtoAfterMappingStep.setReopened(true);
		connectorTypeDtoAfterMappingStep = connectorManager.load(connectorTypeDtoAfterMappingStep);
		// Sync mapping must exist.
		BaseDto mapping = connectorTypeDtoAfterMappingStep.getEmbedded().get(AbstractConnectorType.MAPPING_DTO_KEY);
		Assert.assertTrue(mapping instanceof SysSystemMappingDto);
		SysSystemMappingDto syncMapping = (SysSystemMappingDto) mapping;
		Assert.assertSame(SystemOperationType.PROVISIONING, syncMapping.getOperationType());

		// Attributes had to be created.
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(syncMapping.getId());
		List<SysSystemAttributeMappingDto> attributeMappingDtos = attributeMappingService.find(attributeMappingFilter, null).getContent();
		Assert.assertEquals(7, attributeMappingDtos.size());
	}

	@Test
	public void testFinishStep() {

		SysSystemDto systemDto = helper.createTestResourceSystem(true);
		ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(systemDto);
		Assert.assertEquals(DefaultConnectorType.NAME, connectorType.getConnectorName());

		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDto.setReopened(true);
		connectorTypeDto = connectorManager.load(connectorTypeDto);
		// One mapping already exists.
		BaseDto mapping = connectorTypeDto.getEmbedded().get(AbstractConnectorType.MAPPING_DTO_KEY);
		Assert.assertTrue(mapping instanceof SysSystemMappingDto);

		// Execute finish step.
		String roleName = getHelper().createName();
		connectorTypeDto.setReopened(false);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		connectorTypeDto.getMetadata().put(AbstractConnectorType.MAPPING_ID, mapping.getId().toString());
		connectorTypeDto.setWizardStepName(AbstractConnectorType.STEP_FINISH);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.NEW_ROLE_WITH_SYSTEM_CODE, roleName);
		connectorTypeDto.getMetadata().put(AbstractConnectorType.CREATES_ROLE_WITH_SYSTEM, Boolean.TRUE.toString());
		systemController.executeWizardType(connectorTypeDto);

		// A new role-system was to be created.
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setSystemId(systemDto.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null)
				.getContent();
		Assert.assertEquals(1, roleSystemDtos.size());
		IdmRoleDto roleDto = (IdmRoleDto) roleSystemDtos.get(0).getEmbedded().get(SysRoleSystem_.role.getName());
		Assert.assertEquals(roleName, roleDto.getBaseCode());
	}
}
