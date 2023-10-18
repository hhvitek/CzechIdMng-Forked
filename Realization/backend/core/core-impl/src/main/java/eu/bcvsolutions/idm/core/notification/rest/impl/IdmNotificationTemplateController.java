package eu.bcvsolutions.idm.core.notification.rest.impl;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.multipart.MultipartFile;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationTemplateFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmNotificationTemplateService;
import eu.bcvsolutions.idm.core.notification.domain.NotificationGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Read and write email templates (Apache velocity engine)
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/notification-templates")
@Tag(
		name = IdmNotificationTemplateController.TAG, 
		description = "Configure notification templates"//,
		 
		
		

)
public class IdmNotificationTemplateController extends AbstractEventableDtoController<IdmNotificationTemplateDto, IdmNotificationTemplateFilter> {
	
	protected static final String TAG = "Notification templates";
	private final IdmNotificationTemplateService notificationTemplateService;
	//
	@Autowired private AttachmentManager attachmentManager;
	
	@Autowired
	public IdmNotificationTemplateController(IdmNotificationTemplateService notificationTemplateService) {
		super(notificationTemplateService);
		this.notificationTemplateService = notificationTemplateService;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@Operation(
			summary = "Search notification templates (/search/quick alias)", 
			/* nickname = "searchNotificationTemplates", */ 
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search notification templates", 
			/* nickname = "searchQuickNotificationTemplates", */ 
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return this.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete notification templates (selectbox usage)", 
			/* nickname = "autocompleteNotificationTemplates", */ 
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_AUTOCOMPLETE })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countNotificationTemplates", */ 
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "Notification template detail", 
			/* nickname = "getNotificationTemplate", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmNotificationTemplateDto	.class
                                    )
                            )
                    }
            ),
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE + "')"
			+ " or hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update notification template", 
			/* nickname = "postNotificationTemplate", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmNotificationTemplateDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE,
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE,
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody @NotNull IdmNotificationTemplateDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update notification template", 
			/* nickname = "putNotificationTemplate", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmNotificationTemplateDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@Valid @RequestBody @NotNull IdmNotificationTemplateDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_DELETE + "')")
	@Operation(
			summary = "Delete notification template", 
			/* nickname = "deleteNotificationTemplate", */ 
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	/**
	 * Upload templates.
	 * 
	 * @param name
	 * @param fileName
	 * @param data
	 * @return
	 * @throws IOException
	 * @since 10.6.0
	 */
	@ResponseBody
	@RequestMapping(value = "/deploy", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE + "')"
			+ " or hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	@Operation(
			summary = "Upload templates.", 
			/* nickname = "uploadNotificationTemplates", */ 
			tags = { IdmNotificationTemplateController.TAG },
			description="Templates in archive (ZIP) or single templates (XML) can be uploaded.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE,
                            NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE}),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            NotificationGroupPermission.NOTIFICATIONTEMPLATE_CREATE,
                            NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE})
            }
    )
	public ResponseEntity<?> deploy(String name, String fileName, MultipartFile data) throws IOException {
		// save attachment
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachment.setName(fileName);
		attachment.setMimetype(StringUtils.isBlank(data.getContentType()) ? AttachableEntity.DEFAULT_MIMETYPE : data.getContentType());
		attachment.setInputData(data.getInputStream());
		attachment = attachmentManager.saveAttachment(null, attachment); // owner and version is resolved after attachment is saved
		// deploy
		notificationTemplateService.deploy(attachment, IdmBasePermission.UPDATE);
		// more templates can be deployed => no content status (prevent to return large list)
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/redeploy", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE + "')")
	@Operation(
			summary = "Redeploy notification template", 
			/* nickname = "redeployNotificationTemplate", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmNotificationTemplateDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmNotificationTemplateController.TAG },
			description = "Redeploy template. Redeployed will be only templates, that has pattern in resource."
					+ " Before save newly loaded DO will be backup the old template into backup directory.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            NotificationGroupPermission.NOTIFICATIONTEMPLATE_UPDATE })
            }
    )
	public ResponseEntity<?> redeploy(
			 @Parameter(description = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmNotificationTemplateDto template = notificationTemplateService.get(backendId);
		if (template == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		//
		template = notificationTemplateService.redeploy(template, IdmBasePermission.UPDATE);
		return new ResponseEntity<>(toModel(template), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/backup", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@Operation(
			summary = "Backup notification template", 
			/* nickname = "backupNotificationTemplate", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmNotificationTemplateDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmNotificationTemplateController.TAG },
			description = "Backup template to directory given in application properties.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ })
            }
    )
	public ResponseEntity<?> backup(
			 @Parameter(description = "Template's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmNotificationTemplateDto template = notificationTemplateService.get(backendId);
		if (template == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		notificationTemplateService.backup(template);
		return new ResponseEntity<>(toModel(template), HttpStatus.OK);
	}
	
	/**
	 * Get available bulk actions for notification templates
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			/* nickname = "availableBulkAction", */ 
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	/**
	 * Process bulk action for notification templates
	 *
	 * @param bulkAction
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@Operation(
			summary = "Process bulk action for notification templates", 
			/* nickname = "bulkAction", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	/**
	 * Prevalidate bulk action for notification template
	 *
	 * @param bulkAction
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for notification templates", 
			/* nickname = "prevalidateBulkAction", */ 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmBulkActionDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmNotificationTemplateController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						NotificationGroupPermission.NOTIFICATIONTEMPLATE_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@Override
	protected IdmNotificationTemplateFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmNotificationTemplateFilter(parameters, getParameterConverter());
	}
}
