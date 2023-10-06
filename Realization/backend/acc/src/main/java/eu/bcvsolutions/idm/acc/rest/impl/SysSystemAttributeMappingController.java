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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

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
@Tag(
		name = SysSystemAttributeMappingController.TAG,
		description = "Attribute mapping configuration"//,
		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
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
	@Operation(
			summary = "Search system attribute mappings (/search/quick alias)"
			/* nickname = "searchSystemAttributeMappings", */
			 
			)
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_READ})
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search system attribute mappings",
			/* nickname = "searchQuickSystemAttributeMappings", */
			tags = { SysSystemAttributeMappingController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_READ})
        }
    )
	public CollectionModel<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "System attribute mapping detail",
			/* nickname = "getSystemAttributeMapping", */ 
			/* response = SysSystemAttributeMappingDto.class, */ 
			tags = { SysSystemAttributeMappingController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Attribute mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update system attribute mapping",
			/* nickname = "postSystemAttributeMapping", */ 
			/* response = SysSystemAttributeMappingDto.class, */ 
			tags = { SysSystemAttributeMappingController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull SysSystemAttributeMappingDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update system attribute mapping",
			/* nickname = "putSystemAttributeMapping", */ 
			/* response = SysSystemAttributeMappingDto.class, */ 
			tags = { SysSystemAttributeMappingController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "Attribute mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysSystemAttributeMappingDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete system attribute mapping",
			/* nickname = "deleteSystemAttributeMapping", */ 
			tags = { SysSystemAttributeMappingController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "Attribute mapping's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@ResponseBody
	@GetMapping(value = "script-usage/{backendId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@Operation(
			summary = "Script usage in mapping",
			/* nickname = "getScriptUsage", */
			tags = { SysSystemAttributeMappingController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.SCRIPT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.SCRIPT_READ })
        }
    )
	public CollectionModel<?> getScriptUsageInMapping(
			@Parameter(name = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return toCollectionModel(systemAttributeMappingService.getScriptUsage(backendId), SysSystemAttributeMappingDto.class);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{schemaAttrName}/value/{accountId}", method = RequestMethod.GET)
	@Operation(
			summary = "System attribute mapping value",
			/* nickname = "getSystemAttributeMappingValue", */
			/* response = Object.class, */
			tags = { SysSystemAttributeMappingController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "Schema attribute name", required = true)
			@PathVariable @NotNull String schemaAttrName,
			@Parameter(name = "Account's uuid identifier.", required = true)
			@PathVariable @NotNull String accountId) {

		Object transformedValueForAttributeAndAccount = systemAttributeMappingService.getTransformedValueForAttributeAndAccount(accountId, schemaAttrName);
		EntityModel<Object> resourceSupport = new EntityModel<>(transformedValueForAttributeAndAccount);
		return new ResponseEntity<>(resourceSupport, HttpStatus.OK);
	}
}
