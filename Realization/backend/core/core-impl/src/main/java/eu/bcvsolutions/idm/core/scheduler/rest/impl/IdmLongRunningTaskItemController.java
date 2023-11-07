package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmProcessedTaskItem_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Default controller for Processed Task Item
 *
 * @author Marek Klement
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/long-running-task-items")
@Tag(name = IdmLongRunningTaskItemController.TAG, description = "Operations with processed task items")
public class IdmLongRunningTaskItemController extends AbstractReadWriteDtoController<IdmProcessedTaskItemDto, IdmProcessedTaskItemFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmLongRunningTaskItemController.class);
	protected static final String TAG = "Long running task items";
	//
	private final IdmProcessedTaskItemService itemService;
	//
	@Autowired private IdmAuditService auditService;

	@Autowired
	public IdmLongRunningTaskItemController(IdmProcessedTaskItemService itemService) {
		super(itemService);
		//
		Assert.notNull(itemService, "Service is required.");
		//
		this.itemService = itemService;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Processed task items",
			operationId = "getProcessedTaskItems",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmProcessedTaskItemDto.class
                                    )
                            )
                    }
            ),
			tags={ IdmLongRunningTaskItemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Processed task's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(summary = "Search processed task's items (/search/quick alias)", operationId = "searchProcessedTaskItems", tags={ IdmLongRunningTaskItemController.TAG })
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
	@Transactional(readOnly = true)
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(summary = "Search processed task's items", operationId = "searchQuickProcessedTaskItems", tags={ IdmLongRunningTaskItemController.TAG })
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
	
	@Transactional(readOnly = true)
	@Override
	public Page<IdmProcessedTaskItemDto> find(IdmProcessedTaskItemFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmProcessedTaskItemDto> dtos = super.find(filter, pageable, permission);
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		dtos.forEach(dto -> {
			loadEmbeddedEntity(loadedDtos, dto);
		});
		//
		return dtos;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_DELETE + "')")
	@Operation(
			summary = "Delete record",
			operationId = "deleteRecord",
			tags = { IdmLongRunningTaskItemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Records's uuid identifier", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/queue-item", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@Operation(
			summary = "Create record",
			operationId = "createRecord",
			tags = { IdmLongRunningTaskItemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_CREATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_CREATE })
    })
	public ResponseEntity<?> addToQueue(
			 @Parameter(description = "Records's uuid identifier", required = true)
			@PathVariable @NotNull String backendId, @Valid @RequestBody UUID scheduledTask) {
		IdmProcessedTaskItemDto itemDto = itemService.get(backendId);
		itemService.createQueueItem(itemDto, new OperationResult(OperationState.EXECUTED), scheduledTask);
		//
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@Override
	protected IdmProcessedTaskItemFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmProcessedTaskItemFilter filter = super.toFilter(parameters);
		
		return filter;
	}
	
	/**
	 * Fills referenced entity to dto - prevent to load entity for each row
	 * 
	 * @param dto
	 */
	@SuppressWarnings("unchecked")
	private void loadEmbeddedEntity(Map<UUID, BaseDto> loadedDtos, IdmProcessedTaskItemDto dto) {
		UUID entityId = dto.getReferencedEntityId();
		try {
			BaseDto item = null;
			if (!loadedDtos.containsKey(entityId)) {
				String referencedDtoType = dto.getReferencedDtoType();
				item = getLookupService().lookupDto(referencedDtoType, entityId);
				//
				// try to find in audit for deleted entities
				if (item == null) {
					dto.setDeleted(true);
					Object lastPersistedVersion = auditService.findLastPersistedVersion(
							getLookupService().getEntityClass((Class<? extends Identifiable>) Class.forName(referencedDtoType)), 
							entityId
					);
					if (lastPersistedVersion != null) {
						item = getLookupService().toDto((BaseEntity) lastPersistedVersion, null, null);
					}
				}
				loadedDtos.put(entityId, item);
			}
			dto.getEmbedded().put(IdmProcessedTaskItem_.referencedEntityId.getName(), loadedDtos.get(entityId));
		} catch (IllegalArgumentException | ClassNotFoundException ex) {
			LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getReferencedDtoType(), ex);
		}
	}
}
