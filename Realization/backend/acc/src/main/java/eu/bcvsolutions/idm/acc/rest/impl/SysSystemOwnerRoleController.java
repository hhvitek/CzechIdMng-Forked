package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerRoleFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerRoleService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * system owner controller - by role
 * 
 * @author Roman Kucera
 * @since 13.0.0
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/system-owner-roles")
@Tag(
		name = SysSystemOwnerRoleController.TAG,
		description = "Operations with system owners by role"//, 
		
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class SysSystemOwnerRoleController extends AbstractEventableDtoController<SysSystemOwnerRoleDto, SysSystemOwnerRoleFilter> {
	
	protected static final String TAG = "System owners - by roles";
	
	@Autowired
	public SysSystemOwnerRoleController(SysSystemOwnerRoleService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@GetMapping
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_READ + "')")
	@Operation(
			summary = "Search system owner roles (/search/quick alias)", 
			/* nickname = "searchSystemOwnerRoles", */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/search/quick")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_READ + "')")
	@Operation(
			summary = "Search system owner roles", 
			/* nickname = "searchQuickSystemOwnerRoles", */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/search/autocomplete")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete system owner roles (selectbox usage)", 
			/* nickname = "autocompleteSystemOwnerRoles", */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@GetMapping(value = "/search/count")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countSystemOwnerRoles", */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_READ + "')")
	@Operation(
			summary = "system owner role detail", 
			/* nickname = "getSystemOwnerRole", */ 
			/* response = SysSystemOwnerRoleDto.class, */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PostMapping
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_UPDATE + "')")
	@Operation(
			summary = "Create / update system owner role", 
			/* nickname = "postSystemOwnerRole", */ 
			/* response = SysSystemOwnerRoleDto.class, */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_CREATE,
						AccGroupPermission.SYSTEMOWNERROLE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_CREATE,
						AccGroupPermission.SYSTEMOWNERROLE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody SysSystemOwnerRoleDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PutMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_UPDATE + "')")
	@Operation(
			summary = "Update system owner role", 
			/* nickname = "putSystemOwnerRole", */ 
			/* response = SysSystemOwnerRoleDto.class, */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody SysSystemOwnerRoleDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PatchMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_UPDATE + "')")
	@Operation(
			summary = "Update system owner role", 
			/* nickname = "patchSystemOwnerRole", */ 
			/* response = SysSystemOwnerRoleDto.class, */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			@Parameter(name = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@DeleteMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_DELETE + "')")
	@Operation(
			summary = "Delete system owner role", 
			/* nickname = "deleteSystemOwnerRole", */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@GetMapping(value = "/{backendId}/permissions")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNERROLE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnSystemOwnerRole", */ 
			tags = { SysSystemOwnerRoleController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNERROLE_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
