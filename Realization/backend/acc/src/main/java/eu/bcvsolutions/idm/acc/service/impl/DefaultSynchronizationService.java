package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityTypeManager;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;

/**
 * Service for do synchronization and reconciliation
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSynchronizationService implements SynchronizationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSynchronizationService.class);
	//
	private final SysSystemAttributeMappingService attributeHandlingService;
	private final SysSyncConfigService synchronizationConfigService;
	private final SysSyncLogService synchronizationLogService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;
	private final PluginRegistry<SynchronizationEntityExecutor, SystemEntityTypeRegistrable> pluginExecutors;
	private final SysSystemMappingService systemMappingService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	//
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private IdmLongRunningTaskService longRunningTaskService;
	@Autowired
	private IdmCacheManager idmCacheManager;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private SysSystemEntityTypeManager systemEntityManager;

	@Autowired
	public DefaultSynchronizationService(SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			AccAccountService accountService, SysSystemEntityService systemEntityService, LongRunningTaskManager longRunningTaskManager,
			List<SynchronizationEntityExecutor> executors, SysSystemMappingService systemMappingService,
			SysSystemService systemService, SysSchemaObjectClassService schemaObjectClassService) {
		Assert.notNull(attributeHandlingService, "Service is required.");
		Assert.notNull(synchronizationConfigService, "Service is required.");
		Assert.notNull(synchronizationLogService, "Service is required.");
		Assert.notNull(accountService, "Service is required.");
		Assert.notNull(systemEntityService, "Service is required.");
		Assert.notNull(longRunningTaskManager, "Manager is required.");
		Assert.notNull(executors, "Executors are required.");
		Assert.notNull(systemMappingService, "Service is required.");
		Assert.notNull(systemService, "Service is required.");
		Assert.notNull(schemaObjectClassService, "Service is required.");
		//
		this.attributeHandlingService = attributeHandlingService;
		this.synchronizationConfigService = synchronizationConfigService;
		this.synchronizationLogService = synchronizationLogService;
		this.accountService = accountService;
		this.systemEntityService = systemEntityService;
		this.systemMappingService = systemMappingService;
		this.schemaObjectClassService = schemaObjectClassService;
		//
		this.pluginExecutors = OrderAwarePluginRegistry.create(executors);
	}

	/**
	 * Cancel all previously ran synchronizations
	 */
	@Override
	@Transactional
	public void init() {
		String instanceId = configurationService.getInstanceId();
		LOG.info("Cancel unprocessed synchronizations - tasks was interrupt during instance [{}] restart", instanceId);
		//
		// find all running sync on all instances
		IdmLongRunningTaskFilter lrtFilter = new IdmLongRunningTaskFilter();
		lrtFilter.setRunning(Boolean.TRUE);
		lrtFilter.setTaskType(SynchronizationSchedulableTaskExecutor.class.getCanonicalName());
		List<IdmLongRunningTaskDto> allRunningSynchronizations = longRunningTaskService.find(lrtFilter, null)
				.getContent();
		// stop logs on the same instance id
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setRunning(Boolean.TRUE);
		synchronizationLogService.find(logFilter, null).forEach(sync -> {
			boolean runningOnOtherInstance = allRunningSynchronizations.stream().anyMatch(lrt -> {
				return !lrt.getInstanceId().equals(instanceId) && sync.getSynchronizationConfig()
						.equals(lrt.getTaskProperties().get(PARAMETER_SYNCHRONIZATION_ID));
			});
			if (!runningOnOtherInstance) {
				String message = MessageFormat.format(
						"Cancel unprocessed synchronization [{0}] - tasks was interrupt during instance [{1}] restart",
						sync.getId(), instanceId);
				LOG.info(message);
				sync.addToLog(message);
				sync.setRunning(false);
				synchronizationLogService.save(sync);
			}
		});
		// Clear the executor cache
		this.idmCacheManager.evictCache(SYNC_EXECUTOR_CACHE_NAME);
	}

	@Override
	public AbstractSysSyncConfigDto startSynchronization(AbstractSysSyncConfigDto config) {
		Assert.notNull(config, "Configuration is required.");
		Assert.notNull(config.getId(), "Id of sync config is required!");
		SynchronizationSchedulableTaskExecutor lrt = new SynchronizationSchedulableTaskExecutor(config.getId());
		longRunningTaskManager.execute(lrt);
		return config;
	}

	/**
	 * Prepare and execute sync executor
	 */
	@Override
	public void startSynchronization(AbstractSysSyncConfigDto config,
			AbstractSchedulableTaskExecutor<Boolean> longRunningTaskExecutor) {

		Assert.notNull(config, "Sync configuration is required!");
		Assert.notNull(config.getId(), "Id of sync configuration is required!");
		
		if (synchronizationConfigService.isRunning(config)) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}
		
		UUID syncConfigId = config.getId();
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		Assert.notNull(mapping, "Mapping is required.");
		String entityType = mapping.getEntityType();

		SynchronizationEntityExecutor executor = getSyncExecutor(entityType, syncConfigId);
		executor.setLongRunningTaskExecutor(longRunningTaskExecutor);
		executor.process(config.getId());
	}

	@Override
	public AbstractSysSyncConfigDto stopSynchronization(AbstractSysSyncConfigDto config) {
		Assert.notNull(config, "Configuration is required.");
		// Synchronization must be running
		SysSyncLogFilter logFilter = new SysSyncLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		logFilter.setRunning(Boolean.TRUE);
		List<SysSyncLogDto> logs = synchronizationLogService.find(logFilter, null).getContent();

		if (logs.isEmpty()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_NOT_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}

		logs.forEach(log -> {
			log.setRunning(false);
			log.setEnded(ZonedDateTime.now());
		});
		synchronizationLogService.saveAll(logs);
		return config;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public boolean doItemSynchronization(SynchronizationContext context) {
		Assert.notNull(context, "Context is required.");
		return getSyncExecutor(context.getEntityType(), context.getConfig().getId()).doItemSynchronization(context);
	}

	@Override
	public SysSyncItemLogDto resolveMissingEntitySituation(String uid, String entityType,
			List<IcAttribute> icAttributes, UUID configId, String actionType) {
		Assert.notNull(uid, "Uid is required.");
		Assert.notNull(entityType, "Entity type is required.");
		Assert.notNull(icAttributes, "Connector attribues are required.");
		Assert.notNull(configId, "Configuration identifier is required.");
		Assert.notNull(actionType, "Action type is required.");

		AbstractSysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());

		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(sysSchemaObjectClassDto, SysSchemaObjectClass_.system);

		SysSystemAttributeMappingFilter attributeHandlingFilter = new SysSystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMappingDto> mappedAttributes = attributeHandlingService
				.find(attributeHandlingFilter, null).getContent();
		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();
		// Little workaround, we have only IcAttributes ... we create IcObject manually
		IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
		icObject.setAttributes(icAttributes);
		icObject.setUidValue(uid);

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid) //
				.addSystem(system) //
				.addConfig(config) //
				.addEntityType(entityType) //
				.addLogItem(itemLog) //
				.addMappedAttributes(mappedAttributes) //
				.addIcObject(icObject); //

		getSyncExecutor(entityType, configId)
				.resolveMissingEntitySituation(SynchronizationMissingEntityActionType.valueOf(actionType), context);
		return itemLog;

	}

	@Override
	public SysSyncItemLogDto resolveLinkedSituation(String uid, String entityType,
			List<IcAttribute> icAttributes, UUID accountId, UUID configId, String actionType) {
		Assert.notNull(uid, "Uid is required.");
		Assert.notNull(entityType, "Entity type is required.");
		Assert.notNull(icAttributes, "Connector attribues are required.");
		Assert.notNull(configId, "Configuration identifier is required.");
		Assert.notNull(actionType, "Action type is required.");
		Assert.notNull(accountId, "Account identifier is required.");

		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();

		AbstractSysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		AccAccountDto account = accountService.get(accountId);

		SysSystemAttributeMappingFilter attributeHandlingFilter = new SysSystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMappingDto> mappedAttributes = attributeHandlingService
				.find(attributeHandlingFilter, null).getContent();

		// Little workaround, we have only IcAttributes ... we create IcObject manually
		IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
		icObject.setAttributes(icAttributes);
		icObject.setUidValue(uid);

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid) //
				.addAccount(account) //
				.addConfig(config) //
				.addEntityType(entityType) //
				.addLogItem(itemLog) //
				.addMappedAttributes(mappedAttributes) //
				.addIcObject(icObject); //

		getSyncExecutor(entityType, configId)
				.resolveLinkedSituation(SynchronizationLinkedActionType.valueOf(actionType), context);
		return itemLog;
	}

	@Override
	public SysSyncItemLogDto resolveUnlinkedSituation(String uid, String entityType, UUID entityId,
			UUID configId, String actionType, List<IcAttribute> icAttributes) {
		Assert.notNull(uid, "Uid is required.");
		Assert.notNull(entityType, "Entity type is required.");
		Assert.notNull(configId, "Configuration identifier is required.");
		Assert.notNull(actionType, "Action type is required.");
		Assert.notNull(entityId, "Entity identifier is required.");

		AbstractSysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());

		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(sysSchemaObjectClassDto, SysSchemaObjectClass_.system);
		SysSystemEntityDto systemEntity = findSystemEntity(uid, system, entityType);
		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();

		SysSystemAttributeMappingFilter attributeHandlingFilter = new SysSystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMappingDto> mappedAttributes = attributeHandlingService
				.find(attributeHandlingFilter, null).getContent();
		
		// Little workaround, we have only IcAttributes ... we create IcObject manually
				IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
				icObject.setAttributes(icAttributes);
				icObject.setUidValue(uid);

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid) //
				.addSystem(system) //
				.addConfig(config) //
				.addEntityType(entityType) //
				.addEntityId(entityId) //
				.addLogItem(itemLog) //
				.addSystemEntity(systemEntity) //
				.addIcObject(icObject) //
				.addMappedAttributes(mappedAttributes); //


		getSyncExecutor(entityType, configId)
				.resolveUnlinkedSituation(SynchronizationUnlinkedActionType.valueOf(actionType), context);
		return itemLog;
	}

	@Override
	public SysSyncItemLogDto resolveMissingAccountSituation(String uid, String entityType, UUID accountId,
			UUID configId, String actionType) {
		Assert.notNull(uid, "Uid is required.");
		Assert.notNull(entityType, "Entity type is required.");
		Assert.notNull(configId, "Configuration identifier is required.");
		Assert.notNull(actionType, "Action type is required.");
		Assert.notNull(accountId, "Account identifier is required.");

		AbstractSysSyncConfigDto config = synchronizationConfigService.get(configId);
		SysSystemMappingDto mapping = systemMappingService.get(config.getSystemMapping());
		AccAccountDto account = accountService.get(accountId);
		SysSchemaObjectClassDto sysSchemaObjectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(sysSchemaObjectClassDto, SysSchemaObjectClass_.system);
		SysSyncItemLogDto itemLog = new SysSyncItemLogDto();

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid) //
				.addSystem(system) //
				.addConfig(config) //
				.addEntityType(entityType) //
				.addAccount(account) //
				.addLogItem(itemLog); //

		getSyncExecutor(entityType, configId)
				.resolveMissingAccountSituation(ReconciliationMissingAccountActionType.valueOf(actionType), context);
		return itemLog;
	}

	private SysSystemEntityDto findSystemEntity(String uid, SysSystemDto system, String entityType) {
		SysSystemEntityFilter systemEntityFilter = new SysSystemEntityFilter();
		systemEntityFilter.setEntityType(entityType);
		systemEntityFilter.setSystemId(system.getId());
		systemEntityFilter.setUid(uid);
		List<SysSystemEntityDto> systemEntities = systemEntityService.find(systemEntityFilter, null).getContent();
		SysSystemEntityDto systemEntity = null;
		if (systemEntities.size() == 1) {
			systemEntity = systemEntities.get(0);
		} else if (systemEntities.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_TO_MANY_SYSTEM_ENTITY, uid);
		}
		return systemEntity;
	}

	@Override
	public SynchronizationEntityExecutor getSyncExecutor(String entityType, UUID syncConfigId) {
		ValueWrapper value = this.idmCacheManager.getValue(SYNC_EXECUTOR_CACHE_NAME, syncConfigId);
		if (value == null) {
			return getExecutor(entityType, syncConfigId);
		}
		return (SynchronizationEntityExecutor) value.get();
	}

	private SynchronizationEntityExecutor getExecutor(String entityType, UUID syncConfigId) {
		SystemEntityTypeRegistrable systemEntityType = systemEntityManager.getSystemEntityByCode(entityType);
		SynchronizationEntityExecutor executor = pluginExecutors.getPluginFor(systemEntityType).orElse(null);
		if (executor == null) {
			throw new UnsupportedOperationException(MessageFormat
					.format("Synchronization executor for SystemEntityType {0} is not supported!", entityType));
		}
		@SuppressWarnings("unchecked")
		Class<SynchronizationEntityExecutor> targetClass = (Class<SynchronizationEntityExecutor>) AopUtils.getTargetClass(executor);
		SynchronizationEntityExecutor prototypeExecutor = AutowireHelper.createBean(targetClass);
		this.idmCacheManager.cacheValue(SYNC_EXECUTOR_CACHE_NAME, syncConfigId, prototypeExecutor);

		return prototypeExecutor;
	}

}
