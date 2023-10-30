package eu.bcvsolutions.idm.vs.rest.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
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
import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import eu.bcvsolutions.idm.vs.dto.VsConnectorObjectDto;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsRequestFilter;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;
import eu.bcvsolutions.idm.vs.service.api.VsSystemImplementerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest methods for virtual system request
 *
 * @author Svanda
 * @author Ondrej Husnik
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/vs/requests")
@Tag(name = VsRequestController.TAG, description = "Operations with requests (in virtual system)"//, 

)
public class VsRequestController extends AbstractReadWriteDtoController<VsRequestDto, VsRequestFilter> {

	protected static final String TAG = "Requests";

	@Autowired
	private VsSystemImplementerService requestImplementerService;
	@Autowired
	private IdmRoleRequestService rolerequestService;
	@Autowired
	private IdmIdentityService identityService;

	@Autowired
	public VsRequestController(VsRequestService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@Operation(summary = "Search requests (/search/quick alias)", /* nickname = "searchRequests", */ tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@Operation(summary = "Search requests", /* nickname = "searchQuickRequests", */ tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE + "')")
	@Operation(summary = "Autocomplete requests (selectbox usage)", /* nickname = "autocompleteRequests", */ tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter"
			/* nickname = "countRequests", */
			
			)
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_REQUEST_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_REQUEST_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@Operation(summary = "Request detail", /* nickname = "getRequest", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = VsRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		VsRequestDto request = this.getDto(backendId);
		if (request == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}

		UUID roleRequestId = request.getRoleRequestId();
		if (roleRequestId != null) {
			IdmRoleRequestDto roleRequestDto = rolerequestService.get(roleRequestId);
			if (roleRequestDto != null) {

				UUID roleRequestCreatorId = roleRequestDto.getCreatorId();
				if (roleRequestCreatorId != null) {
					IdmIdentityDto roleRequestCreator = identityService.get(roleRequestCreatorId);
					roleRequestDto.getEmbedded().put(Auditable.PROPERTY_CREATOR, roleRequestCreator);
				}

				request.getEmbedded().put(IdmConceptRoleRequestService.ROLE_REQUEST_FIELD, roleRequestDto);
			}
		}

		RepresentationModel resource = toModel(request);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/realize", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_UPDATE + "')")
	@Operation(summary = "Realize request", /* nickname = "realizeRequest", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = VsRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> realize(
			 @Parameter(description = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			 @Parameter(description = "Reason in request DTO. Reason is optional.", required = false) @RequestBody(required = false) VsRequestDto reason){
		VsRequestDto request = ((VsRequestService) getService()).realize(getService().get(backendId), reason == null ? null : reason.getReason());
		return new ResponseEntity<>(request, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/cancel", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_UPDATE + "')")
	@Operation(summary = "Cancel request", /* nickname = "cancelRequest", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = VsRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> cancel(
			 @Parameter(description = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			 @Parameter(description = "Reason in request DTO. Reason must be filled!", required = true) @RequestBody(required = true) VsRequestDto reason) {
		VsRequestDto request = ((VsRequestService) getService()).cancel(getService().get(backendId),
				reason.getReason());
		return new ResponseEntity<>(request, HttpStatus.OK);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_DELETE + "')")
	@Operation(summary = "Delete request", /* nickname = "deleteRequest", */ tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_DELETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')" + " or hasAuthority('"
			+ VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE + "')")
	@Operation(summary = "What logged request can do with given record", /* nickname = "getPermissionsOnRequest", */ tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ,
							VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ,
							VirtualSystemGroupPermission.VS_REQUEST_AUTOCOMPLETE })
        }
    )

	public Set<String> getPermissions(
			 @Parameter(description = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/connector-object", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@Operation(summary = "Read connector object", /* nickname = "getConnectorObject", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IcConnectorObject.class
                                    )
                            )
                    }
            ), tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ })
        }
    )
	public ResponseEntity<?> getConnectorObject(
			 @Parameter(description = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		IcConnectorObject connectorObject = ((VsRequestService) getService())
				.getVsConnectorObject(getService().get(backendId));
		if (connectorObject != null) {
			return new ResponseEntity<>(connectorObject, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new IcConnectorObjectImpl(), HttpStatus.OK);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/wish-connector-object", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@Operation(summary = "Read wish connector object. Object contains current attributes from virtual system + changed attributes from given request.", /* nickname = "getVsConnectorObject", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = VsConnectorObjectDto.class
                                    )
                            )
                    }
            ), tags = {
			VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ })
        }
    )
	public ResponseEntity<?> getWishConnectorObject(
			 @Parameter(description = "Request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		VsConnectorObjectDto connectorObject = ((VsRequestService) getService())
				.getWishConnectorObject(getService().get(backendId));
		if (connectorObject != null) {
			return new ResponseEntity<>(connectorObject, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(new VsConnectorObjectDto(), HttpStatus.OK);
		}
	}


	/**
	 * Get available bulk actions for request
	 *
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			/* nickname = "availableBulkAction", */
			tags = { VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}

	/**
	 * Process bulk action for requests
	 *
	 * @param bulkAction
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@Operation(
			summary = "Process bulk action for request",
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
			tags = { VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}

	/**
	 * Prevalidate bulk action for requests
	 *
	 * @param bulkAction
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_REQUEST_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for identities",
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
			tags = { VsRequestController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							VirtualSystemGroupPermission.VS_REQUEST_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}


	@Override
	public VsRequestDto getDto(Serializable backendId) {
		VsRequestDto requestDto = super.getDto(backendId);

		// Request was not found
		if (requestDto == null) {
			return null;
		}

		// Add list of implementers
		addImplementers(requestDto);

		return requestDto;
	}

	@Override
	public Page<VsRequestDto> find(VsRequestFilter filter, Pageable pageable, BasePermission permission) {
		Page<VsRequestDto> page = super.find(filter, pageable, permission);
		page.forEach(request -> addImplementers(request));

		return page;
	}

	/**
	 * Load and add implementers for that system to the request
	 *
	 * @param requestDto
	 */
	private void addImplementers(VsRequestDto requestDto) {
		List<IdmIdentityDto> implementers = this.requestImplementerService
				.findRequestImplementers(requestDto.getSystem(), 10);
		requestDto.setImplementers(implementers);
	}

	@Override
	protected VsRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		VsRequestFilter filter = new VsRequestFilter(parameters, getParameterConverter());
		filter.setState(getParameterConverter().toEnum(parameters, "state", VsRequestState.class));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setUid(getParameterConverter().toString(parameters, "uid"));
		filter.setCreatedAfter(getParameterConverter().toDateTime(parameters, "createdAfter"));
		filter.setCreatedBefore(getParameterConverter().toDateTime(parameters, "createdBefore"));
		filter.setModifiedAfter(getParameterConverter().toDateTime(parameters, "modifiedAfter"));
		filter.setModifiedBefore(getParameterConverter().toDateTime(parameters, "modifiedBefore"));
		filter.setOnlyArchived(getParameterConverter().toBoolean(parameters, "onlyArchived"));
		filter.setImplementers(getParameterConverter().toUuids(parameters, "implementers"));
		// Context property for load and set owner to the request dto.
		filter.setIncludeOwner(true);

		return filter;
	}
}
