package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
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

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccPasswordChangeOptionDto;
import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccUniformPasswordFilter;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
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
 * Rest controller for standard CRUD operation for uniform password definitions,
 * but also for change a valid method.
 * 
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/uniform-passwords")
@Tag(name = AccUniformPasswordController.TAG, description = "Uniform password definitions and method for check a valid")
public class AccUniformPasswordController extends AbstractReadWriteDtoController<AccUniformPasswordDto, AccUniformPasswordFilter> {

	@Autowired
	private AccUniformPasswordService uniformPasswordService;

	protected static final String TAG = "Uniform password definitions";
	
	@Autowired
	public AccUniformPasswordController(AccUniformPasswordService uniformPasswordService) {
		super(uniformPasswordService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@Operation(
        summary = "Search definition for uniform password (/search/quick alias)",
        operationId = "searchUniformPasswords"
    )
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.UNIFORM_PASSWORD_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.UNIFORM_PASSWORD_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search definition for uniform password", 
			operationId = "searchQuickUniformPasswords",
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.UNIFORM_PASSWORD_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.UNIFORM_PASSWORD_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete uniform passwords (selectbox usage)", 
			operationId = "autocompleteUniformPasswords",
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.UNIFORM_PASSWORD_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Uniform password detail", 
			operationId = "getUniformPassword",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccUniformPasswordDto.class
                                    )
                            )
                    }
            ), 
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.UNIFORM_PASSWORD_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.UNIFORM_PASSWORD_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update uniform password definition", 
			operationId = "postUniformPassword",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccUniformPasswordDto.class
                                    )
                            )
                    }
            ), 
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.UNIFORM_PASSWORD_CREATE,
						AccGroupPermission.UNIFORM_PASSWORD_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.UNIFORM_PASSWORD_CREATE,
						AccGroupPermission.UNIFORM_PASSWORD_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull AccUniformPasswordDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update uniform password definition",
			operationId = "putUniformPassword",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccUniformPasswordDto.class
                                    )
                            )
                    }
            ), 
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.UNIFORM_PASSWORD_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.UNIFORM_PASSWORD_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccUniformPasswordDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_UPDATE + "')")
	@Operation(
			summary = "Update uniform password definition", 
			operationId = "patchUnifromPassword",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccUniformPasswordDto.class
                                    )
                            )
                    }
            ), 
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.UNIFORM_PASSWORD_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.UNIFORM_PASSWORD_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete uniform password definition", 
			operationId = "deleteUniformPassword",
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.UNIFORM_PASSWORD_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.UNIFORM_PASSWORD_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.UNIFORM_PASSWORD_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			operationId = "getPermissionsOnUniformPassword",
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.UNIFORM_PASSWORD_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.UNIFORM_PASSWORD_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Uniform password's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Find and compose password change options. Options will be united by {@link AccUniformPasswordDto} definition.
	 * Endpoint has permission from account (ACCOUNT_READ) - backward compatibility and returned IDs is account id.
	 *
	 * @param identityIdentifier
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@RequestMapping(value= "/search/password-change-options/{identityIdentifier}", method = RequestMethod.GET)
	@Operation(
			summary = "Search available password change options for given identity. For call this method must be permissions for read account!", 
			operationId = "searchPasswordChangeOptions",
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_READ })
    })
	public CollectionModel<?> findPasswordChangeOptions(@PathVariable @NotNull String identityIdentifier) {

		BaseDto identityDto = getLookupService().lookupDto(IdmIdentityDto.class, identityIdentifier);
		
		return toCollectionModel(uniformPasswordService.findOptionsForPasswordChange((IdmIdentityDto)identityDto, IdmBasePermission.READ), AccPasswordChangeOptionDto.class);
	}

	@Override
	protected AccUniformPasswordFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccUniformPasswordFilter filter = new AccUniformPasswordFilter(parameters, getParameterConverter());
		return filter;
	}
}
