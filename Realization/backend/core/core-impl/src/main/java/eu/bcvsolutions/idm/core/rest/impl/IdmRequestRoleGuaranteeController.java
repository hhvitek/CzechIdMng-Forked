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
import eu.bcvsolutions.idm.core.api.dto.IdmRoleGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
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
 * Request for role guarantee controller
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/requests")
@Tag(name = IdmRequestRoleGuaranteeController.TAG, description = "Operations with identity role guarantees")
public class IdmRequestRoleGuaranteeController extends AbstractRequestDtoController<IdmRoleGuaranteeDto, IdmRoleGuaranteeFilter> {
	
	protected static final String TAG = "Role guarantees";
	protected static final String REQUEST_SUB_PATH = "/role-guarantees";
	
	@Autowired
	public IdmRequestRoleGuaranteeController(IdmRoleGuaranteeService service) {
		super(service);
	}
	
	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH , method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_READ + "')")
	@Operation(
			summary = "Search role guarantees (/search/quick alias)",
			operationId = "searchRoleGuarantees",
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLEGUARANTEE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLEGUARANTEE_READ })
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
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +  "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_READ + "')")
	@Operation(
			summary = "Search role guarantees",
			operationId = "searchQuickRoleGuarantees",
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLEGUARANTEE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLEGUARANTEE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +  "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete role guarantees (selectbox usage)",
			operationId = "autocompleteRoleGuarantees",
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLEGUARANTEE_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLEGUARANTEE_AUTOCOMPLETE })
    })
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
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +  "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countRoleGuarantees",
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLEGUARANTEE_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLEGUARANTEE_COUNT })
    })
	public long count(@PathVariable @NotNull String requestId, @RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(requestId, parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_READ + "')")
	@Operation(
			summary = "Role guarantee detail",
			operationId = "getRoleGuarantee",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleGuaranteeDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLEGUARANTEE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLEGUARANTEE_READ })
    })
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_UPDATE + "')")
	@Operation(
			summary = "Create / update role guarantee",
			operationId = "postRoleGuarantee",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleGuaranteeDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLEGUARANTEE_CREATE,
						CoreGroupPermission.ROLEGUARANTEE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLEGUARANTEE_CREATE,
						CoreGroupPermission.ROLEGUARANTEE_UPDATE})
        }
    )
	public ResponseEntity<?> post(
			@PathVariable @NotNull String requestId, 
			@Valid @RequestBody IdmRoleGuaranteeDto dto) {
		return super.post(requestId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_UPDATE + "')")
	@Operation(
			summary = "Update role guarantee",
			operationId = "putRoleGuarantee",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleGuaranteeDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLEGUARANTEE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLEGUARANTEE_UPDATE })
    })
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmRoleGuaranteeDto dto) {
		return super.put(requestId, backendId, dto);
	}
	
//	@Override
//	@ResponseBody
//	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.PATCH)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_UPDATE + "')")
//	@Operation(
//			summary = "Update role guarantee",
//			operationId = "patchRoleGuarantee",
//            responses = @ApiResponse(
//                    responseCode = "200",
//                    content = {
//                            @Content(
//                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
//                                    schema = @Schema(
//                                            implementation = IdmRoleGuaranteeDto.class
//                                    )
//                            )
//                    }
//            ),
//			tags = { IdmRequestRoleGuaranteeController.TAG }, 
//			authorizations = { 
//				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
//						CoreGroupPermission.ROLEGUARANTEE_UPDATE }),
//        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { //						CoreGroupPermission.ROLEGUARANTEE_UPDATE })})
//	public ResponseEntity<?> patch(
//			 @Parameter(description = "Role guarantee's uuid identifier.", required = true)
//			@PathVariable @NotNull String backendId,
//			HttpServletRequest nativeRequest)
//			throws HttpMessageNotReadableException {
//		return super.patch(backendId, nativeRequest);
//	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_DELETE + "')")
	@Operation(
			summary = "Delete role guarantee",
			operationId = "deleteRoleGuarantee",
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLEGUARANTEE_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLEGUARANTEE_DELETE })
    })
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLEGUARANTEE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnRoleGuarantee",
			tags = { IdmRequestRoleGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLEGUARANTEE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLEGUARANTEE_READ })
    })
	public Set<String> getPermissions(
			@PathVariable @NotNull String requestId,
			 @Parameter(description = "Role guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(requestId, backendId);
	}
}
