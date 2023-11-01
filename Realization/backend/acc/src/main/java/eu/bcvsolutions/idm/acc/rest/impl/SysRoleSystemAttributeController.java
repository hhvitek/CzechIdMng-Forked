package eu.bcvsolutions.idm.acc.rest.impl;

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

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Role system attribute rest
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/role-system-attributes")
@Tag(name = SysRoleSystemAttributeController.TAG, description = "Override system mapping attribute by role")
public class SysRoleSystemAttributeController
		extends AbstractReadWriteDtoController<SysRoleSystemAttributeDto, SysRoleSystemAttributeFilter> {

	protected static final String TAG = "Role system - attributes";
	
	@Autowired
	public SysRoleSystemAttributeController(SysRoleSystemAttributeService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@Operation(
        summary = "Search role system attributes (/search/quick alias)",
        operationId = "searchRoleSystemAttributes"
    )
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search role system attributes", 
			operationId = "searchQuickRoleSystemAttributes",
			tags = { SysRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Role system attribute detail", 
			operationId = "getRoleSystemAttribute", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysRoleSystemAttributeDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update role system attribute", 
			operationId = "postRoleSystemAttribute", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysRoleSystemAttributeDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull SysRoleSystemAttributeDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update role system attribute",
			operationId = "putRoleSystemAttribute", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysRoleSystemAttributeDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysRoleSystemAttributeDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete role system attribute", 
			operationId = "deleteRoleSystemAttribute", 
			tags = { SysRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
}
