package eu.bcvsolutions.idm.core.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
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
 * Entity states.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/entity-states")
@Tag(name = IdmEntityStateController.TAG, description = "Operations with entity states")
public class IdmEntityStateController extends AbstractEventableDtoController<IdmEntityStateDto, IdmEntityStateFilter> {
	
	protected static final String TAG = "Entity states";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmEntityStateController.class);
	//
	@Autowired private EntityEventManager manager;
	
	@Autowired
	public IdmEntityStateController(IdmEntityStateService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
	@Operation(
			summary = "Search entity states (/search/quick alias)",
			operationId = "searchEntityStates", 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ENTITYSTATE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ENTITYSTATE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
	@Operation(
			summary = "Search entity states",
			operationId = "searchQuickEntityStates", 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ENTITYSTATE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ENTITYSTATE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete entity states (selectbox usage)",
			operationId = "autocompleteEntityStates", 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countEntityStates", 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ENTITYSTATE_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ENTITYSTATE_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
	@Operation(
			summary = "EntityState detail",
			operationId = "getEntityState", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmEntityStateDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ENTITYSTATE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ENTITYSTATE_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "EntityState's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ENTITYSTATE_UPDATE + "')")
	@Operation(
			summary = "Create / update entity state",
			operationId = "postEntityState", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmEntityStateDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYSTATE_CREATE,
						CoreGroupPermission.ENTITYSTATE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYSTATE_CREATE,
						CoreGroupPermission.ENTITYSTATE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmEntityStateDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_UPDATE + "')")
	@Operation(
			summary = "Update entity state",
			operationId = "putEntityState", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmEntityStateDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ENTITYSTATE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ENTITYSTATE_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "EntityState's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmEntityStateDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_UPDATE + "')")
	@Operation(
			summary = "Update entity state",
			operationId = "patchEntityState", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmEntityStateDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ENTITYSTATE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ENTITYSTATE_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "EntityState's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_DELETE + "')")
	@Operation(
			summary = "Delete entity state",
			operationId = "deleteEntityState", 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ENTITYSTATE_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ENTITYSTATE_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "EntityState's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnEntityState", 
			tags = { IdmEntityStateController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYSTATE_READ,
						CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYSTATE_READ,
						CoreGroupPermission.ENTITYSTATE_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "EntityState's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	public Page<IdmEntityStateDto> find(IdmEntityStateFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmEntityStateDto> results = super.find(filter, pageable, permission);
		// fill entity embedded for FE
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		results.getContent().forEach(dto -> {
			UUID ownerId = dto.getOwnerId();
			if (!loadedDtos.containsKey(ownerId)) {
				try {
					loadedDtos.put(ownerId, getLookupService().lookupDto(dto.getOwnerType(), ownerId));
				} catch (IllegalArgumentException ex) {
					LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getOwnerType(), ex);
				}
			}
			dto.getEmbedded().put("ownerId", loadedDtos.get(ownerId));
		});
		return results;
	}

    /**
     * Get available bulk actions for entity state
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
    @Operation(
            summary = "Get available bulk actions",
            operationId = "availableBulkAction",
            tags = {IdmEntityStateController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            CoreGroupPermission.ENTITYSTATE_READ}),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            CoreGroupPermission.ENTITYSTATE_READ})
        }
    )
    public List<IdmBulkActionDto> getAvailableBulkActions() {
        return super.getAvailableBulkActions();
    }

    /**
     * Process bulk action for entity state
     *
     * @param bulkAction
     * @return
     */
    @ResponseBody
    @RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
    @Operation(
            summary = "Process bulk action for entity state",
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
            tags = {IdmEntityStateController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            CoreGroupPermission.ENTITYSTATE_READ}),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            CoreGroupPermission.ENTITYSTATE_READ})
        }
    )
    public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
        return super.bulkAction(bulkAction);
    }

    /**
     * Prevalidate bulk action for entity state
     *
     * @param bulkAction
     * @return
     */
    @ResponseBody
    @RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYSTATE_READ + "')")
    @Operation(
            summary = "Prevalidate bulk action for entity state",
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
            tags = {IdmEntityStateController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            CoreGroupPermission.ENTITYSTATE_READ}),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            CoreGroupPermission.ENTITYSTATE_READ})
        }
    )
    public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
        return super.prevalidateBulkAction(bulkAction);
    }
    
    @Override
	protected IdmEntityStateFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmEntityStateFilter filter = new IdmEntityStateFilter(parameters, getParameterConverter());
		//
		// owner decorator
		String ownerId = getParameterConverter().toString(parameters, IdmEntityStateFilter.PARAMETER_OWNER_ID);
		UUID ownerUuid = null;
		if (StringUtils.isNotEmpty(filter.getOwnerType()) 
				&& StringUtils.isNotEmpty(ownerId)) {
			// try to find entity owner by Codeable identifier
			AbstractDto owner = manager.findOwner(filter.getOwnerType(), ownerId);
			if (owner != null) {
				ownerUuid = owner.getId();
			} else {
				LOG.debug("Entity type [{}] with identifier [{}] does not found, raw ownerId will be used as uuid.", 
						filter.getOwnerType(), ownerId);
				// Better exception for FE.
				try {
					DtoUtils.toUuid(ownerId);
				} catch (ClassCastException ex) {
					throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", ownerId), ex);
				}
			}
		}
		if (ownerUuid == null) {
			ownerUuid = getParameterConverter().toUuid(parameters, "ownerId");
		}
		filter.setOwnerId(ownerUuid);
		//
		return filter;
	}

}
