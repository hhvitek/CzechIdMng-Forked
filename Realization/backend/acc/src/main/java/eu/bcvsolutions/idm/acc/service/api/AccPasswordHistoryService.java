package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.AccPasswordHistoryDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordHistoryDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordHistoryFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Service for check account password history
 *
 * @author Jirka Koula
 *
 */
public interface AccPasswordHistoryService extends ReadWriteDtoService<AccPasswordHistoryDto, AccPasswordHistoryFilter> {

	/**
	 * Check password equals with password trough history.
	 * Beware on some situation doesn't exists password history record.
	 *
	 * @param accountId - id of account, for this account will be done check in password history
	 * @param countOfIteration - count of back iteration.
	 * @param newPassword - new password
	 * @return true if founded some equals password in history, otherwise return false
	 */
	boolean checkHistory(UUID accountId, int countOfIteration, GuardedString newPassword);
	
	/**
	 * Remove all password history record by identity.
	 *
	 * @param accountId
	 */
	void deleteAllByAccount(UUID accountId);
}
