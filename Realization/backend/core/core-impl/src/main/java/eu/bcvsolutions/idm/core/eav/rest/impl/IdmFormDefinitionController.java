package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
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
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * EAV Form definitions.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/form-definitions")
@Tag(
		name = IdmFormDefinitionController.TAG, 
		description = "Operations with form definitions (eav)"

		/*produces = BaseController.APPLICATION_HAL_JSON_VALUE*/)
public class IdmFormDefinitionController extends AbstractReadWriteDtoController<IdmFormDefinitionDto, IdmFormDefinitionFilter>  {

	protected static final String TAG = "Form definitions";
	//
	@Autowired private FormService formService;
	@Autowired private AttachmentManager attachmentManager;
	
	@Autowired
	public IdmFormDefinitionController(IdmFormDefinitionService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_READ + "')")
	@Operation(
			summary = "Search form definitions (/search/quick alias)", 
			/* nickname = "searchFormDefinitions", */
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_READ + "')")
	@Operation(
			summary = "Search form definitions", 
			/* nickname = "searchQuickFormDefinitions", */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ })
        }
    )
	@Override
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete form definitions (selectbox usage)", 
			/* nickname = "autocompleteFormDefinitions", */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countFormDefinitions", */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_READ + "')")
	@Operation(
			summary = "Form definition detail", 
			/* nickname = "getFormDefiniton", */ 
			/* response = IdmFormDefinitionDto.class, */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Definition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_CREATE + "') or hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_UPDATE + "')")
	@Operation(
			summary = "Create / update form definition", 
			/* nickname = "postFormDefinition", */ 
			/* response = IdmFormDefinitionDto.class, */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_CREATE,
						CoreGroupPermission.FORM_DEFINITION_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_CREATE,
						CoreGroupPermission.FORM_DEFINITION_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmFormDefinitionDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_UPDATE + "')")
	@Operation(
			summary = "Update form definition",
			/* nickname = "putFormDefinition", */ 
			/* response = IdmFormDefinitionDto.class, */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Form definition's uuid identifier", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmFormDefinitionDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_UPDATE + "')")
	@Operation(
			summary = "Patch form definition", 
			/* nickname = "patchFormDefinition", */ 
			/* response = IdmFormDefinitionDto.class, */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			@Parameter(name = "Form definition's uuid identifier", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_DELETE + "')")
	@Operation(
			summary = "Delete form definition", 
			/* nickname = "deleteFormDefinition", */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Form definition's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnFormDefinition", */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Definition's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/types", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_READ + "')")
	@Operation(
			summary = "Form definition owner types", 
			/* nickname = "getFormDefinitionOwnerTypes", */ 
			tags = { IdmFormDefinitionController.TAG },
			description = "Returns all supported form definition owner types (FormableEntity descendants).")
    @SecurityRequirements(
        value = {
 
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							CoreGroupPermission.FORM_DEFINITION_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							CoreGroupPermission.FORM_DEFINITION_READ })
        }
    )
	public List<String> getOwnerTypes() {
		return formService.getOwnerTypes();
	}
	
	/**
	 * Get available bulk actions for form definition
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_READ + "')")
	@Operation(
			summary = "Get available bulk actions for form definitions", 
			/* nickname = "availableBulkAction", */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ })
        }
    )
	@Override
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	/**
	 * Process bulk action for form definition
	 *
	 * @param bulkAction
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_READ + "')")
	@Operation(
			summary = "Process bulk action for form definition", 
			/* nickname = "bulkAction", */ 
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ})
        }
    )
	@Override
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	/**
	 * Prevalidate bulk action for form definition
	 *
	 * @param bulkAction
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_DEFINITION_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for form definition", 
			/* nickname = "prevalidateBulkAction", */ 
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmFormDefinitionController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_DEFINITION_READ})
        }
    )
	@Override
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@Override
	public void deleteDto(IdmFormDefinitionDto entity) {
		// definitions flagged as system definition can't be deleted from controller
		if (entity.isUnmodifiable()) {
			throw new ResultCodeException(CoreResultCode.FORM_DEFINITION_DELETE_FAILED_SYSTEM_DEFINITION, ImmutableMap.of("code", entity.getCode()));
		}
		super.deleteDto(entity);
	}
	
	@Override
	protected IdmFormDefinitionDto validateDto(IdmFormDefinitionDto entity) {
		// check if exist id = create entity, then check if exist old entity = create entity with id
		if (entity.getId() == null) {
			return super.validateDto(entity);
		}
		IdmFormDefinitionDto previousDto = getDto(entity.getId());
		
		if (previousDto != null) {
			// type cannot be changed
			if (!previousDto.getType().equals(entity.getType())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "type", "class", entity.getClass().getSimpleName()));
			}
			// check explicit attributes that can't be changed
			if (previousDto.isUnmodifiable() && !previousDto.getCode().equals(entity.getCode())) {
				throw new ResultCodeException(CoreResultCode.UNMODIFIABLE_ATTRIBUTE_CHANGE, ImmutableMap.of("name", "name", "class", entity.getClass().getSimpleName()));
			}
		}
		return super.validateDto(entity);
	}
	
	/**
	 * Returns default definition for given ownerClass
	 * 
	 * @param ownerClass
	 * @param permission
	 * @return
	 * @throws ForbiddenEntityException if authorization policy AUTOCOMPLETE for form definition doesn't met
	 */
	public ResponseEntity<?> getDefinition(Class<? extends Identifiable> ownerClass, BasePermission... permission) {
		IdmFormDefinitionDto definition = getDefinition(ownerClass, (IdmFormDefinitionDto) null, permission);
		//
		return new ResponseEntity<>(toModel(definition), HttpStatus.OK);
	}
	
	/**
	 * Returns all definitions for given ownerClass. Permission will not be
	 * evaluated.
	 *
	 * @param ownerType
	 *
	 * @return
	 */
	public ResponseEntity<?> getDefinitions(Class<? extends FormableEntity> ownerType) {
		return getDefinitions(ownerType, null);
	}
	
	/**
	 * Returns all definitions for given ownerClass.
	 * 
	 * @param ownerType
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 */
	public ResponseEntity<?> getDefinitions(Class<? extends Identifiable> ownerType, BasePermission permission) {
		IdmFormDefinitionFilter filter = new IdmFormDefinitionFilter();
		filter.setType(formService.getDefaultDefinitionType(ownerType));
		//
		// find definitions + sorted
		Page<IdmFormDefinitionDto> definitions = find(filter, formService.getDefinitionPageable(), permission);
		//
		return new ResponseEntity<>(toCollectionModel(definitions, getDtoClass()), HttpStatus.OK);
	}
	
	/**
	 * Wraps given definitions to resources
	 * 
	 * @param definitions
	 * @return
	 */
	public ResponseEntity<?> toCollectionModel(List<IdmFormDefinitionDto> definitions) {
		return new ResponseEntity<>(toCollectionModel(definitions, getDtoClass()), HttpStatus.OK); 
	}
	
	/**
	 * Returns definition by given ID
	 * 
	 * @param definitionId
	 * @return
	 */
	public ResponseEntity<?> getDefinitions(UUID definitionId) {
		IdmFormDefinitionFilter filter = new IdmFormDefinitionFilter();
		filter.setId(definitionId);
		//
		return new ResponseEntity<>(toCollectionModel(find(filter, null, null), getDtoClass()), HttpStatus.OK);
	}
	
	/**
	 * Returns given formDefinition or default definition for given ownerClass, if no formDefinition is given.
	 * 
	 * @param ownerClass
	 * @param formDefinitionId [optional]
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 */
	private IdmFormDefinitionDto getDefinition(Class<? extends Identifiable> ownerClass, IdmFormDefinitionDto formDefinition, BasePermission... permission) {
		Assert.notNull(ownerClass, "Owner class is required to get form definition.");
		//
		if (formDefinition != null) {
			return formDefinition;
		}
		formDefinition = formService.getDefinition(ownerClass, permission);
		if (formDefinition == null) {			
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("formDefinition", ownerClass));
		}
		return formDefinition;
	}
	
	/**
	 * Gets form definition for given owner type.
	 * 
	 * @param ownerClass owner type
	 * @param definitionCode [optional] definition code, default definition will be returned, if no code is given
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 */
	public IdmFormDefinitionDto getDefinition(Class<? extends Identifiable> ownerClass, String definitionCode, BasePermission... permission) {
		IdmFormDefinitionDto formDefinition = null; // default will be used
		if (StringUtils.isNotEmpty(definitionCode)) {
			formDefinition = formService.getDefinition(ownerClass, definitionCode, permission);
			if (formDefinition == null) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of(
						"formDefinition", ownerClass.getSimpleName(),
						"code", definitionCode));
			}
		}
		if (formDefinition == null) {
			formDefinition = formService.getDefinition(ownerClass, permission);
		}
		if (formDefinition == null) {			
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("formDefinition", ownerClass));
		}
		return formDefinition;
	}
	
	/**
	 * Secure form attributes by configured authorization policies. Usable, when
	 * owner not exists (is created together with eavs).
	 *
	 * @param formInstance
	 *
	 * @since 10.2.0
	 */
	public void secureAttributes(IdmFormInstanceDto formInstance) {
		Assert.notNull(formInstance, "Form instance is required.");
		IdmFormDefinitionDto formDefinition = formInstance.getFormDefinition();
		Assert.notNull(formDefinition, "Form definition is required.");
		Class<? extends Identifiable> ownerType = formInstance.getOwnerType();
		Assert.notNull(ownerType, "Form instance owner type is required.");
		//
		FormValueService<FormableEntity> formValueService = formService.getFormValueService(ownerType);
		List<IdmFormAttributeDto> attributes = formDefinition.getFormAttributes();
		Set<UUID> removeAttributes = new HashSet<>(attributes.size());
		attributes.forEach(attribute -> {
			IdmFormValueDto formValue = new IdmFormValueDto(attribute);
			formValue.setOwner(formService.getEmptyOwner(formDefinition));
			if (formInstance.getOwnerId() != null) {
				formValue.getOwner().setId(formInstance.getOwnerId());
			}
			Set<String> valuePermissions = formValueService.getPermissions(formValue);
			if (!PermissionUtils.hasPermission(valuePermissions, IdmBasePermission.READ)) {
				removeAttributes.add(attribute.getId());
			} else if (!PermissionUtils.hasAnyPermission(valuePermissions, IdmBasePermission.CREATE, IdmBasePermission.UPDATE)) {
				if (formInstance.getOwnerId() == null) {
					// new owner - remove readonly fields
					removeAttributes.add(attribute.getId());
				} else {
					formDefinition.getMappedAttribute(attribute.getId()).setReadonly(true);
				}
			}
		});	
		removeAttributes.forEach(attributeId -> {
			formDefinition.removeFormAttribute(attributeId);
		});
	}
	
	/**
	 * Returns owner's form values
	 * 
	 * @param owner 
	 * @param formDefinition 
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 */
	public EntityModel<IdmFormInstanceDto> getFormValues(Identifiable owner, IdmFormDefinitionDto formDefinition, BasePermission... permission) {
		Assert.notNull(owner, "Owner is required to get form values.");
		//
		return new EntityModel<>(formService.getFormInstance(owner, getDefinition(owner.getClass(), formDefinition, permission), permission));
	}
	
	/**
	 * Saves owner's form values.
	 * 
	 * @param owner
	 * @param formDefinition
	 * @param formValues
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	public EntityModel<?> saveFormValues(Identifiable owner, IdmFormDefinitionDto formDefinition, List<IdmFormValueDto> formValues, BasePermission... permission) {		
		formDefinition = getDefinition(owner.getClass(), formDefinition); 
		// construct form instance with given values
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(owner, formDefinition, formValues);
		// prepare event envelope
		CoreEvent<IdmFormInstanceDto> event = new CoreEvent<>(CoreEventType.UPDATE, formInstance);
		// FE - high event priority
		event.setPriority(PriorityType.HIGH);
		// publish event for save form instance
		return new EntityModel<>(formService.publish(event, permission).getContent());
	}
	
	/**
	 * Saves owner's form value (single)
	 * 
	 * @param owner
	 * @param formValue
	 * @param permission base permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @since 9.4.0
	 */
	public EntityModel<?> saveFormValue(Identifiable owner, IdmFormValueDto formValue, BasePermission... permission) {		
		Assert.notNull(owner, "Owner is required to save form value.");
		Assert.notNull(formValue, "Form value is required to save her.");
		//
		IdmFormAttributeDto attribute = formService.getAttribute(formValue.getFormAttribute());
		if (attribute == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", formValue.getFormAttribute()));
		}
		IdmFormDefinitionDto formDefinition = formService.getDefinition(attribute.getFormDefinition());		
		// construct form instance with given values
		IdmFormInstanceDto formInstance = new IdmFormInstanceDto(owner, formDefinition, Lists.newArrayList(formValue));
		// prepare event envelope
		CoreEvent<IdmFormInstanceDto> event = new CoreEvent<>(CoreEventType.UPDATE, formInstance);
		// FE - high event priority
		event.setPriority(PriorityType.HIGH);
		// publish event for save form instance
		return new EntityModel<>(formService.publish(event, permission).getContent());
	}

	/**
	 * Returns input stream to attachment saved in given form value.
	 * 
	 * @param value
	 * @return
	 * @since 9.4.0
	 */
	public ResponseEntity<InputStreamResource> downloadAttachment(IdmFormValueDto value) {
		Assert.notNull(value, "Form value holds the attachment identifier is required to download the attachment.");
		//
		if (value.getPersistentType() != PersistentType.ATTACHMENT) {
			throw new ResultCodeException(
					CoreResultCode.FORM_VALUE_WRONG_TYPE, 
					"Download attachment of form value [%s], attribute [%s] with type [%s] is not supported. Download supports [%] persistent type only.", 
					ImmutableMap.of(
						"value", Objects.toString(value.getId()), 
						"formAttribute", value.getFormAttribute().toString(), 
						"persistentType", value.getPersistentType().toString(), 
						"requiredPersistentType", PersistentType.ATTACHMENT.toString()
					));
		}
		if (value.getUuidValue() == null) {
			throw new ResultCodeException(
					CoreResultCode.BAD_VALUE, 
					"Attachment of form value [%s], attribute [%s] is empty, cannot be dowloaded.", 
					ImmutableMap.of(
						"value", Objects.toString(value.getId()), 
						"formAttribute", value.getFormAttribute().toString()
					));
		}
		//
		IdmAttachmentDto attachment = attachmentManager.get(value.getUuidValue());
		if (attachment == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", value.getUuidValue()));
		}
		String mimetype = attachment.getMimetype();
		InputStream is = attachmentManager.getAttachmentData(attachment.getId());
		try {
			BodyBuilder response = ResponseEntity
					.ok()
					.contentLength(is.available())
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s\"", 
							attachmentManager.getValidFileName(attachment.getName())));
			// append media type, if it's filled
			if (StringUtils.isNotBlank(mimetype)) {
				response = response.contentType(MediaType.valueOf(attachment.getMimetype()));
			}
			//
			return response.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	/**
	 * Returns input stream to attachment saved in given form value.
	 * 
	 * @param value
	 * @return
	 * @since 9.4.0
	 */
	public ResponseEntity<InputStreamResource> previewAttachment(IdmFormValueDto value) {
		Assert.notNull(value, "Form value holds the attachment identifier is required to download the attachment.");
		//
		if (value.getPersistentType() != PersistentType.ATTACHMENT) {
			throw new ResultCodeException(
					CoreResultCode.FORM_VALUE_WRONG_TYPE, 
					"Download attachment of form value [%s], attribute [%s] with type [%s] is not supported. Download supports [%] persistent type only.", 
					ImmutableMap.of(
						"value", Objects.toString(value.getId()), 
						"formAttribute", value.getFormAttribute().toString(), 
						"persistentType", value.getPersistentType().toString(), 
						"requiredPersistentType", PersistentType.ATTACHMENT.toString()
					));
		}
		if (value.getUuidValue() == null) {
			throw new ResultCodeException(
					CoreResultCode.BAD_VALUE, 
					"Attachment of form value [%s], attribute [%s] is empty, cannot be dowloaded.", 
					ImmutableMap.of(
						"value", Objects.toString(value.getId()), 
						"formAttribute", value.getFormAttribute().toString()
					));
		}
		//
		IdmAttachmentDto attachment = attachmentManager.get(value.getUuidValue());
		if (attachment == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", value.getUuidValue()));
		}
		String mimetype = attachment.getMimetype();
		// TODO: naive check => implement better + image resize (thumbnail)
		if (!mimetype.startsWith("image/")) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT); 
		}
		//
		InputStream is = attachmentManager.getAttachmentData(attachment.getId());
		try {
			BodyBuilder response = ResponseEntity
					.ok()
					.contentLength(is.available())
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s\"", attachment.getName()));
			// append media type, if it's filled
			if (StringUtils.isNotBlank(mimetype)) {
				response = response.contentType(MediaType.valueOf(attachment.getMimetype()));
			}
			//
			return response.body(new InputStreamResource(is));
		} catch (IOException e) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
		}
	}
	
	@Override
	protected IdmFormDefinitionFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmFormDefinitionFilter(parameters, getParameterConverter());
	}
}
