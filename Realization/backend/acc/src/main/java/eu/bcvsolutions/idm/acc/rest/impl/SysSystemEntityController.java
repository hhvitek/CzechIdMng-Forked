package eu.bcvsolutions.idm.acc.rest.impl;

import javax.validation.constraints.NotNull;

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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Entities on target system.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-entities")
@Tag(name = SysSystemEntityController.TAG, description = "Raw entities on target system")
public class SysSystemEntityController extends AbstractReadWriteDtoController<SysSystemEntityDto, SysSystemEntityFilter> {

	protected static final String TAG = "System entities";
	
	@Autowired
	public SysSystemEntityController(SysSystemEntityService systemEntityService) {
		super(systemEntityService);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
        summary = "Search system entities (/search/quick alias)",
        operationId = "searchSystemEntities"
    )
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_READ})
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
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search system entities", 
			operationId = "searchQuickSystemEntities",
			tags = { SysSystemEntityController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_READ})
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
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "System entity detail", 
			operationId = "getSystemEntity",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemEntityDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemEntityController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "System entity's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update system entity", 
			operationId = "postSystemEntity",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemEntityDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemEntityController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_CREATE,
						AccGroupPermission.SYSTEM_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_CREATE,
						AccGroupPermission.SYSTEM_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull SysSystemEntityDto dto) {
		return super.post(dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update system entity",
			operationId = "putSystemEntity",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemEntityDto.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemEntityController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "System entity's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @NotNull SysSystemEntityDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete system entity", 
			operationId = "deleteSystemEntity",
			tags = { SysSystemEntityController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "System entity's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-object", method = RequestMethod.GET)
	@Operation(
			summary = "Connector object for the system entity", 
			operationId = "getConnectorObject",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IcConnectorObject.class
                                    )
                            )
                    }
            ), 
			tags = { SysSystemEntityController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<IcConnectorObject> getConnectorObject(
			 @Parameter(description = "System entity's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemEntityDto systemEntity = this.getDto(backendId);
		if (systemEntity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IcConnectorObject connectorObject = ((SysSystemEntityService)getService())
				.getConnectorObject(systemEntity, IdmBasePermission.READ);
		if (connectorObject == null) {
			return new ResponseEntity<IcConnectorObject>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<IcConnectorObject>(connectorObject, HttpStatus.OK);
	}

	@Override
	protected SysSystemEntityFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new SysSystemEntityFilter(parameters, getParameterConverter());
	}
}
