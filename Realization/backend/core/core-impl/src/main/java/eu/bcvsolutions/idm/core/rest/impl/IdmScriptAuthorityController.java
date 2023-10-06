package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptAuthorityFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Default controller for script authority (allowed services and class).
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/script-authorities")
@Tag(
		name = IdmScriptAuthorityController.TAG,
		 
		description = "Allowed services and clasess in scripts"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmScriptAuthorityController extends AbstractReadWriteDtoController<IdmScriptAuthorityDto, IdmScriptAuthorityFilter>{
	
	protected static final String TAG = "Script authorities";
	private final IdmScriptAuthorityService service;
	
	@Autowired
	public IdmScriptAuthorityController(IdmScriptAuthorityService service) {
		super(service);
		//
		Assert.notNull(service, "Service is required.");
		//
		this.service = service;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Search script authorities (/search/quick alias)", 
			/* nickname = "searchScriptAuthorities", */
			tags = { IdmScriptAuthorityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Search script authorities", 
			/* nickname = "searchQuickScriptAuthorities", */
			tags = { IdmScriptAuthorityController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_AUTOCOMPLETE + "')")
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@Operation(
			summary = "Autocomplete script authorities (selectbox usage)", 
			/* nickname = "autocompleteScriptAuthorities", */
			tags = { IdmScriptAuthorityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/service", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Get available services", 
			/* nickname = "searchScriptAvailableServices", */
			tags = { IdmScriptAuthorityController.TAG }, 
						description = "Returns IdM services (key, class), whitch can be used in scripts, if they will be assigned to the script by script authority.")
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
    @Parameters({
            @Parameter(name = "page", schema = @Schema( implementation=String.class, type = "query"), description = "Results page you want to retrieve (0..N)"),
            @Parameter(name = "size", schema = @Schema( implementation=String.class, type = "query"), description = "Number of records per page."),
            @Parameter(name = "sort", schema = @Schema( implementation=String.class, type = "query"),
                    description = "Sorting criteria in the format: property(,asc|desc)." + "Default sort order is ascending. " + "Multiple sort criteria are supported."
            ),
    })
	//@ApiImplicitParams({
    //    @ApiImplicitParam(name = "page", dataTypeClass = String.class, paramType = "query",
    //            value = "Results page you want to retrieve (0..N)"),
    //    @ApiImplicitParam(name = "size", dataTypeClass = String.class, paramType = "query",
    //            value = "Number of records per page."),
    //    @ApiImplicitParam(name = "sort", allowMultiple = true, dataTypeClass = String.class, paramType = "query",
    //            value = "Sorting criteria in the format: property(,asc|desc). " +
    //                    "Default sort order is ascending. " +
    //                    "Multiple sort criteria are supported.")
	//})
	public CollectionModel<?> findService(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		// TODO: serviceName in @RequestParam + drop pageable
		String serviceName = parameters.get("serviceName") == null ? null : String.valueOf(parameters.get("serviceName"));
		List<AvailableServiceDto> services = service.findServices(serviceName);
		// TODO: pageable?
//		int start = pageable.getPageNumber() * pageable.getPageSize();
//		int end = (pageable.getPageNumber() * pageable.getPageSize()) + pageable.getPageSize();
//		if (end > services.size()) {
//			end = services.size();
//		}
		Page<AvailableServiceDto> pages = new PageImpl<AvailableServiceDto>(
				services, pageable, services.size());
		return toCollectionModel(pages, AvailableServiceDto.class);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Script authority detail", 
			/* nickname = "getScriptAuthority", */
			/* response = IdmScriptAuthorityDto.class, */
			tags = { IdmScriptAuthorityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Authority's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update script authority", 
			/* nickname = "postScriptAuthority", */
			/* response = IdmScriptAuthorityDto.class, */
			tags = { IdmScriptAuthorityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_CREATE,
						CoreGroupPermission.SCRIPT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_CREATE,
						CoreGroupPermission.SCRIPT_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmScriptAuthorityDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@Operation(
			summary = "Update script authority", 
			/* nickname = "putScriptAuthority", */
			/* response = IdmScriptAuthorityDto.class, */
			tags = { IdmScriptAuthorityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Authority's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@RequestBody @NotNull IdmScriptAuthorityDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete script authority", 
			/* nickname = "deleteScriptAuthority", */
			tags = { IdmScriptAuthorityController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Authority's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	protected IdmScriptAuthorityFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmScriptAuthorityFilter(parameters, getParameterConverter());
	}
}
