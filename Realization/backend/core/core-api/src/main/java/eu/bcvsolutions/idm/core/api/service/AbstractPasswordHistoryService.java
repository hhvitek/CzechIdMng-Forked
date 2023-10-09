package eu.bcvsolutions.idm.core.api.service;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AbstractPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Service template for check password history
 *
 * @author Jirka Koula
 *
 */
public interface AbstractPasswordHistoryService<P extends AbstractPasswordHistoryDto, F extends AbstractPasswordHistoryFilter> extends ReadWriteDtoService<P, F> {

	/**
	 * Check password equals with password trough history.
	 * Beware on some situation doesn't exists password history record.
	 *
	 * @param entityId - id of entity, for this entity will be done check in password history
	 * @param countOfIteration - count of back iteration.
	 * @param newPassword - new password
	 * @return true if founded some equals password in history, otherwise return false
	 */
	boolean checkHistory(UUID entityId, int countOfIteration, GuardedString newPassword);

	/**
	 * Remove all password history record by entity.
	 *
	 * @param entityId - id of entity, for this entity all password history will be deleted
	 */
	void deleteAllByEntity(UUID entityId);
}
