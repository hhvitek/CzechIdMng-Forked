package eu.bcvsolutions.idm.core.rest.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.AvailableServiceDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AvailableServiceFilter;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.IdmScriptAuthorityService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * available service administration.
 *
 * @author Ondrej Husnik
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/available-service")
@Tag(name = AvailableServiceController.TAG, description = "Displays available services")
public class AvailableServiceController  {

	protected static final String TAG = "available service";

    @Autowired private IdmScriptAuthorityService availableServiceService;
    @Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@Operation(
			summary = "Find all available services",
			operationId = "findAllAvailableServices",
			tags = { AvailableServiceController.TAG },
						description = "Returns all available services.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.MODULE_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.MODULE_READ })
    })
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
	List<AvailableServiceDto> serviceDtos = availableServiceService.findServices(toFilter(parameters));
		PageImpl page = new PageImpl(serviceDtos, PageRequest.of(0, serviceDtos.size() == 0 ? 10 : serviceDtos.size()), serviceDtos.size());
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyModel(page, AvailableServiceDto.class);
		}
		return pagedResourcesAssembler.toModel(page);
	}
	
	
    protected AvailableServiceFilter toFilter(MultiValueMap<String, Object> parameters) {
        return new AvailableServiceFilter(parameters);
    }
}
