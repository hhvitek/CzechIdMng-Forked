package eu.bcvsolutions.idm.core.rest.impl;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestByIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.bulk.action.impl.rolerequest.RoleRequestDeleteBulkAction;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleRequestApprovalProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Role request endpoint
 * 
 * Returns request doesn't contains concepts (from version 9.7.0!)
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-requests")
@Tag(name = IdmRoleRequestController.TAG, description = "Operations with role requests")
public class IdmRoleRequestController extends AbstractReadWriteDtoController<IdmRoleRequestDto, IdmRoleRequestFilter> {

	protected static final String TAG = "Role Request - requests";
	//
	@Autowired private IdmConceptRoleRequestController conceptRoleRequestController;
	@Autowired private IdmRoleRequestService service;

	@Autowired
	public IdmRoleRequestController(IdmRoleRequestService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Search role requests (/search/quick alias). Returns requests doesn't contains concepts (from version 9.7.0!).", operationId = "searchRoleRequests", tags = {
			IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Search role requests. Returns requests doesn't contains concepts (from version 9.7.0!).", operationId = "searchQuickRoleRequests", tags = {
			IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countRoleRequests"
    )
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Role request detail. Returns request doesn't contains concepts (from version 9.7.0!).", operationId = "getRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Role request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {

		// 
		IdmRoleRequestFilter filter = new IdmRoleRequestFilter();
		filter.setIncludeApprovers(true);

		IdmRoleRequestDto requestDto = getService().get(backendId, filter, IdmBasePermission.READ);
		if (requestDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}

