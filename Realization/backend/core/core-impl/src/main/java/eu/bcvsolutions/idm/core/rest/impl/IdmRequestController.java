package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemChangesDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestItemFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRequestItemService;
import eu.bcvsolutions.idm.core.api.service.IdmRequestService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Universal request endpoint
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/requests")
@Tag(
		name = IdmRequestController.TAG, 
		description = "Operations with requests"//,

		//produces = BaseController.APPLICATION_HAL_JSON_VALUE

//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmRequestController extends AbstractReadWriteDtoController<IdmRequestDto, IdmRequestFilter>{

	protected static final String TAG = "Universal requests";
	
	@Autowired
	private RequestManager requestManager;
	@Autowired
	private IdmRequestItemService requestItemService;
	
	@Autowired
	public IdmRequestController(
			IdmRequestService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_READ + "')")
	@Operation(
			summary = "Search requests (/search/quick alias)",
			/* nickname = "searchRequests", */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.REQUEST_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.REQUEST_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_READ + "')")
	@Operation(
			summary = "Search requests",
			/* nickname = "searchQuickRequests", */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.REQUEST_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.REQUEST_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete requests (selectbox usage)",
			/* nickname = "autocompleteRequests", */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.REQUEST_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.REQUEST_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_READ + "')")
	@Operation(
			summary = "Request detail",
			/* nickname = "getRequest", */
			/* response = IdmRequestDto.class, */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.REQUEST_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.REQUEST_UPDATE + "')")
	@Operation(
			summary = "Create / update request",
			/* nickname = "postRequest", */
			/* response = IdmRequestDto.class, */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {
 
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.REQUEST_CREATE,
							CoreGroupPermission.REQUEST_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.REQUEST_CREATE,
							CoreGroupPermission.REQUEST_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmRequestDto request) {
		if (getService().isNew(request)) { 
			request.setResult(new OperationResultDto(OperationState.CREATED));
			request.setState(RequestState.CONCEPT);
		}
		return super.post(request);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_UPDATE + "')")
	@Operation(
			summary = "Update request",
			/* nickname = "putRequest", */
			/* response = IdmRequestDto.class, */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@RequestBody @NotNull IdmRequestDto dto) {
		return super.put(backendId, dto);
	}

	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_DELETE + "')")
	@Operation(
			summary = "Delete request",
			/* nickname = "deleteRequest", */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.REQUEST_DELETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.REQUEST_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmRequestService service = ((IdmRequestService)this.getService());
		IdmRequestDto dto = service.get(backendId);
		//
		checkAccess(dto, IdmBasePermission.DELETE);
		//
		// Request in Executed state can not be delete or change
		if(RequestState.EXECUTED == dto.getState()){
			throw new ResultCodeException(CoreResultCode.REQUEST_EXECUTED_CANNOT_DELETE,
					ImmutableMap.of("request", dto));
		}
		
		// Only request in Concept state, can be deleted. In others states, will be request set to Canceled state and save.
		if(RequestState.CONCEPT == dto.getState()){
			service.delete(dto);
		}else {
			requestManager.cancel(dto);
		}
		
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			/* nickname = "getPermissionsOnRequest", */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.REQUEST_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.REQUEST_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_UPDATE + "')")
	@Operation(
			summary = "Start request",
			/* nickname = "startRequest", */
			/* response = IdmRequestDto.class, */
			tags = { IdmRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> startRequest(
			@Parameter(name = "Request's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {

		UUID requestId = UUID.fromString(backendId);
		IdmRequestDto request = this.getService().get(requestId, IdmBasePermission.EXECUTE);
	
		// Validate request
		List<IdmRequestItemDto> items = requestManager.findRequestItems(request.getId(), null);
		if (items.isEmpty()) {
			throw new ResultCodeException(CoreResultCode.REQUEST_CANNOT_BE_EXECUTED_NONE_ITEMS,
					ImmutableMap.of("request", request.toString()));
		}
		requestManager.startRequest(requestId, true);

		return this.get(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/entity/{entityId}/changes", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.REQUEST_READ + "')")
	@Operation(
			summary = "Request changes of entity",
			/* nickname = "getRequestEntityChange", */
			/* response = IdmRequestItemDto.class, */
			tags = { IdmRequestItemController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.REQUEST_READ })
        }
    )
	public ResponseEntity<?> getChanges(
			@Parameter(name = "Item's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@Parameter(name = "Entity's uuid identifier.", required = true)
			@PathVariable @NotNull String entityId) {
		
		IdmRequestDto dto = this.getDto(backendId);
		// Find item by entity ID and request ID
		IdmRequestItemFilter itemFilter = new IdmRequestItemFilter();
		itemFilter.setRequestId(dto.getId());
		itemFilter.setOwnerId(UUID.fromString(entityId));
		List<IdmRequestItemDto> items = requestItemService.find(itemFilter, null, IdmBasePermission.READ).getContent();
		if (items.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		IdmRequestItemChangesDto result = requestManager.getChanges(items.get(0));
		if (result == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	

	@Override
	protected IdmRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmRequestFilter filter = new IdmRequestFilter(parameters);
		filter.setOwnerType(getParameterConverter().toString(parameters, "ownerType"));
		filter.setOwnerId(getParameterConverter().toUuid(parameters, "ownerId"));
		filter.setStates(getParameterConverter().toEnums(parameters, "states", RequestState.class));
		filter.setCreatedAfter(getParameterConverter().toDateTime(parameters, "from"));
		filter.setCreatedBefore(getParameterConverter().toDateTime(parameters, "till"));
		return filter;
	}
	
}
