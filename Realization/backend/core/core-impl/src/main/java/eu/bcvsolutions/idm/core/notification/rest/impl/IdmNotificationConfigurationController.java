package eu.bcvsolutions.idm.core.notification.rest.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationConfigurationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationConfigurationService;
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
 * Configuration for notification routing.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-configurations")
@Tag(
		name = IdmNotificationConfigurationController.TAG, 
		description = "Configure message sending"//,

		


)
public class IdmNotificationConfigurationController extends AbstractReadWriteDtoController<NotificationConfigurationDto, IdmNotificationConfigurationFilter> {

	protected static final String TAG = "Notification configuration";
	private final IdmNotificationConfigurationService configurationService;
	
	@Autowired
	public IdmNotificationConfigurationController(IdmNotificationConfigurationService configurationService) {
		super(configurationService);
		this.configurationService = configurationService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	@Operation(
			summary = "Search notification configuration items (/search/quick alias)", 
			/* nickname = "searchNotificationConfigurations", */ 
			tags = { IdmNotificationConfigurationController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search notification configuration items", 
			/* nickname = "searchQuickNotificationConfigurations", */ 
			tags = { IdmNotificationConfigurationController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Notification configuration item detail", 
			/* nickname = "getNotificationConfiguration", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = NotificationConfigurationDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmNotificationConfigurationController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Item's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_CREATE + "')"
			+ " or hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update notification configuration item", 
			/* nickname = "postNotificationConfiguration", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = NotificationConfigurationDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmNotificationConfigurationController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_CREATE,
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_CREATE,
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody @NotNull NotificationConfigurationDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update notification configuration item", 
			/* nickname = "putNotificationConfiguration", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = NotificationConfigurationDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmNotificationConfigurationController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "Item's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody @NotNull NotificationConfigurationDto dto){
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONCONFIGURATION_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete notification configuration item", 
			/* nickname = "deleteNotificationConfiguration", */ 
			tags = { IdmNotificationConfigurationController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATIONCONFIGURATION_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "Item's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Returns registered senders notification types.
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/all/notification-types", method = RequestMethod.GET)
	@Operation(
			summary = "Supported notification (sender) type", 
			/* nickname = "getSupportedNotificationTypes", */
			tags = { IdmNotificationConfigurationController.TAG },
			description = "Returns registered senders notification types.")
	public List<String> getSupportedNotificationTypes() {
		Set<String> types = configurationService.getSupportedNotificationTypes();
		List<String> results = Lists.newArrayList(types);
		Collections.sort(results);
		//
		return results;
	}
}
