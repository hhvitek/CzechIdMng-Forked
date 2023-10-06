package eu.bcvsolutions.idm.core.audit.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Controller for {@link IdmLoggingEventExceptionDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/logging-event-exceptions")
@Tag(name = IdmLoggingEventController.TAG, description = "Read / search log exception from LOG4J"
//, //produces = BaseController.APPLICATION_HAL_JSON_VALUE
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmLoggingEventExceptionController
		extends AbstractReadDtoController<IdmLoggingEventExceptionDto, IdmLoggingEventExceptionFilter> {

	protected static final String TAG = "Logging event exceptions";

	@Autowired
	public IdmLoggingEventExceptionController(IdmLoggingEventExceptionService service) {
		super(service);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search logging event exceptions"
			/*, nickname = "searchQuickLoggingEventExceptions", */)
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            CoreGroupPermission.AUDIT_READ }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            CoreGroupPermission.AUDIT_READ })
            }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return this.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.AUDIT_READ + "')")
	@Override
	@Operation(
			summary = "Logging event exception detail", 
			/* nickname = "getLoggingEventException", */
			/* response = IdmLoggingEventExceptionDto.class, */
			tags = { IdmLoggingEventExceptionController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.AUDIT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.AUDIT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Logging event exception's identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
}
