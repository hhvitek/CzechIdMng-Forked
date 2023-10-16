package eu.bcvsolutions.idm.core.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.PasswordManageable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;

/**
 * Service supports password management.
 *
 * @author Jirka Koula
 */
public interface PasswordManageableService<E extends AbstractDto & PasswordManageable> {
	/**
	 * Changes given entity password by the event processing. New password property has to be set in event properties.
	 *
	 * @param passwordChangeEvent password change event
	 * @return change on accounts
	 */
	List<OperationResult> passwordChange(CoreEvent<E> passwordChangeEvent);

	/**
	 * Changes given entity password
	 *
	 * @param entity            entity for which password should be changed
	 * @param passwordChangeDto change request dto
	 * @return change on accounts
	 */
	List<OperationResult> passwordChange(E entity, PasswordChangeDto passwordChangeDto);

	/**
	 * Method create new event for pre validate password
	 *
	 * @param passwordChange change password request dto
	 */

	void validatePassword(PasswordChangeDto passwordChange);
}
