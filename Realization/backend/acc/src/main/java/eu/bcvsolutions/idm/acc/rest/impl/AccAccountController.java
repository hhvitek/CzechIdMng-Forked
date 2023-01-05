package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.AccountWizardDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccountWizardManager;
import eu.bcvsolutions.idm.acc.service.api.AccountWizardsService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResolvedIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.rest.WizardController;
import eu.bcvsolutions.idm.core.api.service.IdmIncompatibleRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.rest.impl.AbstractFormableDtoController;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 * @author Tomáš Doischer
 * @author Roman Kucera
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/accounts")
@Api(
		value = AccAccountController.TAG, 
		tags = AccAccountController.TAG, 
		description = "Account on target system",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccAccountController extends AbstractFormableDtoController<AccAccountDto, AccAccountFilter> implements WizardController<AccountWizardDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccAccountController.class);
	protected static final String TAG = "Accounts";
	//
	@Autowired private SysSystemEntityService systemEntityService;
	@Autowired private IdmFormDefinitionService formDefinitionService;
	@Autowired private AccountWizardManager accountWizardManager;
	@Autowired private IdmIncompatibleRoleService incompatibleRoleService;
	@Autowired private AccAccountRoleAssignmentService accountRoleAssignmentService;
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public AccAccountController(AccAccountService accountService,
			IdmFormDefinitionController formDefinitionController) {

		super(accountService);
		
		this.formDefinitionController = formDefinitionController;
	}

	@Transactional(readOnly = true)
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Search accounts (/search/quick alias)", 
			nickname = "searchAccounts",
			tags = { AccAccountController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search accounts", 
			nickname = "searchQuickAccounts",
			tags = { AccAccountController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Transactional(readOnly = true)
	@Override
	public Page<AccAccountDto> find(AccAccountFilter filter, Pageable pageable, BasePermission permission) {
		Page<AccAccountDto> dtos = super.find(filter, pageable, permission);
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		dtos.forEach(dto -> {
			loadEmbeddedEntity(loadedDtos, dto);
		});
		return dtos;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete accounts (selectbox usage)", 
			nickname = "autocompleteAccounts", 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Account detail", 
			nickname = "getAccount", 
			response = AccAccountDto.class, 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update account", 
			nickname = "postAccount", 
			response = AccAccountDto.class, 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull AccAccountDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update account",
			nickname = "putAccount", 
			response = AccAccountDto.class, 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccAccountDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')")
	@ApiOperation(
			value = "Update account", 
			nickname = "patchAccount", 
			response = AccAccountDto.class, 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete account", 
			nickname = "deleteAccount", 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	protected AccAccountDto validateDto(AccAccountDto dto) {
		dto = super.validateDto(dto);
		// preset entity type
		if (dto.getSystemEntity() != null) {
			SysSystemEntityDto systemEntity = systemEntityService.get(dto.getSystemEntity());
			dto.setEntityType(systemEntity.getEntityType());
		}
		if (!getService().isNew(dto)) {
			AccAccountDto previous = getDto(dto.getId());
			if(previous.isInProtection() && !dto.isInProtection()) {
				throw new ResultCodeException(AccResultCode.ACCOUNT_CANNOT_UPDATE_IS_PROTECTED, ImmutableMap.of("uid", dto.getUid()));
			}
		}
		return dto;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnAccount", 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-object", method = RequestMethod.GET)
	@ApiOperation(
			value = "Connector object for the account. Contains only attributes for witch have a schema attribute definitons.", 
			nickname = "getConnectorObject", 
			response = IcConnectorObject.class, 
			tags = { SysSystemEntityController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
					})
	public ResponseEntity<IcConnectorObject> getConnectorObject(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		AccAccountDto account = this.getDto(backendId);
		if(account == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IcConnectorObject connectorObject = ((AccAccountService)getService())
				.getConnectorObject(account, IdmBasePermission.READ);
		if(connectorObject == null) {
			return new ResponseEntity<IcConnectorObject>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<IcConnectorObject>(connectorObject, HttpStatus.OK);
	}
	
	/**
	 * Get available bulk actions for account
	 *
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	/**
	 * Process bulk action for accounts
	 *
	 * @param bulkAction
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Process bulk action for account", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	/**
	 * Prevalidate bulk action for accounts
	 *
	 * @param bulkAction
	 * @return
	 */
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for accounts", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/preprocess", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Preprocess bulk action for accounts", 
			nickname = "preprocessBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> preprocessBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.preprocessBulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Account extended attributes form definitions", 
			nickname = "getAccountFormDefinitions", 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE, description = "")})
				})
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getFormDefinitions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/form-values/prepare", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Account form definition - prepare available values", 
			nickname = "prepareAccountFormValues", 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")})
				})
	public Resource<?> prepareFormValues(
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, required = false) String definitionCode) {
		
		return super.prepareFormValues(definitionCode);
	}
	
	/**
	 * Returns filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Account form definition - read values", 
			nickname = "getAccountFormValues", 
			tags = { AccAccountController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")})
				})
	public Resource<?> getFormValues(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		AccAccountDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = getFormDefinitionForAccount(dto);
		if (formDefinition == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, String.format("No form definition found for %s.", backendId));
		}
		//
		return formDefinitionController.getFormValues(dto, formDefinition, IdmBasePermission.READ);
	}
	
	/**
	 * Save form values.
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')"
			+ "or hasAuthority('" + CoreGroupPermission.FORM_VALUE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH })
	@ApiOperation(
			value = "Account form definition - save values", 
			nickname = "postAccountFormValues", 
			tags = { AccAccountController.TAG }, 
			notes = "Only given form attributes by the given values will be saved.",
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.FORM_VALUE_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = ""),
						@AuthorizationScope(scope = CoreGroupPermission.FORM_VALUE_UPDATE, description = "")})
				})
	public Resource<?> saveFormValues(
			@ApiParam(value = "Account uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Filled form data.", required = true)
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		AccAccountDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = getFormDefinitionForAccount(dto);
		if (formDefinition == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, String.format("No form definition found for %s.", backendId));
		}
		//
		return formDefinitionController.saveFormValues(dto, formDefinition, formValues, IdmBasePermission.UPDATE);
	}
	
	@Override
	protected AccAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccAccountFilter filter = new AccAccountFilter(parameters);
		//
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setSystemEntityId(getParameterConverter().toUuid(parameters, "systemEntityId"));
		filter.setSystemMapping(getParameterConverter().toUuid(parameters, "systemMappingId"));
		//
		// for first check identityId, this attribute has bigger priority than identity parameter
		UUID identityId = getParameterConverter().toUuid(parameters, "identityId");
		if (identityId == null) {
			identityId = getParameterConverter().toEntityUuid(parameters, AccAccountFilter.PARAMETER_IDENTITY_ID, IdmIdentityDto.class);
		}
		filter.setIdentityId(identityId);
		//
		filter.setUid(getParameterConverter().toString(parameters, "uid"));
		filter.setAccountType(getParameterConverter().toEnum(parameters, "accountType", AccountType.class));
		filter.setOwnership(getParameterConverter().toBoolean(parameters, "ownership"));
		filter.setSupportChangePassword(getParameterConverter().toBoolean(parameters, "supportChangePassword"));
		filter.setIncludeEcho(getParameterConverter().toBoolean(parameters, "includeEcho"));
		filter.setEntityType(getParameterConverter().toString(parameters, "entityType"));
		filter.setInProtection(getParameterConverter().toBoolean(parameters, "inProtection"));
		// OR is supported only
		if (parameters.containsKey(AccAccountFilter.PARAMETER_ROLE_IDS)) {
			for (Object role : parameters.get(AccAccountFilter.PARAMETER_ROLE_IDS)) {
				if (role != null) {
					filter.getRoleIds().add(getParameterConverter().toEntityUuid((String) role, IdmRoleDto.class));
				}
			}
		}
		// OR is supported only
		if (parameters.containsKey(AccAccountFilter.PARAMETER_IDENTITY_IDS)) {
			for (Object identity : parameters.get(AccAccountFilter.PARAMETER_IDENTITY_IDS)) {
				if (identity != null) {
					filter.getIdentities().add(getParameterConverter().toEntityUuid((String) identity, IdmIdentityDto.class));
				}
			}
		}
		if (parameters.containsKey(AccAccountFilter.PARAMETER_SYSTEM_IDS)) {
			for (Object system : parameters.get(AccAccountFilter.PARAMETER_SYSTEM_IDS)) {
				if (system != null) {
					filter.getSystems().add(getParameterConverter().toEntityUuid((String) system, SysSystemDto.class));
				}
			}
		}
		//
		return filter;
	}

	/**
	 * Returns all registered account wizards.
	 *
	 * @return accounts wizards
	 */
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Get all supported account wizards",
			nickname = "getSupportedAccountWizards",
			tags = {AccAccountController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")})
			})
	public Resources<AccountWizardDto> getSupportedTypes() {
		List<AccountWizardsService> supportedTypes = accountWizardManager.getSupportedTypes();
		List<AccountWizardDto> accountWizardDtos = supportedTypes.stream().map(accountWizardsService -> accountWizardManager.convertTypeToDto(accountWizardsService)).collect(Collectors.toList());

		return new Resources<>(
				accountWizardDtos.stream()
						.sorted(Comparator.comparing(AccountWizardDto::getOrder))
						.collect(Collectors.toList())
		);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/wizards/execute", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')")
	@ApiOperation(
			value = "Execute some wizard step.",
			nickname = "executeWizard",
			response = AccountWizardDto.class,
			tags = { AccAccountController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_UPDATE, description = "")})
			})
	public ResponseEntity<AccountWizardDto> executeWizardType(@Valid @RequestBody AccountWizardDto wizardDto) {
		AccountWizardDto result = accountWizardManager.execute(wizardDto);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/wizards/load", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Load data for specific wizards -> open existed account in the wizard step.",
			nickname = "loadWizard",
			response = AccountWizardDto.class,
			tags = { AccAccountController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "")})
			})
	public ResponseEntity<AccountWizardDto> loadWizardType(@NotNull @Valid @RequestBody AccountWizardDto wizardDto) {
		// Returning error, because loading wizard is not supported, we are using wizard only for new accounts
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/incompatible-roles", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@ApiOperation(
			value = "Incompatible roles assigned to account",
			nickname = "getAccountIncompatibleRoles",
			tags = { AccAccountController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.ACCOUNT_READ, description = "") })
			},
			notes = "Incompatible roles are resolved from assigned account roles, which can logged used read.")
	public Resources<?> getIncompatibleRoles(
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable String backendId) {
		AccAccountDto accountDto = getDto(backendId);
		if (accountDto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		AccAccountRoleAssignmentFilter filter = new AccAccountRoleAssignmentFilter();
		filter.setAccountId(accountDto.getId());
		List<AccAccountRoleAssignmentDto> accountRoles = accountRoleAssignmentService.find(filter, null, IdmBasePermission.READ).getContent();
		//
		Set<ResolvedIncompatibleRoleDto> incompatibleRoles = incompatibleRoleService.resolveIncompatibleRoles(
				accountRoles
						.stream()
						.map(ar -> {
							IdmRoleDto role = DtoUtils.getEmbedded(ar, IdmIdentityRole_.role);
							//
							return role;
						})
						.collect(Collectors.toList())
		);
		//
		return toResources(incompatibleRoles, ResolvedIncompatibleRoleDto.class);
	}

	/**
	 * Fills referenced entity to dto - prevent to load entity for each row
	 * 
	 * @param dto
	 */
	private void loadEmbeddedEntity(Map<UUID, BaseDto> loadedDtos, AccAccountDto dto) {
		UUID entityId = dto.getTargetEntityId();
		if (entityId == null || StringUtils.isEmpty(dto.getTargetEntityType())) {
			return; // IdM entity is not linked to account 
		}
		try {
			if (!loadedDtos.containsKey(entityId)) {
				loadedDtos.put(entityId, getLookupService().lookupDto(dto.getTargetEntityType(), entityId));
			}
			dto.getEmbedded().put("targetEntityId", loadedDtos.get(entityId));
		} catch (IllegalArgumentException ex) {
			LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", dto.getTargetEntityType(), ex);
		}
	}
	
	private IdmFormDefinitionDto getFormDefinitionForAccount(AccAccountDto account) {
		if (account.getFormDefinition() != null) {
			return formDefinitionService.get(account.getFormDefinition());
		}
		
		return null;
	}
}