		RepresentationModel resource = toModel(requestDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Create / update role request. Returns request doesn't contains concepts (from version 9.7.0!).", operationId = "postRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmRoleRequestController.TAG } )
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmRoleRequestDto dto) {
		if (RoleRequestedByType.AUTOMATICALLY == dto.getRequestedByType()) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_AUTOMATICALLY_NOT_ALLOWED,
					ImmutableMap.of("new", dto));
		}
		ResponseEntity<?> response = super.post(dto);
		return response;
	}


	@RequestMapping(value = "/{backendId}/copy-roles", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(
			summary = "Create concepts by identity. Returns request doesn't contains concepts (from version 9.7.0!).", 
			operationId = "Copy roles",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleRequestDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE})
        }
    )
	public ResponseEntity<?> copyRoles( @Parameter(description = "Role request's uuid identifier.", required = true)
	@PathVariable @NotNull String backendId, @RequestBody @NotNull IdmRoleRequestByIdentityDto dto) {
		dto.setRoleRequest(UUID.fromString(backendId));
		IdmRoleRequestDto roleRequest = this.service.copyRolesByIdentity(dto);

		return new ResponseEntity<Object>(roleRequest, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/copy-roles", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(
			summary = "Create concepts by identity", 
			operationId = "Copy roles",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleRequestDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE})
        }
    )
	public ResponseEntity<?> copyRolesWithoutRequest(@RequestBody @NotNull IdmRoleRequestByIdentityDto dto) {
		IdmRoleRequestDto roleRequest = this.service.copyRolesByIdentity(dto);

		return new ResponseEntity<Object>(roleRequest, HttpStatus.OK);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Update role request. Returns request doesn't contains concepts (from version 9.7.0!).", operationId = "putRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Role request's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmRoleRequestDto dto) {
		ResponseEntity<?> response =  super.put(backendId, dto);
		return response;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_DELETE + "')")
	@Operation(summary = "Delete role request", operationId = "deleteRoleRequest", tags = {
			IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Role request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		IdmRoleRequestDto dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}

		checkAccess(dto, IdmBasePermission.DELETE);
		IdmBulkActionDto deleteAction = getAvailableBulkActions()
				.stream()
				.filter(action -> {return RoleRequestDeleteBulkAction.NAME.equals(action.getName());})
				.findFirst()
				.get();
		deleteAction.setIdentifiers(Set.of(dto.getId()));
		bulkAction(deleteAction);
		
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "What logged identity can do with given record", operationId = "getPermissionsOnRoleRequest", tags = {
			IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Start role request. Returns request doesn't contains concepts (from version 9.7.0!).", operationId = "startRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_UPDATE })
    })
	public ResponseEntity<?> startRequest(
			 @Parameter(description = "Role request's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		IdmRoleRequestDto requestDto = service.get(backendId, new IdmRoleRequestFilter(true), IdmBasePermission.READ);
		// Validate
		service.validate(requestDto);
		// Start request
		Map<String, Serializable> variables = new HashMap<>();
		variables.put(RoleRequestApprovalProcessor.CHECK_RIGHT_PROPERTY, Boolean.TRUE);
		RoleRequestEvent event = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, requestDto, variables);
		event.setPriority(PriorityType.HIGH);
		//
		requestDto = service.startRequest(event);
		if(!requestDto.getState().isTerminatedState()) {
			throw new AcceptedException();
		}
		RepresentationModel resource = toModel(requestDto);
		ResponseEntity<RepresentationModel> response = new ResponseEntity<>(resource, HttpStatus.OK);
		
		return response;
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/concepts", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Role request concepts", operationId = "getRoleRequestConcepts", tags = {
			IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
    @Parameters({
         @Parameter(name = "parameters", schema = @Schema( implementation=String.class, type = "query"), description = "Search criteria parameters. Parameters could be registered by module. Example id=25c5b9e8-b15d-4f95-b715-c7edf6f4aee6"),
    })
	@PageableAsQueryParam
	public CollectionModel<?> getConcepts(
			 @Parameter(description = "Role request's uuid identifier.", required = true) @PathVariable String backendId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		IdmRoleRequestDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmConceptRoleRequestFilter filter = conceptRoleRequestController.toFilter(parameters);
		filter.setRoleRequestId(entity.getId());
		//
		return toCollectionModel(conceptRoleRequestController.find(filter, pageable, IdmBasePermission.READ),
				IdmRoleRequestDto.class);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/incompatible-roles", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(
			summary = "Incompatible roles related to this request", 
			operationId = "getRoleRequestIncompatibleRoles",
			tags = { IdmRoleRequestController.TAG }, 
						description = "Incompatible roles are resolved from currently assigned identity roles (which can logged used read) and the current request concepts.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	public CollectionModel<?> getIncompatibleRoles(
			 @Parameter(description = "Role request's uuid identifier.", required = true)
			@PathVariable String backendId) {	
		IdmRoleRequestDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = service.getIncompatibleRoles(entity, IdmBasePermission.READ);
		//
		return toCollectionModel(incompatibleRoles, ResolvedIncompatibleRoleDto.class);
	}
	
	/**
	 * Get available bulk actions for role request
	 *
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			operationId = "availableBulkAction",
			tags = { IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	/**
	 * Process bulk action for identities
	 *
	 * @param bulkAction
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(
			summary = "Process bulk action for role request", 
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
			tags = { IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLE_REQUEST_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLE_REQUEST_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	/**
	 * Prevalidate bulk action for role requests
	 *
	 * @param bulkAction
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for role request", 
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
			tags = { IdmRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLE_REQUEST_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLE_REQUEST_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}


	@Override
	protected IdmRoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmRoleRequestFilter filter = new IdmRoleRequestFilter(parameters, getParameterConverter());
		//
		filter.setApplicant(getParameterConverter().toString(parameters, "applicant"));
		filter.setApplicantId(getParameterConverter().toUuid(parameters, "applicantId"));
		//
		if (filter.getApplicant() != null) {
			try {
				// Applicant can be UUID (Username vs UUID identification schizma)
				// TODO: replace with parameterConverter#toEntityUuid ... 
				filter.setApplicantId(UUID.fromString(filter.getApplicant()));
				filter.setApplicant(null);
			} catch (IllegalArgumentException ex) {
				// Ok applicant is not UUID
			}
		}
		filter.setApplicantType(getParameterConverter().toString(parameters, "applicantType"));
		// TODO: remove redundant state field
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		filter.setStates(getParameterConverter().toEnums(parameters, "states", RoleRequestState.class));
		filter.setSystemStates(getParameterConverter().toEnums(parameters, "systemStates", OperationState.class));
		filter.setApplicants(getParameterConverter().toUuids(parameters, "applicants"));
		filter.setCreatorId(getParameterConverter().toEntityUuid(parameters, IdmRoleRequestFilter.PARAMETER_CREATOR_ID, IdmIdentityDto.class));
		filter.setExecuted(getParameterConverter().toBoolean(parameters, "executed"));
		filter.setIncludeApprovers(getParameterConverter().toBoolean(parameters, "includeApprovers", false));
		return filter;
	}

}
