package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFormAttributeFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleFormAttributeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.AbstractRequestDtoController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Request controller for relation between role and definition of form-attribution. Is elementary part
 * of role form "sub-definition".
 * 
 * @author Vít Švanda
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/requests")
@Tag(
		name = IdmRoleFormAttributeController.TAG, 
		description = "Operations with role form attributes"//, 
		 
		
		

)
public class IdmRequestRoleFormAttributeController extends AbstractRequestDtoController<IdmRoleFormAttributeDto, IdmRoleFormAttributeFilter> {
	
	protected static final String TAG = "Role attributes";
	protected static final String REQUEST_SUB_PATH = "/role-form-attributes";
	
	@Autowired
	public IdmRequestRoleFormAttributeController(IdmRoleFormAttributeService service) {
		super(service);
	}
	
	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +   "/{requestId}"+ REQUEST_SUB_PATH , method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_READ + "')")
	@Operation(
			summary = "Search role attributes (/search/quick alias)",
			/* nickname = "searchRoleFormAttributes", */
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +   "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_READ + "')")
	@Operation(
			summary = "Search role attributes",
			/* nickname = "searchQuickRoleFormAttributes", */
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +   "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete role attributes (selectbox usage)",
			/* nickname = "autocompleteRoleFormAttributes", */
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_AUTOCOMPLETE })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.autocomplete(requestId, parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +   "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			/* nickname = "countRoleFormAttributes", */
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_COUNT })
        }
    )
	public long count(@PathVariable @NotNull String requestId, @RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(requestId, parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +   "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_READ + "')")
	@Operation(
			summary = "Role attribute detail",
			/* nickname = "getRoleFormAttribute", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleFormAttributeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_READ })
        }
    )
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_UPDATE + "')")
	@Operation(
			summary = "Create / update role attribute",
			/* nickname = "postRoleFormAttribute", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleFormAttributeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_CREATE,
						CoreGroupPermission.ROLEFORMATTRIBUTE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_CREATE,
						CoreGroupPermission.ROLEFORMATTRIBUTE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@PathVariable @NotNull String requestId, @Valid @RequestBody IdmRoleFormAttributeDto dto) {
		return super.post(requestId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +   "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_UPDATE + "')")
	@Operation(
			summary = "Update role attribute",
			/* nickname = "putRoleFormAttribute", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleFormAttributeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmRoleFormAttributeDto dto) {
		return super.put(requestId, backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +   "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_DELETE + "')")
	@Operation(
			summary = "Delete role attribute",
			/* nickname = "deleteRoleFormAttribute", */
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +   "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEFORMATTRIBUTE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			/* nickname = "getPermissionsOnRoleFormAttribute", */
			tags = { IdmRoleFormAttributeController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEFORMATTRIBUTE_READ })
        }
    )
	public Set<String> getPermissions(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(requestId, backendId);
	}
}
