package eu.bcvsolutions.idm.vs.rest.impl;

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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.vs.domain.VirtualSystemGroupPermission;
import eu.bcvsolutions.idm.vs.dto.VsAccountDto;
import eu.bcvsolutions.idm.vs.dto.filter.VsAccountFilter;
import eu.bcvsolutions.idm.vs.entity.VsAccount;
import eu.bcvsolutions.idm.vs.service.api.VsAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Rest methods for virtual system account
 * 
 * @author Svanda
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/vs/accounts")
@Tag(
		name = VsAccountController.TAG,
		 
		description = "Operations with accounts (in virtual system)"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE

//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class VsAccountController extends AbstractReadWriteDtoController<VsAccountDto, VsAccountFilter> {

	protected static final String TAG = "Accounts";
	//
	private final IdmFormDefinitionController formDefinitionController;
	
	@Autowired
	public VsAccountController(
			VsAccountService service, 
			IdmFormDefinitionController formDefinitionController) {
		super(service);
		//
		Assert.notNull(formDefinitionController, "Controller is required.");
		//
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@Operation(
			summary = "Search accounts (/search/quick alias)", 
			/* nickname = "searchAccounts", */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ })
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
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@Operation(
			summary = "Search accounts", 
			/* nickname = "searchQuickAccounts", */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ })
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
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete accounts (selectbox usage)", 
			/* nickname = "autocompleteAccounts", */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@Operation(
			summary = "Account detail", 
			/* nickname = "getAccount", */
			/* response = VsAccountDto.class, */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_CREATE + "') or hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE + "')")
	@Operation(
			summary = "Create / update account", 
			/* nickname = "postAccount", */
			/* response = VsAccountDto.class, */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_CREATE,
						VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_CREATE,
						VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody VsAccountDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE + "')")
	@Operation(
			summary = "Update account", 
			/* nickname = "putAccount", */
			/* response = VsAccountDto.class, */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody VsAccountDto dto) {
		return super.put(backendId, dto);
	}
	

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_DELETE + "')")
	@Operation(
			summary = "Delete account", 
			/* nickname = "deleteAccount", */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')"
			+ " or hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged account can do with given record", 
			/* nickname = "getPermissionsOnAccount", */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ,
						VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ,
						VirtualSystemGroupPermission.VS_ACCOUNT_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Returns form definition to given account.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@Operation(
			summary = "Account extended attributes form definitions", 
			/* nickname = "getAccountFormDefinitions", */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ })
        }
    )
	public ResponseEntity<?> getFormDefinitions(
			@Parameter(name = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return formDefinitionController.getDefinitions(VsAccount.class);
	}
	
	/**
	 * Returns filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_READ + "')")
	@Operation(
			summary = "Account form definition - read values", 
			/* nickname = "getAccountFormValues", */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_READ })
        }
    )
	public EntityModel<?> getFormValues(
			@Parameter(name = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Parameter(name = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode) {
		VsAccountDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(VsAccount.class, definitionCode);
		//
		return formDefinitionController.getFormValues(entity, formDefinition);
	}
	
	/**
	 * Saves connector configuration form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.POST)
	@Operation(
			summary = "Account form definition - save values", 
			/* nickname = "postAccountFormValues", */
			tags = { VsAccountController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						VirtualSystemGroupPermission.VS_ACCOUNT_UPDATE })
        }
    )
	public EntityModel<?> saveFormValues(
			@Parameter(name = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@Parameter(name = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			@Parameter(name = "Filled form data.", required = true)
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		VsAccountDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(entity, IdmBasePermission.UPDATE);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(VsAccount.class, definitionCode);
		//
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues);
	}
	
	@Override
	protected VsAccountFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new VsAccountFilter();
	}
}
