package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBatchDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Searching entities, using filters
 *
 * @author Petr Hanák
 *
 */
@Transactional
public class SysProvisioningOperationFilterIntegrationTest extends AbstractIntegrationTest {

	@Autowired private SysProvisioningOperationService operationService;
	@Autowired private SysSystemService systemService;
	@Autowired private SysProvisioningBatchService batchService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void typeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SysSystemDto system = createSystem();

		createProvisioningOperation(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningOperationDto provisioningOperation3 = createProvisioningOperation(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE);

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation3));
		assertFalse(result.getContent().contains(provisioningOperation2));
	}

	@Test
	public void operationTypeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createSystem();

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningOperation2.setOperationType(ProvisioningEventType.CANCEL);
		operationService.save(provisioningOperation2);
		SysProvisioningOperationDto provisioningOperation3 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningOperation3.setOperationType(ProvisioningEventType.CANCEL);
		operationService.save(provisioningOperation3);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setOperationType(ProvisioningEventType.CANCEL);

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation1));
	}

	@Test
	public void systemIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system2 = createSystem();
		SysSystemDto system1 = createSystem();

		createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system2);
		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system1);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system2.getId());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation1));
	}

	@Test
	public void systemEntityUidFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createSystem();

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningOperationDto provisioningOperation3 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setSystemEntityUid(provisioningOperation1.getSystemEntityUid());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertFalse(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation3));
	}

	@Test
	public void batchIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SysSystemDto system = createSystem();

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningBatchDto provisioningBatch = new SysProvisioningBatchDto(provisioningOperation1);
		provisioningBatch = batchService.save(provisioningBatch);
		provisioningOperation1.setBatch(provisioningBatch.getId());
		provisioningOperation1 = operationService.save(provisioningOperation1);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningOperation2.setBatch(provisioningBatch.getId());
		provisioningOperation2 = operationService.save(provisioningOperation2);
		SysProvisioningOperationDto provisioningOperation3 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setBatchId(provisioningBatch.getId());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertTrue(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation3));
	}

	@Test
	public void entityIdentifierFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SysSystemDto system = createSystem();

		createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningOperation1.setEntityIdentifier(UUID.randomUUID());
		operationService.save(provisioningOperation1);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(provisioningOperation1.getEntityIdentifier());
		filter.setSystemId(system.getId());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation1));
		assertFalse(result.getContent().contains(provisioningOperation2));
	}

	@Test
	public void resultStateFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SysSystemDto system = createSystem();

		OperationResult resultState = new OperationResult();
		resultState.setState(OperationState.CREATED);

		SysProvisioningOperationDto provisioningOperation1 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);
		provisioningOperation2.setResult(resultState);
		operationService.save(provisioningOperation2);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setResultState(OperationState.CREATED);
		filter.setSystemId(system.getId());

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation2));
		assertFalse(result.getContent().contains(provisioningOperation1));
	}

	@Test
	public void dateTimeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;
		SysSystemDto system = createSystem();

		createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		getHelper().waitForResult(null, 30, 1);

		ZonedDateTime dateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

		SysProvisioningOperationDto provisioningOperation2 = createProvisioningOperation(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(system.getId());
		filter.setFrom(dateTime);

		Page<SysProvisioningOperationDto> result = operationService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(provisioningOperation2));

		dateTime = dateTime.minusHours(1);
		SysProvisioningOperationFilter filter2 = new SysProvisioningOperationFilter();
		filter2.setSystemId(system.getId());
		filter2.setTill(dateTime);

		Page<SysProvisioningOperationDto> result2 = operationService.find(filter2, null, permission);
		assertEquals(0, result2.getTotalElements());
	}

	private SysSystemDto createSystem() {
		SysSystemDto system = new SysSystemDto();
		system.setName("system_" + UUID.randomUUID());
		system.setReadonly(true);
		system.setDisabled(false);
		system.setVirtual(false);
		system.setQueue(false);
		return systemService.save(system);
	}

	private SysProvisioningOperationDto createProvisioningOperation (String type, SysSystemDto system) {
		SysProvisioningOperationDto provisioningOperation = new SysProvisioningOperationDto();
		provisioningOperation.setEntityType(type);
		provisioningOperation.setOperationType(ProvisioningEventType.CREATE);
		provisioningOperation.setProvisioningContext(new ProvisioningContext());
		provisioningOperation.setSystem(system.getId());
		provisioningOperation.setEntityIdentifier(UUID.randomUUID());

		SysSystemEntityDto systemEntity = ((TestHelper) getHelper()).createSystemEntity(system);
		provisioningOperation.setSystemEntity(systemEntity.getId());

		OperationResult result = new OperationResult();
		result.setState(OperationState.RUNNING);

		provisioningOperation.setResult(result);

		return operationService.save(provisioningOperation);
	}

}
