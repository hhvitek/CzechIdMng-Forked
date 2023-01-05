package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Cover exceptions for assigning automatic roles by tree structure.
 * 
 * @author Radek Tomiška
 *
 */
public class ProcessAutomaticRoleByTreeTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired @Qualifier(ProcessAutomaticRoleByTreeTaskExecutor.TASK_NAME)
	private ProcessAutomaticRoleByTreeTaskExecutor executor;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	
	@Test
	public void testPreventEndIfNotProcessed() throws InterruptedException, ExecutionException {
		// concept request
		IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
		// set required request props and save request
		roleRequest.setState(RoleRequestState.CONCEPT);
		roleRequest.setExecuteImmediately(true); // without approval
		roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
		IdmIdentityDto applicant = getHelper().createIdentity((GuardedString) null);
		roleRequest.setApplicantInfo(new ApplicantImplDto(applicant.getId(), IdmIdentityDto.class.getCanonicalName()));
		roleRequest = roleRequestService.save(roleRequest);
		IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(applicant);
		conceptRoleRequest.setIdentityContract(contract.getId());
		conceptRoleRequest.setValidFrom(contract.getValidFrom());
		conceptRoleRequest.setValidTill(contract.getValidTill());
		conceptRoleRequest.setRole(getHelper().createRole().getId());
		conceptRoleRequest.setOperation(ConceptRoleRequestOperation.ADD);
		conceptRoleRequest.setRoleRequest(roleRequest.getId());
		//
		conceptRoleRequestService.save(conceptRoleRequest);
		//
		ProcessAutomaticRoleByTreeTaskExecutor executor = new ProcessAutomaticRoleByTreeTaskExecutor();
		AutowireHelper.autowire(executor);
		//
		executor.setRemoveNotProcessedIdentityRoles(true);
		executor.setProcessedRoleRequests(Sets.newHashSet(roleRequest.getId()));
		IdmLongRunningTaskDto lrt = longRunningTaskManager.resolveLongRunningTask(executor, null, OperationState.RUNNING);
		//
		executor.end(Boolean.TRUE, null);
		//
		// long running task has a proper state with exception
		lrt = longRunningTaskManager.getLongRunningTask(lrt.getId());
		Assert.assertFalse(lrt.isRunning());
		Assert.assertEquals(OperationState.EXCEPTION, lrt.getResultState());
		Assert.assertEquals(CoreResultCode.AUTOMATIC_ROLE_ASSIGN_NOT_COMPLETE.name(), lrt.getResult().getCode());
	}
	
	@Test
	public void testFormInstance() {
		ConfigurationMap properties = new ConfigurationMap();
		//
		Assert.assertNull(executor.getFormInstance(properties));
		//
		UUID automaticRoleId = UUID.randomUUID();
		properties.put(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE, automaticRoleId);
		//
		IdmFormInstanceDto formInstance = executor.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(automaticRoleId) 
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
		//
		IdmRoleTreeNodeDto automaticRole = getHelper().createRoleTreeNode(getHelper().createRole(), getHelper().createTreeNode(), true);
		properties.put(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE, automaticRole.getId());
		//
		formInstance = executor.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(automaticRole.getId())
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
	}
	
}
