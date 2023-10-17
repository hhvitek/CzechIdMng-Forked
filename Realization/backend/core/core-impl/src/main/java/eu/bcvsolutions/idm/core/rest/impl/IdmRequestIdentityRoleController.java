package eu.bcvsolutions.idm.core.rest.impl;

import java.util.UUID;

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
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRequestIdentityRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
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
 * Controller for show and processing wish on assigned identity roles.
 * 
 * @author Vít Švanda
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/request-identity-roles")
@Tag(name = IdmRequestIdentityRoleController.TAG, description = "Operations with single roles in request"
//, //produces = BaseController.APPLICATION_HAL_JSON_VALUE
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmRequestIdentityRoleController
		extends AbstractReadWriteDtoController<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter> {

	protected static final String TAG = "Role Request identity-roles";
	@Autowired
	private final IdmRequestIdentityRoleService service;

	@Autowired
	public IdmRequestIdentityRoleController(IdmRequestIdentityRoleService service) {
		super(service);
		Assert.notNull(service, "Service is required.");
		this.service = service;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Search concept role requests (/search/quick alias)", /* nickname = "searchConceptRoleRequests", */ tags = {
			IdmRequestIdentityRoleController.TAG })
    @SecurityRequirements(
        value = {

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
	@Operation(summary = "Search concept role requests", /* nickname = "searchQuickConceptRoleRequests", */ tags = {
			IdmRequestIdentityRoleController.TAG })
    @SecurityRequirements(
        value = {

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
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(summary = "Concept detail", /* nickname = "getConceptRoleRequest", */
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
			IdmRequestIdentityRoleController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_REQUEST_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	public Page<IdmRequestIdentityRoleDto> find(IdmRequestIdentityRoleFilter filter, Pageable pageable,
			BasePermission permission) {
		// We need to load EAV attributes
		filter.setIncludeEav(true);
		
		Page<IdmRequestIdentityRoleDto> results = super.find(filter, pageable, permission);
		return results;
	}
	

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Create / update request-identity-role", /* nickname = "postRequest-identity-role", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRequestIdentityRoleDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmRequestIdentityRoleController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_REQUEST_CREATE,
							CoreGroupPermission.ROLE_REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmRequestIdentityRoleDto dto) {
	
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@Operation(summary = "Update request-identity-role", /* nickname = "putRequest-identity-role", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRequestIdentityRoleDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmRequestIdentityRoleController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_REQUEST_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "Request-identity-role to update.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmRequestIdentityRoleDto dto) {
		return super.put(backendId, dto);
	}

	/**
	 * Delete is realized as PUT, because we need to use 
	 * body (is not supported for DELETE)
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/delete", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_DELETE + "')")
	@Operation(summary = "Delete request-identity-role", /* nickname = "delete request-identity-role", */ tags = {
			IdmRequestIdentityRoleController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_REQUEST_DELETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_REQUEST_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "Request-identity-role to delete.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmRequestIdentityRoleDto dto) {
		dto.setId(UUID.fromString(backendId));
		IdmRequestIdentityRoleDto deletedRequestIdentityRole = service.deleteRequestIdentityRole(dto, IdmBasePermission.DELETE);
		return new ResponseEntity<>(toModel(deletedRequestIdentityRole), HttpStatus.OK);
	}

}
