package eu.bcvsolutions.idm.core.monitoring.rest;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.MonitoringEvaluatorDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
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
 * Configgured monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/monitorings")
@Tag(
		name = IdmMonitoringController.TAG,
		description = "Operations with configured monitoring evaluators"//,

		


)
public class IdmMonitoringController extends AbstractEventableDtoController<IdmMonitoringDto, IdmMonitoringFilter> {
	
	protected static final String TAG = "Monitoring evaluators";
	
	@Autowired private MonitoringManager monitoringManager;
	@Autowired private IdmMonitoringResultController monitoringResultController;
	
	@Autowired
	public IdmMonitoringController(IdmMonitoringService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@Operation(
			summary = "Search configured monitoring evaluators (/search/quick alias)",
			/* nickname = "searchMonitorings", */
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_READ })
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
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@Operation(
			summary = "Search configured monitoring evaluators",
			/* nickname = "searchQuickMonitorings", */
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_READ })
        }
    )
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
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete configured monitoring evaluators (selectbox usage)",
			/* nickname = "autocompleteMonitorings", */
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_AUTOCOMPLETE })
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
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter",
			/* nickname = "countMonitorings", */
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@Operation(
			summary = "Monitoring detail",
			/* nickname = "getMonitoring", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmMonitoringDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_READ })
        }
    )
	public ResponseEntity<?> get(
			 @Parameter(description = "Monitoring's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_CREATE + "')"
			+ " or hasAuthority('" + MonitoringGroupPermission.MONITORING_UPDATE + "')")
	@Operation(
			summary = "Create / update configured monitoring evaluator",
			/* nickname = "postMonitoring", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmMonitoringDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_CREATE,
						MonitoringGroupPermission.MONITORING_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_CREATE,
						MonitoringGroupPermission.MONITORING_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmMonitoringDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_UPDATE + "')")
	@Operation(
			summary = "Update configured monitoring evaluator",
			/* nickname = "putMonitoring", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmMonitoringDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			 @Parameter(description = "Monitoring's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmMonitoringDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_UPDATE + "')")
	@Operation(
			summary = "Update configured monitoring evaluator",
			/* nickname = "patchMonitoring", */
            responses = @ApiResponse(
                    responseCode = "200",
                    content = {
                            @Content(
                                    mediaType = BaseController.APPLICATION_HAL_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = IdmMonitoringDto.class
                                    )
                            )
                    }
            ),
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			 @Parameter(description = "Monitoring's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_DELETE + "')")
	@Operation(
			summary = "Delete configured monitoring evaluator",
			/* nickname = "deleteMonitoring", */
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			 @Parameter(description = "Monitoring's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')"
			+ " or hasAuthority('" + MonitoringGroupPermission.MONITORING_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record",
			/* nickname = "getPermissionsOnMonitoring", */
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_READ,
						MonitoringGroupPermission.MONITORING_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_READ,
						MonitoringGroupPermission.MONITORING_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			 @Parameter(description = "Monitoring's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@Operation(
			summary = "Get available bulk actions",
			/* nickname = "availableBulkAction", */
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@Operation(
			summary = "Process bulk action",
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
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action",
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
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	/**
	 * Returns all registered evaluators
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_AUTOCOMPLETE + "') "
			+ "or hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@Operation(
			summary = "Get all supported evaluators",
			/* nickname = "getSupportedMonitoringEvaluators", */
			tags = { IdmMonitoringController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORING_AUTOCOMPLETE,
						MonitoringGroupPermission.MONITORING_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORING_AUTOCOMPLETE,
						MonitoringGroupPermission.MONITORING_READ})
        }
    )
	public CollectionModel<MonitoringEvaluatorDto> getSupportedEvaluators() {
		return new CollectionModel<>(monitoringManager.getSupportedEvaluators());
	}
	
	/**
	 * Execute monitoring evaluator synchronously.
	 * 
	 * @since 11.2.0
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/execute")
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_EXECUTE + "')")
	@Operation(
			summary = "Execute monitoring evaluator",
			/* nickname = "executeMonitoring", */
			tags={ IdmMonitoringController.TAG },
			description = "Execute monitoring evaluator synchronously.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            MonitoringGroupPermission.MONITORING_EXECUTE }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            MonitoringGroupPermission.MONITORING_EXECUTE })
            }
    )
	public ResponseEntity<?> execute(
			 @Parameter(description = "Monitoring codeable identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmMonitoringDto monitoring = getDto(backendId);
		if (monitoring == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmMonitoringResultDto monitoringResult = monitoringManager.execute(monitoring, IdmBasePermission.EXECUTE);
		// without result
		if (monitoringResult == null) {
			new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		// return current result
		return new ResponseEntity<>(monitoringResultController.toModel(monitoringResult), HttpStatus.CREATED);
	}
	
	/**
	 * Get last monitoring result.
	 * 
	 * @since 11.2.0
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}/last-result")
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "') "
			+ "and hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "Get last monitoring result",
			/* nickname = "getLastMonitoringResult", */
			tags={ IdmMonitoringController.TAG },
			description = "Get last monitoring result.")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            MonitoringGroupPermission.MONITORING_READ,
                            MonitoringGroupPermission.MONITORINGRESULT_READ}),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            MonitoringGroupPermission.MONITORING_READ,
                            MonitoringGroupPermission.MONITORINGRESULT_READ})
            }
    )
	public ResponseEntity<?> getLastResult(
			 @Parameter(description = "Monitoring codeable identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmMonitoringDto monitoring = getDto(backendId);
		if (monitoring == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
		filter.setMonitoring(monitoring.getId());
		List<IdmMonitoringResultDto> lastResults = monitoringManager.getLastResults(filter, PageRequest.of(0, 1), IdmBasePermission.READ).getContent();
		// without result
		if (lastResults.isEmpty()) {
			new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		// return last result
		return new ResponseEntity<>(monitoringResultController.toModel(lastResults.get(0)), HttpStatus.OK);
	}

	@Override
	public Page<IdmMonitoringDto> find(IdmMonitoringFilter filter, Pageable pageable, BasePermission permission) {
		Page<IdmMonitoringDto> results = super.find(filter, pageable, permission);
		results
			.stream()
			.forEach(dto -> {
				dto.getEmbedded().put(IdmFormInstanceDto.PROPERTY_FORM_INSTANCE, monitoringManager.getEvaluatorFormInstance(dto));
			});
		//
		return results;
	}

	@Override
	protected IdmMonitoringFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmMonitoringFilter(parameters, getParameterConverter());
	}
}
