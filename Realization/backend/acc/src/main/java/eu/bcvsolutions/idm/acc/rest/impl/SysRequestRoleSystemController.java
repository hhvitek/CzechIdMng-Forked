package eu.bcvsolutions.idm.acc.rest.impl;

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

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
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
 * Role-system request - Role could assign identity account on target system.
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/requests")
@Tag(name = SysRequestRoleSystemController.TAG, description = "Reqeusts for - Assign system to role")
public class SysRequestRoleSystemController extends AbstractRequestDtoController<SysRoleSystemDto, SysRoleSystemFilter> {
	
	protected static final String TAG = "Role system - mappings";
	protected static final String REQUEST_SUB_PATH = "/role-systems";
	
	@Autowired
	public SysRequestRoleSystemController(SysRoleSystemService roleSysteService) {
		super(roleSysteService);
	}
	
	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH, method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@Operation(
        summary = "Search role systems (/search/quick alias)",
        operationId = "searchRoleSystems"
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
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@RequestMapping(value= "/{requestId}"+REQUEST_SUB_PATH+"/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search role systems",
			operationId = "searchQuickRoleSystems",
			tags = { SysRequestRoleSystemController.TAG })
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
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Role system detail",
			operationId = "getRoleSystem", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysRoleSystemDto.class
                                    )
                            )
                    }
            ),
			tags = { SysRequestRoleSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_READ })
    })
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH, method = RequestMethod.POST)
	@Operation(
			summary = "Create / update role system",
			operationId = "postRoleSystem", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysRoleSystemDto.class
                                    )
                            )
                    }
            ),
			tags = { SysRequestRoleSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@PathVariable @NotNull String requestId, @RequestBody @NotNull SysRoleSystemDto dto) {
		return super.post(requestId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH +"/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update role system",
			operationId = "putRoleSystem", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysRoleSystemDto.class
                                    )
                            )
                    }
            ),
			tags = { SysRequestRoleSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_UPDATE })
    })
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysRoleSystemDto dto) {
		return super.put(requestId, backendId, dto);
	}
	
//	@Override
//	@ResponseBody
//	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
//	@Operation(
//			summary = "Update role system",
//			operationId = "patchRoleSystem", 
//            responses = @ApiResponse(
//                    responseCode = "200",
//                    content = {
//                            @Content(
//                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
//                                    schema = @Schema(
//                                            implementation = SysRoleSystemDto.class
//                                    )
//                            )
//                    }
//            ),
//			tags = { SysRequestRoleSystemController.TAG }, 
//			authorizations = { 
//				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
//						CoreGroupPermission.ROLE_UPDATE }),
//        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { //						CoreGroupPermission.ROLE_UPDATE })})
//	public ResponseEntity<?> patch(
//			 @Parameter(description = "Role system mapping's uuid identifier.", required = true)
//			@PathVariable @NotNull String backendId,
//			HttpServletRequest nativeRequest)
//			throws HttpMessageNotReadableException {
//		return super.patch(backendId, nativeRequest);
//	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete role system",
			operationId = "deleteRoleSystem", 
			tags = { SysRequestRoleSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_DELETE })
    })
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role system mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_UPDATE + "')")
	@Operation(
			summary = "Create request for role system",
			operationId = "createRequestForRoleSystem",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysRoleSystemDto.class
                                    )
                            )
                    }
            ),
			tags = { SysRequestRoleSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                    CoreGroupPermission.ROLE_CREATE,
                    CoreGroupPermission.ROLE_UPDATE }),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                    CoreGroupPermission.ROLE_CREATE,
                    CoreGroupPermission.ROLE_UPDATE })
        }
    )
	public ResponseEntity<?> createRequest(@Valid @RequestBody SysRoleSystemDto dto) {
		return super.createRequest(dto);
	}
	
	
	@Override
	protected SysRoleSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysRoleSystemFilter filter = new SysRoleSystemFilter();
		filter.setRoleId(getParameterConverter().toUuid(parameters, "roleId"));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		return filter;
	}
}
