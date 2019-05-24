package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joda.time.DateTime;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Delete executed events
 * 
 * @author Radek Tomiška
 * @since 9.7.0
 */
@Service(DeleteExecutedEventTaskExecutor.TASK_NAME)
@DisallowConcurrentExecution
@Description("Delete executed events.")
public class DeleteExecutedEventTaskExecutor
		extends AbstractSchedulableStatefulExecutor<IdmEntityEventDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteExecutedEventTaskExecutor.class);
	public static final String TASK_NAME = "core-delete-executed-event-long-running-task";
	public static final String PARAMETER_NUMBER_OF_DAYS = "numberOfDays"; // events older than
	public static final int DEFAULT_NUMBER_OF_DAYS = 3;
	//
	@Autowired private IdmEntityEventService service;
	@Autowired private EntityEventManager manager;
	//
	private int numberOfDays = 0; // optional
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		Long givenNumberOfDays = getParameterConverter().toLong(properties, PARAMETER_NUMBER_OF_DAYS);
		if (givenNumberOfDays != null) {
			numberOfDays = Math.toIntExact(givenNumberOfDays);
		} else {
			numberOfDays = 0;
		}
	}
	
	@Override
	protected boolean start() {
		LOG.warn("Start deleting executed events older than [{}] days.", numberOfDays);
		//
		return super.start();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		result = super.end(result, ex);
		LOG.warn("End deleting executed events  older than [{}] days. Processed events [{}].", numberOfDays, counter);
		return result;
	}
	
	@Override
	public boolean isInProcessedQueue(IdmEntityEventDto dto) {
		// we want to log items, but we want to execute them every times
		return false;
	}
	
	@Override
	public Page<IdmEntityEventDto> getItemsToProcess(Pageable pageable) {
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.getStates().add(OperationState.EXECUTED);
		if (numberOfDays > 0) {
			filter.setCreatedTill(DateTime.now().withTimeAtStartOfDay().minusDays(numberOfDays));
		}
		return service.find(filter, null); // pageable is not given => records are deleted and wee ned the fist page all time
	}

	@Override
	public Optional<OperationResult> processItem(IdmEntityEventDto dto) {
		if (service.get(dto) != null) { // event can be removed by his parent
			manager.deleteEvent(dto);
		}
		//
		return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_NUMBER_OF_DAYS);
		//
		return parameters;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_NUMBER_OF_DAYS, numberOfDays);
		//
		return properties;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto numberOfDaysAttribute = new IdmFormAttributeDto(PARAMETER_NUMBER_OF_DAYS, PARAMETER_NUMBER_OF_DAYS, PersistentType.LONG);
		numberOfDaysAttribute.setDefaultValue(String.valueOf(DEFAULT_NUMBER_OF_DAYS));
		//
		return Lists.newArrayList(numberOfDaysAttribute);
	}
	
    @Override
    public boolean supportsDryRun() {
    	return false; // TODO: get context (or LRT) in getItems to process ...
    }
}
