package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleSystemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleSystemFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleSystemService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
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
 * IdM role-system controller - (we need to work with role-system also in the core module).
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/core/role-systems")
@Tag(name = IdmRoleSystemController.TAG, description = "IdM role-system controller - (we need to work with role-system also in the core module)")
public class IdmRoleSystemController extends AbstractReadDtoController<IdmRoleSystemDto, IdmRoleSystemFilter> {

	protected static final String TAG = "IdM role-system controller - (we need to work with role-system also in the core module).";
	
	@Autowired
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IdmRoleSystemController(IdmRoleSystemService service) {
		super((ReadDtoService) service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@Operation(
			summary = "Search system roles (/search/quick alias)",
			operationId = "searchSystemRoles",
			tags = {IdmRoleSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_READ})
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@Operation(
			summary = "Search system roles",
			operationId = "searchQuickSystemRoles",
			tags = {IdmRoleSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_READ})
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete system roles (selectbox usage)",
			operationId = "autocompleteSystemRoles",
			tags = {IdmRoleSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_AUTOCOMPLETE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_AUTOCOMPLETE})
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

//	@ResponseBody
//	@RequestMapping(value= "/search/can-be-requested", method = RequestMethod.GET)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_CANBEREQUESTED + "')")
//	@Operation(
//			summary = "Find assigned roles, which can be requested",
//			operationId = "findCanBeRequestedSystemRoles",
//			tags = { IdmRoleSystemController.TAG }, 
//			authorizations = { 
//				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
//						CoreGroupPermission.ROLE_CANBEREQUESTED }),
//        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { //						CoreGroupPermission.ROLE_CANBEREQUESTED })})
//	@PageableAsQueryParam
//	public CollectionModel<?> findCanBeRequested(
//			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
//			@Parameter(hidden = true)
//			@PageableDefault Pageable pageable) {
//		return toCollectionModel(find(toFilter(parameters), pageable, RoleBasePermission.CANBEREQUESTED), getDtoClass());
//	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countSystemRoles",
			tags = {IdmRoleSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.ROLE_COUNT}),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.ROLE_COUNT})
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@Operation(
			summary = "System role detail",
			operationId = "getSystemRole",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmRoleSystemDto.class
                                    )
                            )
                    }
            ),
			tags = {IdmRoleSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_READ})
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "System role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnRoleSystem",
			tags = {IdmRoleSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.ROLE_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.ROLE_READ})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "System role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
