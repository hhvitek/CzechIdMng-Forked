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
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.FilterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Entity event procesor's administration.
 * 
 * Be careful: page and size is not implemented in find methods
 * 
 * TODO: change processors order
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/entity-event-processors")
@Tag(name = EntityEventProcessorController.TAG, description = "Configure event processing")
public class EntityEventProcessorController {

	protected static final String TAG = "Entity event processors";
	private final EntityEventManager entityEventManager;
	//
	@Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	@Autowired private ObjectMapper mapper;
	@Autowired private LookupService lookupService;
	//
	private FilterConverter filterConverter;
	
	@Autowired
	public EntityEventProcessorController(EntityEventManager entityEventManager) {
		Assert.notNull(entityEventManager, "EntityEventManager is required");
		//
		this.entityEventManager = entityEventManager;
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_READ + "')")
	@Operation(
			summary = "Find all processors", 
			operationId = "findAllEntityEventProcessors", 
			tags = { EntityEventProcessorController.TAG }, 
						description = "Returns all registered entity event processors with state properties (disabled, order).")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_READ })
    })
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		List<EntityEventProcessorDto> records = entityEventManager.find(toFilter(parameters));
		PageImpl page = new PageImpl(records, PageRequest.of(0, records.size() == 0 ? 10 : records.size()), records.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyModel(page, EntityEventProcessorDto.class);
		}
		return pagedResourcesAssembler.toModel(page);
	}

	/**
	 * Enable event processor
	 * @param processorId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{processorId}/enable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@Operation(
			summary = "Enable processor",
			operationId = "enableProcessor",
			tags = { EntityEventProcessorController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_UPDATE })
    })
	public void enable(
			 @Parameter(description = "Processor's identifier.", required = true)
			@PathVariable @NotNull String processorId) {
		entityEventManager.enable(processorId);
	}
	
	/**
	 * Disable event processor
	 * @param processorId
	 */
	@ResponseStatus(code = HttpStatus.NO_CONTENT)
	@RequestMapping(value = "/{processorId}/disable", method = { RequestMethod.PATCH, RequestMethod.PUT })
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.MODULE_UPDATE + "')")
	@Operation(
			summary = "Disable processor",
			operationId = "disableProcessor",
			tags = { EntityEventProcessorController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_UPDATE })
    })
	public void disable(
			 @Parameter(description = "Processor's identifier.", required = true)
			@PathVariable @NotNull String processorId) {
		entityEventManager.disable(processorId);
	}

	/**
	 * Return parameter converter helper
	 * 
	 * @return
	 */
	protected FilterConverter getParameterConverter() {
		if (filterConverter == null) {
			filterConverter = new FilterConverter(lookupService, mapper);
		}
		return filterConverter;
	}
	
	private EntityEventProcessorFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new EntityEventProcessorFilter(parameters, getParameterConverter());
	}
}
