package eu.bcvsolutions.idm.document.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.document.DocumentModuleDescriptor;
import eu.bcvsolutions.idm.document.domain.DocumentGroupPermission;
import eu.bcvsolutions.idm.document.dto.DocumentDto;
import eu.bcvsolutions.idm.document.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.document.service.api.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Document controller
 *
 */
@RestController
@Enabled(DocumentModuleDescriptor.MODULE_ID)
@RequestMapping(value = DocumentController.DOCUMENT_BASE_PATH)
@Tag(name = DocumentController.TAG, description = "Document operations")
public class DocumentController extends AbstractReadWriteDtoController<DocumentDto, DocumentFilter> {

	public static final String DOCUMENT_BASE_PATH = BaseController.BASE_PATH + "/documents";
	
	protected static final String TAG = "Documents";

	public DocumentController(DocumentService service) {
		super(service);
	}

	/*
	AuthenticationFilter

	023-12-17 20:00:05.062  WARN 49088 --- [           main] o.s.s.c.a.web.builders.WebSecurity       : You are asking Spring Security to ignore Ant [pattern='/error/**']. This is not recommended -- please use permitAll via HttpSecurity#authorizeHttpRequests instead.
2023-12-17 20:00:05.062  WARN 49088 --- [           main] o.s.s.c.a.web.builders.WebSecurity       : You are asking Spring Security to ignore Ant [pattern='/api/v1/doc']. This is not recommended -- please use permitAll via HttpSecurity#authorizeHttpRequests instead.
2023-12-17 20:00:05.062  WARN 49088 --- [           main] o.s.s.c.a.web.builders.WebSecurity       : You are asking Spring Security to ignore Ant [pattern='/api/v1/doc/**']. This is not recommended -- please use permitAll via HttpSecurity#authorizeHttpRequests instead.
	*/


	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_READ + "')")
	@Operation(
			summary = "Search documents (/search/quick alias)",
			operationId = "searchDocuments",
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { DocumentGroupPermission.DOCUMENT_READ }),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { DocumentGroupPermission.DOCUMENT_READ })
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
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_READ + "')")
	@Operation(
			summary = "Search documents quick",
			operationId = "searchQuickDocuments",
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { DocumentGroupPermission.DOCUMENT_READ }),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { DocumentGroupPermission.DOCUMENT_READ })
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
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete document (selectbox usage)",
			operationId = "autocompleteDocuments",
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { DocumentGroupPermission.DOCUMENT_AUTOCOMPLETE }),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { DocumentGroupPermission.DOCUMENT_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countDocuments",
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { DocumentGroupPermission.DOCUMENT_COUNT }),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { DocumentGroupPermission.DOCUMENT_COUNT })
	})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@Operation(
			summary = "Get document",
			operationId = "getDocument",
			responses = @ApiResponse(
					responseCode = "200",
					content = {
							@Content(
									mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
									schema = @Schema(
											implementation = DocumentDto.class
									)
							)
					}
			),
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { DocumentGroupPermission.DOCUMENT_READ }),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { DocumentGroupPermission.DOCUMENT_READ })
	})
	public ResponseEntity<?> get(
			@Parameter(description = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_CREATE + "') or hasAuthority('" + DocumentGroupPermission.DOCUMENT_UPDATE + "')")
	@Operation(
			summary = "Create / update document",
			operationId = "postDocument",
			responses = @ApiResponse(
					responseCode = "200",
					content = {
							@Content(
									mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
									schema = @Schema(
											implementation = DocumentDto.class
									)
							)
					}
			),
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
					DocumentGroupPermission.DOCUMENT_CREATE,
					DocumentGroupPermission.DOCUMENT_UPDATE}),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
					DocumentGroupPermission.DOCUMENT_CREATE,
					DocumentGroupPermission.DOCUMENT_UPDATE})
	}
	)
	public ResponseEntity<?> post(@Valid @RequestBody DocumentDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_UPDATE + "')")
	@Operation(
			summary = "Update document",
			operationId = "putDocument",
			responses = @ApiResponse(
					responseCode = "200",
					content = {
							@Content(
									mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
									schema = @Schema(
											implementation = DocumentDto.class
									)
							)
					}
			),
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { DocumentGroupPermission.DOCUMENT_UPDATE }),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { DocumentGroupPermission.DOCUMENT_UPDATE })
	})
	public ResponseEntity<?> put(
			@Parameter(description = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@Valid @RequestBody DocumentDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_UPDATE + "')")
	@Operation(
			summary = "Update document",
			operationId = "patchDocument",
			responses = @ApiResponse(
					responseCode = "200",
					content = {
							@Content(
									mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
									schema = @Schema(
											implementation = DocumentDto.class
									)
							)
					}
			),
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { DocumentGroupPermission.DOCUMENT_UPDATE }),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { DocumentGroupPermission.DOCUMENT_UPDATE })
	})
	public ResponseEntity<?> patch(
			@Parameter(description = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_DELETE + "')")
	@Operation(
			summary = "Delete document",
			operationId = "deleteDocument",
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { DocumentGroupPermission.DOCUMENT_DELETE }),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { DocumentGroupPermission.DOCUMENT_DELETE })
	})
	public ResponseEntity<?> delete(
			@Parameter(description = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + DocumentGroupPermission.DOCUMENT_READ + "')"
			+ " or hasAuthority('" + DocumentGroupPermission.DOCUMENT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnDocument",
			tags = { DocumentController.TAG })
	@SecurityRequirements({
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
					DocumentGroupPermission.DOCUMENT_READ,
					DocumentGroupPermission.DOCUMENT_AUTOCOMPLETE}),
			@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
					DocumentGroupPermission.DOCUMENT_READ,
					DocumentGroupPermission.DOCUMENT_AUTOCOMPLETE})
	}
	)
	public Set<String> getPermissions(
			@Parameter(description = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
