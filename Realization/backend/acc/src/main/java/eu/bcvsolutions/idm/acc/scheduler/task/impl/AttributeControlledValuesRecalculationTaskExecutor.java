package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.eav.domain.AccFaceType;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping_;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.impl.IdentitySynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Merge - attribute controlled values recalculation.
 * 
 * @author Vít Švanda
 * 
 */
@DisallowConcurrentExecution
@Component(AttributeControlledValuesRecalculationTaskExecutor.TASK_NAME)
public class AttributeControlledValuesRecalculationTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributeControlledValuesRecalculationTaskExecutor.class);
	public static final String TASK_NAME = "acc-attribute-controlled-values-recalculation-long-running-task";
	public static final String PARAMETER_SYSTEM_UUID = "system-uuid";
	public static final String PARAMETER_ENTITY_TYPE = "entity-type";
	public static final String PARAMETER_ONLY_EVICTED = "only-evicted";
	public static final String PARAMETER_MAPPING_UUID = "mapping-uuid";

	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;

	private UUID systemId;
	private boolean onlyEvicted;
	private String entityType;
	private UUID mappingId;

	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);

		MultiValueMap<String, UUID> params = getParameterConverter().toSystemMappingAttribute(properties, CoreResultCode.LONG_RUNNING_TASK_WRONG_CONFIGURATION);
		systemId = params.getFirst(ParameterConverter.PARAMETER_SYSTEM);
		mappingId = params.getFirst(ParameterConverter.PARAMETER_SYSTEM_MAPPING);
		onlyEvicted = getParameterConverter().toBoolean(properties, PARAMETER_ONLY_EVICTED);
		entityType = getParameterConverter().toString(properties, PARAMETER_ENTITY_TYPE);
	}

	@Override
	protected boolean start() {
		LOG.info("Start: Merge - attribute controlled values recalculation for system [{}]", systemId);
		//
		return super.start();
	}

	@Override
	public Boolean process() {
		this.counter = 0L;
		SysSystemMappingDto mapping = systemMappingService.findProvisioningMapping(systemId, entityType, mappingId);
		Assert.notNull(mapping, "Provisioning mapping is mandatory!");

		// Loading all attributes for recalculation (on given system)
		SysSystemAttributeMappingFilter attributeFilter = new SysSystemAttributeMappingFilter();
		attributeFilter.setSystemId(systemId);
		attributeFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMappingDto> attributes = systemAttributeMappingService.find(attributeFilter, null) //
				.getContent().stream() //
				.filter(attribute -> AttributeMappingStrategyType.MERGE == attribute.getStrategyType()) //
				.filter(attribute -> !onlyEvicted ? true : attribute.isEvictControlledValuesCache()) //
				.collect(Collectors.toList());

		// Total items
		count = Long.valueOf(attributes.size());

		for (SysSystemAttributeMappingDto attribute : attributes) {
			counter++;
			boolean canContinue = updateState();
			if (!canContinue) {
				break;
			}
			try {
				SysSchemaAttributeDto schemaAttributeDto = DtoUtils.getEmbedded(attribute,
						SysSystemAttributeMapping_.schemaAttribute, SysSchemaAttributeDto.class);
				// Recalculate controlled values
				systemAttributeMappingService.recalculateAttributeControlledValues(systemId, entityType,
						schemaAttributeDto.getName(), attribute, mappingId);
				// Success
				this.logItemProcessed(attribute, new OperationResult.Builder(OperationState.EXECUTED).build());
			} catch (Exception ex) {
				this.logItemProcessed(attribute, new OperationResult.Builder(OperationState.EXCEPTION)
						.setException(new ResultCodeException(AccResultCode.PROVISIONING_EX_ATTR_CONTROLED_VALUE_RECALC,
								ImmutableMap.of("attribute", attribute.getName(), "system", systemId), ex))
						.build());
			}
		};

		LOG.info("End: Merge - attribute controlled values recalculation for system [{}]", systemId);
		return Boolean.TRUE;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = super.getProperties();
		properties.put(PARAMETER_SYSTEM_UUID, systemId);
		properties.put(PARAMETER_ONLY_EVICTED, onlyEvicted);
		properties.put(PARAMETER_ENTITY_TYPE, entityType);
		properties.put(PARAMETER_MAPPING_UUID, mappingId);
		return properties;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto evictedAttribute = new IdmFormAttributeDto(PARAMETER_ONLY_EVICTED, PARAMETER_ONLY_EVICTED,
				PersistentType.BOOLEAN);
		evictedAttribute.setDefaultValue(String.valueOf(Boolean.TRUE));
		evictedAttribute.setRequired(false);
		IdmFormAttributeDto entityTypeAttribute = new IdmFormAttributeDto(PARAMETER_ENTITY_TYPE, PARAMETER_ENTITY_TYPE,
				PersistentType.SHORTTEXT);
		entityTypeAttribute.setDefaultValue(String.valueOf(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE));
		entityTypeAttribute.setRequired(true);
		IdmFormAttributeDto mappingAttribute = new IdmFormAttributeDto(ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES, ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES,
				PersistentType.TEXT);
		mappingAttribute.setRequired(true);
		mappingAttribute.setFaceType(AccFaceType.SYSTEM_MAPPING_ATTRIBUTE_FILTERED_SELECT);
		//
		return Lists.newArrayList(mappingAttribute, entityTypeAttribute, evictedAttribute);
	}

	@Override
	public boolean isRecoverable() {
		return true;
	}
}
