package eu.bcvsolutions.idm.core.rest.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmScriptFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Default controller for scripts, basic methods.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/scripts")
@Tag(
		name = IdmScriptController.TAG,

		description = "Groovy scripts administration"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE

//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmScriptController extends AbstractReadWriteDtoController<IdmScriptDto, IdmScriptFilter> {
	
	protected static final String TAG = "Scripts";
	private final IdmScriptService service;
	//
	@Autowired private AttachmentManager attachmentManager;
	
	@Autowired
	public IdmScriptController(IdmScriptService service) {
		super(service);
		//
		this.service = service;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Search scripts (/search/quick alias)", 
			/* nickname = "searchScripts", */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Search scripts", 
			/* nickname = "searchQuickScripts", */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete scripts (selectbox usage)", 
			/* nickname = "autocompleteScripts", */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Script detail", 
			/* nickname = "getScript", */ 
			/* response = IdmScriptDto.class, */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update script", 
			/* nickname = "postScript", */ 
			/* response = IdmScriptDto.class, */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_CREATE,
						CoreGroupPermission.SCRIPT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_CREATE,
						CoreGroupPermission.SCRIPT_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull IdmScriptDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@Operation(
			summary = "Update script", 
			/* nickname = "putScript", */ 
			/* response = IdmScriptDto.class, */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull IdmScriptDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete script", 
			/* nickname = "deleteScript", */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/redeploy", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@Operation(
			summary = "Redeploy script", 
			/* nickname = "redeployScript", */ 
			/* response = IdmScriptDto.class, */ 
			tags = { IdmScriptController.TAG },
			description = "Redeploy script. Redeployed will be only scripts, that has pattern in resource."
					+ " Before save newly loaded DO will be backup the old script into backup directory.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            CoreGroupPermission.SCRIPT_UPDATE }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            CoreGroupPermission.SCRIPT_UPDATE })
            }
    )
	public ResponseEntity<?> redeploy(
			@Parameter(name = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmScriptDto script = service.get(backendId);
		if (script == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		script = service.redeploy(script, IdmBasePermission.UPDATE);
		return new ResponseEntity<>(toModel(script), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/backup", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Backup script", 
			/* nickname = "backupScript", */ 
			/* response = IdmScriptDto.class, */ 
			tags = { IdmScriptController.TAG }, 
						description = "Backup template to directory given in application properties.")
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public ResponseEntity<?> backup(
			@Parameter(name = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmScriptDto script = service.get(backendId);
		if (script == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, backendId);
		}
		service.backup(script);
		return new ResponseEntity<>(toModel(script), HttpStatus.OK);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countScripts", */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							CoreGroupPermission.SCRIPT_COUNT }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							CoreGroupPermission.SCRIPT_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@Operation(
			summary = "Update script",
			/* nickname = "patchScript", */ 
			/* response = IdmScriptDto.class, */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							CoreGroupPermission.SCRIPT_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							CoreGroupPermission.SCRIPT_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			@Parameter(name = "Script's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
					throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnScript", */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "Script's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Upload scripts.
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_CREATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.SCRIPT_UPDATE + "')")
	@Operation(
			summary = "Upload scripts.", 
			/* nickname = "uploadScripts", */ 
			tags = { IdmScriptController.TAG }, 
						description="Scripts in archive (ZIP) or single scripts (XML) can be uploaded.")
    @SecurityRequirements(
        value = {
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                    CoreGroupPermission.SCRIPT_CREATE,
                    CoreGroupPermission.SCRIPT_UPDATE}),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                    CoreGroupPermission.SCRIPT_CREATE,
                    CoreGroupPermission.SCRIPT_UPDATE})
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
		service.deploy(attachment, IdmBasePermission.UPDATE);
		// more scripts can be deployed => no content status (prevent to return large list)
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			/* nickname = "availableBulkAction", */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Process bulk action for script definition", 
			/* nickname = "bulkAction", */ 
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for script definition", 
			/* nickname = "prevalidateBulkAction", */ 
			/* response = IdmBulkActionDto.class, */ 
			tags = { IdmScriptController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						CoreGroupPermission.SCRIPT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						CoreGroupPermission.SCRIPT_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@Override
	protected IdmScriptFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmScriptFilter(parameters, getParameterConverter());
	}
}
