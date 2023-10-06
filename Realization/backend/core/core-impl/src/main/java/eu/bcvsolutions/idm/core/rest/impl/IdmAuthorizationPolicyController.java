package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
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

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizationEvaluatorDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Controller for assigning authorization evaluators to roles.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/authorization-policies")
@Tag(
		name = IdmAuthorizationPolicyController.TAG,
		description = "Operations with authorization policies"//, 

		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmAuthorizationPolicyController extends AbstractReadWriteDtoController<IdmAuthorizationPolicyDto, IdmAuthorizationPolicyFilter> {
	
	protected static final String TAG = "Authorization policies";
	private final AuthorizationManager authorizationManager;
	
	@Autowired
	public IdmAuthorizationPolicyController(
			IdmAuthorizationPolicyService service,
			AuthorizationManager authorizationManager) {
		super(service);
		//
		Assert.notNull(authorizationManager, "Manager is required.");
		//
		this.authorizationManager = authorizationManager;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "Search authorization policies (/search/quick alias)", 
			/* nickname = "searchAuthorizationPolicies", */
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "Search authorization policies", 
			/* nickname = "searchQuickAuthorizationPolicies", */
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countAuthorizationPolicies", */
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "Authorization policy detail", 
			/* nickname = "getAuthorizationPolicy", */
			/* response = IdmAuthorizationPolicyDto.class, */ 
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE + "')")
	@Operation(
			summary = "Create / update authorization policy", 
			/* nickname = "postAuthorizationPolicy", */
			/* response = IdmAuthorizationPolicyDto.class, */ 
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE,
						CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_CREATE,
						CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmAuthorizationPolicyDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE + "')")
	@Operation(
			summary = "Update authorization policy", 
			/* nickname = "putAuthorizationPolicy", */
			/* response = IdmAuthorizationPolicyDto.class, */ 
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmAuthorizationPolicyDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE + "')")
	@Operation(
			summary = "Delete authorization policy", 
			/* nickname = "deleteAuthorizationPolicy", */
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnAuthorizationPolicy", */
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Policy's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			/* nickname = "availableBulkAction", */
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "Process authorization policies bulk action", 
			/* nickname = "bulkAction", */
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "Prevalidate authorization policies bulk action", 
			/* nickname = "prevalidateBulkAction", */
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	/**
	 * Returns all registered evaluators
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "Get all supported evaluators", 
			/* nickname = "getSupportedAuthorizationEvaluators", */
			tags = { IdmAuthorizationPolicyController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ })
        }
    )
	public CollectionModel<AuthorizationEvaluatorDto> getSupportedEvaluators() {
		return new CollectionModel<>(authorizationManager.getSupportedEvaluators());
	}
	
	/**
	 * Returns all registered tasks
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/authorizable-types")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUTHORIZATIONPOLICY_READ + "')")
	@Operation(
			summary = "Get all supported authorizable types", 
			/* nickname = "getAuthorizableTypes", */
			tags = { IdmAuthorizationPolicyController.TAG }, 
						description = "Returns all types, with securing data support (by authorization policies).")
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUTHORIZATIONPOLICY_READ })
        }
    )
	public CollectionModel<AuthorizableType> getAuthorizableTypes() {
		return new CollectionModel<>(authorizationManager.getAuthorizableTypes());
	}
	
	@Override
	protected IdmAuthorizationPolicyFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmAuthorizationPolicyFilter filter = new IdmAuthorizationPolicyFilter(parameters, getParameterConverter());
		//
		// role and identity decorator
		filter.setIdentityId(getParameterConverter().toEntityUuid(
				parameters, IdmAuthorizationPolicyFilter.PARAMETER_IDENTITY_ID, IdmIdentityDto.class));
		filter.setRoleId(getParameterConverter().toEntityUuid(
				parameters, IdmAuthorizationPolicyFilter.PARAMETER_ROLE_ID, IdmRoleDto.class));
		//
		return filter;
	}
}
