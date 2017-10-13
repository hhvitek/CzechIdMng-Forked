package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningBreakConfiguration;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningBreakItems;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningBreakConfigFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBreakConfig_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBreakConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBreakRecipientService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationTemplateDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Default implementation for {@link SysProvisioningBreakConfigService}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
public class DefaultSysProvisioningBreakConfigService extends
		AbstractReadWriteDtoService<SysProvisioningBreakConfigDto, SysProvisioningBreakConfig, SysProvisioningBreakConfigFilter>
		implements SysProvisioningBreakConfigService {

	private final Integer MAX_CONFIGS_FOR_SYSTEM = 3;
	private final String CACHE_NAME = "idm-provisioning-cache";
	
	private final SysProvisioningBreakRecipientService breakRecipientService;
	private final CacheManager cacheManager;
	private final ProvisioningBreakConfiguration provisioningBreakConfiguration;

	@Autowired
	public DefaultSysProvisioningBreakConfigService(SysProvisioningBreakConfigRepository repository,
			SysProvisioningBreakRecipientService breakRecipientService,
			CacheManager cacheManager,
			ProvisioningBreakConfiguration provisioningBreakConfiguration) {
		super(repository);
		//
		Assert.notNull(breakRecipientService);
		Assert.notNull(cacheManager);
		Assert.notNull(provisioningBreakConfiguration);
		//
		this.breakRecipientService = breakRecipientService;
		this.cacheManager = cacheManager;
		this.provisioningBreakConfiguration = provisioningBreakConfiguration;
	}
	
	@Override
	public Page<SysProvisioningBreakConfigDto> find(SysProvisioningBreakConfigFilter filter, Pageable pageable,
			BasePermission... permission) {
		Page<SysProvisioningBreakConfigDto> configs = super.find(filter, pageable, permission);
		//
		// if include global config and set systemId add global configurations
		if (filter != null && filter.isIncludeGlobalConfig() && filter.getSystemId() != null && configs.getTotalElements() != MAX_CONFIGS_FOR_SYSTEM) {
			List<SysProvisioningBreakConfigDto> configsList = addGlobalConfigs(configs.getContent(), filter.getSystemId());
			//
			PageRequest pageRequest = new PageRequest(pageable.getPageNumber(), configsList.size(), pageable.getSort());
			Page<SysProvisioningBreakConfigDto> dtoPage = new PageImpl<>(configsList, pageRequest, configsList.size());
			return dtoPage;
		}
		//
		return configs;
	}
	
	@Override
	public void delete(SysProvisioningBreakConfigDto dto, BasePermission... permission) {
		Assert.notNull(dto);
		// check global configuration
		if (dto.getGlobalConfiguration() != null && dto.getGlobalConfiguration()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_BREAK_GLOBAL_CONFIG_DELETE, ImmutableMap.of("operationType", dto.getOperationType()));
		}
		breakRecipientService.deleteAllByBreakConfig(dto.getId());
		super.delete(dto, permission);
	}
	
	@Override
	public SysProvisioningBreakConfigDto save(SysProvisioningBreakConfigDto dto, BasePermission... permission) {
		// check global configuration
		if (dto.getGlobalConfiguration() != null && dto.getGlobalConfiguration()) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_BREAK_GLOBAL_CONFIG_SAVE, ImmutableMap.of("operationType", dto.getOperationType()));
		}
		// check if for same system doesn't exist same operation type
		SysProvisioningBreakConfigFilter filter = new SysProvisioningBreakConfigFilter();
		filter.setSystemId(dto.getSystem());
		filter.setOperationType(dto.getOperationType());
		List<SysProvisioningBreakConfigDto> similarConfigs = this.find(filter, null).getContent();
		boolean existSimilar = similarConfigs.stream().filter(config -> !config.getId().equals(dto.getId())).findFirst().isPresent();
		if (!existSimilar) {
			return super.save(dto, permission);			
		}
		throw new ProvisioningException(AccResultCode.PROVISIONING_BREAK_OPERATION_EXISTS,
				ImmutableMap.of("operationType", dto.getOperationType(), "systemId", dto.getSystem()));
	}
	
	@Override
	public SysProvisioningBreakItems getCacheProcessedItems(UUID systemId) {
		SimpleValueWrapper cachedValueWrapper = (SimpleValueWrapper) this.getCache().get(systemId);
		if (cachedValueWrapper == null) {
			return new SysProvisioningBreakItems();
		}
		SysProvisioningBreakItems cache = (SysProvisioningBreakItems) cachedValueWrapper.get();
		if (cache == null) {
			return new SysProvisioningBreakItems();
		}
		return cache;
	}

	@Override
	public void saveCacheProcessedItems(UUID systemId, SysProvisioningBreakItems cache) {
		this.getCache().put(systemId, cache);
	}

	private Cache getCache() {
		return this.cacheManager.getCache(CACHE_NAME);
	}
	
	@Override
	protected SysProvisioningBreakConfigDto toDto(SysProvisioningBreakConfig entity,
			SysProvisioningBreakConfigDto dto) {
		SysProvisioningBreakConfigDto newDto = super.toDto(entity, dto);
		//
		if (newDto != null) {
			// set provisioning break counter
			newDto.setActualOperationCount(getCounter(newDto.getSystem(), newDto.getOperationType()));
		}
		//
		return newDto;
	}

	@Override
	public SysProvisioningBreakConfigDto getConfig(ProvisioningEventType operationType, UUID systemId) {
		SysProvisioningBreakConfigFilter filter = new SysProvisioningBreakConfigFilter();
		filter.setOperationType(operationType);
		filter.setSystemId(systemId);
		List<SysProvisioningBreakConfigDto> configs = this.find(filter, null).getContent();
		//
		if (configs.isEmpty()) {
			return null;
		}
		// must exists only one configs for operation type and system id
		return configs.stream().findFirst().get();
	}

	@Override
	public SysProvisioningBreakConfigDto getGlobalBreakConfiguration(ProvisioningEventType eventType) {
		SysProvisioningBreakConfigDto globalConfig = new SysProvisioningBreakConfigDto();
		Boolean disable = provisioningBreakConfiguration.getDisabled(eventType);
		if (disable == null) {
			// global provisioning break configuration isn't set
			return null;
		}
		globalConfig.setDisabled(disable);
		globalConfig.setDisableLimit(provisioningBreakConfiguration.getDisableLimit(eventType));
		globalConfig.setGlobalConfiguration(Boolean.TRUE);
		//
		IdmNotificationTemplateDto disabledTemplate = provisioningBreakConfiguration.getDisableTemplate(eventType);
		if (disabledTemplate != null) {
			globalConfig.setDisableTemplate(disabledTemplate.getId());
			globalConfig.setDisableTemplateEmbedded(disabledTemplate);
		}
		//
		IdmNotificationTemplateDto warningTemplate = provisioningBreakConfiguration.getWarningTemplate(eventType);
		if (warningTemplate != null) {
			globalConfig.setWarningTemplate(warningTemplate.getId());
			globalConfig.setWarningTemplateEmbedded(warningTemplate);
		}
		//
		globalConfig.setOperationType(eventType);
		globalConfig.setPeriod(provisioningBreakConfiguration.getPeriod(eventType));
		globalConfig.setSystem(null); // global provisioning break hasn't system id, don't save global config
		globalConfig.setWarningLimit(provisioningBreakConfiguration.getWarningLimit(eventType));
		globalConfig.setTrimmed(true);
		return globalConfig;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.SYSTEM, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<SysProvisioningBreakConfig> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, SysProvisioningBreakConfigFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.system).get(AbstractEntity_.id), filter.getSystemId()));
		}
		//
		if (filter.getPeriod() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.period), filter.getPeriod()));
		}
		//
		if (filter.getWarningLimit() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.warningLimit), filter.getWarningLimit()));
		}
		//
		if (filter.getDisableLimit() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.disableLimit), filter.getDisableLimit()));
		}
		//
		if (filter.getOperationType() != null) {
			predicates.add(builder.equal(root.get(SysProvisioningBreakConfig_.operationType), filter.getOperationType()));
		}
		//
		return predicates;
	}

	@Override
	public void clearCache(UUID systemId, ProvisioningEventType event) {
		SysProvisioningBreakItems cache = this.getCacheProcessedItems(systemId);
		cache.clearRecords(event);
	}
	
	/**
	 * Method return counter for system id and operation type
	 * 
	 * @param systemId
	 * @param operationType
	 * @return
	 */
	private Integer getCounter(UUID systemId, ProvisioningEventType operationType) {
		// set provisioning break counter
		SysProvisioningBreakItems cache = this.getCacheProcessedItems(systemId);
		return cache.getSize(operationType);
	}
	
	/**
	 * Methods for system and his provisioning break config add global configuration
	 *
	 * @param configs
	 * @param systemId
	 * @return
	 */
	private List<SysProvisioningBreakConfigDto> addGlobalConfigs(List<SysProvisioningBreakConfigDto> configsOld, UUID systemId) {
		boolean containsCreate = configsOld.stream().filter(item -> item.getOperationType() == ProvisioningEventType.CREATE).findFirst().isPresent();
		boolean containsDelete = configsOld.stream().filter(item -> item.getOperationType() == ProvisioningEventType.DELETE).findFirst().isPresent();
		boolean containsUpdate = configsOld.stream().filter(item -> item.getOperationType() == ProvisioningEventType.UPDATE).findFirst().isPresent();
		// unmodifiable list, create copy
		List<SysProvisioningBreakConfigDto> configs = new ArrayList<>(configsOld);
		if (!containsCreate) {
			SysProvisioningBreakConfigDto global = this.getGlobalBreakConfiguration(ProvisioningEventType.CREATE);
			if (global != null) {
				global.setActualOperationCount(getCounter(systemId, ProvisioningEventType.CREATE));
				configs.add(global);
			}
		}
		if (!containsDelete) {
			SysProvisioningBreakConfigDto global = this.getGlobalBreakConfiguration(ProvisioningEventType.DELETE);
			if (global != null) {
				global.setActualOperationCount(getCounter(systemId, ProvisioningEventType.DELETE));
				configs.add(global);
			}
		}
		if (!containsUpdate) {
			SysProvisioningBreakConfigDto global = this.getGlobalBreakConfiguration(ProvisioningEventType.UPDATE);
			if (global != null) {
				global.setActualOperationCount(getCounter(systemId, ProvisioningEventType.UPDATE));
				configs.add(global);
			}
		}
		return configs;
	}
}
