	package eu.bcvsolutions.idm.core.notification.rest.impl;

    import java.util.Set;

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
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestMethod;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.web.bind.annotation.RestController;

    import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
    import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
    import eu.bcvsolutions.idm.core.api.rest.BaseController;
    import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
    import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;
    import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
    import eu.bcvsolutions.idm.core.notification.api.service.IdmEmailLogService;
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
 * Read email logs.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-emails")
@Tag(
		name = IdmEmailLogController.TAG, 
		description = "Emails history"//,

		//produces = BaseController.APPLICATION_HAL_JSON_VALUE

//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmEmailLogController extends AbstractReadWriteDtoController<IdmEmailLogDto, IdmNotificationFilter> {
	
	protected static final String TAG = "Notification logs - email";
	
	@Autowired
	public IdmEmailLogController(IdmEmailLogService entityLookupService) {
		super(entityLookupService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@Operation(
			summary = "Search email logs (/search/quick alias)", 
			/* nickname = "searchEmailLogs", */
			tags = { IdmEmailLogController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATION_READ })
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
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search email logs", 
			/* nickname = "searchQuickEmailLogs", */
			tags = { IdmEmailLogController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATION_READ })
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
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Email log detail", 
			/* nickname = "getEmailLog", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmEmailLogDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmEmailLogController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATION_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATION_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Email log's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATION_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnEmail", */
			tags = { IdmEmailLogController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						NotificationGroupPermission.NOTIFICATION_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						NotificationGroupPermission.NOTIFICATION_READ})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Email uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
