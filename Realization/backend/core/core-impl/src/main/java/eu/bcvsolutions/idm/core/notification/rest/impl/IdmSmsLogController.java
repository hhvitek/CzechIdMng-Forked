package eu.bcvsolutions.idm.core.notification.rest.impl;

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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmSmsLogService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Read sms logs
 *
 * @author Peter Sourek
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-sms")
@Tag(
		name = IdmSmsLogController.TAG, 
		description = "Sms history"//, 
		
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmSmsLogController extends AbstractReadDtoController<IdmSmsLogDto, IdmNotificationFilter> {

	protected static final String TAG = "Notification logs - sms";
	
	@Autowired
	public IdmSmsLogController(IdmSmsLogService entityLookupService) {
		super(entityLookupService);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@Operation(
			summary = "Search sms logs (/search/quick alias)", 
			/* nickname = "searchSmsLogs", */ 
			tags = { IdmSmsLogController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATION_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search sms logs", 
			/* nickname = "searchQuickSmsLogs", */ 
			tags = { IdmSmsLogController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATION_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Sms log detail", 
			/* nickname = "getSmsLog", */ 
			/* response = IdmSmsLogDto.class, */ 
			tags = { IdmSmsLogController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATION_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Sms log's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
}
