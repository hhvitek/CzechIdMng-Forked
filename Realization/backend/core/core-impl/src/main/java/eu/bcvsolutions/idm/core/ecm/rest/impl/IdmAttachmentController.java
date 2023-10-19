package eu.bcvsolutions.idm.core.ecm.rest.impl;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
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
import org.springframework.http.converter.HttpMessageNotReadableException;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.filter.IdmAttachmentFilter;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * ECM attachments
 * 
 * @author Radek Tomiška
 * @since 9.2.0
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/attachments")
@Tag(name = IdmAttachmentController.TAG, description = "Operations with attachments (metadata, upload)")
public class IdmAttachmentController extends AbstractReadWriteDtoController<IdmAttachmentDto, IdmAttachmentFilter>  {

	protected static final String TAG = "Form attributes";
	//
	private final AttachmentManager attachmentManager;
	
	@Autowired
	public IdmAttachmentController(AttachmentManager attachmentManager) {
		super(attachmentManager);
		//
		this.attachmentManager = attachmentManager;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Search form attributes (/search/quick alias)", 
			operationId = "searchAttachments",
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Search form attributes", 
			operationId = "searchQuickAttachments",
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Autocomplete form attributes (selectbox usage)", 
			operationId = "autocompleteAttachments",
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
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
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			operationId = "countAttachments",
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Form attribute detail", 
			operationId = "getAttachment",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAttachmentDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')"
			+ " or hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Create / update form attribute", 
			operationId = "postAttachment",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAttachmentDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						IdmGroupPermission.APP_ADMIN,
						IdmGroupPermission.APP_ADMIN}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						IdmGroupPermission.APP_ADMIN,
						IdmGroupPermission.APP_ADMIN})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmAttachmentDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Update form attribute",
			operationId = "putAttachment",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAttachmentDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "Form attribute's uuid identifier", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmAttachmentDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Patch form attribute", 
			operationId = "patchAttachment",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAttachmentDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "Form attribute's uuid identifier", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "Delete form attribute", 
			operationId = "deleteAttachment",
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Form attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			operationId = "getPermissionsOnAttachment",
			tags = { IdmAttachmentController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	public Set<String> getPermissions(
			 @Parameter(description = "Attribute's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@Operation(
			summary = "Upload file", 
			operationId = "uploadFile",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmAttachmentDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmAttachmentController.TAG }, 
			description=  "Upload file and save them as attachment.")
	public ResponseEntity<?> upload(
			@NotNull 
			@RequestParam(required = true, name = "fileName")
			String fileName,
			@RequestParam(required = true, name = "data") 
			MultipartFile data) throws IOException {
		// save attachment
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setOwnerType(AttachmentManager.TEMPORARY_ATTACHMENT_OWNER_TYPE);
		attachment.setName(fileName);
		attachment.setMimetype(StringUtils.isBlank(data.getContentType()) ? AttachableEntity.DEFAULT_MIMETYPE : data.getContentType());
		attachment.setInputData(data.getInputStream());
		attachment = attachmentManager.saveAttachment(null, attachment); // owner and version is resolved after attachment is saved
		//
		return new ResponseEntity<>(toModel(attachment), HttpStatus.CREATED);
	}
}
