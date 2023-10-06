package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.TaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping(UpcomingTasksController.BASE_PATH + "/upcoming-tasks")
@Tag(name = UpcomingTasksController.TAG, description = "Upcoming tasks for task dashboard")
public class UpcomingTasksController implements BaseController {

	protected static final String TAG = "Upcoming tasks";
	private SchedulerManager schedulerService;
	private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	private ParameterConverter parameterConverter;

	public UpcomingTasksController(SchedulerManager schedulerService, LookupService lookupService, PagedResourcesAssembler<Object> pagedResourcesAssembler) {
		this.schedulerService = schedulerService;
		this.pagedResourcesAssembler = pagedResourcesAssembler;
		this.parameterConverter = new ParameterConverter(lookupService);
	}

	/**
	 * Finds upcoming scheduled tasks
	 *
	 * @return upcoming tasks
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Search upcoming scheduled tasks",
			/* nickname = "searchUpcomingSchedulerTasks", */
			tags={ UpcomingTasksController.TAG })
    @SecurityRequirements(
        value = {

					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							CoreGroupPermission.SCHEDULER_READ }),
					@SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							CoreGroupPermission.SCHEDULER_READ })
        }
    )
    @Parameters({
            @Parameter(name = "page", schema = @Schema( implementation=String.class, type = "query"), description = "Results page you want to retrieve (0..N)"),
            @Parameter(name = "size", schema = @Schema( implementation=String.class, type = "query"), description = "Number of records per page."),
            @Parameter(name = "nextFireTimesLimitSeconds", schema = @Schema( implementation=String.class, type = "query"), description = "Limit number of seconds in the future for cron trigger"),
            @Parameter(name = "nextFireTimesLimitCount", schema = @Schema( implementation=String.class, type = "query"), description = "Limit size of nextFireTimes list"),
    })
	//@ApiImplicitParams({
	//		@ApiImplicitParam(name = "page", dataTypeClass = String.class, paramType = "query",
	//				value = "Results page you want to retrieve (0..N)"),
	//		@ApiImplicitParam(name = "size", dataTypeClass = String.class, paramType = "query",
	//				value = "Number of records per page."),
	//		@ApiImplicitParam(name = "nextFireTimesLimitSeconds", dataTypeClass = String.class, paramType = "query",
	//				value = "Limit number of seconds in the future for cron trigger"),
	//		@ApiImplicitParam(name = "nextFireTimesLimitCount", dataTypeClass = String.class, paramType = "query",
	//				value = "Limit size of nextFireTimes list"),
	//})
	public CollectionModel<?> findUpcomingTasks(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		Page tasks = schedulerService.findUpcomingTasks(toFilter(parameters), pageable);
		//
		return pageToResources(tasks, Task.class);
	}

	protected CollectionModel<?> pageToResources(Page<Object> page, Class<?> domainType) {
		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyModel(page, domainType);
		}

		return pagedResourcesAssembler.toModel(page);
	}

	private ParameterConverter getParameterConverter() {
		return parameterConverter;
	}

	private TaskFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new TaskFilter(parameters, getParameterConverter());
	}
}
