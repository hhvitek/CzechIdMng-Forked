package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.DelegationTypeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationDefinitionFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
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
 * Controller for a definition of delegation.
 * 
 * @author Vít Švanda
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/delegation-definitions") 
@Tag(name = IdmDelegationDefinitionController.TAG, description = "Delegation definitions")
public class IdmDelegationDefinitionController extends AbstractReadWriteDtoController<IdmDelegationDefinitionDto, IdmDelegationDefinitionFilter>  {

	protected static final String TAG = "Delegation definitions";
	
	@Autowired
	private DelegationManager delegationManager;
	
	@Autowired
	public IdmDelegationDefinitionController(
			IdmDelegationDefinitionService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Search definitions (/search/quick alias)",
			operationId = "searchDefinitions",
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_READ })
    })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Search definitions",
			operationId = "searchQuickDefinitions",
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_READ })
    })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete definitions (selectbox usage)",
			operationId = "autocompleteDefinitions",
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE })
    })
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Definition detail",
			operationId = "getDefinition",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmDelegationDefinitionDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Definition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_CREATE + "') or hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_UPDATE + "')")
	@Operation(
			summary = "Create / update delegation definition",
			operationId = "postDelegationDefinition",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmDelegationDefinitionDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.DELEGATIONDEFINITION_CREATE,
						CoreGroupPermission.DELEGATIONDEFINITION_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.DELEGATIONDEFINITION_CREATE,
						CoreGroupPermission.DELEGATIONDEFINITION_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmDelegationDefinitionDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_UPDATE + "')")
	@Operation(
			summary = "Update delegation definition",
			operationId = "putDelegationDefinition",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmDelegationDefinitionDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Delegation definition's uuid identifier", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmDelegationDefinitionDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_DELETE + "')")
	@Operation(
			summary = "Delete definition",
			operationId = "deleteDefinition",
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Definition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countDelegationDefinitions",
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	/**
	 * Returns all registered delegation types.
	 *
	 * @return delegation types
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Get all supported delegation types",
			operationId = "getSupportedDelegationTypes",
			tags = {IdmDelegationDefinitionController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
			CoreGroupPermission.DELEGATIONDEFINITION_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
			CoreGroupPermission.DELEGATIONDEFINITION_READ})
        }
    )
	public CollectionModel<DelegationTypeDto> getSupportedTypes() {
		return new CollectionModel<>(delegationManager.getSupportedTypes()
				.stream()
				.map(delegationType -> delegationManager.convertDelegationTypeToDto(delegationType))
				.collect(Collectors.toList())
		);
	}
	
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			operationId = "availableBulkAction",
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.DELEGATIONDEFINITION_READ })
    })
	@Override
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Process bulk action for delegation definitions",
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
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.DELEGATIONDEFINITION_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.DELEGATIONDEFINITION_READ})
        }
    )
	@Override
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for delegation definitions",
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
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.DELEGATIONDEFINITION_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.DELEGATIONDEFINITION_READ})
        }
    )
	@Override
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnDefinition",
			tags = { IdmDelegationDefinitionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.DELEGATIONDEFINITION_READ,
						CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.DELEGATIONDEFINITION_READ,
						CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Definition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	protected IdmDelegationDefinitionFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmDelegationDefinitionFilter filter = new IdmDelegationDefinitionFilter(parameters, getParameterConverter());
		return filter;
	}

}
