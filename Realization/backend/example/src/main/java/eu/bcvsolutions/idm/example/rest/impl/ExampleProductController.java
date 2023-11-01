package eu.bcvsolutions.idm.example.rest.impl;

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
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;
import eu.bcvsolutions.idm.example.domain.ExampleGroupPermission;
import eu.bcvsolutions.idm.example.dto.ExampleProductDto;
import eu.bcvsolutions.idm.example.dto.filter.ExampleProductFilter;
import eu.bcvsolutions.idm.example.service.api.ExampleProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * RESTful example product endpoint
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/example-products")
@Tag(name = ExampleProductController.TAG, description = "Example products")
public class ExampleProductController extends AbstractReadWriteDtoController<ExampleProductDto, ExampleProductFilter> {

	protected static final String TAG = "Example products";
	
	@Autowired
	public ExampleProductController(ExampleProductService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')")
	@Operation(
			summary = "Search example products (/search/quick alias)", 
			operationId = "searchExampleProducts",
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_READ })
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
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')")
	@Operation(
			summary = "Search example products", 
			operationId = "searchQuickExampleProducts",
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_READ })
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
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete example products (selectbox usage)", 
			operationId = "autocompleteExampleProducts",
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			operationId = "countExampleProducts",
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')")
	@Operation(
			summary = "Example product detail", 
			operationId = "getExampleProduct",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = ExampleProductDto.class
                                    )
                            )
                    }
            ),
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_CREATE + "') or hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE + "')")
	@Operation(
			summary = "Create / update example product", 
			operationId = "postExampleProduct",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = ExampleProductDto.class
                                    )
                            )
                    }
            ),
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						ExampleGroupPermission.EXAMPLE_PRODUCT_CREATE,
						ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						ExampleGroupPermission.EXAMPLE_PRODUCT_CREATE,
						ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody ExampleProductDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE + "')")
	@Operation(
			summary = "Update example product", 
			operationId = "putExampleProduct",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = ExampleProductDto.class
                                    )
                            )
                    }
            ),
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody ExampleProductDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE + "')")
	@Operation(
			summary = "Update example product", 
			operationId = "patchExampleProduct",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = ExampleProductDto.class
                                    )
                            )
                    }
            ),
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_DELETE + "')")
	@Operation(
			summary = "Delete example product", 
			operationId = "deleteExampleProduct",
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { ExampleGroupPermission.EXAMPLE_PRODUCT_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')"
			+ " or hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			operationId = "getPermissionsOnExampleProduct",
			tags = { ExampleProductController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ,
						ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ,
						ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	protected ExampleProductFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new ExampleProductFilter(parameters, getParameterConverter());
	}
}
