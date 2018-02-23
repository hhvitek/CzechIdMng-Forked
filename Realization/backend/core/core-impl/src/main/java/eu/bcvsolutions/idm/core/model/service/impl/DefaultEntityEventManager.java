package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Entity processing based on event publishing.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEntityEventManager implements EntityEventManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEntityEventManager.class);
	private final ApplicationContext context;
	private final ApplicationEventPublisher publisher;
	private final EnabledEvaluator enabledEvaluator;
	private final LookupService lookupService;
	private final ConfigurationService configurationService;

	public DefaultEntityEventManager(
			ApplicationContext context, 
			ApplicationEventPublisher publisher,
			EnabledEvaluator enabledEvaluator,
			LookupService lookupService,
			ConfigurationService configurationService) {
		Assert.notNull(context, "Spring context is required");
		Assert.notNull(publisher, "Event publisher is required");
		Assert.notNull(enabledEvaluator, "Enabled evaluator is required");
		Assert.notNull(lookupService, "LookupService is required");
		Assert.notNull(configurationService, "ConfigurationService is required!");
		//
		this.context = context;
		this.publisher = publisher;
		this.enabledEvaluator = enabledEvaluator;
		this.lookupService = lookupService;
		this.configurationService = configurationService;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends Serializable> EventContext<E> process(EntityEvent<E> event) {
		Assert.notNull(event);
		Serializable content = event.getContent();
		//
		LOG.info("Publishing event [{}]", event);
		//
		// continue suspended event
		event.getContext().setSuspended(false);
		//
		// read previous (original) dto source - usable in "check modification" processors
		if (event.getOriginalSource() == null && (content instanceof AbstractDto)) { // original source could be set externally
			AbstractDto contentDto = (AbstractDto) content;
			// works only for dto modification
			if (contentDto.getId() != null && lookupService.getDtoLookup(contentDto.getClass()) != null) {
				event.setOriginalSource((E) lookupService.lookupDto(contentDto.getClass(), contentDto.getId()));
			}
		}
		//
		publisher.publishEvent(event); 
		LOG.info("Event [{}] is completed", event);
		//
		return event.getContext();
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<EntityEventProcessorDto> find(EntityEventProcessorFilter filter) {
		List<EntityEventProcessorDto> dtos = new ArrayList<>();
		Map<String, EntityEventProcessor> processors = context.getBeansOfType(EntityEventProcessor.class);
		for(Entry<String, EntityEventProcessor> entry : processors.entrySet()) {
			EntityEventProcessor<?> processor = entry.getValue();
			// entity event processor depends on module - we could not call any processor method
			if (!enabledEvaluator.isEnabled(processor)) {
				continue;
			}
			//
			EntityEventProcessorDto dto = new EntityEventProcessorDto();
			dto.setId(entry.getKey());
			dto.setName(processor.getName());
			dto.setModule(processor.getModule());
			dto.setContentClass(processor.getEntityClass());
			dto.setEntityType(processor.getEntityClass().getSimpleName());
			dto.setEventTypes(Lists.newArrayList(processor.getEventTypes()));
			dto.setClosable(processor.isClosable());
			dto.setDisabled(processor.isDisabled());
			dto.setDisableable(processor.isDisableable());
			dto.setOrder(processor.getOrder());
			// resolve documentation
			dto.setDescription(processor.getDescription());
			dto.setConfigurationProperties(processor.getConfigurationMap());
			//
			if (passFilter(dto, filter)) {
				dtos.add(dto);
			}

		}
		LOG.debug("Returning [{}] registered entity event processors", dtos.size());
		return dtos;
	}

	@Override
	public void publishEvent(Object event) {
		publisher.publishEvent(event);
	}
	
	/**
	 * Returns true, when given processor pass given filter
	 * 
	 * @param processor
	 * @param filter
	 * @return
	 */
	private boolean passFilter(EntityEventProcessorDto processor, EntityEventProcessorFilter filter) {
		if (filter == null) {
			// empty filter
			return true;
		}
		// id - not supported
		if (filter.getId() != null) {
			throw new UnsupportedOperationException("Filtering event processors by [id] is not supported.");
		}
		// text - lowercase like in name, description, content class - canonical name
		if (StringUtils.isNotEmpty(filter.getText())) {
			if (!processor.getName().toLowerCase().contains(filter.getText().toLowerCase())
					&& (processor.getDescription() == null || !processor.getDescription().toLowerCase().contains(filter.getText().toLowerCase()))
					&& !processor.getContentClass().getCanonicalName().toLowerCase().contains(filter.getText().toLowerCase())) {
				return false;
			}
		}
		// processors name
		if (StringUtils.isNotEmpty(filter.getName()) && !processor.getName().equals(filter.getName())) {
			return false; 
		}
		// content ~ entity type - dto type
		if (filter.getContentClass() != null && !filter.getContentClass().isAssignableFrom(processor.getContentClass())) {
			return false;
		}
		// module id
		if (StringUtils.isNotEmpty(filter.getModule()) && !filter.getModule().equals(processor.getModule())) {
			return false;
		}
		// description - like
		if (StringUtils.isNotEmpty(filter.getDescription()) 
				&& StringUtils.isNotEmpty(processor.getDescription()) 
				&& !processor.getDescription().contains(filter.getDescription())) {
			return false;
		}
		// entity ~ content type - simple name
		if (StringUtils.isNotEmpty(filter.getEntityType()) && !processor.getEntityType().equals(filter.getEntityType())) {
			return false;
		}
		// event types
		if (!filter.getEventTypes().isEmpty() && !processor.getEventTypes().containsAll(filter.getEventTypes())) {
			return false;
		}
		//
		return true;
	}

	@Override
	public void enable(String processorId) {
		setEnabled(processorId, true);
	}

	@Override
	public void disable(String processorId) {
		setEnabled(processorId, false);
	}

	@Override
	public void setEnabled(String processorId, boolean enabled) {
		EntityEventProcessor<?> processor = (EntityEventProcessor<?>) context.getBean(processorId);
		String enabledPropertyName = processor.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED);
		configurationService.setBooleanValue(enabledPropertyName, enabled);
	}

}
