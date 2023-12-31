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
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = AccAccountController.TAG, description = "Account on target system")
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
	@Operation(
        summary = "Search accounts (/search/quick alias)",
        operationId = "searchAccounts"
    )
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Transactional(readOnly = true)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search accounts", 
			operationId = "searchQuickAccounts",
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
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
	@Operation(
			summary = "Autocomplete accounts (selectbox usage)", 
			operationId = "autocompleteAccounts",
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Account detail", 
			operationId = "getAccount",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccAccountDto.class
                                    )
                            )
                    }
            ),
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update account", 
			operationId = "postAccount",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccAccountDto.class
                                    )
                            )
                    }
            ),
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.ACCOUNT_CREATE,
						AccGroupPermission.ACCOUNT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNT_CREATE,
						AccGroupPermission.ACCOUNT_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull AccAccountDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update account",
			operationId = "putAccount",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccAccountDto.class
                                    )
                            )
                    }
            ),
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccAccountDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')")
	@Operation(
			summary = "Update account", 
			operationId = "patchAccount",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccAccountDto.class
                                    )
                            )
                    }
            ),
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete account", 
			operationId = "deleteAccount",
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Account's uuid identifier.", required = true)
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
	@Operation(
			summary = "What logged identity can do with given record", 
			operationId = "getPermissionsOnAccount",
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_READ })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-object", method = RequestMethod.GET)
	@Operation(
			summary = "Connector object for the account. Contains only attributes for witch have a schema attribute definitons.", 
			operationId = "getConnectorObject",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IcConnectorObject.class
                                    )
                            )
                    }
            ),
			tags = { SysSystemEntityController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<IcConnectorObject> getConnectorObject(
			 @Parameter(description = "Account's uuid identifier.", required = true)
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
	@Operation(
			summary = "Get available bulk actions", 
			operationId = "availableBulkAction",
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_READ })
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
	@Operation(
			summary = "Process bulk action for account", 
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
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.ACCOUNT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNT_READ})
        }
    )
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
	@Operation(
			summary = "Prevalidate bulk action for accounts", 
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
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.ACCOUNT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNT_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/preprocess", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@Operation(
			summary = "Preprocess bulk action for accounts", 
			operationId = "preprocessBulkAction",
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
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.ACCOUNT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNT_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> preprocessBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.preprocessBulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@Operation(
			summary = "Account extended attributes form definitions", 
			operationId = "getAccountFormDefinitions",
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.ACCOUNT_READ,
						CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNT_READ,
						CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE})
        }
    )
	public ResponseEntity<?> getFormDefinitions(
			 @Parameter(description = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getFormDefinitions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/form-values/prepare", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@Operation(
			summary = "Account form definition - prepare available values", 
			operationId = "prepareAccountFormValues",
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.ACCOUNT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNT_READ})
        }
    )
	public EntityModel<?> prepareFormValues(
			 @Parameter(description = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
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
	@Operation(
			summary = "Account form definition - read values", 
			operationId = "getAccountFormValues",
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.ACCOUNT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNT_READ})
        }
    )
	public EntityModel<?> getFormValues(
			 @Parameter(description = "Account's uuid identifier.", required = true)
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
	@Operation(
			summary = "Account form definition - save values", 
			operationId = "postAccountFormValues",
			tags = { AccAccountController.TAG }, 
			description = "Only given form attributes by the given values will be saved.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.ACCOUNT_UPDATE,
						CoreGroupPermission.FORM_VALUE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						AccGroupPermission.ACCOUNT_UPDATE,
						CoreGroupPermission.FORM_VALUE_UPDATE})
        }
    )
	public EntityModel<?> saveFormValues(
			 @Parameter(description = "Account uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			 @Parameter(description = "Filled form data.", required = true)
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
	@Operation(
			summary = "Get all supported account wizards",
			operationId = "getSupportedAccountWizards",
			tags = {AccAccountController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.ACCOUNT_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.ACCOUNT_READ})
        }
    )
	public CollectionModel<AccountWizardDto> getSupportedTypes() {
		List<AccountWizardsService> supportedTypes = accountWizardManager.getSupportedTypes();
		List<AccountWizardDto> accountWizardDtos = supportedTypes.stream().map(accountWizardsService -> accountWizardManager.convertTypeToDto(accountWizardsService)).collect(Collectors.toList());

		return new CollectionModel<>(
				accountWizardDtos.stream()
						.sorted(Comparator.comparing(AccountWizardDto::getOrder))
						.collect(Collectors.toList())
		);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/wizards/execute", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_UPDATE + "')")
	@Operation(
			summary = "Execute some wizard step.",
			operationId = "executeWizard",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccountWizardDto.class
                                    )
                            )
                    }
            ),
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.ACCOUNT_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.ACCOUNT_UPDATE})
        }
    )
	public ResponseEntity<AccountWizardDto> executeWizardType(@Valid @RequestBody AccountWizardDto wizardDto) {
		AccountWizardDto result = accountWizardManager.execute(wizardDto);
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/wizards/load", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@Operation(
			summary = "Load data for specific wizards -> open existed account in the wizard step.",
			operationId = "loadWizard",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = AccountWizardDto.class
                                    )
                            )
                    }
            ),
			tags = { AccAccountController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.ACCOUNT_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.ACCOUNT_READ})
        }
    )
	public ResponseEntity<AccountWizardDto> loadWizardType(@NotNull @Valid @RequestBody AccountWizardDto wizardDto) {
		// Returning error, because loading wizard is not supported, we are using wizard only for new accounts
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/incompatible-roles", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNT_READ + "')")
	@Operation(
			summary = "Incompatible roles assigned to account",
			operationId = "getAccountIncompatibleRoles",
			tags = { AccAccountController.TAG },
						description = "Incompatible roles are resolved from assigned account roles, which can logged used read.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.ACCOUNT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.ACCOUNT_READ })
    })
	public CollectionModel<?> getIncompatibleRoles(
			 @Parameter(description = "Account's uuid identifier.", required = true)
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
		return toCollectionModel(incompatibleRoles, ResolvedIncompatibleRoleDto.class);
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
