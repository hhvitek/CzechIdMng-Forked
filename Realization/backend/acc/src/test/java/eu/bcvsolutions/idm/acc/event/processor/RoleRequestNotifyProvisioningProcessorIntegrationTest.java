package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.testng.collections.Lists;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Execute role request
 * - one provisioning operation should be executed
 * - prevent to drop and create target account, if one assigned role is deleted 
 * 
 * @author Radek Tomiška
 *
 */
public class RoleRequestNotifyProvisioningProcessorIntegrationTest extends AbstractIntegrationTest{
	
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private SysProvisioningArchiveService provisioningArchiveService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private AccAccountService accountService;
	@Autowired private IdmEntityEventService entityEventService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	
	@Test
	public void testAssignSubRolesByRequestAsync() {
		try {
			getHelper().enableAsynchronousProcessing();
			// prepare role composition
			IdmRoleDto superior = getHelper().createRole();
			IdmRoleDto subOne = getHelper().createRole();
			IdmRoleDto subTwo = getHelper().createRole();
			IdmRoleDto subOneSub = getHelper().createRole();
			IdmRoleDto subOneSubSub = getHelper().createRole();
			getHelper().createRoleComposition(superior, subOne);
			getHelper().createRoleComposition(superior, subTwo);
			getHelper().createRoleComposition(subOne, subOneSub);
			getHelper().createRoleComposition(subOneSub, subOneSubSub);
			//
			IdmRoleDto other = getHelper().createRole();
			IdmRoleDto otherOne = getHelper().createRole();
			getHelper().createRoleComposition(other, otherOne);
			//
			// create test system with mapping and link her to the sub roles
			SysSystemDto system = getHelper().createTestResourceSystem(true);
			getHelper().createRoleSystem(subOneSubSub, system);
			getHelper().createRoleSystem(otherOne, system);
			//
			// assign superior role
			IdmIdentityDto identity = getHelper().createIdentity();
			//
			final IdmRoleRequestDto roleRequestOne = getHelper().createRoleRequest(identity, superior);
			//
			getHelper().executeRequest(roleRequestOne, false);
			//
			// wait for executed events
			IdmEntityEventFilter eventFilter = new IdmEntityEventFilter();
			eventFilter.setOwnerId(roleRequestOne.getId());
			eventFilter.setStates(Lists.newArrayList(OperationState.RUNNING, OperationState.CREATED));
			getHelper().waitForResult(res -> {
				return entityEventService.find(eventFilter, PageRequest.of(0, 1)).getTotalElements() != 0;
			}, 1000, 30);
			//
			// check after create
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(5, assignedRoles.size());
			IdmIdentityRoleDto directRole = assignedRoles.stream().filter(ir -> ir.getDirectRole() == null).findFirst().get();
			Assert.assertEquals(superior.getId(), directRole.getRole());
			//
			// check created account
			AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNotNull(account);
			Assert.assertNotNull(getHelper().findResource(account.getRealUid()));
			//
			// check provisioning archive
			SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
			archiveFilter.setEntityIdentifier(identity.getId());
			//
			// TODO: add better waiting for child events + count of all sub events?
//			List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
//			Assert.assertEquals(1, executedOperations.size());
//			Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.CREATE));
			//
			// remove one role and add other
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setApplicantInfo(new ApplicantImplDto(identity.getId(), IdmIdentityDto.class.getCanonicalName()));
			roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
			roleRequest.setExecuteImmediately(true);
			IdmRoleRequestDto roleRequestTwo = roleRequestService.save(roleRequest);
			// remove
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequestTwo.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setRole(superior.getId());
			conceptRoleRequest.setIdentityRole(directRole.getId());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
			conceptRoleRequestService.save(conceptRoleRequest);
			// add
			conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequestTwo.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setRole(other.getId());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
			conceptRoleRequestService.save(conceptRoleRequest);
			// execute
			getHelper().executeRequest(roleRequestTwo, false);	
			//
			// wait for executed events
			eventFilter.setOwnerId(roleRequestTwo.getId());
			getHelper().waitForResult(res -> {
				return entityEventService.find(eventFilter, PageRequest.of(0, 1)).getTotalElements() != 0;
			}, 1000, 30);
			//
			// check after role request is executed
			assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(2, assignedRoles.size());
			//
			// check updated account
			AccAccountDto updatedAccount = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNotNull(updatedAccount);
			Assert.assertNotNull(getHelper().findResource(updatedAccount.getRealUid()));
			Assert.assertEquals(account.getCreated(), updatedAccount.getCreated());
			Assert.assertEquals(account.getRealUid(), updatedAccount.getRealUid());
			//
			// check provisioning archive
//			executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
//			Assert.assertEquals(2, executedOperations.size());
//			Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.CREATE));
//			Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.UPDATE));
		} finally {
			getHelper().disableAsynchronousProcessing();
		}
	}
	
