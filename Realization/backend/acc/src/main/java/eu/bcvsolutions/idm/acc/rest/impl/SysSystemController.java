package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.connector.AbstractConnectorType;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccPasswordFilterRequestDto;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncItemLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.event.SystemEvent;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
import eu.bcvsolutions.idm.acc.service.api.SysRemoteServerService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.rest.WizardController;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.service.api.CheckLongPollingResult;
import eu.bcvsolutions.idm.core.model.service.api.LongPollingManager;
import eu.bcvsolutions.idm.core.rest.DeferredResultWrapper;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.domain.IcResultCode;
import eu.bcvsolutions.idm.ic.exception.IcCantConnectException;
import eu.bcvsolutions.idm.ic.exception.IcInvalidCredentialException;
import eu.bcvsolutions.idm.ic.exception.IcRemoteServerException;
import eu.bcvsolutions.idm.ic.exception.IcServerNotFoundException;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationFacade;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;


/**
 * Target system setting controller.
 *
 * @author Radek Tomiška
 * @author Vít Švanda
 * @author Ondřej Kopr
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/systems")
@Tag(name = SysSystemController.TAG, description = "Operations with target systems")
public class SysSystemController extends AbstractReadWriteDtoController<SysSystemDto, SysSystemFilter> implements WizardController<ConnectorTypeDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SysSystemController.class);
	
	public static final String PASSWORD_FILTER_BASE_ENDPOINT = "/password-filter";
	
	protected static final String TAG = "Systems";

	private final SysSystemService systemService;
	private final IcConfigurationFacade icConfiguration;
	private final ConfidentialStorage confidentialStorage;
	private final IdmFormDefinitionController formDefinitionController;
	//
	@Autowired private SysSyncItemLogService syncItemLogService;
	@Autowired private SysSyncLogService syncLogService;
	@Autowired private LongPollingManager longPollingManager;
	@Autowired private PasswordFilterManager passwordFilterManager;
	@Autowired private ConnectorManager connectorManager;
	@Autowired private SysRemoteServerService remoteServerService;

	@Autowired
	public SysSystemController(
			SysSystemService systemService,
			IdmFormDefinitionController formDefinitionController,
			IcConfigurationFacade icConfiguration,
			ConfidentialStorage confidentialStorage) {
		super(systemService);
		//
		Assert.notNull(systemService, "Service is required.");
		Assert.notNull(formDefinitionController, "Controller is required.");
		Assert.notNull(icConfiguration, "Configuration is required.");
		Assert.notNull(confidentialStorage, "Confidential storage is required.");
		//
		this.systemService = systemService;
		this.formDefinitionController = formDefinitionController;
		this.icConfiguration = icConfiguration;
		this.confidentialStorage = confidentialStorage;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
        summary = "Search systems (/search/quick alias)",
        operationId = "searchSystems"
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
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@Operation(
			summary = "Search systems",
			operationId = "searchQuickSystems",
			tags = { SysSystemController.TAG })
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
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete systems (selectbox usage)",
			operationId = "autocompleteSystems",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_AUTOCOMPLETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			operationId = "countSystems",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_COUNT }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_COUNT })
    })
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@Operation(
			summary = "System detail",
			operationId = "getSystem",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemDto.class
                                    )
                            )
                    }
            ),
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(method = RequestMethod.POST)
	@Operation(
			summary = "Create / update system",
			operationId = "postSystem",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemDto.class
                                    )
                            )
                    }
            ),
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_CREATE,
						AccGroupPermission.SYSTEM_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_CREATE,
						AccGroupPermission.SYSTEM_UPDATE})
        }
    )
	public ResponseEntity<?> post(@RequestBody @NotNull SysSystemDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@Operation(
			summary = "Update system",
			operationId = "putSystem",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemDto.class
                                    )
                            )
                    }
            ),
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public ResponseEntity<?> put(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, @RequestBody @NotNull SysSystemDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@Operation(
			summary = "Patch system",
			operationId = "patchSystem",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysSystemDto.class
                                    )
                            )
                    }
            ),
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public ResponseEntity<?> patch(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_DELETE + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@Operation(
			summary = "Delete system",
			operationId = "deleteSystem",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_DELETE })
    })
	public ResponseEntity<?> delete(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			operationId = "getPermissionsOnSystem",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_READ,
						AccGroupPermission.SYSTEM_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_READ,
						AccGroupPermission.SYSTEM_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/generate-schema", method = RequestMethod.POST)
	@Operation(
			summary = "Generate system schema",
			operationId = "generateSystemSchema",
			tags = { SysSystemController.TAG },
						description = "Genetares schema by system's connector configuration")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public ResponseEntity<?> generateSchema(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		systemService.generateSchema(system);
		return new ResponseEntity<>(toModel(system), HttpStatus.OK);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/duplicate", method = RequestMethod.POST)
	@Operation(
			summary = "Create system duplicate (copy)",
			operationId = "duplicateSystem",
			tags = { SysSystemController.TAG },
						description = "Creates system duplicate with all configurations - connector, schemas, mappings etc.. Duplicate is disabled by default.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public ResponseEntity<?> duplicate(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		EntityEvent<SysSystemDto> event = new SystemEvent(SystemEventType.DUPLICATE, system);
		SysSystemDto duplicate = systemService.publish(event, IdmBasePermission.UPDATE).getContent();
		return new ResponseEntity<>(toModel(duplicate), HttpStatus.OK);
	}

	/**
	 * Test usage only
	 *
	 * @return
	 */
	@Deprecated
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/test/create-test-system", method = RequestMethod.POST)
	@Operation(
			summary = "Create test system",
			operationId = "createTestSystem",
			tags = { SysSystemController.TAG },
						description = "Creates system with test connector configuration - usign local table \"system_users\".")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { IdmGroupPermission.APP_ADMIN }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { IdmGroupPermission.APP_ADMIN })
    })
	public ResponseEntity<?> createTestSystem() {
		systemService.createTestSystem();
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Returns connector form definition to given system
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 *
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-form-definition", method = RequestMethod.GET)
	@Operation(
			summary = "Connector configuration - form definition",
			operationId = "getConnectorFormDefinition",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public ResponseEntity<?> getConnectorFormDefinition(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getConnectorFormDefinition(system);
		//
		return new ResponseEntity<>(new EntityModel<>(formDefinition), HttpStatus.OK);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/pooling-connector-form-definition", method = RequestMethod.GET)
	@Operation(
			summary = "Pooling connector configuration - form definition",
			operationId = "getPoolingConnectorFormDefinition",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public ResponseEntity<?> getPoolingConnectorFormDefinition(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getPoolingConnectorFormDefinition(system);
		//
		return new ResponseEntity<>(new EntityModel<>(formDefinition), HttpStatus.OK);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/operation-options-connector-form-definition", method = RequestMethod.GET)
	@Operation(
			summary = "Operation options connector configuration - form definition",
			operationId = "getOperationOptionsConnectorFormDefinition",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public ResponseEntity<?> getOperationOptionsConnectorFormDefinition(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto system = getDto(backendId);
		if (system == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getOperationOptionsConnectorFormDefinition(system);
		//
		return new ResponseEntity<>(new EntityModel<>(formDefinition), HttpStatus.OK);
	}

	/**
	 * Returns filled connector configuration
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 *
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/connector-form-values", method = RequestMethod.GET)
	@Operation(
			summary = "Connector configuration - read values",
			operationId = "getConnectorFormValues",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public EntityModel<?> getConnectorFormValues(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getConnectorFormDefinition(entity);
		return formDefinitionController.getFormValues(entity, formDefinition);
	}


	/**
	 * Returns filled pooling connector configuration
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 *
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/pooling-connector-form-values", method = RequestMethod.GET)
	@Operation(
			summary = "Connector configuration - read values",
			operationId = "getPoolingConnectorFormValues",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public EntityModel<?> getPoolingConnectorFormValues(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getPoolingConnectorFormDefinition(entity);
		return formDefinitionController.getFormValues(entity, formDefinition);
	}

	/**
	 * Returns filled pooling connector configuration
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 *
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/operation-options-connector-form-values", method = RequestMethod.GET)
	@Operation(
			summary = "Connector configuration - read values",
			operationId = "getOperationOptionsConnectorFormValues",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public EntityModel<?> getOperationOptionsConnectorFormValues(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getOperationOptionsConnectorFormDefinition(entity);
		return formDefinitionController.getFormValues(entity, formDefinition);
	}



	/**
	 * Saves connector configuration form values
	 *
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/connector-form-values", method = RequestMethod.POST)
	@Operation(
			summary = "Connector configuration - save values",
			operationId = "postConnectorFormValues",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public EntityModel<?> saveConnectorFormValues(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getConnectorFormDefinition(entity);
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues);
	}



	/**
	 * Saves pooling connector configuration form values
	 *
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/pooling-connector-form-values", method = RequestMethod.POST)
	@Operation(
			summary = "Pooling connector configuration - save values",
			operationId = "postPoolingConnectorFormValues",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public EntityModel<?> savePoolingConnectorFormValues(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getPoolingConnectorFormDefinition(entity);
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues);
	}


	/**
	 * Saves operation options connector configuration form values
	 *
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/operation-options-connector-form-values", method = RequestMethod.POST)
	@Operation(
			summary = "Operation options connector configuration - save values",
			operationId = "postOperationOptionsConnectorFormValues",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public EntityModel<?> saveOperationOptionsConnectorFormValues(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {
		SysSystemDto entity = getDto(backendId);
		if (entity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmFormDefinitionDto formDefinition = getOperationOptionsConnectorFormDefinition(entity);
		return formDefinitionController.saveFormValues(entity, formDefinition, formValues);
	}

	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@RequestMapping(value = "/{backendId}/check", method = RequestMethod.GET)
	@Operation(
			summary = "Check system",
			operationId = "checkSystem",
			tags = { SysSystemController.TAG },
						description = "Check system connector configuration.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_UPDATE })
    })
	public ResponseEntity<?> checkSystem(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		systemService.checkSystem(super.getDto(backendId));
		return new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK);
	}

	/**
	 * Return all local connectors of given framework
	 *
	 * @param framework - ic framework
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/search/local")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Get available local connectors",
			operationId = "getAvailableLocalConnectors",
			tags = { SysSystemController.TAG },
						description = "Supported local conectors (on classpath).")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public ResponseEntity<Map<String, Set<IcConnectorInfo>>> getAvailableLocalConnectors(
			 @Parameter(description = "Connector framework.", example = "connId")
			@RequestParam(required = false) String framework) {
		Map<String, Set<IcConnectorInfo>> infos = new HashMap<>();
		if (framework != null) {
			if (!icConfiguration.getIcConfigs().containsKey(framework)) {
				throw new ResultCodeException(IcResultCode.IC_FRAMEWORK_NOT_FOUND,
						ImmutableMap.of("framework", framework));
			}
			infos.put(framework, icConfiguration.getIcConfigs().get(framework)
					.getAvailableLocalConnectors());

		} else {
			infos = icConfiguration.getAvailableLocalConnectors();
		}
		return new ResponseEntity<Map<String, Set<IcConnectorInfo>>>(infos, HttpStatus.OK);
	}

	/**
	 * Rest endpoints return available remote connectors.
	 * If entity hasn't set for remote or isn't exists return empty map of connectors
	 *
	 * @param backendId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "{backendId}/search/remote")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Get available remote connectors",
			operationId = "getAvailableRemoteConnectors",
			tags = { SysSystemController.TAG },
						description = "Supported remote conectors (by remote server configuration).")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public ResponseEntity<Map<String, Set<IcConnectorInfo>>> getAvailableRemoteConnectors(
			 @Parameter(description = "System's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		SysSystemDto dto = this.getDto(backendId);

		Map<String, Set<IcConnectorInfo>> infos = new HashMap<>();

		// if entity hasn't set up for remote return empty map
		if (dto == null || !dto.isRemote()) {
			return new ResponseEntity<Map<String, Set<IcConnectorInfo>>>(infos, HttpStatus.OK);
		}

 		Assert.notNull(dto.getConnectorServer(), "Connector server is required.");
 		//
 		try {
 			for (IcConfigurationService config: icConfiguration.getIcConfigs().values()) {
				SysConnectorServerDto server = dto.getConnectorServer();
				server.setPassword(this.confidentialStorage.getGuardedString(dto.getId(), SysSystem.class, SysSystemService.REMOTE_SERVER_PASSWORD));
				infos.put(config.getFramework(), config.getAvailableRemoteConnectors(server));
			}
		} catch (IcInvalidCredentialException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_INVALID_CREDENTIAL,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcServerNotFoundException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_NOT_FOUND,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcCantConnectException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_CANT_CONNECT,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		} catch (IcRemoteServerException e) {
			throw new ResultCodeException(AccResultCode.REMOTE_SERVER_UNEXPECTED_ERROR,
					ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e);
		}
		//
		return new ResponseEntity<Map<String, Set<IcConnectorInfo>>>(infos, HttpStatus.OK);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			operationId = "availableBulkAction",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { AccGroupPermission.SYSTEM_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { AccGroupPermission.SYSTEM_READ })
    })
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Process bulk action for role",
			operationId = "bulkAction",
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
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action for role",
			operationId = "prevalidateBulkAction",
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
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}

	/**
	 * Long polling for check sync in progress for given system
	 *
	 * @param backendId - system ID
	 *
	 * @return DeferredResult<OperationResultDto>, where:
	 *
	 * - RUNNING = Some sync are not resolved, but some sync was changed (since previous check).
	 * - NOT_EXECUTED = Deferred-result expired
	 * - BLOCKED - Long polling is disabled
	 *
	 */
	@ResponseBody
	@RequestMapping(value = "{backendId}/check-running-sync", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Check changes of unresloved sync for the system (Long-polling request).",
			operationId = "checkRunningSyncs",
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						AccGroupPermission.SYSTEM_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						AccGroupPermission.SYSTEM_READ})
        }
    )
	public DeferredResult<OperationResultDto> checkRunningSyncs(
			 @Parameter(description = "System's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		SysSystemDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		UUID systemId = dto.getId();

		DeferredResultWrapper result = new DeferredResultWrapper( //
				systemId, //
				dto.getClass(),//
				new DeferredResult<OperationResultDto>( //
						30000l, new OperationResultDto(OperationState.NOT_EXECUTED)) //
		); //

		result.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result,
					LongPollingSubscriber subscriber) {
				checkDeferredRequest(result, subscriber);
			}
		});

		// If isn't long polling enabled, then Blocked response will be sent.
		if (!longPollingManager.isLongPollingEnabled()) {
			result.getResult().setResult(new OperationResultDto(OperationState.BLOCKED));
			return result.getResult();
		}

		longPollingManager.addSuspendedResult(result);

		return result.getResult();
	}

	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	@RequestMapping(value = PASSWORD_FILTER_BASE_ENDPOINT + "/validate", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_PASSWORDFILTERVALIDATE + "')")
	@Operation(
			summary = "Validate password request from resources with password filters including check for unform password defintions",
			operationId = "validate",
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_PASSWORDFILTERVALIDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_PASSWORDFILTERVALIDATE})
        }
    )
	public ResponseEntity<?> validate(
			@RequestBody @Valid AccPasswordFilterRequestDto request) {
		passwordFilterManager.validate(request);
		return new ResponseEntity<Object>(HttpStatus.OK);
	}

	@ResponseBody
	@ResponseStatus(code = HttpStatus.OK)
	@RequestMapping(value = PASSWORD_FILTER_BASE_ENDPOINT + "/change", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_PASSWORDFILTERCHANGE + "')")
	@Operation(
			summary = "Change pasword given from resources with applied password filters including uniform password defintions",
			operationId = "change",
			tags = { AccUniformPasswordController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_PASSWORDFILTERCHANGE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_PASSWORDFILTERCHANGE})
        }
    )
	public ResponseEntity<?> change(@RequestBody @Valid AccPasswordFilterRequestDto request) {
		passwordFilterManager.change(request);
		//
		return new ResponseEntity<Object>(HttpStatus.OK);
	}
	
	
	/**
	 * Returns all registered connector types.
	 *
	 * @return connector types
	 */
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Get all supported connector types",
			operationId = "getSupportedConnectorTypes",
			tags = {SysSystemController.TAG})
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public CollectionModel<ConnectorTypeDto> getSupportedTypes() {
		Map<SysConnectorServerDto, List<IcConnectorInfo>> allConnectorInfos = new LinkedHashMap<>();
		// All remote connectors - optionally, but with higher priority.
		remoteServerService.find(null)
				.forEach(connectorServer -> {
					for (IcConfigurationService config : icConfiguration.getIcConfigs().values()) {
						try {
							connectorServer.setPassword(remoteServerService.getPassword(connectorServer.getId()));
							Set<IcConnectorInfo> availableRemoteConnectors = config.getAvailableRemoteConnectors(connectorServer);
							if (CollectionUtils.isNotEmpty(availableRemoteConnectors)) {
								allConnectorInfos.put(connectorServer, Lists.newArrayList(availableRemoteConnectors));
							}
						} catch (IcInvalidCredentialException e) {
							ExceptionUtils.log(LOG, new ResultCodeException(AccResultCode.REMOTE_SERVER_INVALID_CREDENTIAL,
									ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e));
						} catch (IcServerNotFoundException e) {
							ExceptionUtils.log(LOG, new ResultCodeException(AccResultCode.REMOTE_SERVER_NOT_FOUND,
									ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e));
						} catch (IcCantConnectException e) {
							ExceptionUtils.log(LOG, new ResultCodeException(AccResultCode.REMOTE_SERVER_CANT_CONNECT,
									ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e));
						} catch (IcRemoteServerException e) {
							ExceptionUtils.log(LOG, new ResultCodeException(AccResultCode.REMOTE_SERVER_UNEXPECTED_ERROR,
									ImmutableMap.of("server", e.getHost() + ":" + e.getPort()), e));
						}
					}
				});
		// Local connectors
		Map<String, Set<IcConnectorInfo>> availableLocalConnectors = icConfiguration.getAvailableLocalConnectors();
		if (availableLocalConnectors != null) {
			List<IcConnectorInfo> localConnectorInfos = Lists.newArrayList();
			availableLocalConnectors
					.values()
					.forEach(infos -> {
						localConnectorInfos.addAll(infos);
					});
			SysConnectorServerDto localServer = new SysConnectorServerDto();
			localServer.setLocal(true);
			allConnectorInfos.put(localServer, localConnectorInfos);
		}
		//
		List<ConnectorTypeDto> resolvedConnectorTypes = Lists.newArrayListWithExpectedSize(allConnectorInfos.values().stream().mapToInt(List::size).sum());
		for (ConnectorType supportedConnectorType : connectorManager.getSupportedTypes()) {
			// remote connector has higher priority => linked hash map => find first
			// Find connector info and set version to the connectorTypeDto.
			SysConnectorServerDto connectorServer = null;
			IcConnectorInfo info = null;
			for (Entry<SysConnectorServerDto, List<IcConnectorInfo>> entry : allConnectorInfos.entrySet()) {
				for (IcConnectorInfo connectorInfo : entry.getValue()) {
					if (supportedConnectorType.getConnectorName().equals(connectorInfo.getConnectorKey().getConnectorName())) {
						connectorServer = entry.getKey();
						info = connectorInfo;
						break;
					}
				}
				if (info != null) {
					break;
				}
			}
			if (info == null) {
				// default connector types are resolved bellow
				continue;
			}

			ConnectorTypeDto connectorType = connectorManager.convertTypeToDto(supportedConnectorType);
			if (connectorServer != null) {
				connectorType.setRemoteServer(connectorServer.getId());
			}
			connectorType.setLocal(connectorType.getRemoteServer() == null);
			connectorType.setVersion(info.getConnectorKey().getBundleVersion());
			connectorType.setName(info.getConnectorDisplayName());
			resolvedConnectorTypes.add(connectorType);
		}

		// Find connectors without extension (specific connector type).
		List<ConnectorTypeDto> defaultConnectorTypes = Lists.newArrayList();
		for (Entry<SysConnectorServerDto, List<IcConnectorInfo>> entry : allConnectorInfos.entrySet()) {
			SysConnectorServerDto connectorServer = entry.getKey();
			for (IcConnectorInfo connectorInfo : entry.getValue()) {
				ConnectorTypeDto connectorType = connectorManager.convertIcConnectorInfoToDto(connectorInfo);
				if (!resolvedConnectorTypes.stream().anyMatch(supportedType ->
						supportedType.getConnectorName().equals(connectorType.getConnectorName()) && supportedType.isHideParentConnector())) {
					if (connectorServer != null) {
						connectorType.setRemoteServer(connectorServer.getId());
					}
					connectorType.setLocal(connectorType.getRemoteServer() == null);

					defaultConnectorTypes.add(connectorType);
				}
			}
		}
		resolvedConnectorTypes.addAll(defaultConnectorTypes);

		return new CollectionModel<>(
				resolvedConnectorTypes.stream()
						.sorted(Comparator.comparing(ConnectorTypeDto::getOrder))
						.collect(Collectors.toList())
		);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/wizards/execute", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_UPDATE + "')")
	@Operation(
			summary = "Execute specific connector type -> execute some wizard step.",
			operationId = "executeConnectorType",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = ConnectorTypeDto.class
                                    )
                            )
                    }
            ),
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_UPDATE}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_UPDATE})
        }
    )
	public ResponseEntity<ConnectorTypeDto> executeWizardType(@Valid @RequestBody ConnectorTypeDto wizardDto) {
		ConnectorTypeDto result = connectorManager.execute(wizardDto);

		return new ResponseEntity<ConnectorTypeDto>(result, HttpStatus.CREATED);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/wizards/load", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_READ + "')")
	@Operation(
			summary = "Load data for specific connector type -> open existed system in the wizard step.",
			operationId = "loadConnectorType",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = ConnectorTypeDto.class
                                    )
                            )
                    }
            ),
			tags = { SysSystemController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							AccGroupPermission.SYSTEM_READ}),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							AccGroupPermission.SYSTEM_READ})
        }
    )
	public ResponseEntity<ConnectorTypeDto> loadWizardType(@NotNull @Valid @RequestBody ConnectorTypeDto wizardDto) {
		if (!wizardDto.isReopened()) {
			// Load default values for new system.
			ConnectorTypeDto result = connectorManager.load(wizardDto);
			return new ResponseEntity<ConnectorTypeDto>(result, HttpStatus.OK);
		}
		// Load data for already existed system.
		String systemId = wizardDto.getMetadata().get(AbstractConnectorType.SYSTEM_DTO_KEY);
		Assert.notNull(systemId, "System ID have to be present in the connector type metadata.");
		SysSystemDto systemDto = getDto(systemId);
		if (systemDto != null) {
			// If connector type is not given (ID is null), then try to find it by connector name.
			// If connector name is null, then default connector type will be used.
			if (Strings.isBlank(wizardDto.getId())) {
				ConnectorType connectorType = connectorManager.findConnectorTypeBySystem(
						systemDto
				);
				ConnectorTypeDto newConnectorTypeDto = connectorManager.convertTypeToDto(connectorType);
				newConnectorTypeDto.setReopened(wizardDto.isReopened());
				newConnectorTypeDto.setMetadata(wizardDto.getMetadata());
				wizardDto = newConnectorTypeDto;
			}
			wizardDto.getEmbedded().put(AbstractConnectorType.SYSTEM_DTO_KEY, systemDto);
			
			ConnectorTypeDto result = connectorManager.load(wizardDto);

			return new ResponseEntity<ConnectorTypeDto>(result, HttpStatus.OK);
		}
		throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", systemId));
	}



	@Scheduled(fixedDelay = 2000)
	public synchronized void checkDeferredRequests() {
		longPollingManager.checkDeferredRequests(SysSystemDto.class);
	}

	/**
	 * Check deferred result - using default implementation from long-polling-manager.
	 *
	 * @param deferredResult
	 * @param subscriber
	 */
	public void checkDeferredRequest(DeferredResult<OperationResultDto> deferredResult, LongPollingSubscriber subscriber) {
		Assert.notNull(deferredResult, "Deferred result is required.");
		Assert.notNull(subscriber.getEntityId(), "Entity identifier is required.");

		SysSyncLogFilter filterLog = new SysSyncLogFilter();
		filterLog.setSystemId(subscriber.getEntityId());
		longPollingManager.baseCheckDeferredResult(deferredResult, subscriber, filterLog, syncLogService, false);

		if(deferredResult.isSetOrExpired()) {
			return;
		}
		SysSyncItemLogFilter filter = new SysSyncItemLogFilter();
		filter.setSystemId(subscriber.getEntityId());
		longPollingManager.baseCheckDeferredResult(deferredResult, subscriber, filter, syncItemLogService, true);
	}

	/**
	 * Returns definition for given system
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 *
	 * @param system
	 * @return
	 */
	private synchronized IdmFormDefinitionDto getConnectorFormDefinition(SysSystemDto system) {
		Assert.notNull(system, "System is required.");
		//
		// connector key can't be null
		if (system.getConnectorKey() == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_FORM_DEFINITION_NOT_FOUND, ImmutableMap.of("system", system.getId()));
		}
		//
		return systemService.getConnectorFormDefinition(system);
	}

	private synchronized IdmFormDefinitionDto getPoolingConnectorFormDefinition(SysSystemDto system) {
		Assert.notNull(system, "System is required.");
		//
		// connector key can't be null
		if (system.getConnectorKey() == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_FORM_DEFINITION_NOT_FOUND, ImmutableMap.of("system", system.getId()));
		}
		//
		return systemService.getPoolingConnectorFormDefinition(system);
	}

	private synchronized IdmFormDefinitionDto getOperationOptionsConnectorFormDefinition(SysSystemDto system) {
		Assert.notNull(system, "System is required.");
		//
		// connector key can't be null
		if (system.getConnectorKey() == null) {
			throw new ResultCodeException(AccResultCode.CONNECTOR_FORM_DEFINITION_NOT_FOUND, ImmutableMap.of("system", system.getId()));
		}
		//
		return systemService.getOperationOptionsConnectorFormDefinition(system);
	}

	@Override
	protected SysSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new SysSystemFilter(parameters, getParameterConverter());
	}
}
