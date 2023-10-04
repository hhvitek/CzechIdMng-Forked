package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeTypeFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.rest.impl.IdmLongRunningTaskController;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Tree type structures.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + BaseDtoController.TREE_BASE_PATH + "-types")
@Api(
		value = IdmTreeTypeController.TAG,  
		tags = { IdmTreeTypeController.TAG }, 
		description = "Operation with tree types",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmTreeTypeController extends AbstractEventableDtoController<IdmTreeTypeDto, IdmTreeTypeFilter> {
	
	protected static final String TAG = "Tree structure - types";
	private final IdmLongRunningTaskController longRunningTaskController;
	private final IdmTreeTypeService service;
	private final IdmTreeNodeService treeNodeservice;
	
	@Autowired
	public IdmTreeTypeController(
			IdmTreeTypeService service, 
			IdmTreeNodeService treeNodeService,
			IdmLongRunningTaskController longRunningTaskController) {
		super(service);
		//
		Assert.notNull(longRunningTaskController, "Controller is required.");
		Assert.notNull(treeNodeService, "Service is required.");
		//
		this.service = service;
		this.longRunningTaskController = longRunningTaskController;
		this.treeNodeservice = treeNodeService;
	}
	
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Search tree types (/search/quick alias)", 
			nickname = "searchTreeTypes",
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Search tree types", 
			nickname = "searchQuickTreeTypes", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete tree types (selectbox usage)", 
			nickname = "autocompleteTreeTypes", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_AUTOCOMPLETE, description = "") })
				})
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countTreeTypes", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Tree type detail", 
			nickname = "getTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update tree type", 
			nickname = "postTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmTreeTypeDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree type",
			nickname = "putTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmTreeTypeDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree type",
			nickname = "patchTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_DELETE + "')")
	@ApiOperation(
			value = "Delete tree type", 
			nickname = "deleteTreeType", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnTreeType", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Process bulk action for tree types", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for tree types", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	/**
	 * Returns default tree type or {@code null}, if no default tree type is defined
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/default", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_AUTOCOMPLETE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Get default tree type detail", 
			nickname = "getDefaultTreeType", 
			response = IdmTreeTypeDto.class, 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_AUTOCOMPLETE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_AUTOCOMPLETE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
					})
	public ResponseEntity<?> getDefaultTreeType() {
		IdmTreeTypeDto defaultTreeType = service.getDefaultTreeType();
		if (defaultTreeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", "default tree type"));
		}
		Set<String> permissions = getService().getPermissions(defaultTreeType.getId());
		if (!PermissionUtils.hasAnyPermission(permissions, IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ)) {
			throw new ForbiddenEntityException(defaultTreeType.getId(), IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		}
		return new ResponseEntity<>(toModel(defaultTreeType), HttpStatus.OK);
	}
	
	/**
	 * Returns all configuration properties for given tree type.
	 * 
	 * @param backendId
	 * @return list of granted authorities
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/configurations", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREETYPE_AUTOCOMPLETE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREETYPE_READ + "')")
	@ApiOperation(
			value = "Get tree type configuration items", 
			nickname = "getTreeTypeConfigurations", 
			tags = { IdmTreeTypeController.TAG }, 
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_AUTOCOMPLETE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_AUTOCOMPLETE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.TREETYPE_READ, description = "") })
					})
	public List<IdmConfigurationDto> getConfigurations(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable String backendId) {
		IdmTreeType treeType = (IdmTreeType) getLookupService().lookupEntity(IdmTreeType.class, backendId);
		if (treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		Set<String> permissions = service.getPermissions(treeType.getId());
		if (!PermissionUtils.hasAnyPermission(permissions, IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ)) {
			throw new ForbiddenEntityException(treeType.getId(), IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		}
		//
		return service.getConfigurations(treeType.getId());
	}
	
	/**
	 * Rebuild (drop and create) all indexes for given treeType.
	 * 
	 * @param backendId tree type id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/index/rebuild", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@ApiOperation(
			value = "Rebuild tree type index", 
			nickname = "rebuildTreeTypeIndex", 
			response = IdmLongRunningTaskDto.class, 
			tags = { IdmTreeTypeController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_EXECUTE, description = "") })
					},
			notes = "Rebuild forest index for given tree type.")
	public ResponseEntity<?> rebuildIndex(
			@ApiParam(value = "Type's uuid identifier or code.", required = true)
			@PathVariable String backendId) {
		IdmTreeTypeDto treeType = getDto(backendId);
		if (treeType == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		UUID longRunningTaskId = treeNodeservice.rebuildIndexes(treeType.getId());
		//
		return longRunningTaskController.get(longRunningTaskId.toString());
	}
	
	@Override
	protected IdmTreeTypeFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmTreeTypeFilter(parameters, getParameterConverter());
	}
}
