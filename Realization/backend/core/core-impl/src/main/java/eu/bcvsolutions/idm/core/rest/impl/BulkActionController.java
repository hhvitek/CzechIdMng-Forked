package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.filter.BulkActionFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Bulk action administration.
 * 
 * Be careful: page and size is not implemented in find methods.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/bulk-actions")
@Tag(name = BulkActionController.TAG, description = "Configure bulk actions")
public class BulkActionController {

	protected static final String TAG = "Bulk action administration";	
	//
	@Autowired private BulkActionManager bulkActionManager;
	@Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	@Autowired private ObjectMapper mapper;
	@Autowired private LookupService lookupService;
	//
	private FilterConverter filterConverter;
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@Operation(
			summary = "Find all bulk actions",
			operationId = "findAllBulkActions", 
			tags = { BulkActionController.TAG }, 
						description = "Returns all registered bulk actions with state properties (disabled, order).")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_READ })
    })
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		List<IdmBulkActionDto> records = bulkActionManager.find(toFilter(parameters));
		PageImpl page = new PageImpl(records, PageRequest.of(0, records.size() == 0 ? 10 : records.size()), records.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyModel(page, IdmBulkActionDto.class);
		}
		return pagedResourcesAssembler.toModel(page);
	}
	
	/**
	 * Enable bulk action.
	 * 
	 * @param bulkActionId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{bulkActionId}/enable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@Operation(
			summary = "Enable bulk action",
			operationId = "enableBulkAction",
			tags = { BulkActionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_UPDATE })
    })
	public void enable(
			 @Parameter(description = "Bulk action identifier.", required = true)
			@PathVariable @NotNull String bulkActionId) {
		bulkActionManager.enable(bulkActionId);
	}
	
	/**
	 * Disable bulk action.
	 * 
	 * @param bulkActionId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{bulkActionId}/disable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@Operation(
			summary = "Disable bulk action",
			operationId = "disableBulkAction",
			tags = { BulkActionController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_UPDATE })
    })
	public void disable(
			 @Parameter(description = "Bulk action identifier.", required = true)
			@PathVariable @NotNull String bulkActionId) {
		bulkActionManager.disable(bulkActionId);
	}

	/**
	 * Return parameter converter helper.
	 * 
	 * @return
	 */
	protected FilterConverter getParameterConverter() {
		if (filterConverter == null) {
			filterConverter = new FilterConverter(lookupService, mapper);
		}
		return filterConverter;
	}
	
	private BulkActionFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new BulkActionFilter(parameters, getParameterConverter());
	}
}
