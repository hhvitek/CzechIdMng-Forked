package eu.bcvsolutions.idm.acc.rest.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Schema attribute handling rest
 * 
 * @author svandav
 * @author Roman Kucera
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-attribute-mappings")
@Api(
		value = SysSystemAttributeMappingController.TAG, 
		tags = SysSystemAttributeMappingController.TAG, 
		description = "Attribute mapping configuration",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSystemAttributeMappingController extends AbstractReadWriteDtoController<SysSystemAttributeMappingDto, SysSystemAttributeMappingFilter> {

	protected static final String TAG = "System mapping - atributes";

	private final SysSystemAttributeMappingService systemAttributeMappingService;

	@Autowired
	public SysSystemAttributeMappingController(SysSystemAttributeMappingService service) {
		super(service);
		systemAttributeMappingService = service;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@ApiOperation(
			value = "Search system attribute mappings (/search/quick alias)", 
			nickname = "searchSystemAttributeMappings",
			tags = { SysSystemAttributeMappingController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
				})
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search system attribute mappings", 
			nickname = "searchQuickSystemAttributeMappings",
			tags = { SysSystemAttributeMappingController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
				})
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "System attribute mapping detail", 
			nickname = "getSystemAttributeMapping", 
			response = SysSystemAttributeMappingDto.class, 
			tags = { SysSystemAttributeMappingController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
					})
	public ResponseEntity<?> get(
			@ApiParam(value = "Attribute mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@ApiOperation(
			value = "Create / update system attribute mapping", 
			nickname = "postSystemAttributeMapping", 
			response = SysSystemAttributeMappingDto.class, 
			tags = { SysSystemAttributeMappingController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@RequestBody @NotNull SysSystemAttributeMappingDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Update system attribute mapping",
			nickname = "putSystemAttributeMapping", 
			response = SysSystemAttributeMappingDto.class, 
			tags = { SysSystemAttributeMappingController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Attribute mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysSystemAttributeMappingDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete system attribute mapping", 
			nickname = "deleteSystemAttributeMapping", 
			tags = { SysSystemAttributeMappingController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Attribute mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@ResponseBody
	@GetMapping(value = "script-usage/{backendId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@ApiOperation(
			value = "Script usage in mapping",
			nickname = "getScriptUsage",
			tags = { SysSystemAttributeMappingController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") })
				})
	public CollectionModel<?> getScriptUsageInMapping(
			@ApiParam(value = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return toCollectionModel(systemAttributeMappingService.getScriptUsage(backendId), SysSystemAttributeMappingDto.class);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{schemaAttrName}/value/{accountId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "System attribute mapping value",
			nickname = "getSystemAttributeMappingValue",
			response = Object.class,
			tags = { SysSystemAttributeMappingController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_READ, description = "")})
			})
	public ResponseEntity<?> get(
			@ApiParam(value = "Schema attribute name", required = true)
			@PathVariable @NotNull String schemaAttrName,
			@ApiParam(value = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String accountId) {

		Object transformedValueForAttributeAndAccount = systemAttributeMappingService.getTransformedValueForAttributeAndAccount(accountId, schemaAttrName);
		EntityModel<Object> resourceSupport = new EntityModel<>(transformedValueForAttributeAndAccount);
		return new ResponseEntity<>(resourceSupport, HttpStatus.OK);
	}
}
