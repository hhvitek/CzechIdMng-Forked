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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
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
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Entity events.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/entity-events")
@Tag(
		name = IdmEntityEventController.TAG, 
		description = "Operations with entity events"//, 

		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmEntityEventController extends AbstractEventableDtoController<IdmEntityEventDto, IdmEntityEventFilter> {
	
	protected static final String TAG = "Entity events";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmEntityEventController.class);
	//
	@Autowired private EntityEventManager manager;
	
	@Autowired
	public IdmEntityEventController(IdmEntityEventService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_READ + "')")
	@Operation(
			summary = "Search entity events (/search/quick alias)", 
			/* nickname = "searchEntityEvents", */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_READ + "')")
	@Operation(
			summary = "Search entity events", 
			/* nickname = "searchQuickEntityEvents", */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete entity events (selectbox usage)", 
			/* nickname = "autocompleteEntityEvents", */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countEntityEvents", */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_READ + "')")
	@Operation(
			summary = "EntityEvent detail", 
			/* nickname = "getEntityEvent", */ 
			/* response = IdmEntityEventDto.class, */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "EntityEvent's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ENTITYEVENT_UPDATE + "')")
	@Operation(
			summary = "Create / update entity event", 
			/* nickname = "postEntityEvent", */ 
			/* response = IdmEntityEventDto.class, */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_CREATE,
						CoreGroupPermission.ENTITYEVENT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_CREATE,
						CoreGroupPermission.ENTITYEVENT_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmEntityEventDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_UPDATE + "')")
	@Operation(
			summary = "Update entity event", 
			/* nickname = "putEntityEvent", */ 
			/* response = IdmEntityEventDto.class, */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "EntityEvent's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmEntityEventDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_UPDATE + "')")
	@Operation(
			summary = "Update entity event", 
			/* nickname = "patchEntityEvent", */ 
			/* response = IdmEntityEventDto.class, */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			@Parameter(name = "EntityEvent's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_DELETE + "')")
	@Operation(
			summary = "Delete entity event", 
			/* nickname = "deleteEntityEvent", */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "EntityEvent's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ENTITYEVENT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnEntityEvent", */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ,
						CoreGroupPermission.ENTITYEVENT_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ,
						CoreGroupPermission.ENTITYEVENT_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "EntityEvent's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			/* nickname = "availableBulkAction", */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_READ + "')")
	@Operation(
			summary = "Process bulk action", 
			/* nickname = "bulkAction", */ 
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ENTITYEVENT_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action", 
			/* nickname = "prevalidateBulkAction", */ 
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmEntityEventController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ENTITYEVENT_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(value = "/action/bulk/delete", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Delete entity events", 
			/* nickname = "deleteAllEntityEvents", */
			tags = { IdmEntityEventController.TAG },
			description = "Delete all persisted events and their states.")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							IdmGroupPermission.APP_ADMIN }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							IdmGroupPermission.APP_ADMIN })
        }
    )
	public ResponseEntity<?> deleteAll() {
		manager.deleteAllEvents();
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	public Page<IdmEntityEventDto> find(IdmEntityEventFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmEntityEventDto> results = super.find(filter, pageable, permission);
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
			dto.getEmbedded().put(AttachableEntity.PARAMETER_OWNER_ID, loadedDtos.get(ownerId));
		});
		return results;
	}
	
	@Override
	public void deleteDto(IdmEntityEventDto dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		getService().checkAccess(dto, IdmBasePermission.DELETE);
		//
		manager.deleteEvent(dto);
	}
	
	@Override
	protected IdmEntityEventFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmEntityEventFilter filter = new IdmEntityEventFilter(parameters, getParameterConverter());
		// owner decorator
		String ownerId = getParameterConverter().toString(parameters, IdmEntityEventFilter.PARAMETER_OWNER_ID);
		UUID ownerUuid = null;
		String ownerType = filter.getOwnerType();
		if (StringUtils.isNotEmpty(ownerType) && StringUtils.isNotEmpty(ownerId)) {
			// try to find entity owner by Codeable identifier
			AbstractDto owner = manager.findOwner(ownerType, ownerId);
			if (owner != null) {
				ownerUuid = owner.getId();
			} else {
				LOG.debug("Entity type [{}] with identifier [{}] does not found, raw ownerId will be used as uuid.", 
						ownerType, ownerId);
				// Better exception for FE.
				try {
					DtoUtils.toUuid(ownerId);
				} catch (ClassCastException ex) {
					throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", ownerId), ex);
				}
			}
		}
		if (ownerUuid == null) {
			ownerUuid = getParameterConverter().toUuid(parameters, IdmEntityEventFilter.PARAMETER_OWNER_ID);
		}
		filter.setOwnerId(ownerUuid);
		//
		// super owner decorator
		String superOwnerId = getParameterConverter().toString(parameters, IdmEntityEventFilter.PARAMETER_SUPER_OWNER_ID);
		UUID superOwnerUuid = null;
		String superOwnerType = getParameterConverter().toString(parameters, IdmEntityEventFilter.PARAMETER_SUPER_OWNER_TYPE);
		if (StringUtils.isNotEmpty(superOwnerType) && StringUtils.isNotEmpty(superOwnerId)) {
			// try to find entity owner by Codeable identifier
			AbstractDto owner = manager.findOwner(superOwnerType, superOwnerId);
			if (owner != null) {
				superOwnerUuid = owner.getId();
			} else {
				LOG.debug("Entity type [{}] with identifier [{}] does not found, raw ownerId will be used as uuid.", 
						superOwnerType, superOwnerId);
				// Better exception for FE.
				try {
					DtoUtils.toUuid(ownerId);
				} catch (ClassCastException ex) {
					throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", ownerId), ex);
				}
			}
		}
		if (superOwnerUuid == null) {
			superOwnerUuid = getParameterConverter().toUuid(parameters, IdmEntityEventFilter.PARAMETER_SUPER_OWNER_ID);
		}
		filter.setSuperOwnerId(superOwnerUuid);
		//
		return filter;
	}
}
