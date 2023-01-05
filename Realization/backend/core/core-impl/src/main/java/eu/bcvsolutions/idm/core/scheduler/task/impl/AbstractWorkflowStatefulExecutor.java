package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;

/**
 * Common LRT with workflow support
 * 
 * @author Jan Helbich
 *
 * @param <T>
 */
public abstract class AbstractWorkflowStatefulExecutor<T extends AbstractDto> extends AbstractSchedulableStatefulExecutor<T> {
	
	protected static final String SCHEDULED_TASK_ID_WF_PARAM = "scheduledTaskId";
	protected static final String LONG_RUNNING_TASK_ID_WF_PARAM = "longRunningTaskId";
	protected static final String DTO_WF_PARAM = "dto";
	
	@Autowired protected WorkflowProcessInstanceService workflowService;

	@Override
	public Optional<OperationResult> processItem(T dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		Map<String, Object> variables = getVariables();
		variables.put(SCHEDULED_TASK_ID_WF_PARAM, this.getScheduledTaskId());
		variables.put(LONG_RUNNING_TASK_ID_WF_PARAM, this.getLongRunningTaskId());
		variables.put(DTO_WF_PARAM, dto);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// execute process
		ProcessInstance pi = workflowService.startProcess(this.getWorkflowName(), null, null, variables);
		
		if (pi instanceof VariableScope) {
			VariableScope vs = (VariableScope) pi;
			Object or = vs.getVariable(OPERATION_RESULT_VAR);
			return or == null ? Optional.empty() : Optional.of((OperationResult) or);
		}
		return Optional.empty();
	}
	
	/**
	 * Return the name of workflow definition, which executes the process.
	 * @return
	 */
	public abstract String getWorkflowName();
	
	/**
	 * Return variables for WF. Variables will be added into process and to variables will be added these attributes:
	 * - dto (process item),
	 * - scheduledTaskId,
	 * - longRunningTaskId.
	 * 
	 * @return
	 */
	protected Map<String, Object> getVariables() {
		return new HashMap<>();
	}
}