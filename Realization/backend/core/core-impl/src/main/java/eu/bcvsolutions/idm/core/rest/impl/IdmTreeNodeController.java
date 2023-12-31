package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Tree nodes endpoint.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + BaseDtoController.TREE_BASE_PATH + "-nodes")
@Tag(name = IdmTreeNodeController.TAG, description = "Operation with tree nodes")
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
	@Operation(
			summary = "Search tree nodes (/search/quick alias)",
			operationId = "searchTreeNodes",
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@Operation(
			summary = "Search tree nodes",
			operationId = "searchQuickTreeNodes", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete tree nodes (selectbox usage)",
			operationId = "autocompleteTreeNodes", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countTreeNodes", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@Operation(
			summary = "Tree node detail",
			operationId = "getTreeNode", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmTreeNodeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@Operation(
			summary = "Create / update tree node",
			operationId = "postTreeNode", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmTreeNodeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.TREENODE_CREATE,
						CoreGroupPermission.TREENODE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.TREENODE_CREATE,
						CoreGroupPermission.TREENODE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmTreeNodeDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@Operation(
			summary = "Update tree node",
			operationId = "putTreeNode", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmTreeNodeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmTreeNodeDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_UPDATE + "')")
	@Operation(
			summary = "Update tree node",
			operationId = "patchTreeNode", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmTreeNodeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_DELETE + "')")
	@Operation(
			summary = "Delete tree node",
			operationId = "deleteTreeNode", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnTreeNode", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			operationId = "availableBulkAction", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_READ })
    })
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@Operation(
			summary = "Process bulk action for tree nodes",
			operationId = "bulkAction", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.TREENODE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.TREENODE_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.TREENODE_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for tree nodes",
			operationId = "prevalidateBulkAction", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.TREENODE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.TREENODE_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(value = "{backendId}/revisions/{revId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@Operation(
			summary = "Tree node audit - read revision detail",
			operationId = "getTreeNodeRevision", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
	public ResponseEntity<?> findRevision(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable("backendId") String backendId, 
			 @Parameter(description = "Revision identifier.", required = true)
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
	@Operation(
			summary = "Tree node audit - read all revisions",
			operationId = "getTreeNodeRevisions", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.AUDIT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.AUDIT_READ })
    })
	public CollectionModel<?> findRevisions(
			 @Parameter(description = "Node's uuid identifier.", required = true)
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
	@Operation(
			summary = "Search root tree nodes",
			operationId = "searchRootTreeNodes", 
			tags = { IdmTreeNodeController.TAG },
			description = "Tree type parameter can be used. If no tree type ios given, then configured default tree type is used."
					+ " If no default tree type is configured, then all roots are returnde")
	@PageableAsQueryParam
	public CollectionModel<?> findRoots(
			 @Parameter(description = "Tree type uuid identifier.", required = false)
			@RequestParam(value = "treeTypeId", required = false) String treeTypeId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
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
	@Operation(
			summary = "Search sub tree nodes",
			operationId = "searchChildrenTreeNodes", 
			tags = { IdmTreeNodeController.TAG },
			description = "Finds direct chilren by given parent node uuid identifier. Set 'parent' parameter.")
	@PageableAsQueryParam
	public CollectionModel<?> findChildren(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
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
	@Operation(
			summary = "Tree node extended attributes form definitions",
			operationId = "getTreeNodeFormDefinitions", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.TREENODE_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.TREENODE_READ})
        }
    )
	public ResponseEntity<?> getFormDefinitions(
			 @Parameter(description = "Node's uuid identifier.", required = true)
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
	@Operation(
			summary = "Tree node form definition - read values",
			operationId = "getTreeNodeFormValues", 
			tags = { IdmTreeNodeController.TAG })
	public EntityModel<?> getFormValues(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			 @Parameter(description = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
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
	@Operation(
			summary = "Tree node form definition - save values",
			operationId = "postTreeNodeFormValues", 
			tags = { IdmTreeNodeController.TAG })
	public EntityModel<?> saveFormValues(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			 @Parameter(description = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
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
	@Operation(
			summary = "TreeNode form definition - save value",
			operationId = "postTreeNodeFormValue", 
			tags = { IdmTreeNodeController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_UPDATE })
    })
	public EntityModel<?> saveFormValue(
			 @Parameter(description = "Node's uuid identifier.", required = true)
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
	@Operation(
			summary = "Download form value attachment",
			operationId = "downloadFormValue",
			tags = { IdmTreeNodeController.TAG },
			description = "Returns input stream to attachment saved in given form value.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_READ })
    })
	public ResponseEntity<InputStreamResource> downloadFormValue(
			 @Parameter(description = "Node's uuid identifier.", required = true)
			@PathVariable String backendId,
			 @Parameter(description = "Form value identifier.", required = true)
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
	@Operation(
			summary = "Download form value attachment preview",
			operationId = "downloadFormValue",
			tags = { IdmTreeNodeController.TAG },
			description = "Returns input stream to attachment preview saved in given form value. Preview is supported for the png, jpg and jpeg mime types only")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.TREENODE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.TREENODE_READ })
    })
	public ResponseEntity<InputStreamResource> previewFormValue(
			 @Parameter(description = "TreeNode's uuid identifier.", required = true)
			@PathVariable String backendId,
			 @Parameter(description = "Form value identifier.", required = true)
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
	@Operation(
			summary = "Get default tree node detail",
			operationId = "getDefaultTreeNode", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmTreeNodeDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmTreeNodeController.TAG }
			)
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.TREENODE_AUTOCOMPLETE,
							CoreGroupPermission.TREENODE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.TREENODE_AUTOCOMPLETE,
							CoreGroupPermission.TREENODE_READ })
        }
    )
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
