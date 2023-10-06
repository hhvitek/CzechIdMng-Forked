package eu.bcvsolutions.idm.acc.rest.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment_;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
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

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Account role assignment controller
 * - read only for now: @Enabled by some public configuration property
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/" + AccModuleDescriptor.MODULE_ID + "/account-role-assignments")
@Tag(
		name = AccAccountRoleAssignmentController.TAG,
		description = "Operations with account role assignments"//,
		
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class AccAccountRoleAssignmentController extends AbstractReadWriteDtoController<AccAccountRoleAssignmentDto, AccAccountRoleAssignmentFilter> {

	protected static final String TAG = "Account role assignments";
	//
	@Autowired private IdmFormDefinitionController formDefinitionController;
	@Autowired private IdmRoleService roleService;
	@Autowired private FormService formService;

	@Autowired
	public AccAccountRoleAssignmentController(AccAccountRoleAssignmentService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@Operation(
			summary = "Search account role assignments (/search/quick alias)",
			/* nickname = "searchAccountRoleAssignments", */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@Operation(
			summary = "Search account role assignment",
			/* nickname = "searchQuickAccountRoleAssignments", */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete account role assignments (selectbox usage)",
			/* nickname = "autocompleteAccountRoleAssignments", */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/can-be-requested", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_CANBEREQUESTED + "')")
	@Operation(
			summary = "Find assigned roles, which can be requested",
			/* nickname = "findCanBeRequestedAccountRoleAssignments", */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_CANBEREQUESTED }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_CANBEREQUESTED })
        }
    )
	public CollectionModel<?> findCanBeRequested(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return toCollectionModel(find(toFilter(parameters), pageable, RoleBasePermission.CANBEREQUESTED), getDtoClass());
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			/* nickname = "countAccountRoleAssignments", */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@Operation(
			summary = "Account role assignment detail",
			/* nickname = "getAccountRoleAssignment", */
			/* response = AccAccountRoleAssignmentDto.class, */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Account role assignment's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	//@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			/* nickname = "getPermissionsOnAccountRoleAssignment", */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Account role assignment's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	
	/**
	 * Returns form definition to given role.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@Operation(
			summary = "Account role assignment extended attributes form definitions",
			/* nickname = "getAccountRoleAssignmentFormDefinitions", */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ })
        }
    )
	public ResponseEntity<?> getFormDefinitions(
			@Parameter(name = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		
		AccAccountRoleAssignmentDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		// Search definition by definition in role
		IdmRoleDto roleDto = DtoUtils.getEmbedded(dto, AbstractRoleAssignment_.role, IdmRoleDto.class);
		if (roleDto != null && roleDto.getIdentityRoleAttributeDefinition() != null) {
			IdmFormDefinitionDto definition = roleService.getFormAttributeSubdefinition(roleDto);
			return formDefinitionController.toCollectionModel(Lists.newArrayList(definition));
		}
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	/**
	 * Returns entity's filled form values
	 * 
	 * In this case is code of definition ignored, we will load only definition by given role and sub-definition.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@Operation(
			summary = "Account role assignment form definition - read values",
			/* nickname = "getAccountRoleFormValues", */
			tags = { AccAccountRoleAssignmentController.TAG })
    @SecurityRequirements(
        value = {
 
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ })
        }
    )
	public EntityModel<?> getFormValues(
			@Parameter(name = "Account role assignment's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			@Parameter(name = "Code of form definition (default will be used if no code is given)."
					+ " In this case is code of definition ignored, we will load only definition by given role and sub-definition.",
					required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode) {
		AccAccountRoleAssignmentDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmRoleDto roleDto = DtoUtils.getEmbedded(dto, AbstractRoleAssignment_.role, IdmRoleDto.class);
		IdmFormDefinitionDto definition = roleService.getFormAttributeSubdefinition(roleDto);
		//
		return new EntityModel<>(formService.getFormInstance(dto, definition));
	}
	
	@Override
	protected AccAccountRoleAssignmentFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccAccountRoleAssignmentFilter filter = new  AccAccountRoleAssignmentFilter(parameters);
		// TODO: resolve codeable parameters automatically ...
		filter.setIdentityId(getParameterConverter().toEntityUuid(parameters, BaseRoleAssignmentFilter.PARAMETER_IDENTITY_ID, IdmIdentityDto.class));
		filter.setRoleId(getParameterConverter().toEntityUuid(parameters, BaseRoleAssignmentFilter.PARAMETER_ROLE_ID, IdmRoleDto.class));
		//
		return filter;
	}
}
