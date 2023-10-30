package eu.bcvsolutions.idm.core.workflow.rest;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.activiti.engine.task.IdentityLinkType;
import org.apache.commons.collections.CollectionUtils;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegation_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.workflow.model.dto.FormDataWrapperDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceAbstractDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest controller for workflow instance tasks
 *
 * TODO: secure endpoints
 *
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-tasks")
@Tag(
		name = WorkflowTaskInstanceController.TAG,
		
		description = "Running WF tasks"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class WorkflowTaskInstanceController extends AbstractReadDtoController<WorkflowTaskInstanceDto, WorkflowFilterDto> {

	protected static final String TAG = "Workflow - task instances";

	@Autowired
	private DelegationManager delegationManager;
	@Autowired
	private BulkActionManager bulkActionManager;
	@Autowired
	private SecurityService securityService;
	@Autowired
	private WorkflowProcessInstanceService processInstanceService;

	private final WorkflowTaskInstanceService workflowTaskInstanceService;

	@Autowired
	public WorkflowTaskInstanceController(
			WorkflowTaskInstanceService entityService) {
		super(entityService);
		//
		this.workflowTaskInstanceService = entityService;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_TASK_READ + "')")
	@Operation(
			summary = "Search task instances",
			/* nickname = "searchTaskInstances", */
			tags = {WorkflowTaskInstanceController.TAG})
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			CoreGroupPermission.WORKFLOW_TASK_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			CoreGroupPermission.WORKFLOW_TASK_READ})
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

    @RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
    @Operation(
            summary = "Historic task instance detail",
            /* nickname = "getHistoricTaskInstance", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = WorkflowTaskInstanceDto.class
                                    )
                            )
                    }
            ),
            tags = {WorkflowTaskInstanceController.TAG})
    public ResponseEntity<?> get(
            @Parameter(description = "Task instance id.", required = true)
            @PathVariable String backendId) {
        return super.get(backendId);
    }

	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/complete")
	@Operation(
			summary = "Complete task instance",
			/* nickname = "completeTaskInstance", */
			tags = {WorkflowTaskInstanceController.TAG},
			description = "Complete task with given decision.")
	public void completeTask(
			 @Parameter(description = "Task instance id.", required = true)
			@PathVariable String backendId,
			 @Parameter(description = "Complete decision, variables etc.", required = true)
			@RequestBody FormDataWrapperDto formData) {
		workflowTaskInstanceService.completeTask(backendId, formData.getDecision(), formData.getFormData(), formData.getVariables());
		//
		// TODO: no content should be returned
		// return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}/permissions")
	@Operation(
			summary = "Historic task instance detail",
			/* nickname = "getHistoricTaskInstance", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = WorkflowTaskInstanceDto.class
                                    )
                            )
                    }
            ),
			tags = {WorkflowTaskInstanceController.TAG})
	@Override
	public Set<String> getPermissions(
			 @Parameter(description = "Task instance id.", required = true)
			@PathVariable String backendId) {
		WorkflowFilterDto context = new WorkflowFilterDto();
		context.setOnlyInvolved(Boolean.FALSE);
		WorkflowTaskInstanceDto taskInstanceDto = workflowTaskInstanceService.get(backendId, context);
		if (taskInstanceDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		return workflowTaskInstanceService.getPermissions(taskInstanceDto);
	}

	@Override
	// We need override that method (#1320). Parent using lookup service for get DTO. Lookup
	// service get DTO without permissions and after that check permission on READ.
	// Without permissions is loaded Task with all buttons (canExecute is true). We
	// need call direct service with Permission.READ!
	public WorkflowTaskInstanceDto getDto(Serializable backendId) {
		WorkflowTaskInstanceDto dto = getService().get(backendId, IdmBasePermission.READ);
		// Found task is returned immediately only if is not null and is not historic.

		// Why if is historic? If user isn't assigned to this task, but is involved (is delegator), then historic task will
		// be returned, but in same time real task instance is not completed. This can confuse a user, because information/label
		// on FE will said that task was resolved. For prevent this, I try to find task instance if user has permission to the process.
		if (dto == null || dto instanceof WorkflowHistoricTaskInstanceDto) {
			WorkflowFilterDto filter = new WorkflowFilterDto();
			filter.setOnlyInvolved(Boolean.FALSE);
			if (dto == null) {
				// First load the task without check permission. We need ID of a process.
				WorkflowTaskInstanceDto task = workflowTaskInstanceService.get(backendId, filter);
				if (task == null) {
					return null;
				}
				dto = task;
			}
			boolean hasUsePermissionOnProcess = processInstanceService.canReadProcessOrHistoricProcess(dto.getProcessInstanceId());
			if (hasUsePermissionOnProcess) {
				// User has permission to read the process. We can set filter for find all tasks (check on the user has to be involved in tasks, will be skip).
				dto = getService().get(backendId, filter, IdmBasePermission.READ);
			}
		}

		// Add delegation to a task.
		addDelegationToTask(dto, IdmBasePermission.READ);

		return dto;
	}

	@Override
	public Page<WorkflowTaskInstanceDto> find(WorkflowFilterDto filter, Pageable pageable, BasePermission permission) {
		Page<WorkflowTaskInstanceDto> results = super.find(filter, pageable, permission);
		// Add delegation to a tasks.
		results.getContent()
				.forEach(task -> addDelegationToTask(task, permission));

		return results;
	}
	
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_TASK_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			/* nickname = "availableBulkAction", */ 
			tags = { WorkflowTaskInstanceController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			CoreGroupPermission.WORKFLOW_TASK_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			CoreGroupPermission.WORKFLOW_TASK_READ})
        }
    )
	@Override
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return bulkActionManager.getAvailableActionsForDto(WorkflowTaskInstanceAbstractDto.class);
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_TASK_READ + "')")
	@Operation(
			summary = "Process bulk action",
			/* nickname = "bulkAction", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = { WorkflowTaskInstanceController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			CoreGroupPermission.WORKFLOW_TASK_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			CoreGroupPermission.WORKFLOW_TASK_READ})
        }
    )
	@Override
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		// Set DTO name to the action directly.
		bulkAction.setDtoClass(WorkflowTaskInstanceAbstractDto.class.getCanonicalName());
		
		return super.bulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.WORKFLOW_TASK_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action",
			/* nickname = "prevalidateBulkAction", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = { WorkflowTaskInstanceController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			CoreGroupPermission.WORKFLOW_TASK_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			CoreGroupPermission.WORKFLOW_TASK_READ})
        }
    )
	@Override
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		// Set DTO name to the action directly.
		bulkAction.setDtoClass(WorkflowTaskInstanceAbstractDto.class.getCanonicalName());
		
		return super.prevalidateBulkAction(bulkAction);
	}

	/**
	 * Find and add definition of the delegation connected with this task.
	 *
	 * @param dto
	 */
	private void addDelegationToTask(WorkflowTaskInstanceDto dto, BasePermission... permission) {
		if (dto != null && dto.getId() != null) {
			// We need to create mock task, because DTO can be instance of historic task here.
			WorkflowTaskInstanceDto mockTask = new WorkflowTaskInstanceDto();
			mockTask.setId(dto.getId());
			
			UUID currentUserId = securityService.getCurrentId();
			
			boolean currentUserIsCandidate = dto.getIdentityLinks().stream()
				.filter(identityLink -> IdentityLinkType.CANDIDATE.equals(identityLink.getType())
				|| IdentityLinkType.ASSIGNEE.equals(identityLink.getType()))
				.anyMatch(identityLink -> currentUserId != null
						&& UUID.fromString(identityLink.getUserId()).equals(currentUserId));
			
			boolean filterOnlyForCurrentUser = currentUserIsCandidate && !workflowTaskInstanceService.canReadAllTask(permission);
			
			List<IdmDelegationDto> delegations = delegationManager.findDelegationForOwner(mockTask, permission)
					.stream()
					.filter(delegation -> {
						// Filter only delegation where delegator or delegate is logged user (and user is not admin).
						if (!filterOnlyForCurrentUser) {
							return true;
						}
						IdmDelegationDefinitionDto definition = DtoUtils.getEmbedded(delegation,
								IdmDelegation_.definition.getName(), IdmDelegationDefinitionDto.class);
						
						return definition.getDelegate().equals(currentUserId)
								|| definition.getDelegator().equals(currentUserId);
					})
					.sorted(Comparator.comparing(IdmDelegationDto::getCreated))
					.collect(Collectors.toList());
			
			// TODO: ONLY first delegation definition is sets to the task!
			if (!CollectionUtils.isEmpty(delegations)) {
				Collections.reverse(delegations);
				IdmDelegationDto delegation = delegations.get(0);
				IdmDelegationDefinitionDto definition = DtoUtils.getEmbedded(delegation,
						IdmDelegation_.definition.getName(), IdmDelegationDefinitionDto.class);
				dto.setDelegationDefinition(definition);
			}
		}
	}

	@Override
	protected WorkflowFilterDto toFilter(MultiValueMap<String, Object> parameters) {
		WorkflowFilterDto filter = super.toFilter(parameters);
		if (filter == null) {
			return new WorkflowFilterDto();
		}
		if (filter.getOnlyInvolved() != Boolean.TRUE) {
			throw new ResultCodeException(CoreResultCode.WF_TASK_FILTER_INVOLVED_ONLY);
		}
		return filter;
	}
	
}
