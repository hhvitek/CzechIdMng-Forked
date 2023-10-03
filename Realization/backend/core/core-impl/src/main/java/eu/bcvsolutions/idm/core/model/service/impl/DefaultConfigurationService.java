package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import eu.bcvsolutions.idm.core.api.audit.service.SiemLoggerManager;
import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;
import eu.bcvsolutions.idm.core.api.domain.comparator.CodeableComparator;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.config.domain.DynamicCorsConfiguration;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration;
import eu.bcvsolutions.idm.core.model.entity.IdmConfiguration_;
import eu.bcvsolutions.idm.core.model.event.processor.configuration.ConfigurationSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmConfigurationRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Default implementation finds configuration in database, if configuration for
 * given key is not found, then configuration in property file will be returned. 
 * Public (not secured) configuration could be read without authentication. 
 * Confidential properties are saved to confidential storage in ({@link ConfigurationSaveProcessor}).
 * 
 * Cache manager is used directly without annotations - its easier for overloaded methods and internal cache usage.
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultConfigurationService 
		extends AbstractEventableDtoService<IdmConfigurationDto, IdmConfiguration, DataFilter> 
		implements IdmConfigurationService, ConfigurationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultConfigurationService.class);
	//
	private final IdmConfigurationRepository repository;
	private final ConfidentialStorage confidentialStorage;
	private final ConfigurableEnvironment env;
	private final IdmCacheManager cacheManager;
	//
	@Autowired
	public DefaultConfigurationService(
			IdmConfigurationRepository repository,
			ConfidentialStorage confidentialStorage,
			ConfigurableEnvironment env,
			EntityEventManager entityEventManager,
			IdmCacheManager cacheManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
		this.confidentialStorage = confidentialStorage;
		this.env = env;
		this.cacheManager = cacheManager;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.CONFIGURATION, getEntityClass());
	}
	
	/**
	 * @since 12.0.0
	 */
	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmConfigurationDto getByCode(String name) {
		return toDto(repository.findOneByName(name));
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmConfiguration> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			DataFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.like(builder.lower(root.get(IdmConfiguration_.name)), "%" + filter.getText().toLowerCase() + "%"));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getValue(String key) {
		return getValue(key, null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<String> getValues(String key) {
		String value = getValue(key);
		if (StringUtils.isBlank(value)) {
			return Collections.<String>emptyList();
		}
		return Arrays
				.stream(value.split(PROPERTY_MULTIVALUED_SEPARATOR))
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.collect(Collectors.toList());
	}
	
	@Override
	@Transactional
	public void setValue(String key, String value) {
		Assert.hasText(key, "Key is required.");
		//
		saveConfiguration(new IdmConfigurationDto(key, value));
	}
	
	@Override
	@Transactional
	public void setValues(String key, List<String> values) {
		Assert.hasText(key, "Key is required.");
		//
		// join not null values
		String value = null;
		if (values != null) {
			values.removeIf(Objects::isNull);
			if (CollectionUtils.isNotEmpty(values)) {
				value = StringUtils.join(values, PROPERTY_MULTIVALUED_SEPARATOR);
			}
		}
		//
		setValue(key, value);
	}
	
	@Override
	@Transactional
	public String deleteValue(String key) {
		IdmConfigurationDto dto = getByCode(key);
		if (dto == null) {
			return null;
		}
		delete(dto);
		//
		return dto.getValue();
	}
	
	@Override
	@Transactional
	public void saveConfiguration(IdmConfigurationDto configuration) {
		Assert.notNull(configuration, "Configuration is required.");
		Assert.hasText(configuration.getName(), "Configuration name is required.");
		// only maps dto to entity
		IdmConfigurationDto configurationEntity = getByCode(configuration.getName());
		if (configurationEntity == null) {
			configurationEntity = new IdmConfigurationDto(configuration.getName(), configuration.getValue(), configuration.isSecured(), configuration.isConfidential());
		} else {
			configurationEntity.setValue(configuration.getValue());
			configurationEntity.setSecured(configuration.isSecured());
			configurationEntity.setConfidential(configuration.isConfidential());
		}
		save(configurationEntity);
	}
	
	@Override
	@Transactional
	public IdmConfigurationDto saveInternal(IdmConfigurationDto dto) {
		Assert.notNull(dto, "Entity is required.");
		// check secured option
		if (shouldBeSecured(dto.getName())) {
			dto.setSecured(true);
		}
		//
		dto = super.saveInternal(dto);
		if (dto.isConfidential()
				&& dto.getValue() != null) {
			dto.setValue(GuardedString.SECRED_PROXY_STRING);
		}
		//
		return dto;
	}
	
	/**
	 * @since 12.0.0
	 */
	@Override
	protected IdmConfigurationDto applyContext(IdmConfigurationDto dto, DataFilter context, BasePermission... permission) {
		dto = super.applyContext(dto, context, permission);
		//
		if (dto != null 
				&& StringUtils.isNotEmpty(dto.getValue())
				&& dto.isConfidential()
				&& context != null
				&& context.isAddSecredProxyString()) {
			dto.setValue(GuardedString.SECRED_PROXY_STRING);
		}
		//
		return dto;
	}
	
	@Override
	@Transactional
	public EventContext<IdmConfigurationDto> publish(EntityEvent<IdmConfigurationDto> event, EntityEvent<?> parentEvent, BasePermission... permission) {
		Assert.notNull(event, "Event must be not null!");
		IdmConfigurationDto content = event.getContent();
		Assert.notNull(content, "Content (dto) in event must be not null!");
		// 
		// load original source without confidential proxy string to support audit changes
		if (!isNew(content)) {
			DataFilter context = new DataFilter(getDtoClass());
			context.setAddSecredProxyString(false);
			event.setOriginalSource(get(content.getId(), context));
		}
		return super.publish(event, parentEvent, permission);
	}
	
	@Override
	@Transactional
	public void deleteInternal(IdmConfigurationDto dto) {
		Assert.notNull(dto, "DTO is required.");
		//
		if (dto.isConfidential()) {
			confidentialStorage.delete(dto.getId(), getEntityClass(), CONFIDENTIAL_PROPERTY_VALUE);
			LOG.debug("Configuration value [{}] was removed from confidential storage", dto.getName());
		}
		super.deleteInternal(dto);
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getValue(String key, String defaultValue) {
		ValueWrapper cachedValue = getCachedValue(key);
		if (cachedValue != null) {
			return (String) cachedValue.get();
		}	
		//
		LOG.debug("Reading configuration for key [{}]", key);
		String value = null;
		boolean confidential = true;
		// IdM configuration has higher priority than property file.
		IdmConfigurationDto config = getByCode(key);
		if (config != null) {
			if (config.isConfidential()) {
				value = confidentialStorage.get(config.getId(), getEntityClass(), CONFIDENTIAL_PROPERTY_VALUE, String.class);
				LOG.debug("Configuration value for key [{}] was found in confidential storage", config.getName());
			} else {			
				value = config.getValue();
				confidential = false;
				LOG.trace("Configuration value for key [{}] was found in database.", key);
			}			
		} else if (env != null) {
			// try to find value in property configuration
			value = env.getProperty(key);
			confidential = GuardedString.shouldBeGuarded(key);
		}
		// fill default value
		if (value == null) { // TODO: null vs. isEmpty?
			value = defaultValue;
		}	
		LOG.debug("Resolved configuration value for key [{}] is [{}].", key, confidential ? GuardedString.SECRED_PROXY_STRING : value);
		setCachedValue(key, value);
		return value;
	}
	
	@Override
	@Transactional(readOnly = true)
	public GuardedString getGuardedValue(String key) {
		return getGuardedValue(key, null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public GuardedString getGuardedValue(String key, String defaultValue) {
		String value = getValue(key, defaultValue);
		return value == null ? null : new GuardedString(value);
	}

	@Override
	@Transactional(readOnly = true)
	public Boolean getBooleanValue(String key) {
		String value = getValue(key);
		return value == null ? null : Boolean.valueOf(value);
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean getBooleanValue(String key, boolean defaultValue) {
		Boolean value = getBooleanValue(key);
		return value == null ? defaultValue : value;
	}
	
	@Override
	@Transactional
	public void setBooleanValue(String key, boolean value) {
		setValue(key, Boolean.valueOf(value).toString());
	}
	
	@Override
	@Transactional(readOnly = true)
	public Integer getIntegerValue(String key) {
		String value = getValue(key);
		return value == null ? null : Integer.valueOf(value);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Integer getIntegerValue(String key, Integer defaultValue) {
		String value = getValue(key);
		try {
			return value == null ? defaultValue : Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			LOG.warn("Property [{}] for key [{}] is not integer, returning default value [{}]", value, key, defaultValue, ex);
			return defaultValue; 
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getLongValue(String key) {
		String value = getValue(key);
		//
		return StringUtils.isBlank(value) ? null : Long.valueOf(value);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long getLongValue(String key, Long defaultValue) {
		String value = getValue(key);
		try {
			return value == null ? defaultValue : Long.valueOf(value);
		} catch (NumberFormatException ex) {
			LOG.warn("Property [{}] for key [{}] is not integer, returning default value [{}]", value, key, defaultValue, ex);
			return defaultValue; 
		}
	}

	/**
	 * Returns all public configuration properties
	 * 
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmConfigurationDto> getAllPublicConfigurations() {
		Map<String, Object> configurations = new HashMap<>();
		// defaults from property file
		Map<String, Object> map = getAllProperties(env);
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(IDM_PUBLIC_PROPERTY_PREFIX)) {
				configurations.put(key, entry.getValue());
			}
		}
		// override from database
		repository.findAllBySecuredIsFalse().forEach(idmConfiguration -> {
			configurations.put(idmConfiguration.getName(), idmConfiguration.getValue());
		});
		List<IdmConfigurationDto> results = new ArrayList<>();
		configurations.forEach((k, v) -> {
			results.add(toConfigurationDto(k, v));
		});
		return results;
	}
	
	@Override
	public List<IdmConfigurationDto> getAllConfigurationsFromFiles(BasePermission... permission) {
		Map<String, Object> map = getAllProperties(env);
		return map.entrySet().stream()
				.filter(entry -> {
					return entry.getKey().toLowerCase().startsWith(IDM_PROPERTY_PREFIX);
				})
				.map(entry -> {
					return toConfigurationDto(entry.getKey(), entry.getValue());
				})
				.filter(dto -> {
					// apply security
					BasePermission[] permissions = PermissionUtils.trimNull(permission);
					return ObjectUtils.isEmpty(permissions)
							|| getAuthorizationManager().evaluate(toEntity(dto), permissions);
				})
				.sorted(new CodeableComparator())
				.collect(Collectors.toList());
	}
	
	/**
	 * Returns server environment properties
	 * 
	 * @return
	 */
	@Override
	public List<IdmConfigurationDto> getAllConfigurationsFromEnvironment() {
		Map<String, Object> map = getAllProperties(env);
		return map.entrySet().stream()
				.filter(entry -> {
					return !entry.getKey().toLowerCase().startsWith(IDM_PROPERTY_PREFIX);
				})
				.map(entry -> {
					return toConfigurationDto(entry.getKey(), entry.getValue());
				})
				.sorted(new CodeableComparator())
				.collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getInstanceId() {
		// This should be returned from env. only => instances should not be reused by DB property
		return env.getProperty(PROPERTY_APP_INSTANCE_ID, DEFAULT_APP_INSTANCE_ID);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, IdmConfigurationDto> getConfigurations(String keyPrefix) {
		Map<String, IdmConfigurationDto> configs = new HashMap<>();
		for (IdmConfiguration configuration : repository.findByNameStartingWith(keyPrefix, null)) {
			configs.put(
					configuration.getName().replaceFirst(keyPrefix + PROPERTY_SEPARATOR, ""), 
					new IdmConfigurationDto(configuration.getName(), configuration.getValue(), configuration.isSecured(), configuration.isConfidential())
			);
		}
		return configs;
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getFrontendUrl(String path) {
		String frontendUrl = getValue(ApplicationConfiguration.PROPERTY_FRONTEND_URL);
		if (StringUtils.isBlank(frontendUrl)) {
			// get url of application from allowed origin (~ backward compatible)
			String allowedOrigins = getValue(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN);
			if (StringUtils.isBlank(allowedOrigins) || allowedOrigins.equals("*")) {
				// relative url is returned, when allowed origin is not configured
				return path;
			}
			//
			List<String> urls = Arrays.asList(allowedOrigins.replaceAll("\\s*", "").split(DynamicCorsConfiguration.PROPERTY_ALLOWED_ORIGIN_SEPARATOR));
			frontendUrl = urls.get(0);
		}
		//
		if (StringUtils.isEmpty(path)) {
			return frontendUrl;
		}
		// append path by FE conventions
		return String.format("%s/#/%s", frontendUrl, path.startsWith("/") ? path.replaceFirst("/", "") : path);
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getDateFormat() {
		return getValue(PROPERTY_APP_DATE_FORMAT, DEFAULT_APP_DATE_FORMAT);
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getDateTimeFormat() {
		return getValue(PROPERTY_APP_DATETIME_FORMAT, DEFAULT_APP_DATETIME_FORMAT);
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getDateTimeSecondsFormat() {
		return getValue(PROPERTY_APP_DATETIME_WITH_SECONDS_FORMAT, DEFAULT_APP_DATETIME_WITH_SECONDS_FORMAT);
	}
	
	@Override
	protected IdmConfigurationDto internalExport(UUID id) {

		IdmConfigurationDto dto = super.internalExport(id);
		// If configuration property is confidential, then set value to the null. We
		// don't want modified value in target IdM.
		if (dto != null && dto.isConfidential()) {
			dto.setValue(null);
		}
		return dto;
	}
	
	/**
	 * Method provides specific logic for configuration change siem logging.
	 * 
	 */
	@Override
	protected void siemLog(EntityEvent<IdmConfigurationDto> event, String status, String detail) {
		if (event == null) {
			return;
		}
		IdmConfigurationDto dto = event.getContent();
		String operationType = event.getType().name();
		String action = siemLoggerManager.buildAction(SiemLoggerManager.CONFIGURATION_LEVEL_KEY, operationType);
		if(siemLoggerManager.skipLogging(action)) {
			return;
		}		
		String transactionUuid = Objects.toString(dto.getTransactionId(),"");
		siemLog(action, status, dto, null, transactionUuid, detail);
	}
	
	private static IdmConfigurationDto toConfigurationDto(String key, Object value) {
		String stringValue = value == null ? null : value.toString();
		IdmConfigurationDto configuration = new IdmConfigurationDto(key, stringValue);
		// password etc. has to be guarded - can be used just in BE
		if (GuardedString.shouldBeGuarded(configuration.getCode())) {
			LOG.debug("Configuration value for property [{}] is guarded.", configuration.getName());
			configuration.setValue(GuardedString.SECRED_PROXY_STRING);
			configuration.setConfidential(true);
		}
		if (shouldBeSecured(configuration.getName())) {
			configuration.setSecured(true);
		}
		return configuration;
	}

	private static Map<String, Object> getAllProperties(ConfigurableEnvironment aEnv) {
		Map<String, Object> result = new HashMap<>();
		aEnv.getPropertySources().forEach(ps -> addAll(result, getAllProperties(ps)));
		return result;
	}

	private static Map<String, Object> getAllProperties(PropertySource<?> aPropSource) {
		Map<String, Object> result = new HashMap<>();

		if (aPropSource instanceof CompositePropertySource) {
			CompositePropertySource cps = (CompositePropertySource) aPropSource;
			cps.getPropertySources().forEach(ps -> addAll(result, getAllProperties(ps)));
			return result;
		}
		
		if (aPropSource instanceof EnumerablePropertySource<?>) {
			EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) aPropSource;
			Arrays.asList(ps.getPropertyNames()).forEach(key -> result.put(key, ps.getProperty(key)));
			return result;
		}
		return result;

	}

	private static void addAll(Map<String, Object> aBase, Map<String, Object> aToBeAdded) {
		for (Entry<String, Object> entry : aToBeAdded.entrySet()) {
			if (aBase.containsKey(entry.getKey())) {
				continue;
			}
			aBase.put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * Returns true, if key should be secured by naming convention. Confidential properties are always secured.
	 * 
	 * @param key
	 * @return
	 */
	private static boolean shouldBeSecured(String key) {
		return key.startsWith(ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX) || GuardedString.shouldBeGuarded(key);
	}
	
	private ValueWrapper getCachedValue(String key) {
		return cacheManager.getValue(CACHE_NAME, key);
	}
	
	private void setCachedValue(String key, String value) {
		cacheManager.cacheValue(CACHE_NAME, key, value);
	}

}
