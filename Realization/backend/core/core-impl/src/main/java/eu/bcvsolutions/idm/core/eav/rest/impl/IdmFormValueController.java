package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Form values edpoint - all form values from all definitions.
 *
 * @author Roman Kučera
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/form-values")
@Tag(
		name = IdmFormValueController.TAG,
		description = "Operations with form values"

		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmFormValueController extends AbstractReadWriteDtoController<IdmFormValueDto, IdmFormValueFilter<?>> {

	protected static final String TAG = "Form values";
	//
	@Autowired private FormService formService;
	
	@Autowired
	public IdmFormValueController(
			// common form service only for automatic bulk action registration
			@Qualifier("formValueService") ReadWriteDtoService<IdmFormValueDto, IdmFormValueFilter<?>> service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_VALUE_READ + "')")
	@Operation(
			summary = "Search form values (/search/quick alias)",
			/* nickname = "searchFormValues", */
			tags = { IdmFormValueController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_VALUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_VALUE_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_VALUE_READ + "')")
	@Operation(
			summary = "Search form values",
			/* nickname = "searchQuickFormValues", */ 
			tags = { IdmFormValueController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_VALUE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_VALUE_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_VALUE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete form values (selectbox usage)",
			/* nickname = "autocompleteFormValues", */ 
			tags = { IdmFormValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_VALUE_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_VALUE_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	protected Page<IdmFormValueDto> findWithOperator(IdmFormValueFilter<?> filter, Pageable pageable, BasePermission... permission) {
		return formService.findValues(filter, pageable, permission);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_VALUE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			/* nickname = "countFormValues", */ 
			tags = { IdmFormValueController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.FORM_VALUE_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.FORM_VALUE_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	protected long countWithOperator(IdmFormValueFilter<?> filter, BasePermission... permission) {
		return formService.findValues(filter, PageRequest.of(0, 1), permission).getTotalElements();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_VALUE_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			/* nickname = "availableBulkAction", */
			tags = { IdmFormValueController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.FORM_VALUE_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.FORM_VALUE_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_VALUE_UPDATE + "')")
	@Operation(
			summary = "Process bulk action for form values",
			/* nickname = "bulkAction", */
			/* response = IdmBulkActionDto.class, */
			tags = { IdmFormValueController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.FORM_VALUE_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.FORM_VALUE_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.FORM_VALUE_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for form values",
			/* nickname = "prevalidateBulkAction", */
			/* response = IdmBulkActionDto.class, */
			tags = { IdmFormValueController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.FORM_VALUE_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.FORM_VALUE_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@Override
	protected IdmFormValueFilter<?> toFilter(MultiValueMap<String, Object> parameters) {	
		IdmFormValueFilter<?> filter =  new IdmFormValueFilter<FormableEntity>(parameters, getParameterConverter());
		filter.setAddSecredProxyString(true);
		//
		return filter;
	}
}
