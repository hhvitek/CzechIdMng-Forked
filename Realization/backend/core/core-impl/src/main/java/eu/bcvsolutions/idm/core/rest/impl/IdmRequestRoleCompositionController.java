package eu.bcvsolutions.idm.core.rest.impl;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
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
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCompositionFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.AbstractRequestDtoController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Request for role composition - defines business roles.
 * 
 * @author svandav
 * @since 9.0.0
 */
@RestController
@RequestMapping(value= BaseDtoController.BASE_PATH + "/requests")
@Tag(
		name = IdmRequestRoleCompositionController.TAG, 
		description = "Operations with role composition - defines business roles"//, 
		 
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmRequestRoleCompositionController extends AbstractRequestDtoController<IdmRoleCompositionDto, IdmRoleCompositionFilter> {
	
	protected static final String TAG = "Role compositions - by roles";
	protected static final String REQUEST_SUB_PATH = "/role-compositions";
	
	@Autowired
	public IdmRequestRoleCompositionController(IdmRoleCompositionService service) {
		super(service);
	}
	
	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH, method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_READ + "')")
	@Operation(
			summary = "Search role composition roles (/search/quick alias)", 
			/* nickname = "searchRoleCompositions", */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_READ })
        }
    )
	public CollectionModel<?> find(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH + "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_READ + "')")
	@Operation(
			summary = "Search role composition roles", 
			/* nickname = "searchQuickRoleCompositions", */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(requestId, parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete role composition roles (selectbox usage)", 
			/* nickname = "autocompleteRoleCompositions", */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(requestId, parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countRoleCompositions", */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_COUNT })
        }
    )
	public long count(@PathVariable @NotNull String requestId, @RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(requestId, parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_READ + "')")
	@Operation(
			summary = "Role composition role detail", 
			/* nickname = "getRoleComposition", */ 
			/* response = IdmRoleCompositionDto.class, */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_READ })
        }
    )
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			@Parameter(name = "Role composition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_UPDATE + "')")
	@Operation(
			summary = "Create / update role composition role", 
			/* nickname = "postRoleComposition", */ 
			/* response = IdmRoleCompositionDto.class, */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_CREATE,
						CoreGroupPermission.ROLECOMPOSITION_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_CREATE,
						CoreGroupPermission.ROLECOMPOSITION_UPDATE})
        }
    )
	public ResponseEntity<?> post(
			@PathVariable @NotNull String requestId,
			@Valid @RequestBody IdmRoleCompositionDto dto) {
		return super.post(requestId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_UPDATE + "')")
	@Operation(
			summary = "Update role composition role", 
			/* nickname = "putRoleComposition", */ 
			/* response = IdmRoleCompositionDto.class, */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			@Parameter(name = "Role composition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmRoleCompositionDto dto) {
		return super.put(requestId, backendId, dto);
	}
	
//	@Override
//	@ResponseBody
//	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.PATCH)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_UPDATE + "')")
//	@Operation(
//			summary = "Update role composition role", 
//			/* nickname = "patchRoleComposition", */ 
//			/* response = IdmRoleCompositionDto.class, */ 
//			tags = { IdmRequestRoleCompositionController.TAG }, 
//			authorizations = { 
//				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
//						CoreGroupPermission.ROLECOMPOSITION_UPDATE }),
//				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
//						CoreGroupPermission.ROLECOMPOSITION_UPDATE })
//				})
//	public ResponseEntity<?> patch(
//			@Parameter(name = "Role composition's uuid identifier.", required = true)
//			@PathVariable @NotNull String backendId,
//			HttpServletRequest nativeRequest)
//			throws HttpMessageNotReadableException {
//		return super.patch(backendId, nativeRequest);
//	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_DELETE + "')")
	@Operation(
			summary = "Delete role composition role", 
			/* nickname = "deleteRoleComposition", */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			@Parameter(name = "Role composition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECOMPOSITION_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnRoleComposition", */ 
			tags = { IdmRequestRoleCompositionController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.ROLECOMPOSITION_READ })
        }
    )
	public Set<String> getPermissions(
			@PathVariable @NotNull String requestId,
			@Parameter(name = "Role composition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(requestId, backendId);
	}
}
