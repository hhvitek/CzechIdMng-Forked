package eu.bcvsolutions.idm.core.rest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.util.Strings;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmExportImportFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.ExportManager;
import eu.bcvsolutions.idm.core.api.service.IdmExportImportService;
import eu.bcvsolutions.idm.core.api.service.ImportManager;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
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
 * Export/Import controller
 * 
 * @author Vít Švanda
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/export-imports") 
@Tag(name = IdmExportImportController.TAG, description = "Exports and imports")
public class IdmExportImportController extends AbstractReadWriteDtoController<IdmExportImportDto, IdmExportImportFilter>  {

	protected static final String TAG = "Exports";
	@Autowired
	private ImportManager importManager;
	
	@Autowired
	public IdmExportImportController(
			IdmExportImportService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_READ + "')")
	@Operation(
			summary = "Search batchs (/search/quick alias)", 
			operationId = "searchBatchs", 
			tags = { IdmExportImportController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.EXPORTIMPORT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.EXPORTIMPORT_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_READ + "')")
	@Operation(
			summary = "Search batchs", 
			operationId = "searchQuickBatchs", 
			tags = { IdmExportImportController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.EXPORTIMPORT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.EXPORTIMPORT_READ })
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
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete batchs (selectbox usage)", 
			operationId = "autocompleteBatchs", 
			tags = { IdmExportImportController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.EXPORTIMPORT_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.EXPORTIMPORT_AUTOCOMPLETE })
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
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_READ + "')")
	@Operation(
			summary = "Batch detail", 
			operationId = "getBatch", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmExportImportDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmExportImportController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.EXPORTIMPORT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.EXPORTIMPORT_READ })
    })
	public ResponseEntity<?> get(
			 @Parameter(description = "Batch's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	/**
	 * Upload new import
	 * 
	 * @param name
	 * @param fileName
	 * @param data
	 * @return
	 * @throws IOException
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_CREATE + "')")
	@Operation(
			summary = "Upload new import zip. New import batch will be created.", 
			operationId = "uploadImport", 
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmExportImportDto.class
                                    )
                            )
                    }
            ), 
			tags = { IdmExportImportController.TAG }, 
						description = "Upload new import zip. New import batch will be created.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.EXPORTIMPORT_CREATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.EXPORTIMPORT_CREATE})
        }
    )
	public EntityModel<IdmExportImportDto> uploadImport(String name, String fileName, MultipartFile data)
			throws IOException {
		IdmExportImportDto batch = importManager.uploadImport(name, fileName, data.getInputStream(), IdmBasePermission.CREATE);
		Link selfLink = WebMvcLinkBuilder.linkTo(this.getClass()).slash(batch.getId()).withSelfRel();
		
		return new EntityModel<IdmExportImportDto>(batch, selfLink);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_DELETE + "')")
	@Operation(
			summary = "Delete batch", 
			operationId = "deleteBatch", 
			tags = { IdmExportImportController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.EXPORTIMPORT_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.EXPORTIMPORT_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "Batch's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_READ + "')"
			+ " or hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			operationId = "getPermissionsOnBatch", 
			tags = { IdmExportImportController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.EXPORTIMPORT_READ,
						CoreGroupPermission.EXPORTIMPORT_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.EXPORTIMPORT_READ,
						CoreGroupPermission.EXPORTIMPORT_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Batch's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/download", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_READ + "')")
	@Operation(
			summary = "Download export", 
			operationId = "downloadExport", 
			tags = { IdmExportImportController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.EXPORTIMPORT_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.EXPORTIMPORT_READ })
    })
	public ResponseEntity<InputStreamResource> download(
			 @Parameter(description = "Batch's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		//
		IdmExportImportDto batch = getDto(backendId);
		if (batch == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		try {
			// Batch read rights check was performed above (getDto).
			InputStream is = ((IdmExportImportService)getService()).download(batch);
			//
			// Generate name of ZIP from batch name.
			String zipName = batch.getExecutorName();
			if (Strings.isNotEmpty(batch.getName())) {
				String spinaledName = SpinalCase.format(batch.getName());
				if (spinaledName.length() > 30) {
					spinaledName = spinaledName.substring(0, 29);
				}
				zipName = MessageFormat.format("{0}.{1}", spinaledName, ExportManager.EXTENSION_ZIP);
			}
			return ResponseEntity.ok()
					.contentLength(is.available())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", zipName))
					.body(new InputStreamResource(is));
		} catch (Exception ex) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, ex);
		}
	}
	
	/**
	 * Execute import
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_UPDATE + "')"
			+ " or hasAuthority('" + CoreGroupPermission.EXPORTIMPORT_ADMIN + "')")
	@RequestMapping(value = "/{backendId}/execute-import", method = RequestMethod.PUT)
	@Operation(summary = "Execute import", operationId = "executeImport",            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmExportImportDto.class
                                    )
                            )
                    }
            ), tags = {
			IdmExportImportController.TAG },
					description = "Execute import. "
							+ "UPDATE import batch permission is needed for execute import in dry run mode, "
							+ "ADMIN import batch permission is needed for execute import otherwise.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            CoreGroupPermission.EXPORTIMPORT_UPDATE,
                            CoreGroupPermission.EXPORTIMPORT_ADMIN}),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            CoreGroupPermission.EXPORTIMPORT_UPDATE,
                            CoreGroupPermission.EXPORTIMPORT_ADMIN})
            }
    )
	public ResponseEntity<?> executeImport(
			 @Parameter(description = "Import batch UUID identifier.", required = true) @PathVariable @NotNull String backendId,
			 @Parameter(description = "Import batch is executed as dry run." ) @RequestParam("dryRun") boolean dryRun) {

		IdmExportImportDto batch = getDto(backendId);
		if (batch == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		
		return new ResponseEntity<>(
				toModel(importManager.executeImport(batch, dryRun, dryRun ? IdmBasePermission.UPDATE : IdmBasePermission.ADMIN)),
				HttpStatus.OK);
	}
	
	@Override
	protected IdmExportImportFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmExportImportFilter(parameters, getParameterConverter());
	}

}
