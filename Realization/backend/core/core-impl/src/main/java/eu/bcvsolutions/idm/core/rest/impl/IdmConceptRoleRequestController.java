package eu.bcvsolutions.idm.core.rest.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Concept role request endpoint search all entities are available for identity
 * with ROLE_REQUEST_ADMIN authority. Otherwise roleRequestId in filter has to
 * be filled. CUD methods are secured by role request
 * ({@link IdmConceptRoleRequestService#checkAccess(IdmConceptRoleRequestDto, eu.bcvsolutions.idm.core.security.api.domain.BasePermission...)})
 * 
 * @author svandav
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/concept-role-requests")
@Tag(name = IdmConceptRoleRequestController.TAG, description = "Operations with single roles in request")
public class IdmConceptRoleRequestController
		extends AbstractReadWriteDtoController<IdmConceptRoleRequestDto, IdmConceptRoleRequestFilter> {

	protected static final String TAG = "Role Request - concepts";
	private final SecurityService securityService;
	private final IdmRoleRequestService roleRequestService;
	private final IdmConceptRoleRequestService service;
	@Autowired
	private IdmFormDefinitionController formDefinitionController;

	@Autowired
	public IdmConceptRoleRequestController(IdmConceptRoleRequestService service, SecurityService securityService,
			IdmRoleRequestService roleRequestService) {
		super(service);
		//
		Assert.notNull(securityService, "Service is required.");
		Assert.notNull(roleRequestService, "Service is required.");
		Assert.notNull(service, "Service is required.");
		//
		this.securityService = securityService;
		this.roleRequestService = roleRequestService;
		this.service = service;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Search concept role requests (/search/quick alias)", operationId = "searchConceptRoleRequests", tags = {
			IdmConceptRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_READ,
							CoreGroupPermission.ROLE_REQUEST_ADMIN }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_REQUEST_READ,
							CoreGroupPermission.ROLE_REQUEST_ADMIN })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Search concept role requests", operationId = "searchQuickConceptRoleRequests", tags = {
			IdmConceptRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_READ,
							CoreGroupPermission.ROLE_REQUEST_ADMIN }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_REQUEST_READ,
							CoreGroupPermission.ROLE_REQUEST_ADMIN })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	public Page<IdmConceptRoleRequestDto> find(IdmConceptRoleRequestFilter filter, Pageable pageable,
			BasePermission permission) {
		// check access
		if (!securityService.hasAnyAuthority(CoreGroupPermission.ROLE_REQUEST_ADMIN)) {
			if (filter == null || filter.getRoleRequestId() == null) {
				throw new ForbiddenEntityException((Serializable)null, CoreGroupPermission.ROLEREQUEST, IdmBasePermission.ADMIN);
			}
			IdmRoleRequestDto roleRequest = roleRequestService.get(filter.getRoleRequestId(), permission);
			if (roleRequest == null) {
				// return empty result (find method doesn't throw 404)
				return new PageImpl<>(new ArrayList<>());
			}
		}
		Page<IdmConceptRoleRequestDto> results = super.find(filter, pageable, permission);
		return results;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Concept detail", operationId = "getConceptRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmConceptRoleRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmConceptRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	public IdmConceptRoleRequestDto getDto(Serializable backendId) {
		IdmConceptRoleRequestDto concept = super.getDto(backendId);
		if (concept == null) {
			return null;
		}
		IdmFormInstanceDto formInstanceDto = service.getRoleAttributeValues(concept, true);
		if (formInstanceDto != null) {
			concept.getEavs().clear();
			concept.getEavs().add(formInstanceDto);
		}
		return concept;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Create / update concept", operationId = "postConceptRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmConceptRoleRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmConceptRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmConceptRoleRequestDto dto) {
		// Check if exist same concept for same role and operation type. If yes, we delete it before save new.
		if (dto != null //
				&& dto.getRoleRequest() != null //
				&& dto.getIdentityRole() != null //
				&& dto.getOperation() != null //
				&& ConceptRoleRequestOperation.ADD != dto.getOperation() //
				&& dto.getIdentityContract() != null) {
			IdmConceptRoleRequestFilter filter = new IdmConceptRoleRequestFilter();
			filter.setIdentityRoleId(dto.getIdentityRole());
			filter.setRoleRequestId(dto.getRoleRequest());
			filter.setOperation(dto.getOperation());
			filter.setIdentityContractId(dto.getIdentityContract());
			List<IdmConceptRoleRequestDto> duplicates = getService().find(filter, PageRequest.of(0, 1)).getContent();
			duplicates.forEach(duplicate -> {
				// Delete duplicated concept first
				getService().delete(duplicate);
			});
		}

		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Update concept", operationId = "putConceptRoleRequest",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmConceptRoleRequestDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmConceptRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmConceptRoleRequestDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_DELETE + "')")
	@Operation(summary = "Delete concept", operationId = "delete ConceptRoleRequest", tags = {
			IdmConceptRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "What logged identity can do with given record", operationId = "getPermissionsOnConceptRoleRequest", tags = {
			IdmConceptRoleRequestController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Concept extended attributes form definitions", operationId = "getIdentityRoleFormDefinitions", tags = {
			IdmIdentityRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	public ResponseEntity<?> getFormDefinitions(
			 @Parameter(description = "Role's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId) {
		IdmConceptRoleRequestDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		// Search definition by definition in role
		IdmRoleDto roleDto = DtoUtils.getEmbedded(dto, IdmConceptRoleRequest_.role, IdmRoleDto.class);
		if (roleDto != null && roleDto.getIdentityRoleAttributeDefinition() != null) {
			return formDefinitionController.getDefinitions(roleDto.getIdentityRoleAttributeDefinition());
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Concept form definition - read values", operationId = "getRoleFormValues", tags = {
			IdmIdentityRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_READ })
    })
	public EntityModel<?> getFormValues(
			 @Parameter(description = "Concept's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId,
			 @Parameter(description = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode) {
		IdmConceptRoleRequestDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmConceptRoleRequest.class,
				definitionCode);
		//
		return formDefinitionController.getFormValues(dto, formDefinition);
	}

	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH })
	@Operation(summary = "Concept form definition - save values", operationId = "postIdentityRoleFormValues", tags = {
			IdmIdentityRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_REQUEST_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_REQUEST_UPDATE })
    })
	public EntityModel<?> saveFormValues(
			 @Parameter(description = "Concept's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId,
			 @Parameter(description = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {
		IdmConceptRoleRequestDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(dto, IdmBasePermission.UPDATE);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmConceptRoleRequest.class,
				definitionCode);
		//
		return formDefinitionController.saveFormValues(dto, formDefinition, formValues);
	}

	@Override
	protected IdmConceptRoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmConceptRoleRequestFilter filter = new IdmConceptRoleRequestFilter(parameters);
		
		filter.setRoleRequestId(getParameterConverter().toUuid(parameters, "roleRequestId"));
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		return filter;
	}
}
