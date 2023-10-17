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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.FilterBuilderDto;
import eu.bcvsolutions.idm.core.api.dto.filter.FilterBuilderFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Filter builder administration.
 *
 * @author Kolychev Artem
 * @since 9.7.7
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/filter-builders")
@Tag(
		name = FilterBuilderController.TAG,
		description = "Configure filter builders"//,
		
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class FilterBuilderController  {

	protected static final String TAG = "filter builders filters";

    @Autowired private FilterManager filterManager;
    @Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@Operation(
			summary = "Find all filter builders",
			/* nickname = "findAllFilterBuilders", */
			tags = { FilterBuilderController.TAG },
						description = "Returns all registered filter builders.")
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.MODULE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.MODULE_READ })
        }
    )
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		List<FilterBuilderDto> filterBuilderDtos = filterManager.find(toFilter(parameters));
		PageImpl page = new PageImpl(filterBuilderDtos, PageRequest.of(0, filterBuilderDtos.size() == 0 ? 10 : filterBuilderDtos.size()), filterBuilderDtos.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyModel(page, FilterBuilderDto.class);
		}
		return pagedResourcesAssembler.toModel(page);
	}
	
	/**
	 * Enable filter builder
	 * 
	 * @param filterId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{filterId}/enable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@Operation(
			summary = "Activate filter builder",
			/* nickname = "activateFilterBuilder", */
			tags = { FilterBuilderController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.MODULE_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.MODULE_UPDATE })
        }
    )
	public void enable(
			 @Parameter(description = "Filter builder's identifier.", required = true)
			@PathVariable @NotNull String filterId) {
		filterManager.enable(filterId);
	}

    protected FilterBuilderFilter toFilter(MultiValueMap<String, Object> parameters) {
        return new FilterBuilderFilter(parameters);
    }
}
