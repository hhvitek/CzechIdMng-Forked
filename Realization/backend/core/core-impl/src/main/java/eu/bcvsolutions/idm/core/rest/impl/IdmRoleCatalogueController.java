package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCatalogueFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Role catalogue controller.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/role-catalogues")
@Tag(
		name = IdmRoleCatalogueController.TAG, 
		 
		description = "Operations with role catalogues"//, 
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmRoleCatalogueController extends AbstractEventableDtoController<IdmRoleCatalogueDto, IdmRoleCatalogueFilter> {
	
	protected static final String TAG = "Role catalogues";
	
	@Autowired
	public IdmRoleCatalogueController(IdmRoleCatalogueService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_READ + "')")
	@Operation(
			summary = "Search role catalogues (/search/quick alias)", 
			/* nickname = "searchRoleCatalogues", */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_READ + "')")
	@Operation(
			summary = "Search role catalogues", 
			/* nickname = "searchQuickRoleCatalogues", */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete role catalogues (selectbox usage)", 
			/* nickname = "autocompleteRoleCatalogues", */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countRoleCatalogues", */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_READ + "')")
	@Operation(
			summary = "RoleCatalogue detail", 
			/* nickname = "getRoleCatalogue", */ 
			/* response = IdmRoleCatalogueDto.class, */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "RoleCatalogue's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	@Operation(
			summary = "Create / update role catalogue", 
			/* nickname = "postRoleCatalogue", */ 
			/* response = IdmRoleCatalogueDto.class, */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_CREATE,
						CoreGroupPermission.ROLECATALOGUE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_CREATE,
						CoreGroupPermission.ROLECATALOGUE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmRoleCatalogueDto dto) {		
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	@Operation(
			summary = "Update role catalogue", 
			/* nickname = "putRoleCatalogue", */ 
			/* response = IdmRoleCatalogueDto.class, */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmRoleCatalogueDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_UPDATE + "')")
	@Operation(
			summary = "Update role catalogue", 
			/* nickname = "patchRoleCatalogue	", */ 
			/* response = IdmRoleCatalogueDto.class, */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			@Parameter(name = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_DELETE + "')")
	@Operation(
			summary = "Delete role catalogue", 
			/* nickname = "deleteRoleCatalogue", */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Role catalogue's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnRoleCatalogue", */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ,
						CoreGroupPermission.ROLECATALOGUE_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ,
						CoreGroupPermission.ROLECATALOGUE_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "RoleCatalogue's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			/* nickname = "availableBulkAction", */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_READ + "')")
	@Operation(
			summary = "Process bulk action for role catalogue", 
			/* nickname = "bulkAction", */ 
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for role catalogue", 
			/* nickname = "prevalidateBulkAction", */ 
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmRoleCatalogueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLECATALOGUE_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Search root catalogues", 
			/* nickname = "searchRootRoleCatalogues", */ 
			tags = { IdmRoleCatalogueController.TAG })
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
	public CollectionModel<?> findRoots(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		IdmRoleCatalogueFilter filter = toFilter(parameters);
		filter.setRoots(Boolean.TRUE);
		//
		return toCollectionModel(find(filter, pageable, IdmBasePermission.AUTOCOMPLETE), IdmTreeNode.class);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLECATALOGUE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Search sub catalogues", 
			/* nickname = "searchChildrenRoleCatalogues", */ 
			tags = { IdmRoleCatalogueController.TAG },
			description = "Finds direct chilren by given parent node uuid identifier. Set 'parent' parameter.")
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
	public CollectionModel<?> findChildren(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		IdmRoleCatalogueFilter filter = toFilter(parameters);
		filter.setRecursively(false);
		//
		return toCollectionModel(find(filter, pageable, IdmBasePermission.AUTOCOMPLETE), IdmTreeNode.class);
	}
	
	@Override
	protected IdmRoleCatalogueFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmRoleCatalogueFilter filter = new IdmRoleCatalogueFilter(parameters);
		//
		return filter;
	}
}
