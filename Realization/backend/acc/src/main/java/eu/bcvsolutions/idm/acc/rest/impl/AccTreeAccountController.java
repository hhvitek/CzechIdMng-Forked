package eu.bcvsolutions.idm.acc.rest.impl;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccTreeAccountFilter;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Tree node accounts on target system
 *
 * @author Kučera
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/tree-accounts")
@Tag(
		name = AccTreeAccountController.TAG,
		
		description = "Assigned tree node accounts on target system"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class AccTreeAccountController extends AbstractReadWriteDtoController<AccTreeAccountDto, AccTreeAccountFilter> {
	protected static final String TAG = "Tree accounts";

	@Autowired
	public AccTreeAccountController(ReadWriteDtoService<AccTreeAccountDto, AccTreeAccountFilter> service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_READ + "')")
	@Operation(
			summary = "Search tree accounts (/search/quick alias)", 
			/* nickname = "searchTreeAccounts", */
			tags = { AccTreeAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.TREE_ACCOUNT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.TREE_ACCOUNT_READ })
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
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_READ + "')")
	@Operation(
			summary = "Search tree accounts", 
			/* nickname = "searchQuickTreeAccounts", */
			tags = { AccTreeAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.TREE_ACCOUNT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.TREE_ACCOUNT_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_READ + "')")
	@Operation(
			summary = "Tree node account detail",
			/* nickname = "getTreeNodeAccount", */
			/* response = AccTreeAccountDto.class, */
			tags = { AccTreeAccountController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.TREE_ACCOUNT_READ	}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.TREE_ACCOUNT_READ	})
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Tree node account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_CREATE + "')"
			+ "or hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_UPDATE + "')")
	@Operation(
			summary = "Create / update tree node account",
			/* nickname = "postTreeNodeAccount", */
			/* response = AccTreeAccountDto.class, */
			tags = { AccTreeAccountController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.TREE_ACCOUNT_CREATE,
							AccGroupPermission.TREE_ACCOUNT_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.TREE_ACCOUNT_CREATE,
							AccGroupPermission.TREE_ACCOUNT_UPDATE})
        }
    )
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> post(@RequestBody @NotNull AccTreeAccountDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update tree node account",
			/* nickname = "putTreeNodeAccount", */
			/* response = AccTreeAccountDto.class, */
			tags = { AccTreeAccountController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.TREE_ACCOUNT_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.TREE_ACCOUNT_UPDATE})
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Tree node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccTreeAccountDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete tree node account",
			/* nickname = "deleteTreeNodeAccount", */
			tags = { AccTreeAccountController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.TREE_ACCOUNT_DELETE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.TREE_ACCOUNT_DELETE})
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Tree node's account uuid identifier", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.TREE_ACCOUNT_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnTreeAccount", */
			tags = { AccTreeAccountController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.TREE_ACCOUNT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.TREE_ACCOUNT_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Tree account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	protected AccTreeAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccTreeAccountFilter filter = new AccTreeAccountFilter();
		filter.setAccountId(getParameterConverter().toUuid(parameters, "accountId"));
		filter.setRoleSystemId(getParameterConverter().toUuid(parameters, "roleSystemId"));
		filter.setTreeNodeId(getParameterConverter().toUuid(parameters, "treeNodeId"));
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setOwnership(getParameterConverter().toBoolean(parameters, "ownership"));
		return filter;
	}
}
