package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;
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

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * System groups system - relation between a system and a group of systems.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-group-systems")
@Tag(name = SysSystemGroupSystemController.TAG, description = "System groups system - relation between a system and a group of systems.")
public class SysSystemGroupSystemController extends AbstractEventableDtoController<SysSystemGroupSystemDto, SysSystemGroupSystemFilter> {

	protected static final String TAG = "System groups system - relation between a system and a group of systems.";

	@Autowired
	public SysSystemGroupSystemController(SysSystemGroupSystemService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@Operation(
			summary = "Search configured SystemGroupSystem relations (/search/quick alias)",
			operationId = "searchSystemGroupSystems",
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ})
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
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@Operation(
			summary = "Search configured SystemGroupSystem relations",
			operationId = "searchQuickSystemGroupSystems",
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ})
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete configured SystemGroupSystem relations (selectbox usage)",
			operationId = "autocompleteSystemGroupSystems",
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE})
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
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countSystemGroupSystems",
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_COUNT}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_COUNT})
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@Operation(
			summary = "System group-system detail",
			operationId = "getSystemGroupSystem",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemGroupSystemDto.class
                                    )
                            )
                    }
            ),
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ})
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "System group-system's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE + "')")
	@Operation(
			summary = "Create / update configured SystemGroupSystem relation",
			operationId = "postSystemGroupSystem",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemGroupSystemDto.class
                                    )
                            )
                    }
            ),
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_CREATE,
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_CREATE,
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody SysSystemGroupSystemDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE + "')")
	@Operation(
			summary = "Update configured SystemGroupSystem relation",
			operationId = "putSystemGroupSystem",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemGroupSystemDto.class
                                    )
                            )
                    }
            ),
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE})
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "System group-system's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			@Valid @RequestBody SysSystemGroupSystemDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_DELETE + "')")
	@Operation(
			summary = "Delete configured SystemGroupSystem relation",
			operationId = "deleteSystemGroupSystem",
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_DELETE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_DELETE})
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "System group-system's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnSystemGroupSystem",
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ,
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ,
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "System group-system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			operationId = "availableBulkAction",
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ})
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@Operation(
			summary = "Process bulk action",
			operationId = "bulkAction",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action",
			operationId = "prevalidateBulkAction",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = {SysSystemGroupSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}


	@Override
	protected SysSystemGroupSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new SysSystemGroupSystemFilter(parameters, getParameterConverter());
	}
}
