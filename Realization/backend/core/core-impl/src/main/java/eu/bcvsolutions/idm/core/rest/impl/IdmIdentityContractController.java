package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.AbstractFormableDtoController;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Identity contract endpoint
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/identity-contracts")
@Tag(
		name = IdmIdentityContractController.TAG, 
		description = "Operations with identity contracts"//, 
		 
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmIdentityContractController extends AbstractFormableDtoController<IdmIdentityContractDto, IdmIdentityContractFilter> {
	
	protected static final String TAG = "Contracts";
	private final IdmIdentityContractService contractService;
	private final IdmFormDefinitionController formDefinitionController;
	//
	@Autowired private FormService formService;
	
	@Autowired
	public IdmIdentityContractController(
			LookupService entityLookupService, 
			IdmIdentityContractService identityContractService,
			IdmFormDefinitionController formDefinitionController) {
		super(identityContractService);
		//
		Assert.notNull(formDefinitionController, "Controller is required.");
		//
		this.contractService = identityContractService;
		this.formDefinitionController = formDefinitionController;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Search identity contracts (/search/quick alias)",
			/* nickname = "searchIdentityContracts", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Search identity contracts",
			/* nickname = "searchQuickIdentityContracts", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete identity contracts (selectbox usage)",
			/* nickname = "autocompleteIdentityContracts", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	/**
	 * @since 11.1.0 - apply default sort by prime contract for identity contracts
	 */
	@Override
	public Page<IdmIdentityContractDto> find(IdmIdentityContractFilter filter, Pageable pageable,
			BasePermission permission) {
		Page<IdmIdentityContractDto> results = super.find(filter, pageable, permission);
		if (results.getTotalElements() == 0 // contracts not found
				|| (filter != null && filter.getIdentity() == null) // identity is not set
				|| pageable.getSort().isSorted() // other sort is given
				|| results.getTotalElements() > pageable.getPageSize()) {// pageable is not set properly
			//  => order will not be solved
			return results;
		}
		//
		// apply default sort by prime contract for identity contracts
		List<IdmIdentityContractDto> contracts = new ArrayList<>(results.getContent());
		contractService.sortByPrimeContract(contracts);
		//
		return new PageImpl<>(contracts, pageable, contracts.size());
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			/* nickname = "countIdentityContracts", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Identity contract detail",
			/* nickname = "getIdentityContract", */
			/* response = IdmIdentityContractDto.class, */ 
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	@Operation(
			summary = "Create / update identity contract",
			/* nickname = "postIdentityContract", */
			/* response = IdmIdentityContractDto.class, */ 
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITYCONTRACT_CREATE,
						CoreGroupPermission.IDENTITYCONTRACT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITYCONTRACT_CREATE,
						CoreGroupPermission.IDENTITYCONTRACT_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmIdentityContractDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')")
	@Operation(
			summary = "Update identity contract",
			/* nickname = "putIdentityContract", */
			/* response = IdmIdentityContractDto.class, */ 
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmIdentityContractDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_DELETE + "')")
	@Operation(
			summary = "Delete identity contract",
			/* nickname = "deleteIdentityContract", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			/* nickname = "getPermissionsOnIdentityContract", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			/* nickname = "availableBulkAction", */
			tags = { IdmProfileController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Process bulk action for contract",
			/* nickname = "bulkAction", */
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmProfileController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for profiles",
			/* nickname = "prevalidateBulkAction", */
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmProfileController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Identity contract extended attributes form definitions",
			/* nickname = "getIdentityContractFormDefinitions", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public ResponseEntity<?> getFormDefinitions(
			@Parameter(name = "Contract's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getFormDefinitions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/form-values/prepare", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Identity contract form definition - prepare available values",
			/* nickname = "prepareIdentityContractFormValues", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ})
        }
    )
	public EntityModel<?> prepareFormValues(
			@Parameter(name = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = IdmFormAttributeFilter.PARAMETER_FORM_DEFINITION_CODE, required = false) String definitionCode) {
		return super.prepareFormValues(definitionCode);
	}
	
	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Identity contract form definition - read values",
			/* nickname = "getIdentityContractFormValues", */
			tags = { IdmIdentityContractController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public EntityModel<?> getFormValues(
			@Parameter(name = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Parameter(name = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode) {
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(
				IdmIdentityContract.class,
				definitionCode,
				IdmBasePermission.AUTOCOMPLETE);
		//
		IdmFormInstanceDto formInstance = formService.getFormInstance(dto, formDefinition, IdmBasePermission.READ);
		//
		// If is contract controlled by slice, then we make all
		// attributes in main definition readOnly!
		if (formInstance.getFormDefinition().isMain() && dto.getControlledBySlices()) {
			formInstance.getFormDefinition() //
					.getFormAttributes() //
					.stream() //
					.forEach(attribute -> {
						attribute.setReadonly(true);
					});
		}
		//
		return new EntityModel<>(formInstance);
	}
	
	/**
	 * Saves entity form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')"
			+ "or hasAuthority('" + CoreGroupPermission.FORM_VALUE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH })
	@Operation(
			summary = "Identity contract form definition - save values",
			/* nickname = "postIdentityContractFormValues", */
			tags = { IdmIdentityContractController.TAG }
			)
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITYCONTRACT_UPDATE,
						CoreGroupPermission.FORM_VALUE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITYCONTRACT_UPDATE,
						CoreGroupPermission.FORM_VALUE_UPDATE })
        }
    )
	public EntityModel<?> saveFormValues(
			@Parameter(name = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			@Parameter(name = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode,
			@Parameter(name = "Filled form data.", required = true)
			@RequestBody @Valid List<IdmFormValueDto> formValues) {		
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(
				IdmIdentityContract.class,
				definitionCode,
				IdmBasePermission.AUTOCOMPLETE);
		//
		return formDefinitionController.saveFormValues(dto, formDefinition, formValues, IdmBasePermission.UPDATE);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_UPDATE + "')"
			+ "or hasAuthority('" + CoreGroupPermission.FORM_VALUE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-value", method = { RequestMethod.POST } )
	@Operation(
			summary = "Contract form definition - save value",
			/* nickname = "postIdentityContractFormValue", */
			tags = { IdmIdentityContractController.TAG }
			)
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.IDENTITYCONTRACT_UPDATE,
						CoreGroupPermission.FORM_VALUE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.IDENTITYCONTRACT_UPDATE,
						CoreGroupPermission.FORM_VALUE_UPDATE })
        }
    )
	public EntityModel<?> saveFormValue(
			@Parameter(name = "Contract's uuid identifier .", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid IdmFormValueDto formValue) {		
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		return formDefinitionController.saveFormValue(dto, formValue, IdmBasePermission.UPDATE);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Download form value attachment",
			/* nickname = "downloadFormValue", */
			tags = { IdmIdentityContractController.TAG },
			description = "Returns input stream to attachment saved in given form value.")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.IDENTITYCONTRACT_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public ResponseEntity<InputStreamResource> downloadFormValue(
			@Parameter(name = "Contract's uuid identifier .", required = true)
			@PathVariable String backendId,
			@Parameter(name = "Form value identifier.", required = true)
			@PathVariable String formValueId) {
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormValueDto value = formService.getValue(dto, DtoUtils.toUuid(formValueId), IdmBasePermission.READ);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITYCONTRACT_READ + "')")
	@Operation(
			summary = "Download form value attachment preview",
			/* nickname = "downloadFormValue", */
			tags = { IdmIdentityContractController.TAG },
			description = "Returns input stream to attachment preview saved in given form value. Preview is supported for the png, jpg and jpeg mime types only")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.IDENTITYCONTRACT_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.IDENTITYCONTRACT_READ })
        }
    )
	public ResponseEntity<InputStreamResource> previewFormValue(
			@Parameter(name = "Contract's uuid identifier.", required = true)
			@PathVariable String backendId,
			@Parameter(name = "Form value identifier.", required = true)
			@PathVariable String formValueId) {
		IdmIdentityContractDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormValueDto value = formService.getValue(dto, DtoUtils.toUuid(formValueId), IdmBasePermission.READ);
		if (value == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, formValueId);
		}
		return formDefinitionController.previewAttachment(value);
	}
	
	@Override
	protected IdmIdentityContractFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter(parameters, getParameterConverter());
		// to entity decorator
		filter.setIdentity(getParameterConverter().toEntityUuid(parameters, IdmIdentityContractFilter.PARAMETER_IDENTITY, IdmIdentityDto.class));
		filter.setSubordinatesFor(getParameterConverter().toEntityUuid(parameters, IdmIdentityContractFilter.PARAMETER_SUBORDINATES_FOR, IdmIdentityDto.class));
		//
		return filter;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @since 11.0.0
	 */
	@Override
	protected IdmIdentityContractFilter getContext(MultiValueMap<String, Object> parameters) {
		IdmIdentityContractFilter context = new IdmIdentityContractFilter(parameters, getParameterConverter());
		// load basic fields form definition
		context.setAddBasicFields(true);
		//
		return context;
	}
}
