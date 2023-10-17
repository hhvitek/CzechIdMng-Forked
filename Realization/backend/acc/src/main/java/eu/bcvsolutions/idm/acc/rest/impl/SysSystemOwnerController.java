package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * System owner controller
 * 
 * @author Roman Kucera
 * @since 13.0.0
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/system-owners")
@Tag(
		name = SysSystemOwnerController.TAG, 
		description = "Operations with identity system owners"//, 
		 
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class SysSystemOwnerController extends AbstractReadWriteDtoController<SysSystemOwnerDto, SysSystemOwnerFilter> {
	
	protected static final String TAG = "System owners";
	
	@Autowired
	public SysSystemOwnerController(SysSystemOwnerService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@GetMapping
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_READ + "')")
	@Operation(
			summary = "Search system owners (/search/quick alias)",
			/* nickname = "searchSystemOwners", */ 
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/search/quick")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_READ + "')")
	@Operation(
			summary = "Search system owners",
			/* nickname = "searchQuickSystemOwners", */ 
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/search/autocomplete")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete system owners (selectbox usage)",
			/* nickname = "autocompleteSystemOwners", */
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_AUTOCOMPLETE })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@GetMapping(value = "/search/count")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			/* nickname = "countSystemOwners", */
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_READ + "')")
	@Operation(
			summary = "System owner detail",
			/* nickname = "getSystemOwner", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemOwnerDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PostMapping
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEMOWNER_UPDATE + "')")
	@Operation(
			summary = "Create / update system owner",
			/* nickname = "postSystemOwner", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemOwnerDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_CREATE,
						AccGroupPermission.SYSTEMOWNER_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_CREATE,
						AccGroupPermission.SYSTEMOWNER_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody SysSystemOwnerDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PutMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_UPDATE + "')")
	@Operation(
			summary = "Update system owner",
			/* nickname = "putSystemOwner", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemOwnerDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody SysSystemOwnerDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PatchMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_UPDATE + "')")
	@Operation(
			summary = "Update system owner",
			/* nickname = "patchSystemOwner", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemOwnerDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			 @Parameter(description = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@DeleteMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_DELETE + "')")
	@Operation(
			summary = "Delete system owner",
			/* nickname = "deleteSystemOwner", */
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@GetMapping(value = "/{backendId}/permissions")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			/* nickname = "getPermissionsOnSystemOwner", */
			tags = { SysSystemOwnerController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.SYSTEMOWNER_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.SYSTEMOWNER_READ })
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
