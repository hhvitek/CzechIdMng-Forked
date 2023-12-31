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
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.AbstractRequestDtoController;
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
 * Request - Role system attribute rest
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/requests")
@Tag(name = SysRequestRoleSystemAttributeController.TAG, description = "Requests for - Override system mapping attribute by role")
public class SysRequestRoleSystemAttributeController
		extends AbstractRequestDtoController<SysRoleSystemAttributeDto, SysRoleSystemAttributeFilter> {

	protected static final String TAG = "Request - role system - attributes";	
	protected static final String REQUEST_SUB_PATH = "/role-system-attributes";

	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Autowired
	public SysRequestRoleSystemAttributeController(SysRoleSystemAttributeService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH, method = RequestMethod.GET)
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
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH + "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search role system attributes", 
			operationId = "searchQuickRoleSystemAttributes",
			tags = { SysRequestRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.GET)
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
			tags = { SysRequestRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_READ })
    })
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH, method = RequestMethod.POST)
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
			tags = { SysRequestRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLE_UPDATE})
        }
    )
	public ResponseEntity<?> post(
			@PathVariable @NotNull String requestId,
			@RequestBody @NotNull SysRoleSystemAttributeDto dto) {
		return super.post(requestId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.PUT)
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
			tags = { SysRequestRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_UPDATE })
    })
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysRoleSystemAttributeDto dto) {
		return super.put(requestId, backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_DELETE + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete role system attribute", 
			operationId = "deleteRoleSystemAttribute", 
			tags = { SysRequestRoleSystemAttributeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_DELETE })
    })
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role system attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
}
