package eu.bcvsolutions.idm.core.rest.impl;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller for a delegation.
 * 
 * @author Vít Švanda
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/delegations") 
@Tag(
		name = IdmDelegationController.TAG,  

		description = "Delegations"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmDelegationController extends AbstractReadWriteDtoController<IdmDelegationDto, IdmDelegationFilter>  {

	protected static final String TAG = "Delegations";
	
	@Autowired
	public IdmDelegationController(
			IdmDelegationService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Search delegations (/search/quick alias)", 
			/* nickname = "searchDelegations", */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Search delegations", 
			/* nickname = "searchQuickDelegations", */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete delegations (selectbox usage)", 
			/* nickname = "autocompleteDelegations", */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countDelegations", */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Delegation detail", 
			/* nickname = "getDelegation", */
			/* response = IdmDelegationDto.class, */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Delegation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_DELETE + "')")
	@Operation(
			summary = "Delete definition", 
			/* nickname = "deleteDelegation", */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Delegation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
		@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			/* nickname = "availableBulkAction", */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ })
        }
    )
	@Override
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_READ + "')")
	@Operation(
			summary = "Process bulk action for delegations", 
			/* nickname = "bulkAction", */
			/* response = IdmBulkActionDto.class, */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {
 
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
			summary = "Prevalidate bulk action for delegations", 
			/* nickname = "prevalidateBulkAction", */
			/* response = IdmBulkActionDto.class, */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {
 
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
			/* nickname = "getPermissionsOnDelegation", */
			tags = { IdmDelegationController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ,
						CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.DELEGATIONDEFINITION_READ,
						CoreGroupPermission.DELEGATIONDEFINITION_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Delegation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	protected IdmDelegationFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmDelegationFilter filter = new IdmDelegationFilter(parameters, getParameterConverter());
		filter.setIncludeOwner(true);
		return filter;
	}

}