	/**
	 * Sub role composition assigning target system is created after role is assigned to identity asynchronously.
	 */
	@Test
	public void testAssignSubRolesAfterCompositionIsCreatedAsync() {
		try {
			UUID transactionId = TransactionContextHolder.getContext().getTransactionId();
			Assert.assertNotNull(transactionId);
			//
//			getHelper().enableAsynchronousProcessing();
			// prepare role composition
			IdmRoleDto superior = getHelper().createRole();
			IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
			getHelper().createIdentityRole(identity, superior);
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(1, assignedRoles.size());
			//
			IdmRoleDto subOne = getHelper().createRole();
			IdmRoleDto subTwo = getHelper().createRole();
			// assign system
			SysSystemDto system = getHelper().createTestResourceSystem(true);
			getHelper().createRoleSystem(subTwo, system);
			// create composition at last
			getHelper().createRoleComposition(superior, subOne);
			IdmRoleCompositionDto compositionWithSystem = getHelper().createRoleComposition(subOne, subTwo);
			
			getHelper().waitForResult(res -> {
				return identityRoleService.findAllByIdentity(identity.getId()).size() != 3;
			});
			//
			// sub roles will be assigned
			assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(3, assignedRoles.size());
			// and account created
			AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNotNull(account);
			//
			// remove role composition
			roleCompositionService.delete(compositionWithSystem);
			getHelper().waitForResult(res -> {
				return roleCompositionService.get(compositionWithSystem) != null;
			});
			//
			assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(2, assignedRoles.size());
			//
			// and account deleted
			Assert.assertNull(accountService.getAccount(identity.getUsername(), system.getId()));
			//
			// create composition again and remove assigned role by standard request
			getHelper().createRoleComposition(subOne, subTwo);
			getHelper().waitForResult(res -> {
				return identityRoleService.findAllByIdentity(identity.getId()).size() != 3;
			});
			//
			assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(3, assignedRoles.size());
			Assert.assertNotNull(accountService.getAccount(identity.getUsername(), system.getId()));
			//
			IdmRoleRequestDto roleRequest = getHelper().createRoleRequest(identity, ConceptRoleRequestOperation.REMOVE, superior);
			getHelper().executeRequest(roleRequest, false);
			getHelper().waitForResult(res -> {
				return roleRequestService.get(roleRequest).getState() != RoleRequestState.EXECUTED;
			});
			Assert.assertEquals(RoleRequestState.EXECUTED, roleRequestService.get(roleRequest).getState());
			//
			assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertTrue(assignedRoles.isEmpty());
			Assert.assertNull(accountService.getAccount(identity.getUsername(), system.getId()));
		} finally {
//			getHelper().disableAsynchronousProcessing();
		}
	}
	
