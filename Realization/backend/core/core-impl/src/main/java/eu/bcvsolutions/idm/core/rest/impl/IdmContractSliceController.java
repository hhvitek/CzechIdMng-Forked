package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
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
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Contract time slice controller
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/contract-slices")
@Tag(name = IdmContractSliceController.TAG, description = "Operations with contract slices"
//, //produces = BaseController.APPLICATION_HAL_JSON_VALUE
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmContractSliceController
		extends AbstractEventableDtoController<IdmContractSliceDto, IdmContractSliceFilter> {

	protected static final String TAG = "Contract slice";
	private final IdmFormDefinitionController formDefinitionController;
	@Autowired
	private FormService formService;

	@Autowired
	public IdmContractSliceController(LookupService entityLookupService,
			IdmContractSliceService identityContractService, IdmFormDefinitionController formDefinitionController) {
		super(identityContractService);
		//
		Assert.notNull(formDefinitionController, "Controller is required.");
		//
		this.formDefinitionController = formDefinitionController;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@Operation(summary = "Search contract slices (/search/quick alias)", /* nickname = "searchIdentityContracts", */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ })
        }
    )
	public CollectionModel<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@Operation(summary = "Search contract slices", /* nickname = "searchQuickIdentityContracts", */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ })
        }
    )
	public CollectionModel<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_AUTOCOMPLETE + "')")
	@Operation(summary = "Autocomplete contract slices (selectbox usage)", /* nickname = "autocompleteIdentityContracts", */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_AUTOCOMPLETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter"
			/* nickname = "countContractSlices", */ 
			 
			)
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.CONTRACTSLICE_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.CONTRACTSLICE_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@Operation(summary = "Contract slice detail", /* nickname = "getIdentityContract", */ /* response = IdmContractSliceDto.class, */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.CONTRACTSLICE_UPDATE + "')")
	@Operation(summary = "Create / update contract slice", /* nickname = "postIdentityContract", */ /* response = IdmContractSliceDto.class, */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_CREATE,
							CoreGroupPermission.CONTRACTSLICE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_CREATE,
							CoreGroupPermission.CONTRACTSLICE_UPDATE })
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmContractSliceDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_UPDATE + "')")
	@Operation(summary = "Update contract slice", /* nickname = "putIdentityContract", */ /* response = IdmContractSliceDto.class, */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@Valid @RequestBody IdmContractSliceDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_DELETE + "')")
	@Operation(summary = "Delete contract slice", /* nickname = "deleteIdentityContract", */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_DELETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@Operation(summary = "What logged identity can do with given record", /* nickname = "getPermissionsOnIdentityContract", */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@Operation(summary = "Contract slice extended attributes form definitions", /* nickname = "getIdentityContractFormDefinitions", */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ })
        }
    )
	public ResponseEntity<?> getFormDefinitions(
			@Parameter(name = "Contract's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return formDefinitionController.getDefinitions(IdmIdentityContract.class);
	}

	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@Operation(summary = "Contract slice form definition - read values", /* nickname = "getIdentityContractFormValues", */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_READ })
        }
    )
	public EntityModel<?> getFormValues(
			@Parameter(name = "Identity's uuid identifier or username.", required = true) @PathVariable @NotNull String backendId,
			@Parameter(name = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode) {
		IdmContractSliceDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(dto, IdmBasePermission.READ);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmIdentityContract.class,
				definitionCode);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH } )
	@Operation(summary = "Contract slice form definition - save values", /* nickname = "postIdentityContractFormValues", */ tags = {
			IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.CONTRACTSLICE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.CONTRACTSLICE_UPDATE })
        }
    )
	public EntityModel<?> saveFormValues(
			@Parameter(name = "Identity's uuid identifier or username.", required = true) @PathVariable @NotNull String backendId,
			@Parameter(name = "Code of form definition (default will be used if no code is given).", required = false, example = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode,
			@Parameter(name = "Filled form data.", required = true) @RequestBody @Valid List<IdmFormValueDto> formValues) {
		IdmContractSliceDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		checkAccess(dto, IdmBasePermission.UPDATE);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmIdentityContract.class,
				definitionCode);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-value", method = { RequestMethod.POST } )
	@Operation(
			summary = "Role form definition - save value", 
			/* nickname = "postRoleFormValue", */ 
			tags = { IdmContractSliceController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.CONTRACTSLICE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.CONTRACTSLICE_UPDATE })
        }
    )
	public EntityModel<?> saveFormValue(
			@Parameter(name = "Slice's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid IdmFormValueDto formValue) {		
		IdmContractSliceDto dto = getDto(backendId);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@Operation(
			summary = "Download form value attachment", 
			/* nickname = "downloadFormValue", */
			tags = { IdmContractSliceController.TAG },
			description = "Returns input stream to attachment saved in given form value.")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							CoreGroupPermission.CONTRACTSLICE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							CoreGroupPermission.CONTRACTSLICE_READ })
        }
    )
	public ResponseEntity<InputStreamResource> downloadFormValue(
			@Parameter(name = "Slice's uuid identifier.", required = true)
			@PathVariable String backendId,
			@Parameter(name = "Form value identifier.", required = true)
			@PathVariable String formValueId) {
		IdmContractSliceDto dto = getDto(backendId);
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.CONTRACTSLICE_READ + "')")
	@Operation(
			summary = "Download form value attachment preview", 
			/* nickname = "downloadFormValue", */
			tags = { IdmContractSliceController.TAG },
			description = "Returns input stream to attachment preview saved in given form value. Preview is supported for the png, jpg and jpeg mime types only")
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							CoreGroupPermission.CONTRACTSLICE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							CoreGroupPermission.CONTRACTSLICE_READ })
        }
    )
	public ResponseEntity<InputStreamResource> previewFormValue(
			@Parameter(name = "Slice's uuid identifier.", required = true)
			@PathVariable String backendId,
			@Parameter(name = "Form value identifier.", required = true)
			@PathVariable String formValueId) {
		IdmContractSliceDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormValueDto value = formService.getValue(dto, DtoUtils.toUuid(formValueId));
		if (value == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, formValueId);
		}
		return formDefinitionController.previewAttachment(value);
	}

	@Override
	protected IdmContractSliceFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmContractSliceFilter filter = new IdmContractSliceFilter(parameters, getParameterConverter());
		filter.setIdentity(getParameterConverter().toEntityUuid(parameters, "identity", IdmIdentityDto.class));
		filter.setValid(getParameterConverter().toBoolean(parameters, "valid"));
		filter.setExterne(getParameterConverter().toBoolean(parameters, "externe"));
		filter.setDisabled(getParameterConverter().toBoolean(parameters, "disabled"));
		filter.setMain(getParameterConverter().toBoolean(parameters, "main"));
		filter.setValidNowOrInFuture(getParameterConverter().toBoolean(parameters, "validNowOrInFuture"));
		filter.setExcludeContract(getParameterConverter().toUuid(parameters, "excludeContract"));
		filter.setParentContract(getParameterConverter().toUuid(parameters, "parentContract"));
		filter.setWithoutParent(getParameterConverter().toBoolean(parameters, "withoutParent"));
		filter.setContractCode(getParameterConverter().toString(parameters, "contractCode"));

		return filter;
	}
}
