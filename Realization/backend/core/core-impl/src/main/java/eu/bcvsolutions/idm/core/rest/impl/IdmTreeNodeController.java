package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
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

import eu.bcvsolutions.idm.core.api.audit.dto.IdmAuditDto;
import eu.bcvsolutions.idm.core.api.audit.service.IdmAuditService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.domain.TreeConfiguration;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Tree nodes endpoint.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + BaseDtoController.TREE_BASE_PATH + "-nodes")
@Api(
		value = IdmTreeNodeController.TAG,  
		tags = { IdmTreeNodeController.TAG }, 
		description = "Operation with tree nodes",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmTreeNodeController extends AbstractEventableDtoController<IdmTreeNodeDto, IdmTreeNodeFilter> {
	
	protected static final String TAG = "Tree structure - nodes";
	private final IdmTreeNodeService treeNodeService;
	//
	@Autowired private TreeConfiguration treeConfiguration;
	@Autowired private IdmAuditService auditService;
	@Autowired private IdmFormDefinitionController formDefinitionController;
	@Autowired private FormService formService;
	
	@Autowired
	public IdmTreeNodeController(
			IdmTreeNodeService treeNodeService,
			IdmAuditService auditService,
			IdmFormDefinitionController formDefinitionController) {
		super(treeNodeService);
		//
		Assert.notNull(treeNodeService, "Service is required.");
		Assert.notNull(auditService, "Service is required.");
		Assert.notNull(formDefinitionController, "Controller is required.");
		//
		this.treeNodeService = treeNodeService;
		this.auditService = auditService;
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Search tree nodes (/search/quick alias)", 
			nickname = "searchTreeNodes",
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") })
				})
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Search tree nodes", 
			nickname = "searchQuickTreeNodes", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") })
				})
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete tree nodes (selectbox usage)", 
			nickname = "autocompleteTreeNodes", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_AUTOCOMPLETE, description = "") })
				})
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countTreeNodes", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Tree node detail", 
			nickname = "getTreeNode", 
			response = IdmTreeNodeDto.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update tree node", 
			nickname = "postTreeNode", 
			response = IdmTreeNodeDto.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmTreeNodeDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree node",
			nickname = "putTreeNode", 
			response = IdmTreeNodeDto.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmTreeNodeDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@ApiOperation(
			value = "Update tree node",
			nickname = "patchTreeNode", 
			response = IdmTreeNodeDto.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_DELETE + "')")
	@ApiOperation(
			value = "Delete tree node", 
			nickname = "deleteTreeNode", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnTreeNode", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Process bulk action for tree nodes", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for tree nodes", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(value = "{backendId}/revisions/{revId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@ApiOperation(
			value = "Tree node audit - read revision detail", 
			nickname = "getTreeNodeRevision", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public ResponseEntity<?> findRevision(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable("backendId") String backendId, 
			@ApiParam(value = "Revision identifier.", required = true)
			@PathVariable("revId") Long revId) {
		IdmTreeNodeDto treeNode = getDto(backendId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", backendId));
		}
		
		IdmTreeNode revision;
		try {
			revision = this.auditService.findRevision(IdmTreeNode.class, treeNode.getId(), revId);
		} catch (RevisionDoesNotExistException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,  ImmutableMap.of("revision", revId), ex);
		}
		// TODO: dto
		return new ResponseEntity<>(revision, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "{backendId}/revisions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@ApiOperation(
			value = "Tree node audit - read all revisions", 
			nickname = "getTreeNodeRevisions", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.AUDIT_READ, description = "") })
				})
	public CollectionModel<?> findRevisions(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable("backendId") String backendId, 
			Pageable pageable) {
		IdmTreeNodeDto treeNode = getDto(backendId);
		if (treeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("treeNode", backendId));
		}
		Page<IdmAuditDto> results = this.auditService.findRevisionsForEntity(IdmTreeNode.class.getSimpleName(), DtoUtils.toUuid(backendId), pageable);
		return toCollectionModel(results, IdmTreeNode.class);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_AUTOCOMPLETE + "')")
	@RequestMapping(value = "/search/roots", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search root tree nodes", 
			nickname = "searchRootTreeNodes", 
			tags = { IdmTreeNodeController.TAG },
			notes = "Tree type parameter can be used. If no tree type ios given, then configured default tree type is used."
					+ " If no default tree type is configured, then all roots are returnde")
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataTypeClass = String.class, paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataTypeClass = String.class, paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataTypeClass = String.class, paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public CollectionModel<?> findRoots(
			@ApiParam(value = "Tree type uuid identifier.", required = false)
			@RequestParam(value = "treeTypeId", required = false) String treeTypeId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		UUID treeTypeIdentifier = null;
		if (StringUtils.isNotEmpty(treeTypeId)) {
			treeTypeIdentifier = DtoUtils.toUuid(treeTypeId);
		} else {
			IdmTreeTypeDto defaultType = treeConfiguration.getDefaultType();
			if (defaultType != null) {
				treeTypeIdentifier = defaultType.getId();
			}
		}
		IdmTreeNodeFilter filter = toFilter(parameters);
		filter.setRoots(Boolean.TRUE);
		if (treeTypeIdentifier != null) {
			filter.setTreeTypeId(treeTypeIdentifier);
		}
		//
		Page<IdmTreeNodeDto> roots = find(filter, pageable, IdmBasePermission.AUTOCOMPLETE);
		return toCollectionModel(roots, IdmTreeNode.class);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_AUTOCOMPLETE + "')")
	@RequestMapping(value = "/search/children", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search sub tree nodes", 
			nickname = "searchChildrenTreeNodes", 
			tags = { IdmTreeNodeController.TAG },
			notes = "Finds direct chilren by given parent node uuid identifier. Set 'parent' parameter.")
	@ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataTypeClass = String.class, paramType = "query",
                value = "Results page you want to retrieve (0..N)"),
        @ApiImplicitParam(name = "size", dataTypeClass = String.class, paramType = "query",
                value = "Number of records per page."),
        @ApiImplicitParam(name = "sort", allowMultiple = true, dataTypeClass = String.class, paramType = "query",
                value = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.")
	})
	public CollectionModel<?> findChildren(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		IdmTreeNodeFilter filter = toFilter(parameters);
		filter.setRecursively(false);
		//
		return toCollectionModel(find(filter, pageable, IdmBasePermission.AUTOCOMPLETE), IdmTreeNode.class);
	}
	
	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Tree node extended attributes form definitions", 
			nickname = "getTreeNodeFormDefinitions", 
			tags = { IdmTreeNodeController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "")})
					})
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return formDefinitionController.getDefinitions(IdmTreeNode.class);
	}
	
	/**
	 * Returns filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@ApiOperation(
			value = "Tree node form definition - read values", 
			nickname = "getTreeNodeFormValues", 
			tags = { IdmTreeNodeController.TAG })
	public EntityModel<?> getFormValues(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode) {
		IdmTreeNodeDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmTreeNode.class, definitionCode);
		//
		return formDefinitionController.getFormValues(dto, formDefinition);
	}
	
	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH })
	@ApiOperation(
			value = "Tree node form definition - save values", 
			nickname = "postTreeNodeFormValues", 
			tags = { IdmTreeNodeController.TAG })
	public EntityModel<?> saveFormValues(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		IdmTreeNodeDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmTreeNode.class, definitionCode);
		//
		return formDefinitionController.saveFormValues(dto, formDefinition, formValues);
	}
	
	/**
	 * Save entity's form value
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 * @since 9.4.0
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-value", method = { RequestMethod.POST } )
	@ApiOperation(
			value = "TreeNode form definition - save value", 
			nickname = "postTreeNodeFormValue", 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.TREENODE_UPDATE, description = "") })
				})
	public EntityModel<?> saveFormValue(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid IdmFormValueDto formValue) {		
		IdmTreeNodeDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(dto, IdmBasePermission.UPDATE);
		//
		return formDefinitionController.saveFormValue(dto, formValue);
	}
	
	/**
	 * Returns input stream to attachment saved in given form value.
	 * 
	 * @param backendId
	 * @param formValueId
	 * @return
	 * @since 9.4.0
	 */
	@RequestMapping(value = "/{backendId}/form-values/{formValueId}/download", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Download form value attachment", 
			nickname = "downloadFormValue",
			tags = { IdmTreeNodeController.TAG },
			notes = "Returns input stream to attachment saved in given form value.",
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") })
					})
	public ResponseEntity<InputStreamResource> downloadFormValue(
			@ApiParam(value = "Node's uuid identifier.", required = true)
			@PathVariable String backendId,
			@ApiParam(value = "Form value identifier.", required = true)
			@PathVariable String formValueId) {
		IdmTreeNodeDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormValueDto value = formService.getValue(dto, DtoUtils.toUuid(formValueId));
		if (value == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, formValueId);
		}
		return formDefinitionController.downloadAttachment(value);
	}
	
	/**
	 * Returns input stream to attachment saved in given form value.
	 * 
	 * @param backendId
	 * @param formValueId
	 * @return
	 * @since 9.4.0
	 */
	@RequestMapping(value = "/{backendId}/form-values/{formValueId}/preview", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Download form value attachment preview", 
			nickname = "downloadFormValue",
			tags = { IdmTreeNodeController.TAG },
			notes = "Returns input stream to attachment preview saved in given form value. Preview is supported for the png, jpg and jpeg mime types only",
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") })
					})
	public ResponseEntity<InputStreamResource> previewFormValue(
			@ApiParam(value = "TreeNode's uuid identifier.", required = true)
			@PathVariable String backendId,
			@ApiParam(value = "Form value identifier.", required = true)
			@PathVariable String formValueId) {
		IdmTreeNodeDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormValueDto value = formService.getValue(dto, DtoUtils.toUuid(formValueId));
		if (value == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, formValueId);
		}
		return formDefinitionController.previewAttachment(value);
	}
	
	/**
	 * Returns default tree node or {@code null}, if no default tree node is defined
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/default", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_AUTOCOMPLETE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@ApiOperation(
			value = "Get default tree node detail", 
			nickname = "getDefaultTreeNode", 
			response = IdmTreeNodeDto.class, 
			tags = { IdmTreeNodeController.TAG }, 
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_AUTOCOMPLETE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_AUTOCOMPLETE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.TREENODE_READ, description = "") })
					})
	public ResponseEntity<?> getDefaultTreeNode() {
		IdmTreeNodeDto defaultTreeNode = treeNodeService.getDefaultTreeNode();
		if (defaultTreeNode == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", "default tree type"));
		}
		Set<String> permissions = getService().getPermissions(defaultTreeNode.getId());
		if (!PermissionUtils.hasAnyPermission(permissions, IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ)) {
			throw new ForbiddenEntityException(defaultTreeNode.getId(), IdmBasePermission.AUTOCOMPLETE, IdmBasePermission.READ);
		}
		return new ResponseEntity<>(toModel(defaultTreeNode), HttpStatus.OK);
	}
	
	@Override
	protected IdmTreeNodeFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmTreeNodeFilter filter = new IdmTreeNodeFilter(parameters, getParameterConverter());
		//
		// @deprecated @since 9.4.0 => will be removed finally in 10.x
		UUID legacyParentId = getParameterConverter().toUuid(parameters, "treeNodeId");
		if (legacyParentId != null && filter.getParent() == null) {
			filter.setParent(legacyParentId);
			filter.remove("treeNodeId");
		}
		//
		return filter;
	}	
}