	/**
	 * Backward compatibility - request executed synchronously works the same as async with one exception
	 *  - account management is executed for every identity role => added identity roles are executed before deletions 
	 *  => prevent to remove account from target system is implemented this way
	 */
	@Test
	public void testAssignSubRolesByRequestSync() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		IdmRoleDto subOne = getHelper().createRole();
		IdmRoleDto subTwo = getHelper().createRole();
		IdmRoleDto subOneSub = getHelper().createRole();
		IdmRoleDto subOneSubSub = getHelper().createRole();
		getHelper().createRoleComposition(superior, subOne);
		getHelper().createRoleComposition(superior, subTwo);
		getHelper().createRoleComposition(subOne, subOneSub);
		getHelper().createRoleComposition(subOneSub, subOneSubSub);
		//
		IdmRoleDto other = getHelper().createRole();
		IdmRoleDto otherOne = getHelper().createRole();
		getHelper().createRoleComposition(other, otherOne);
		//
		// create test system with mapping and link her to the sub roles
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		getHelper().createRoleSystem(subOneSubSub, system);
		getHelper().createRoleSystem(otherOne, system);
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		final IdmRoleRequestDto roleRequestOne = getHelper().createRoleRequest(identity, superior);
		//
		getHelper().executeRequest(roleRequestOne, false);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(5, assignedRoles.size());
		IdmIdentityRoleDto directRole = assignedRoles.stream().filter(ir -> ir.getDirectRole() == null).findFirst().get();
		Assert.assertEquals(superior.getId(), directRole.getRole());
		//
		// check created account
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertNotNull(getHelper().findResource(account.getRealUid()));
		//
		// check provisioning archive
		SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
		archiveFilter.setEntityIdentifier(identity.getId());
		//
		List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
		Assert.assertEquals(1, executedOperations.size());
		Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.CREATE));
		//
		// remove one role and add other
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		final IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		roleRequest.setApplicantInfo(new ApplicantImplDto(identity.getId(), IdmIdentityDto.class.getCanonicalName()));
		roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
		roleRequest.setExecuteImmediately(true);
		final IdmRoleRequestDto roleRequestTwo = roleRequestService.save(roleRequest);
		// remove
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(roleRequestTwo.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		conceptRoleRequest.setRole(superior.getId());
		conceptRoleRequest.setIdentityRole(directRole.getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
		conceptRoleRequestService.save(conceptRoleRequest);
		// add
		conceptRoleRequest = new IdmConceptRoleRequestDto();
		conceptRoleRequest.setRoleRequest(roleRequestTwo.getId());
		conceptRoleRequest.setIdentityContract(contract.getId());
		conceptRoleRequest.setRole(other.getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequestService.save(conceptRoleRequest);
		// execute
		getHelper().executeRequest(roleRequestTwo, false);	
		//
		// check after role request is executed
		assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(2, assignedRoles.size());
		//
		// check updated account
		AccAccountDto updatedAccount = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(updatedAccount);
		Assert.assertNotNull(getHelper().findResource(updatedAccount.getRealUid()));
		Assert.assertEquals(account.getCreated(), updatedAccount.getCreated());
		Assert.assertEquals(account.getRealUid(), updatedAccount.getRealUid());
	}
	
	/**
	 * Request will be executed immediate even if asynchronous events are enabled
	 */
	@Test
	public void testAssignSubRolesByRequestImmediate() {
		try {
			getHelper().enableAsynchronousProcessing();
			// prepare role composition
			IdmRoleDto superior = getHelper().createRole();
			IdmRoleDto subOne = getHelper().createRole();
			IdmRoleDto subTwo = getHelper().createRole();
			IdmRoleDto subOneSub = getHelper().createRole();
			IdmRoleDto subOneSubSub = getHelper().createRole();
			getHelper().createRoleComposition(superior, subOne);
			getHelper().createRoleComposition(superior, subTwo);
			getHelper().createRoleComposition(subOne, subOneSub);
			getHelper().createRoleComposition(subOneSub, subOneSubSub);
			//
			IdmRoleDto other = getHelper().createRole();
			IdmRoleDto otherOne = getHelper().createRole();
			getHelper().createRoleComposition(other, otherOne);
			//
			// create test system with mapping and link her to the sub roles
			SysSystemDto system = getHelper().createTestResourceSystem(true);
			getHelper().createRoleSystem(subOneSubSub, system);
			getHelper().createRoleSystem(otherOne, system);
			//
			// assign superior role
			IdmIdentityDto identity = getHelper().createIdentity();
			//
			final IdmRoleRequestDto roleRequestOne = getHelper().createRoleRequest(identity, superior);
			//
			getHelper().executeRequest(roleRequestOne, false, true);
			//
			// check after create
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(5, assignedRoles.size());
			IdmIdentityRoleDto directRole = assignedRoles.stream().filter(ir -> ir.getDirectRole() == null).findFirst().get();
			Assert.assertEquals(superior.getId(), directRole.getRole());
			//
			// check created account
			AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNotNull(account);
			Assert.assertNotNull(getHelper().findResource(account.getRealUid()));
			//
			// check provisioning archive
			SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
			archiveFilter.setEntityIdentifier(identity.getId());
			//
			List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
			Assert.assertEquals(1, executedOperations.size());
			Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.CREATE));
			//
			// remove one role and add other
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
			final IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setApplicantInfo(new ApplicantImplDto(identity.getId(), IdmIdentityDto.class.getCanonicalName()));
			roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
			roleRequest.setExecuteImmediately(true);
			final IdmRoleRequestDto roleRequestTwo = roleRequestService.save(roleRequest);
			// remove
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequestTwo.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setRole(superior.getId());
			conceptRoleRequest.setIdentityRole(directRole.getId());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
			conceptRoleRequestService.save(conceptRoleRequest);
			// add
			conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setRoleRequest(roleRequestTwo.getId());
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setRole(other.getId());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
			conceptRoleRequestService.save(conceptRoleRequest);
			// execute
			getHelper().executeRequest(roleRequestTwo, false, true);	
			//
			// check after role request is executed
			assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
			Assert.assertEquals(2, assignedRoles.size());
			//
			// check updated account
			AccAccountDto updatedAccount = accountService.getAccount(identity.getUsername(), system.getId());
			Assert.assertNotNull(updatedAccount);
			Assert.assertNotNull(getHelper().findResource(updatedAccount.getRealUid()));
			Assert.assertEquals(account.getCreated(), updatedAccount.getCreated());
			Assert.assertEquals(account.getRealUid(), updatedAccount.getRealUid());
		} finally {
			getHelper().disableAsynchronousProcessing();
		}
	}
	
	@Test
	public void testRoleRequestAsyncWithException() {
		try {
			getHelper().enableAsynchronousProcessing();
			// prepare role composition
			IdmRoleDto superior = getHelper().createRole();
			String exceptionMessage = getHelper().createName();
			//
			// create test system with mapping and link her to the sub roles
			SysSystemDto system = getHelper().createTestResourceSystem(true);
			getHelper().createRoleSystem(superior, system);
			//
			// assign superior role
			IdmIdentityDto identity = getHelper().createIdentity();
			//
			final IdmRoleRequestDto roleRequestOne = getHelper().createRoleRequest(identity, superior);
			
			// Broke the system
			SysSystemMappingDto defaultMapping = getHelper().getDefaultMapping(system);
			defaultMapping.setCanBeAccountCreatedScript("throw new Exception('" + exceptionMessage + "')");
			systemMappingService.save(defaultMapping);
			
			//
			getHelper().executeRequest(roleRequestOne, false);
			//
			// wait for executed events
			final IdmEntityEventFilter eventFilter = new IdmEntityEventFilter();
			eventFilter.setOwnerId(roleRequestOne.getId());
			getHelper().waitForResult(res -> {
				IdmEntityEventDto event = entityEventService.find(eventFilter, PageRequest.of(0, 1)).getContent().get(0);
				return OperationState.EXCEPTION != event.getResult().getState()
						|| !roleRequestService.get(roleRequestOne.getId()).getState().isTerminatedState();
			}, 1000, Integer.MAX_VALUE);
			
			IdmRoleRequestDto roleRequest = roleRequestService.get(roleRequestOne.getId());
			
			Assert.assertEquals(RoleRequestState.EXCEPTION, roleRequest.getState());
			Assert.assertTrue(roleRequest.getLog().contains(exceptionMessage));
			
		} finally {
			getHelper().disableAsynchronousProcessing();
		}
	}

	@Test
	public void testAssignRoleWithoutSubRoleSync() {
		// prepare role composition
		IdmRoleDto superior = getHelper().createRole();
		//
		// create test system with mapping and link her to the sub roles
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		getHelper().createRoleSystem(superior, system);
		//
		// assign superior role
		IdmIdentityDto identity = getHelper().createIdentity();
		//
		final IdmRoleRequestDto roleRequestOne = getHelper().createRoleRequest(identity, superior);
		//
		getHelper().executeRequest(roleRequestOne, false);
		//
		// check after create
		List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByIdentity(identity.getId());
		Assert.assertEquals(1, assignedRoles.size());
		//
		// check created account
		AccAccountDto account = accountService.getAccount(identity.getUsername(), system.getId());
		Assert.assertNotNull(account);
		Assert.assertNotNull(getHelper().findResource(account.getRealUid()));
		//
		// check provisioning archive
		SysProvisioningOperationFilter archiveFilter = new SysProvisioningOperationFilter();
		archiveFilter.setEntityIdentifier(identity.getId());
		//
		List<SysProvisioningArchiveDto> executedOperations = provisioningArchiveService.find(archiveFilter, null).getContent();
		Assert.assertEquals(1, executedOperations.size());
		Assert.assertTrue(executedOperations.stream().anyMatch(o -> o.getOperationType() == ProvisioningEventType.CREATE));
	}
	
	protected TestHelper getHelper() {
		return (TestHelper) super.getHelper();
	}
}
