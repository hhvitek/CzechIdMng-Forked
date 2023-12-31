package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Role catalogue controller - by role
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-catalogue-roles")
@Tag(name = IdmRoleCatalogueRoleController.TAG, description = "Operations with role catalogues by role")
public class IdmRoleCatalogueRoleController extends AbstractReadWriteDtoController<IdmRoleCatalogueRoleDto, IdmRoleCatalogueRoleFilter> {
	
	protected static final String TAG = "Role catalogues - role relations";
	
	@Autowired
	public IdmRoleCatalogueRoleController(IdmRoleCatalogueRoleService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_READ + "')")
	@Operation(
			summary = "Search role catalogue roles (/search/quick alias)", 
			operationId = "searchRoleCatalogueRoles",
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_READ + "')")
	@Operation(
			summary = "Search role catalogue roles", 
			operationId = "searchQuickRoleCatalogueRoles",
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete role catalogue roles (selectbox usage)", 
			operationId = "autocompleteRoleCatalogueRoles",
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			operationId = "countRoleCatalogueRoles",
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_READ + "')")
	@Operation(
			summary = "Role catalogue role detail", 
			operationId = "getRoleCatalogueRole",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleCatalogueRoleDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_UPDATE + "')")
	@Operation(
			summary = "Create / update role catalogue role", 
			operationId = "postRoleCatalogueRole",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleCatalogueRoleDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUEROLE_CREATE,
						CoreGroupPermission.ROLECATALOGUEROLE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUEROLE_CREATE,
						CoreGroupPermission.ROLECATALOGUEROLE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmRoleCatalogueRoleDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_UPDATE + "')")
	@Operation(
			summary = "Update role catalogue role", 
			operationId = "putRoleCatalogueRole",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleCatalogueRoleDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmRoleCatalogueRoleDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_UPDATE + "')")
	@Operation(
			summary = "Update role catalogue role", 
			operationId = "patchRoleCatalogueRole",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleCatalogueRoleDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_DELETE + "')")
	@Operation(
			summary = "Delete role catalogue role", 
			operationId = "deleteRoleCatalogueRole",
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUEROLE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			operationId = "getPermissionsOnRoleCatalogueRole",
			tags = { IdmRoleCatalogueRoleController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLECATALOGUEROLE_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
