package eu.bcvsolutions.idm.core.workflow.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.List;

import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.AbstractCoreWorkflowIntegrationTest;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitTestDataProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowHistoricTaskInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;

/**
 * Test history of process and tasks
 *
 * @author svandav
 */
public class HistoryProcessAndTaskTest extends AbstractCoreWorkflowIntegrationTest {

	private static final String PROCESS_KEY = "testHistoryProcessAndTask";

	@Autowired
	private WorkflowHistoricProcessInstanceService historicProcessService;
	@Autowired
	private WorkflowProcessInstanceService processInstanceService;
	@Autowired
	private WorkflowTaskInstanceService taskInstanceService;
	@Autowired
	private WorkflowHistoricTaskInstanceService historicTaskService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private IdmIdentityService identityService;

	@Before
	public void login() {
		super.loginAsAdmin(InitTestDataProcessor.TEST_USER_1);
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void deployAndRunProcess() {
		IdmIdentityDto identityDto = identityService.getByUsername(InitTestDataProcessor.TEST_USER_1);
		//Deploy process
		//Start instance of process
		ProcessInstance instance = processInstanceService.startProcess(PROCESS_KEY, null, identityDto.getId().toString(),
				null);
		logout();
		// Log as user without ADMIN rights
		loginAsNoAdmin(InitTestDataProcessor.TEST_USER_1);
		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessInstanceId(instance.getId());
		List<WorkflowProcessInstanceDto> processes = processInstanceService.find(filter, null, IdmBasePermission.READ).getContent();

		assertEquals(PROCESS_KEY, processes.get(0).getName());
		WorkflowHistoricProcessInstanceDto historicProcessDto = historicProcessService.get(instance.getId());
		assertNotNull(historicProcessDto);

		this.logout();
		// Log as user without ADMIN rights
		loginAsNoAdmin(InitTestDataProcessor.TEST_USER_2);
		// Applicant for this process is testUser1. For testUser2 must be result
		// null
		historicProcessDto = historicProcessService.get(instance.getId());
		assertNull(historicProcessDto);

		this.logout();
		// Log as ADMIN
		loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		// Applicant for this process is testUser1. For testUser2 must be result
		// null, but as ADMIN can see all historic processes
		historicProcessDto = historicProcessService.get(instance.getId());
		assertNotNull(historicProcessDto);
		
		// get diagram
		assertNotNull(historicProcessService.getDiagram(instance.getId()));

		this.logout();
		this.loginAsAdmin(InitTestDataProcessor.TEST_USER_1);

		completeTasksAndCheckHistory();
	}


	private void completeTasksAndCheckHistory() {

		WorkflowFilterDto filter = new WorkflowFilterDto();
		filter.setProcessDefinitionKey(PROCESS_KEY);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.find(filter, null).getContent();
		assertEquals(1, tasks.size());
		assertEquals("userTaskFirst", tasks.get(0).getName());
		String taskId = tasks.get(0).getId();
		String processId = tasks.get(0).getProcessInstanceId();

		taskInstanceService.completeTask(taskId, null);

		//Check task history
		checkTaskHistory(taskId, InitTestDataProcessor.TEST_USER_1);

		//Second task is for testUser2 (is candidate) for testUser1 must be null
		filter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_1);
		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.find(filter, null).getContent();
		assertEquals(0, tasks.size());

		this.logout();
		this.loginAsAdmin(InitTestDataProcessor.TEST_USER_2);
		filter.setCandidateOrAssigned(InitTestDataProcessor.TEST_USER_2);
		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.find(filter, null).getContent();
		assertEquals(1, tasks.size());
		assertEquals("userTaskSecond", tasks.get(0).getName());
		taskId = tasks.get(0).getId();
		taskInstanceService.completeTask(taskId, null);

		//Check task history
		checkTaskHistory(taskId, InitTestDataProcessor.TEST_USER_2);

		tasks = (List<WorkflowTaskInstanceDto>) taskInstanceService.find(filter, null).getContent();
		assertEquals(0, tasks.size());

		//Find history of process. Historic process must exist and must be ended.
		WorkflowHistoricProcessInstanceDto historicProcess = historicProcessService.get(processId);
		assertNotNull(historicProcess);
		assertNotNull(historicProcess.getEndTime());

	}

	/**
	 * Check task history
	 *
	 * @param taskId
	 */
	private void checkTaskHistory(String taskId, String assignee) {
		IdmIdentityDto assigneeIdentity = (IdmIdentityDto) lookupService.getDtoLookup(IdmIdentityDto.class).lookup(assignee);
		WorkflowHistoricTaskInstanceDto taskHistory = historicTaskService.get(taskId);
		assertNotNull(taskHistory);
		assertNull(taskHistory.getDeleteReason());
		assertEquals(assigneeIdentity.getId().toString(), taskHistory.getAssignee());
		assertEquals(taskId, taskHistory.getId());
	}

	/**
	 * Its needed for parsing history from string
	 */
	abstract class IgnoreSetIdMixIn
	{
		@JsonIgnore
		public abstract void setId(Serializable id);
	}
}
