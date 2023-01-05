package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * API for synchronization executor
 * 
 * @author svandav
 *
 */
public interface SynchronizationEntityExecutor extends Plugin<SystemEntityTypeRegistrable> {

	/**
	 * Execute synchronization by given config
	 * 
	 * @param synchronizationConfigId
	 * @return
	 */
	AbstractSysSyncConfigDto process(UUID synchronizationConfigId);

	/**
	 * Do synchronization for one item
	 * @param itemContext
	 * @return
	 */
	boolean doItemSynchronization(SynchronizationContext itemContext);

	void setLongRunningTaskExecutor(AbstractSchedulableTaskExecutor<Boolean> longRunningTaskExecutor);

	/**
	 * Method for resolve missing account situation for one item.
	 */
	public void resolveMissingAccountSituation(ReconciliationMissingAccountActionType action, SynchronizationContext context);

	/**
	 * Method for resolve unlinked situation for one item.
	 */
	void resolveUnlinkedSituation(SynchronizationUnlinkedActionType action, SynchronizationContext context);

	/**
	 * Method for resolve missing entity situation for one item.
	 */
	void resolveMissingEntitySituation(SynchronizationMissingEntityActionType actionType,  SynchronizationContext context);

	/**
	 * Method for resolve linked situation for one item.
	 */
	void resolveLinkedSituation(SynchronizationLinkedActionType action, SynchronizationContext context);

	/**
	 * Find target entity by accountId
	 * @param accountId
	 * @return
	 */
	UUID getEntityByAccount(UUID accountId);

	/**
	 * Finds target entity and its DTO. First tries to find a DTO in embedded data
	 * (in entity-account relation).
	 * 
	 * @param entityId
	 * @param account
	 * @return
	 */
	AbstractDto getDtoByAccount(UUID entityId, AccAccountDto account);
	
	/**
	 * entity type for this synchronization executor
	 * 
	 * @return
	 */
	String getSystemEntityType();

}