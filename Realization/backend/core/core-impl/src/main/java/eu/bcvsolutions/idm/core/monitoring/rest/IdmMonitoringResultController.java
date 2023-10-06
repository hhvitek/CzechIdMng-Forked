package eu.bcvsolutions.idm.core.monitoring.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.service.api.CheckLongPollingResult;
import eu.bcvsolutions.idm.core.model.service.api.LongPollingManager;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoringResult_;
import eu.bcvsolutions.idm.core.rest.DeferredResultWrapper;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/**
 * Configgured monitoringResult evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/monitoring-results")
@Tag(
		name = IdmMonitoringResultController.TAG,
		description = "Operations with monitoring  results"//,

		//produces = BaseController.APPLICATION_HAL_JSON_VALUE
		
//consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmMonitoringResultController extends AbstractEventableDtoController<IdmMonitoringResultDto, IdmMonitoringResultFilter> {
	
	protected static final String TAG = "Monitoring results";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmMonitoringResultController.class);
	//
	@Autowired private MonitoringManager monitoringManager;
	@Autowired private LongPollingManager longPollingManager;
	private final static UUID LONG_POOLING_IDENTIFIER = UUID.randomUUID();
	
	@Autowired
	public IdmMonitoringResultController(IdmMonitoringResultService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "Search monitoring results (/search/quick alias)", 
			/* nickname = "searchMonitoringResults", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ })
        }
    )
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "Search monitoring results", 
			/* nickname = "searchQuickMonitoringResults", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ })
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
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "Autocomplete monitoring results (selectbox usage)", 
			/* nickname = "autocompleteMonitoringResults", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_AUTOCOMPLETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_AUTOCOMPLETE })
        }
    )
	public CollectionModel<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/last-results", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "Find last monitoring results", 
			/* nickname = "findLastMonitoringResults", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ })
        }
    )
	public CollectionModel<?> findLastResults(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		Page<IdmMonitoringResultDto> lastResults = monitoringManager.getLastResults(toFilter(parameters), pageable, IdmBasePermission.READ);
		//
		return toCollectionModel(loadDtos(lastResults), getDtoClass());
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_COUNT + "')")
	@Operation(
			summary = "The number of entities that match the filter", 
			/* nickname = "countMonitoringResults", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_COUNT }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_COUNT })
        }
    )
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "MonitoringResult detail", 
			/* nickname = "getMonitoringResult", */
			/* response = IdmMonitoringResultDto.class, */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ })
        }
    )
	public ResponseEntity<?> get(
			@Parameter(name = "MonitoringResult's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	public IdmMonitoringResultDto getDto(Serializable backendId) {
		IdmMonitoringResultDto dto =  super.getDto(backendId);
		if (dto == null) {
			return null;
		}
		//
		UUID ownerId = dto.getOwnerId();
		String ownerType = dto.getOwnerType();
		if (ownerId != null && StringUtils.isNotEmpty(ownerType)) {
			try {
				dto.getEmbedded().put(AttachableEntity.PARAMETER_OWNER_ID, getLookupService().lookupDto(ownerType, ownerId));
			} catch (IllegalArgumentException ex) {
				LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", ownerType, ex);
			}
		}
		//
		return dto;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_CREATE + "')"
			+ " or hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_UPDATE + "')")
	@Operation(
			summary = "Create / update monitoring result", 
			/* nickname = "postMonitoringResult", */
			/* response = IdmMonitoringResultDto.class, */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_CREATE,
						MonitoringGroupPermission.MONITORINGRESULT_UPDATE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_CREATE,
						MonitoringGroupPermission.MONITORINGRESULT_UPDATE})
        }
    )
	public ResponseEntity<?> post(@Valid @RequestBody IdmMonitoringResultDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_UPDATE + "')")
	@Operation(
			summary = "Update monitoring result", 
			/* nickname = "putMonitoringResult", */
			/* response = IdmMonitoringResultDto.class, */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_UPDATE })
        }
    )
	public ResponseEntity<?> put(
			@Parameter(name = "MonitoringResult's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmMonitoringResultDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_UPDATE + "')")
	@Operation(
			summary = "Update monitoring result", 
			/* nickname = "patchMonitoringResult", */
			/* response = IdmMonitoringResultDto.class, */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_UPDATE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_UPDATE })
        }
    )
	public ResponseEntity<?> patch(
			@Parameter(name = "MonitoringResult's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_DELETE + "')")
	@Operation(
			summary = "Delete monitoring result", 
			/* nickname = "deleteMonitoringResult", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_DELETE }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_DELETE })
        }
    )
	public ResponseEntity<?> delete(
			@Parameter(name = "MonitoringResult's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')"
			+ " or hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_AUTOCOMPLETE + "')")
	@Operation(
			summary = "What logged identity can do with given record", 
			/* nickname = "getPermissionsOnMonitoringResult", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ,
						MonitoringGroupPermission.MONITORINGRESULT_AUTOCOMPLETE}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ,
						MonitoringGroupPermission.MONITORINGRESULT_AUTOCOMPLETE})
        }
    )
	public Set<String> getPermissions(
			@Parameter(name = "MonitoringResult's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "Get available bulk actions", 
			/* nickname = "availableBulkAction", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ }),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ })
        }
    )
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "Process bulk action", 
			/* nickname = "bulkAction", */
			/* response = IdmBulkActionDto.class, */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ})
        }
    )
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "Prevalidate bulk action", 
			/* nickname = "prevalidateBulkAction", */
			/* response = IdmBulkActionDto.class, */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {

				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						MonitoringGroupPermission.MONITORINGRESULT_READ})
        }
    )
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@Override
	public Page<IdmMonitoringResultDto> find(IdmMonitoringResultFilter filter, Pageable pageable, BasePermission permission) {
		return loadDtos(super.find(filter, pageable, permission));
	}
	
	private Page<IdmMonitoringResultDto> loadDtos(Page<IdmMonitoringResultDto> results) {
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		results
			.stream()
			.forEach(dto -> {
				UUID ownerId = dto.getOwnerId();
				String ownerType = dto.getOwnerType();
				if (ownerId != null && StringUtils.isNotEmpty(ownerType)) {
					if (!loadedDtos.containsKey(ownerId)) {
						try {
							loadedDtos.put(ownerId, getLookupService().lookupDto(ownerType, ownerId));
						} catch (IllegalArgumentException ex) {
							LOG.debug("Class [{}] not found on classpath (e.g. module was uninstalled)", ownerType, ex);
						}
					}
					dto.getEmbedded().put(AttachableEntity.PARAMETER_OWNER_ID, loadedDtos.get(ownerId));
				}
				dto.getEmbedded().put(IdmFormInstanceDto.PROPERTY_FORM_INSTANCE, monitoringManager.getEvaluatorFormInstance(dto));
			});
		return results;
	}
	
	/**
	 * Long polling for check unresolved identity role-requests
	 *  
	 * @param backendId - applicant ID
	 * 
	 * @return DeferredResult<OperationResultDto>, where:
	 * 
	 * - EXECUTED = All requests for this identity are resolved,
	 * - RUNNING = Requests are not resolved, but some request was changed (since previous check).
	 * - NOT_EXECUTED = Deferred-result expired
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "{backendId}/check-last-monitoring-results", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_READ + "')")
	@Operation(
			summary = "Check changes of last monitoring results (Long-polling request).", 
			/* nickname = "checkLastMonitoringResults", */
			tags = { IdmMonitoringResultController.TAG })
    @SecurityRequirements(
        value = {
 
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
						CoreGroupPermission.ROLE_REQUEST_READ,
						CoreGroupPermission.IDENTITY_READ}),
				@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
						CoreGroupPermission.ROLE_REQUEST_READ,
						CoreGroupPermission.IDENTITY_READ})
        }
    )
	public DeferredResult<OperationResultDto> checkLastMonitoringResults() {
		DeferredResultWrapper result = new DeferredResultWrapper(
				LONG_POOLING_IDENTIFIER,
				IdmMonitoringResultDto.class,
				new DeferredResult<OperationResultDto>(
						10000l, new OperationResultDto(OperationState.NOT_EXECUTED)
				)
		); 

		result.onCheckResultCallback(new CheckLongPollingResult() {

			@Override
			public void checkDeferredResult(DeferredResult<OperationResultDto> result, LongPollingSubscriber subscriber) {
				checkDeferredRequest(result, subscriber);
			}
		});

		// If isn't long polling enabled, then Blocked response will be sent.
		if (!longPollingManager.isLongPollingEnabled()) {
			result.getResult().setResult(new OperationResultDto(OperationState.BLOCKED));
			//
			return result.getResult();
		}

		longPollingManager.addSuspendedResult(result);

		return result.getResult();
	}
	
	@Scheduled(fixedDelay = 2000)
	public synchronized void checkDeferredRequests() {
		longPollingManager.checkDeferredRequests(IdmMonitoringResultDto.class);
	}
	
	/**
	 * Execute related monitoring evaluator with setting from result again synchronously.
	 * 
	 * @since 11.2.0
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/execute")
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORINGRESULT_EXECUTE + "')")
	@Operation(
			summary = "Execute monitoring evaluator",
			/* nickname = "executeMonitoring", */
			tags={ IdmMonitoringController.TAG },
			description = "Execute related monitoring evaluator with setting from result again synchronously..")
    @SecurityRequirements(
            value = {
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
                            MonitoringGroupPermission.MONITORINGRESULT_EXECUTE }),
                    @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
                            MonitoringGroupPermission.MONITORINGRESULT_EXECUTE })
            }
    )
	public ResponseEntity<?> execute(
			@Parameter(name = "Monitoring result identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		IdmMonitoringResultDto monitoringResult = getDto(backendId);
		if (monitoringResult == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		IdmMonitoringDto monitoring = getLookupService().lookupEmbeddedDto(monitoringResult, IdmMonitoringResult_.monitoring);
		//
		monitoring.setEvaluatorProperties(monitoringResult.getEvaluatorProperties());
		IdmMonitoringResultDto currentResult = monitoringManager.execute(monitoring, IdmBasePermission.EXECUTE);
		// without result
		if (currentResult == null) {
			new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		// return current result
		return new ResponseEntity<>(toModel(currentResult), HttpStatus.CREATED);
	}

	/**
	 * Check deferred result - using default implementation from long-polling-manager.
	 * 
	 * @param deferredResult
	 * @param subscriber
	 */
	private void checkDeferredRequest(
			DeferredResult<OperationResultDto> deferredResult, 
			LongPollingSubscriber subscriber) {
		Assert.notNull(deferredResult, "Deferred result is required.");
		Assert.notNull(subscriber.getEntityId(), "Subscriber identifier is required.");
		
		longPollingManager.baseCheckDeferredResult(deferredResult, subscriber, null, getService(), false);
	}
	
	@Override
	protected IdmMonitoringResultFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter(parameters, getParameterConverter());
		//
		filter.setMonitoring(getParameterConverter().toEntityUuid(parameters, IdmMonitoringResultFilter.PARAMETER_MONITORING, IdmMonitoringDto.class));
		//
		return filter;
	}
}
