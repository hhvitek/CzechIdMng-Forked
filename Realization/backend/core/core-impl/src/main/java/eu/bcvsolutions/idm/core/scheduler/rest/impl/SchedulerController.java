package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import javax.validation.Valid;

import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.AbstractTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.DependentTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.TaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Scheduler administration
 * 
 * @author Radek Tomiška
 */
@RestController
@RequestMapping(BaseController.BASE_PATH + "/scheduler-tasks")
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
@Tag(name = SchedulerController.TAG, description = "Scheduled tasks administration")
public class SchedulerController implements BaseController {

	protected static final String TAG = "Scheduler";
	//
	@Autowired private SchedulerManager schedulerService;
	@Autowired private LookupService lookupService;
	@Autowired private PagedResourcesAssembler<Object> pagedResourcesAssembler;
	private ParameterConverter parameterConverter = null;
	
	/**
	 * Returns all registered tasks
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Get supported tasks",
			operationId = "getSupportedSchedulerTasks",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	public CollectionModel<Task> getSupportedTasks() {
		return new CollectionModel<>(schedulerService.getSupportedTasks());
	}

	/**
	 * Finds scheduled tasks
	 *
	 * @return all tasks
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Search scheduled tasks",
			operationId = "searchSchedulerTasks",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	@PageableAsQueryParam
	public CollectionModel<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@Parameter(hidden = true)
			@PageableDefault Pageable pageable) {
		Page tasks = schedulerService.find(toFilter(parameters), pageable);
		//
		return pageToResources(tasks, Task.class);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{taskId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@Operation(
			summary = "Get scheduled task detail",
			operationId = "getSchedulerTask",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_READ }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_READ })
    })
	public Task getTask(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId) {
		return schedulerService.getTask(taskId);
	}
	
	/**
	 * Creates scheduled task
	 * 
	 * @param task
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@Operation(
			summary = "Create scheduled task",
			operationId = "postSchedulerTask",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_CREATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_CREATE })
    })
	public Task createTask(
			 @Parameter(description = "Task.", required = true)
			@Valid @RequestBody Task task) {
		return schedulerService.createTask(task);
	}
	
	/**
	 * Edit scheduled task
	 * 
	 * @param task
	 * @return
	 */
	@RequestMapping(value = "/{taskId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@Operation(
			summary = "Update scheduled task",
			operationId = "updateSchedulerTask",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_UPDATE })
    })
	public Task updateTask(
		 @Parameter(description = "Task identifier.", required = true)
		@PathVariable String taskId,
		@Valid @RequestBody Task task) {
		return schedulerService.updateTask(taskId, task);
	}
	
	/**
	 * Deletes scheduled task
	 * 
	 * @param taskId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE, value = "/{taskId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_DELETE + "')")
	@Operation(
			summary = "Delete scheduled task",
			operationId = "deleteSchedulerTask",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_DELETE })
    })
	public ResponseEntity<?> deleteTask(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId) {
		schedulerService.deleteTask(taskId);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/run")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@Operation(summary = "Execute scheduled task",
			operationId = "executeSchedulerTask",
			tags={ SchedulerController.TAG }, 
						description = "Create long running task (LRT) by scheduled task definition immediately. Created task will be added to LRTs queue.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE })
    })

	public AbstractTaskTrigger runTask(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId) {
		return schedulerService.runTask(taskId); 
	}
	
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/dry-run")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_EXECUTE + "')")
	@Operation(
			summary = "Execute scheduled task in dry run mode",
			operationId = "executeSchedulerTaskDryRun",
			tags={ SchedulerController.TAG },
						description = "Create long running task (LRT) by scheduled task definition immediately in dry run mode. Created task will be added to LRTs queue.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_EXECUTE })
    })
	public AbstractTaskTrigger dryRunTask(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId) {
		return schedulerService.runTask(taskId, true);
	}

	/**
	 * Creates one time trigger for task
	 *
	 * @param taskId name of task
	 * @param trigger trigger data
	 * @return trigger data containing name
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/triggers/simple")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@Operation(
			summary = "Create simple trigger",
			operationId = "postSimpleTrigger",
			tags={ SchedulerController.TAG }, 
						description = "Create simple trigger by given execution date.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_CREATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_CREATE })
    })
	public AbstractTaskTrigger createSimpleTrigger(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId, 
			 @Parameter(description = "Simple trigger definition.", required = true)
			@Valid @RequestBody SimpleTaskTrigger trigger) {
		return schedulerService.createTrigger(taskId, trigger);
	}

	/**
	 * Creates cron trigger for task
	 *
	 * @param taskId name of task
	 * @param trigger trigger data
	 * @return trigger data containing name
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/triggers/cron")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@Operation(
			summary = "Create cron trigger",
			operationId = "postCronTrigger",
			tags={ SchedulerController.TAG }, 
						description = "Create trigger by given quartz cron expression.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_CREATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_CREATE })
    })
	public AbstractTaskTrigger createCronTrigger(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId,
			 @Parameter(description = "Cron trigger definition.", required = true)
			@Valid @RequestBody CronTaskTrigger trigger) {
		return schedulerService.createTrigger(taskId, trigger);
	}
	
	/**
	 * Creates dependent trigger for task
	 *
	 * @param taskName name of task
	 * @param trigger trigger data
	 * @return trigger data containing name
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/{taskId}/triggers/dependent")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@Operation(
			summary = "Create dependent trigger",
			operationId = "postDependentTrigger",
			tags={ SchedulerController.TAG }, 
						description = "Create trigger, which is triggered, when other scheduled task ends.")
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_CREATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_CREATE })
    })
	public AbstractTaskTrigger createDependentTrigger(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId, 
			 @Parameter(description = "Cron trigger definition.", required = true)
			@Valid @RequestBody DependentTaskTrigger trigger) {
		return schedulerService.createTrigger(taskId, trigger);
	}

	/**
	 * Removes trigger
	 *
	 * @param taskId name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE, value = "/{taskId}/triggers/{triggerName}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_DELETE + "')")
	@Operation(
			summary = "Delete trigger",
			operationId = "deleteTrigger",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_DELETE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_DELETE })
    })
	public ResponseEntity<?> deleteTrigger(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId, 
			 @Parameter(description = "Trigger identifier.", required = true)
			@PathVariable String triggerName) {
		schedulerService.deleteTrigger(taskId, triggerName);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Pauses trigger
	 *
	 * @param taskId name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{taskId}/triggers/{triggerName}/pause")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@Operation(
			summary = "Pause trigger",
			operationId = "pauseTrigger",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_UPDATE })
    })
	public ResponseEntity<?> pauseTrigger(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId, 
			 @Parameter(description = "Trigger identifier.", required = true)
			@PathVariable String triggerName) {
		schedulerService.pauseTrigger(taskId, triggerName);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Resumes trigger
	 *
	 * @param taskId name of task
	 * @param triggerName name of trigger
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.PUT, value = "/{taskId}/triggers/{triggerName}/resume")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_UPDATE + "')")
	@Operation(
			summary = "Resume trigger",
			operationId = "resumeTrigger",
			tags={ SchedulerController.TAG })
    @SecurityRequirements({
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { CoreGroupPermission.SCHEDULER_UPDATE }),
        @SecurityRequirement(name = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { CoreGroupPermission.SCHEDULER_UPDATE })
    })
	public ResponseEntity<?> resumeTrigger(
			 @Parameter(description = "Task identifier.", required = true)
			@PathVariable String taskId, 
			 @Parameter(description = "Trigger identifier.", required = true)
			@PathVariable String triggerName) {
		schedulerService.resumeTrigger(taskId, triggerName);
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	protected CollectionModel<?> pageToResources(Page<Object> page, Class<?> domainType) {

		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyModel(page, domainType);
		}

		return pagedResourcesAssembler.toModel(page);
	}
	
	private ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(lookupService);
		}
		return parameterConverter;
	}
	
	private TaskFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new TaskFilter(parameters, getParameterConverter());
	}
}
