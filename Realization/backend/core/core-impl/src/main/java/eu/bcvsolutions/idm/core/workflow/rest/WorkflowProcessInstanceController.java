package eu.bcvsolutions.idm.core.workflow.rest;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest controller for workflow instance processes
 * 
 * TODO: secure endpoints
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-processes")
@Tag(name = WorkflowProcessInstanceController.TAG, description = "Running WF processes")
public class WorkflowProcessInstanceController extends AbstractReadWriteDtoController<WorkflowProcessInstanceDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - process instances";
	//
	@Autowired
	private LookupService entityLookupService;
	private final WorkflowProcessInstanceService workflowProcessInstanceService;
	
	@Autowired
	public WorkflowProcessInstanceController(
			WorkflowProcessInstanceService workflowProcessInstanceService) {
		super(workflowProcessInstanceService);
		//
		Assert.notNull(workflowProcessInstanceService, "Service is required.");
		//
		this.workflowProcessInstanceService = workflowProcessInstanceService;
	}

	/**
	 * Search workflow processes instances for given identity username and
	 * process definition key
	 * 
	 * @param size
	 * @param page
	 * @param sort
	 * @param identity
	 * @param processDefinitionKey
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	@Operation(
			summary = "Search process instances",
			operationId = "searchQuickProcessInstances",
			tags = { WorkflowProcessInstanceController.TAG })
	@PageableAsQueryParam
	public CollectionModel<?> searchQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{backendId}")
	@Operation(
			summary = "Delete process instances",
			operationId = "deleteProcessInstances",
			tags = { WorkflowProcessInstanceController.TAG })
	public ResponseEntity<WorkflowProcessInstanceDto> delete(
			 @Parameter(description = "Process instance id.", required = true)
			@PathVariable @NotNull String backendId) {
		return new ResponseEntity<WorkflowProcessInstanceDto>(
				workflowProcessInstanceService.delete(backendId, null), HttpStatus.OK);
	}
	
	@Override
	protected WorkflowFilterDto toFilter(MultiValueMap<String, Object> parameters) {
		WorkflowFilterDto filter = super.toFilter(parameters);
		String applicant = getParameterConverter().toString(parameters, "identity");
		if (applicant != null) {
				filter.getEqualsVariables().put(WorkflowProcessInstanceService.APPLICANT_IDENTIFIER, applicant);
		}
		
		return filter;
	}

}
