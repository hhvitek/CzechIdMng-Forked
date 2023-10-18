package eu.bcvsolutions.idm.acc.rest.impl;

import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
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
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * System synchronization configurations.
 * 
 * @author svandav
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-synchronization-configs")
@Tag(name = SysSyncConfigController.TAG, // tags = SysSyncConfigController.TAG, //
		description = "Synchronization setting"//, //

		

)
public class SysSyncConfigController
		extends AbstractEventableDtoController<AbstractSysSyncConfigDto, SysSyncConfigFilter> {

	protected static final String TAG = "Synchronization - configurations";
	//
	private final SysSyncConfigService service;
	private final SynchronizationService synchronizationService;

	@Autowired
	public SysSyncConfigController(SysSyncConfigService service, SynchronizationService synchronizationService) {
		super(service);
		
		Assert.notNull(synchronizationService, "Service is required.");

		this.service = service;
		this.synchronizationService = synchronizationService;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(summary = "Search synchronization configs (/search/quick alias)", /* nickname = "searchSyncConfigs", */ tags = {
			SysSyncConfigController.TAG })
    @SecurityRequirements(
        value = {
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                    AccGroupPermission.SYSTEM_READ }),
            @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                    AccGroupPermission.SYSTEM_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(summary = "Search synchronization configs", /* nickname = "searchQuickSyncConfigs", */ tags = {
			SysSyncConfigController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ })
        }
    )
	@PageableAsQueryParam
	public CollectionModel<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete synchronization configs (selectbox usage)"
			/*, nickname = "autocompleteSyncConfigs" */)
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            AccGroupPermission.SYSTEM_AUTOCOMPLETE }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            AccGroupPermission.SYSTEM_AUTOCOMPLETE })
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
	public Page<AbstractSysSyncConfigDto> find(SysSyncConfigFilter filter, Pageable pageable,
			BasePermission permission) {
		Page<AbstractSysSyncConfigDto> results = super.find(filter, pageable, permission);
		// load running state
		results.getContent().forEach(syncConfig -> {
			syncConfig.setRunning(service.isRunning(syncConfig));
		});
		//
		return results;
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(summary = "Synchronization config detail", /* nickname = "getSyncConfig", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSyncConfig.class
                                    )
                            )
                    }
            ), tags = {
			SysSyncConfigController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Config's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(summary = "Create / update synchronization config", /* nickname = "postSyncConfig", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSyncConfig.class
                                    )
                            )
                    }
            ), tags = {
			SysSyncConfigController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_UPDATE })
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull AbstractSysSyncConfigDto dto)
			throws HttpMessageNotReadableException {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(summary = "Update synchronization config", /* nickname = "putSyncConfig", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSyncConfig.class
                                    )
                            )
                    }
            ), tags = {
			SysSyncConfigController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_UPDATE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "Config's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull AbstractSysSyncConfigDto dto) throws HttpMessageNotReadableException {
		// Validate
		this.validate(this.getService().get(backendId));
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(summary = "Delete synchronization config", /* nickname = "deleteSyncConfig", */ tags = {
			SysSyncConfigController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_DELETE }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "Config's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		// Validate
		this.validate(this.getService().get(backendId));
		return super.delete(backendId);
	}

	/**
	 * Start synchronization
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYNCHRONIZATION_CREATE + "')")
	@RequestMapping(value = "/{backendId}/start", method = RequestMethod.POST)
	@Operation(summary = "Start synchronization", /* nickname = "startSynchronization", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSyncConfig.class
                                    )
                            )
                    }
            ), tags = {
			SysSyncConfigController.TAG }, description = "Start synchronization by given config.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            AccGroupPermission.SYNCHRONIZATION_CREATE }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            AccGroupPermission.SYNCHRONIZATION_CREATE }) ,
            }
    )

	public ResponseEntity<?> startSynchronization(
			 @Parameter(description = "Config's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		// Validate
		this.validate(this.getService().get(backendId));

		return new ResponseEntity<>(
				toModel(this.synchronizationService.startSynchronization(this.getService().get(backendId))),
				HttpStatus.OK);
	}

	/**
	 * Cancel synchronization
	 * 
	 * @param backendId
	 * @returnion
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYNCHRONIZATION_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/cancel", method = RequestMethod.POST)
	@Operation(summary = "Cancel synchronization", /* nickname = "cancelSynchronization", */            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSyncConfig.class
                                    )
                            )
                    }
            ), tags = {
			SysSyncConfigController.TAG }, description = "Cancel synchronization by given config.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            AccGroupPermission.SYNCHRONIZATION_UPDATE }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            AccGroupPermission.SYNCHRONIZATION_UPDATE })
            }
    )
	public ResponseEntity<?> cancelSynchronization(
			 @Parameter(description = "Config's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return new ResponseEntity<>(
				toModel(this.synchronizationService.stopSynchronization(this.getService().get(backendId))),
				HttpStatus.OK);
	}

	/**
	 * Is synchronization running
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/is-running", method = RequestMethod.POST)
	@Operation(summary = "Synchronization is running", /* nickname = "isRunningSynchronization", */ tags = {
			SysSyncConfigController.TAG }, description = "If sync by given config's identifier is running.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            AccGroupPermission.SYSTEM_READ }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            AccGroupPermission.SYSTEM_READ })
            }
    )
	public ResponseEntity<?> isRunningSynchronization(
			 @Parameter(description = "Config's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		boolean running = service.isRunning(this.getService().get(backendId));
		return new ResponseEntity<>(running, HttpStatus.OK);
	}
	
	@Override
	protected SysSyncConfigFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysSyncConfigFilter filter = new SysSyncConfigFilter(parameters);
		filter.setSystemId(getParameterConverter().toUuid(parameters, "systemId"));
		filter.setDifferentialSync(getParameterConverter().toBoolean(parameters, "differentialSync"));
		return filter;
	}

	private void validate(AbstractSysSyncConfigDto config) {
		Assert.notNull(config, "Configuration is required.");
		Assert.notNull(config.getId(), "Configuration identifier is required.");

		// Synchronization can not be running
		boolean running = ((SysSyncConfigService) this.getService()).isRunning(this.getService().get(config.getId()));
		if (running) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}
	}
}
