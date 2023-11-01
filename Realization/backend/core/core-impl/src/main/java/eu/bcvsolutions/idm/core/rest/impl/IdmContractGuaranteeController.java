package eu.bcvsolutions.idm.core.rest.impl;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
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
 * Contract guarantee controller
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/contract-guarantees")
@Tag(name = IdmContractGuaranteeController.TAG, description = "Operations with identity contract guarantees")
public class IdmContractGuaranteeController extends AbstractEventableDtoController<IdmContractGuaranteeDto, IdmContractGuaranteeFilter> {
	
	protected static final String TAG = "Contract guarantees";
	
	@Autowired
	public IdmContractGuaranteeController(IdmContractGuaranteeService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_READ + "')")
	@Operation(
			summary = "Search contract guarantees (/search/quick alias)", 
			operationId = "searchContractGuarantees", 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_READ + "')")
	@Operation(
			summary = "Search contract guarantees", 
			operationId = "searchQuickContractGuarantees", 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete contract guarantees (selectbox usage)", 
			operationId = "autocompleteContractGuarantees", 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_AUTOCOMPLETE })
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
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			operationId = "countContractGuarantees", 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_READ + "')")
	@Operation(
			summary = "Contract guarantee detail", 
			operationId = "getContractGuarantee", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmContractGuaranteeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Contract guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_UPDATE + "')")
	@Operation(
			summary = "Create / update contract guarantee", 
			operationId = "postContractGuarantee", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmContractGuaranteeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.CONTRACTGUARANTEE_CREATE,
						CoreGroupPermission.CONTRACTGUARANTEE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.CONTRACTGUARANTEE_CREATE,
						CoreGroupPermission.CONTRACTGUARANTEE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmContractGuaranteeDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_UPDATE + "')")
	@Operation(
			summary = "Update contract guarantee", 
			operationId = "putContractGuarantee", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmContractGuaranteeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Contract guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmContractGuaranteeDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_UPDATE + "')")
	@Operation(
			summary = "Update contract guarantee", 
			operationId = "patchContractGuarantee", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmContractGuaranteeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "Contract guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_DELETE + "')")
	@Operation(
			summary = "Delete contract guarantee", 
			operationId = "deleteContractGuarantee", 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Contract guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTGUARANTEE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			operationId = "getPermissionsOnContractGuarantee", 
			tags = { IdmContractGuaranteeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.CONTRACTGUARANTEE_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Contract guarantee's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	protected IdmContractGuaranteeFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmContractGuaranteeFilter filter =  super.toFilter(parameters);
		// codeable decorator
		filter.setIdentity(getParameterConverter().toEntityUuid(parameters, IdmContractGuaranteeFilter.PARAMETER_IDENTITY, IdmIdentityDto.class));
		//
		return filter;
	}
}
