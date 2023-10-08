package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.domain.PasswordManageable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.AbstractPasswordFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for working with entity6 password.
 *
 * @author Jirka Koula
 *
 */

public interface AbstractPasswordService<P extends AbstractPasswordDto, E extends AbstractDto & PasswordManageable, F extends AbstractPasswordFilter> extends
		EventableDtoService<P, F>, ScriptEnabled, AuthorizableService<P> {

	/**
	 * Save password to entity. This method not validate password.
	 *
	 * @param entity
	 * @param passwordDto
	 * @return
	 */
	P save(E entity, PasswordChangeDto passwordDto);

	/**
	 * Delete password by given account
	 *
	 * @param entity
	 */
	void delete(E entity);

	/**
	 * Check password matches a passwordToCheck
	 *
	 * @param passwordToCheck
	 * @param password
	 * @return true if matches
	 */
	boolean checkPassword(GuardedString passwordToCheck, P password);

	/**
	 * Method generate password and return hash
	 *
	 * @param password
	 * @return
	 */
	String generateHash(GuardedString password, String salt);

	/**
	 * Get salt
	 *
	 * @return
	 */
	String getSalt();

}
