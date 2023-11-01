package eu.bcvsolutions.idm.core.notification.rest.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
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
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationState;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationRecipientFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationLogService;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationRecipientService;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Read and send notification.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notifications")
@Tag(name = IdmNotificationLogController.TAG, description = "Operations with notifications, history")
public class IdmNotificationLogController
		extends AbstractReadWriteDtoController<IdmNotificationLogDto, IdmNotificationFilter> {

	protected static final String TAG = "Notification logs - all";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmNotificationLogController.class);
	private final NotificationManager notificationManager;
	private final IdmNotificationConfigurationService notificationConfigurationService;
	//
	@Autowired private IdmNotificationRecipientService notificationRecipientService;

	@Autowired
	public IdmNotificationLogController(
			IdmNotificationLogService notificationLogService, 
			NotificationManager notificationManager,
			IdmNotificationConfigurationService notificationConfigurationService) {
		super(notificationLogService);
		//
		Assert.notNull(notificationManager, "Manager is required.");
		Assert.notNull(notificationConfigurationService, "Service is required.");
		//
		this.notificationManager = notificationManager;
		this.notificationConfigurationService = notificationConfigurationService;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@Operation(
			summary = "Search notification logs (/search/quick alias)",
			operationId = "searchNotificationLogs",
			tags = { IdmNotificationLogController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { NotificationGroupPermission.NOTIFICATION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { NotificationGroupPermission.NOTIFICATION_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search notification logs",
			operationId = "searchQuickNotificationLogs",
			tags = { IdmNotificationLogController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { NotificationGroupPermission.NOTIFICATION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { NotificationGroupPermission.NOTIFICATION_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Notification log detail",
			operationId = "getNotificationLog",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmNotificationLogDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmNotificationLogController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { NotificationGroupPermission.NOTIFICATION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { NotificationGroupPermission.NOTIFICATION_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Notification log's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	/**
	 * Send notification
	 * 
	 * @param dto
	 */
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_CREATE + "')"
			+ " or hasAuthority('" + NotificationGroupPermission.NOTIFICATION_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Send notification",
			operationId = "postNotificationLog",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmNotificationLogDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmNotificationLogController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATION_CREATE,
						NotificationGroupPermission.NOTIFICATION_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATION_CREATE,
						NotificationGroupPermission.NOTIFICATION_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmNotificationLogDto dto) {
		return super.post(dto);
	}

	/**
	 * Send notification
	 */
	@Override
	public IdmNotificationLogDto postDto(IdmNotificationLogDto notification) {		
		LOG.debug("Notification log [{}] was created and notification will be send.", notification);
		List<IdmNotificationLogDto> results = notificationManager.send(
				notification.getTopic(), 
				notification.getMessage(), 
				notification.getIdentitySender() == null ? null : (IdmIdentityDto) getLookupService().lookupDto(IdmIdentityDto.class, notification.getIdentitySender()),
				notification
					.getRecipients()
					.stream()
					.map(recipient -> {
						return (IdmIdentityDto) getLookupService().lookupDto(IdmIdentityDto.class, recipient.getIdentityRecipient());
					})
					.collect(Collectors.toList())
				);
		// TODO: parent notification should be returned ...
		if (results.isEmpty()) {
			throw new ResultCodeException(CoreResultCode.NOTIFICATION_NOT_SENT, ImmutableMap.of("topic", notification.getTopic()));
		}
		return getDto(results.get(0).getId());
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_DELETE + "')")
	@Operation(
			summary = "Delete notification",
			operationId = "deleteNotification",
			tags = { IdmNotificationLogController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { NotificationGroupPermission.NOTIFICATION_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { NotificationGroupPermission.NOTIFICATION_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Notification's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnNotification",
			tags = { IdmNotificationLogController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATION_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATION_READ})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Identity's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * FIXME: Add response wrapper => raw list is returned now => all remove method at all => recipient controller should e used.
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/{backendId}/recipients", method = RequestMethod.GET)
	@Operation(
			summary = "Notification recipients",
			operationId = "getNotificationRecipients",
			tags = { IdmNotificationLogController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { NotificationGroupPermission.NOTIFICATION_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { NotificationGroupPermission.NOTIFICATION_READ })
    })
	public List<IdmNotificationRecipientDto> getRecipients(@PathVariable @NotNull String backendId) {
		IdmNotificationRecipientFilter filter = new IdmNotificationRecipientFilter();
		filter.setNotification(DtoUtils.toUuid(backendId));
		//
		return notificationRecipientService.find(filter, null).getContent();
	}
	
	@Override
	protected IdmNotificationFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmNotificationFilter filter = new IdmNotificationFilter();
		filter.setText(getParameterConverter().toString(parameters, "text"));
		filter.setSender(getParameterConverter().toString(parameters, "sender"));
		filter.setRecipient(getParameterConverter().toString(parameters, "recipient"));
		filter.setState(getParameterConverter().toEnum(parameters, "state", NotificationState.class));
		filter.setFrom(getParameterConverter().toDateTime(parameters, "from"));
		filter.setTill(getParameterConverter().toDateTime(parameters, "till"));
		filter.setSent(getParameterConverter().toBoolean(parameters, "sent"));
		filter.setNotificationType(notificationConfigurationService.toSenderType(getParameterConverter().toString(parameters, "notificationType")));
		filter.setParent(getParameterConverter().toUuid(parameters, "parent"));
		filter.setTopic(getParameterConverter().toString(parameters, "topic"));
		return filter;
	}
}
