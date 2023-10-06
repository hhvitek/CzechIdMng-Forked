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
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIncompatibleRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.rest.AbstractRequestDtoController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Request for incompatible roles - defines Segregation of Duties.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@RestController
@RequestMapping(value= BaseDtoController.BASE_PATH + "/requests")
@Tag(
		name = IdmRequestIncompatibleRoleController.TAG, 
		description = "Operations with incompatible role - defines incompatible roles"//, 
		 
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmRequestIncompatibleRoleController extends AbstractRequestDtoController<IdmIncompatibleRoleDto, IdmIncompatibleRoleFilter> {
	
	protected static final String TAG = "Incompatible roles - by roles";
	protected static final String REQUEST_SUB_PATH = "/incompatible-roles";
	
	@Autowired
	public IdmRequestIncompatibleRoleController(IdmIncompatibleRoleService service) {
		super(service);
	}
	
	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH, method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_READ + "')")
	@Operation(
			summary = "Search incompatible role roles (/search/quick alias)", 
			/* nickname = "searchIncompatibleRoles", */
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_READ + "')")
	@Operation(
			summary = "Search incompatible role roles", 
			/* nickname = "searchQuickIncompatibleRoles", */
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete incompatible role roles (selectbox usage)", 
			/* nickname = "autocompleteIncompatibleRoles", */
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countIncompatibleRoles", */
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_COUNT })
        }
    )
	public long count(
			@PathVariable @NotNull String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(requestId, parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_READ + "')")
	@Operation(
			summary = "Incompatible role role detail", 
			/* nickname = "getIncompatibleRole", */
			/* response = IdmIncompatibleRoleDto.class, */ 
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_READ })
        }
    )
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			@Parameter(name = "Incompatible role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_UPDATE + "')")
	@Operation(
			summary = "Create / update incompatible role role", 
			/* nickname = "postIncompatibleRole", */
			/* response = IdmIncompatibleRoleDto.class, */ 
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_CREATE,
						CoreGroupPermission.INCOMPATIBLEROLE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_CREATE,
						CoreGroupPermission.INCOMPATIBLEROLE_UPDATE})
        }
    )
	public ResponseEntity<?> post(
			@PathVariable @NotNull String requestId,
			@Valid @RequestBody IdmIncompatibleRoleDto dto) {
		return super.post(requestId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_UPDATE + "')")
	@Operation(
			summary = "Update incompatible role role", 
			/* nickname = "putIncompatibleRole", */
			/* response = IdmIncompatibleRoleDto.class, */ 
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@PathVariable @NotNull String requestId,
			@Parameter(name = "Incompatible role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmIncompatibleRoleDto dto) {
		return super.put(requestId, backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_DELETE + "')")
	@Operation(
			summary = "Delete incompatible role role", 
			/* nickname = "deleteIncompatibleRole", */
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			@Parameter(name = "Incompatible role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}"+ REQUEST_SUB_PATH +  "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.INCOMPATIBLEROLE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnIncompatibleRole", */
			tags = { IdmRequestIncompatibleRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.INCOMPATIBLEROLE_READ })
        }
    )
	public Set<String> getPermissions(
			@PathVariable @NotNull String requestId,
			@Parameter(name = "Incompatible role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(requestId, backendId);
	}
}
