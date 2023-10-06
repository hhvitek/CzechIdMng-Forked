package eu.bcvsolutions.idm.example.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * RESTful example product endpoint
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/example-products")
@Tag(
		name = ExampleProductController.TAG, 
		description = "Example products"
		)
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
			/* nickname = "searchExampleProducts", */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')")
	@Operation(
			summary = "Search example products", 
			/* nickname = "searchQuickExampleProducts", */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete example products (selectbox usage)", 
			/* nickname = "autocompleteExampleProducts", */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countExampleProducts", */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_READ + "')")
	@Operation(
			summary = "Example product detail", 
			/* nickname = "getExampleProduct", */ 
			/* response = ExampleProductDto.class, */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_CREATE + "') or hasAuthority('" + ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE + "')")
	@Operation(
			summary = "Create / update example product", 
			/* nickname = "postExampleProduct", */ 
			/* response = ExampleProductDto.class, */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {

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
			/* nickname = "putExampleProduct", */ 
			/* response = ExampleProductDto.class, */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Example product's uuid identifier or code.", required = true)
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
			/* nickname = "patchExampleProduct", */ 
			/* response = ExampleProductDto.class, */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			@Parameter(name = "Example product's uuid identifier or code.", required = true)
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
			/* nickname = "deleteExampleProduct", */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						ExampleGroupPermission.EXAMPLE_PRODUCT_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Example product's uuid identifier or code.", required = true)
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
			/* nickname = "getPermissionsOnExampleProduct", */ 
			tags = { ExampleProductController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ,
						ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						ExampleGroupPermission.EXAMPLE_PRODUCT_READ,
						ExampleGroupPermission.EXAMPLE_PRODUCT_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Example product's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	protected ExampleProductFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new ExampleProductFilter(parameters, getParameterConverter());
	}
}
