package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.rest.AbstractRequestDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Request for roles
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/requests")
@Api(
		value = IdmRequestRoleController.TAG, 
		tags = IdmRequestRoleController.TAG, 
		description = "Requests for - Operations with request of roles",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRequestRoleController extends AbstractRequestDtoController<IdmRoleDto, IdmRoleFilter> {
	
	protected static final String TAG = "Request roles";
	protected static final String REQUEST_SUB_PATH = "/roles";
	
	private final IdmFormDefinitionController formDefinitionController;
	//
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private IdmIncompatibleRoleService incompatibleRoleService;
	
	@Autowired
	public IdmRequestRoleController(
			IdmRoleService roleService,
			IdmFormDefinitionController formDefinitionController
	) {
		super(roleService);
		//
		Assert.notNull(formDefinitionController, "Controller is required.");
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	public String getRequestSubPath(){
		return REQUEST_SUB_PATH;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}" + REQUEST_SUB_PATH, method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Search roles (/search/quick alias)", //
			nickname = "searchRoles", //
			tags = { IdmRequestRoleController.TAG }, //
			authorizations = { //
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { //
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }), //
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { //
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }) //
				})
	public CollectionModel<?> find( //
			@PathVariable @NotNull String requestId, //
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, //
			@PageableDefault Pageable pageable) { //
		return super.find(requestId, parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/{requestId}" + REQUEST_SUB_PATH+"/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation( //
			value = "Search roles", //
			nickname = "searchQuickRoles", //
			tags = { IdmRequestRoleController.TAG }, //
			authorizations = { //
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { //
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }), //
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { //
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }) //
				}) 
	public CollectionModel<?> findQuick( //
			@PathVariable @NotNull String requestId, //
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, //
			@PageableDefault Pageable pageable) { //
		return super.find(requestId, parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/{requestId}" + REQUEST_SUB_PATH+"/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_AUTOCOMPLETE + "')")
	@ApiOperation( //
			value = "Autocomplete roles (selectbox usage)", //
			nickname = "autocompleteRoles", //
			tags = { IdmRequestRoleController.TAG }, // 
			authorizations = { //
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { //
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_AUTOCOMPLETE, description = "") }), //
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { //
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_AUTOCOMPLETE, description = "") }) //
				}) //
	public CollectionModel<?> autocomplete( //
			@PathVariable @NotNull String requestId, //
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, //
			@PageableDefault Pageable pageable) { //
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH+"/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Role detail", 
			nickname = "getRole", 
			response = IdmRoleDto.class, 
			tags = { IdmRequestRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(requestId, backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@ApiOperation(
			value = "Create / update role", 
			nickname = "postRequestRole", 
			response = IdmRoleDto.class, 
			tags = { IdmRequestRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_CREATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@PathVariable @NotNull String requestId, @Valid @RequestBody IdmRoleDto dto) {
		return super.post(requestId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH+"/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@ApiOperation(
			value = "Update role",
			nickname = "putRequestRole", 
			response = IdmRole.class, 
			tags = { IdmRequestRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String requestId,
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmRoleDto dto) {
		return super.put(requestId, backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH+"/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_DELETE + "')")
	@ApiOperation(
			value = "Create delete request for role", 
			nickname = "deleteRequestRole", 
			tags = { IdmRequestRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(requestId, backendId);
	}
	
	// TODO permissions (from original controller???)
	@Override
	@ResponseBody
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnRole", 
			tags = { IdmRequestRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public Set<String> getPermissions(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(requestId, backendId);
	}

	
	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Role extended attributes form definitions", 
			nickname = "getRoleFormDefinitions", 
			tags = { IdmRequestRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return formDefinitionController.getDefinitions(IdmRole.class);
	}
	
	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Role form definition - read values", 
			nickname = "getRoleFormValues", 
			tags = { IdmRequestRoleController.TAG }, 
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				})
	public EntityModel<?> getFormValues(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode) {
		IdmRoleDto dto = getDto(requestId, backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmRole.class, definitionCode);
		//
		return this.getFormValues(requestId, dto, formDefinition);
	}
	
	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_UPDATE + "')")
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH } )
	@ApiOperation(
			value = "Role form definition - save values", 
			nickname = "postRoleFormValues", 
			tags = { IdmRequestRoleController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") })
				})
	public EntityModel<?> saveFormValues(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		IdmRoleDto dto = getDto(requestId, backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(dto, IdmBasePermission.UPDATE);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmRole.class, definitionCode);
		//
		return super.saveFormValues(requestId, dto, formDefinition, formValues);
	}
	
	
	@Override
	@ResponseBody
	@RequestMapping(value = REQUEST_SUB_PATH, method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_UPDATE + "')")
	@ApiOperation( //
			value = "Create request for role", //
			nickname = "createRequestForRole", //
			response = IdmRequestDto.class, //
			tags = { IdmRequestRoleController.TAG }, //
			authorizations = { //
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { //
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_CREATE, description = ""), //
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }), //
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { //
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_CREATE, description = ""), //
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_UPDATE, description = "") }) //
			}) //
	public ResponseEntity<?> createRequest(@RequestBody IdmRoleDto dto) {
		return super.createRequest(dto);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{requestId}" + REQUEST_SUB_PATH + "/{backendId}/incompatible-roles", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Incompatible roles from sub roles and the current request", 
			nickname = "getRequestRoleIncompatibleRoles", 
			tags = { IdmIdentityController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "") })
				},
			notes = "Incompatible roles from sub roles and the current request.")
	public CollectionModel<?> getIncompatibleRoles(
			@PathVariable @NotNull String requestId,
			@ApiParam(value = "Roles's uuid identifier or code.", required = true)
			@PathVariable String backendId) {	
		IdmRoleDto role = getDto(backendId);
		if (role == null) {
			if (requestId == null) {
				// If role was not found, then exception is throw only if request mode is not use (requestId is null).
				// We are not able compute incompatible roles for not approving role.
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
			} else {
				return toCollectionModel(Sets.newLinkedHashSet(), ResolvedIncompatibleRoleDto.class);
			}
		}
		//
		// find all sub role composition
		List<IdmRoleCompositionDto> subRoles = roleCompositionService.findAllSubRoles(role.getId(), IdmBasePermission.READ);
		// extract all sub roles - role above is included thx to composition
		Set<IdmRoleDto> distinctRoles = roleCompositionService.resolveDistinctRoles(subRoles);
		// resolve incompatible roles defined by business role
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = incompatibleRoleService.resolveIncompatibleRoles(Lists.newArrayList(distinctRoles));
		//
		return toCollectionModel(incompatibleRoles, ResolvedIncompatibleRoleDto.class);
	}

	@Override
	protected IdmRoleFilter toFilter(MultiValueMap<String, Object> parameters) {
		// TODO: Call to filter from original controller -> make as public?
		IdmRoleFilter filter = new IdmRoleFilter(parameters);
		filter.setRoleType(getParameterConverter().toEnum(parameters, "roleType", RoleType.class));
		filter.setRoleCatalogueId(getParameterConverter().toUuid(parameters, "roleCatalogue"));
		filter.setGuaranteeId(getParameterConverter().toEntityUuid(parameters, "guarantee", IdmIdentity.class));
		return filter;
	}
}
