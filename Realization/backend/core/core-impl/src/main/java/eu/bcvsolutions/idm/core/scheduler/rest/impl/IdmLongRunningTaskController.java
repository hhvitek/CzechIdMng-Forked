package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Default controller long running tasks (LRT)
 *
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/long-running-tasks")
@Tag(name = IdmLongRunningTaskController.TAG, description = "Operations with long running tasks (LRT)")
public class IdmLongRunningTaskController
	extends AbstractReadWriteDtoController<IdmLongRunningTaskDto, IdmLongRunningTaskFilter> {

	protected static final String TAG = "Long running tasks";
	//
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private AttachmentManager attachmentManager;

	@Autowired
	public IdmLongRunningTaskController(
			IdmLongRunningTaskService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Search LRTs (/search/quick alias)", 
			operationId = "searchLongRunningTasks",
			tags={ IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	/**
	 * All endpoints will support find quick method.
	 *
	 * @param parameters
	 * @param pageable
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Search LRTs", 
			operationId = "searchQuickLongRunningTasks",
			tags={ IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete LRTs (selectbox usage)", 
			operationId = "autocompleteLongRunningTasks",
			tags = { IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_AUTOCOMPLETE })
    })
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			operationId = "countLongRunningTasks",
			tags = { IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "LRT detail",
			operationId = "getLongRunningTask",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmLongRunningTaskDto.class
                                    )
                            )
                    }
            ),
			tags={ IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "LRT's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	public IdmLongRunningTaskDto getDto(Serializable backendId) {
		// FIXME: Propagate filter in GET method (in AbstractReadDto controller => requires lookup api improvement).
		IdmLongRunningTaskFilter filter = toFilter(null);
		//
		IdmLongRunningTaskDto dto =  getService().get(backendId, filter, IdmBasePermission.READ);
		if (dto == null) {
			return null;
		}
		dto.getEmbedded().put(IdmFormInstanceDto.PROPERTY_FORM_INSTANCE, longRunningTaskManager.getTaskFormInstance(dto));
		//
		return dto;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_DELETE + "')")
	@Operation(
			summary = "Delete LRT", 
			operationId = "deleteLongRunningTask",
			tags = { IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "LRT's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			operationId = "getPermissionsOnLongRunningTask",
			tags = { IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.SCHEDULER_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCHEDULER_READ})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "LRT's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			operationId = "availableBulkAction",
			tags = { IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Process bulk action", 
			operationId = "bulkAction",
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
			tags = { IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.SCHEDULER_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCHEDULER_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action", 
			operationId = "prevalidateBulkAction",
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
			tags = { IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.SCHEDULER_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCHEDULER_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/download/{attachmentId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Download result from LRT",
			operationId = "downloadReslut",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmLongRunningTaskDto.class
                                    )
                            )
                    }
            ),
			tags={ IdmLongRunningTaskController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	public ResponseEntity<?> downloadResult(
			 @Parameter(description = "LRT's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			 @Parameter(description = "Attachment's id.", required = true)
			@PathVariable @NotNull String attachmentId) {
		
		// check if user has permission for read the long running task
		IdmLongRunningTaskDto longRunningTaskDto = getDto(backendId);
		if (longRunningTaskDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		//
		IdmAttachmentDto attachment = longRunningTaskManager.getAttachment(
				longRunningTaskDto.getId(), 
				DtoUtils.toUuid(attachmentId), 
				IdmBasePermission.READ);
		InputStream is = attachmentManager.getAttachmentData(attachment.getId(), IdmBasePermission.READ);
		String attachmentName = attachment.getName();
		//
		return ResponseEntity.ok()
				.contentLength(attachment.getFilesize())
				.contentType(MediaType.parseMediaType(attachment.getMimetype()))
				.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", attachmentName))
				.body(new InputStreamResource(is));
	}

	/**
	 * Cancels running job.
	 *
	 * @param backendId LRT's uuid identifier
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/cancel")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@Operation(
			summary = "Cancel running task",
			operationId = "cancelLongRunningTask",
			tags={ IdmLongRunningTaskController.TAG },
						description = "Stop running task in next internal task's iteration (when counter is incremented).")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_UPDATE })
    })
	public ResponseEntity<?> cancel(
			 @Parameter(description = "LRT's uuid identifier.", required = true)
			@PathVariable UUID backendId) {
		longRunningTaskManager.cancel(backendId);
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Kills running job
	 *
	 * @param taskName name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/interrupt")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@Operation(
			summary = "Interrupt running task",
			operationId = "interruptLongRunningTask",
			tags={ IdmLongRunningTaskController.TAG },
						description = "Interrupt given LRT - \"kills\" thread with running task.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_UPDATE })
    })
	public ResponseEntity<?> interrupt(
			 @Parameter(description = "LRT's uuid identifier.", required = true)
			@PathVariable UUID backendId) {
		longRunningTaskManager.interrupt(backendId);
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Executes prepared task from long running task queue
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/action/process-created")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@Operation(
			summary = "Process created LRTs",
			operationId = "processCreatedLongRunningTasks",
			tags={ IdmLongRunningTaskController.TAG },
			description = "When LRT is created, then is added to queue with state created only."
					+ " Another scheduled task for processing prepared task will execute them."
					+ " This operation process prepared tasks immediately.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE })
    })
	public ResponseEntity<?> processCreated() {
		longRunningTaskManager.processCreated();
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/process")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@Operation(
			summary = "Process created LRT",
			operationId = "processCreatedLongRunningTask",
			tags={ IdmLongRunningTaskController.TAG },
			description = "When LRT is created, then is added to queue with state created only."
					+ " Another scheduled task for processing prepared task will execute them."
					+ " This operation process prepared task by given identifier immediately.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE })
    })
	public ResponseEntity<?> processCheckedCreated(
			 @Parameter(description = "LRT's uuid identifier.", required = true)
			@PathVariable UUID backendId) {	
		longRunningTaskManager.processCreated(backendId);
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	/**
	 * @since 10.2.0
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/recover")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@Operation(
			summary = "Process LRT again",
			operationId = "recoverLongRunningTask",
			tags={ IdmLongRunningTaskController.TAG },
			description = "When LRT is executed and recoverable, then task can be executed again."
					+ " Task will be executed with the same configuration."
					+ " If task was executed by scheduled task, then scheduled task will be reused => already processed items will be not processed.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE })
    })
	public ResponseEntity<?> recover(
			 @Parameter(description = "LRT's uuid identifier.", required = true)
			@PathVariable UUID backendId) {
		longRunningTaskManager.recover(backendId);
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	public Page<IdmLongRunningTaskDto> find(IdmLongRunningTaskFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmLongRunningTaskDto> results = super.find(filter, pageable, permission);
		results
			.stream()
			.forEach(dto -> {
				dto.getEmbedded().put(IdmFormInstanceDto.PROPERTY_FORM_INSTANCE, longRunningTaskManager.getTaskFormInstance(dto));
			});
		//
		return results;
	}
	
	@Override
	protected IdmLongRunningTaskFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter(parameters, getParameterConverter());
		// counters are loaded from controller all times
		filter.setIncludeItemCounts(true);
		//
		return filter;
	}
}
