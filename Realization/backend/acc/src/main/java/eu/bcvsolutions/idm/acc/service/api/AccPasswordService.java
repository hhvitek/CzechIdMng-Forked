package eu.bcvsolutions.idm.acc.service.api;

import java.io.Serializable;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilter;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for working with technical account password.
 *
 * @author Jirka Koula
 *
 */

public interface AccPasswordService extends
		EventableDtoService<AccPasswordDto, AccPasswordFilter>,
		ScriptEnabled,
		AuthorizableService<AccPasswordDto> {

	/**
	 * Save password to account. This method not validate password.
	 * 
	 * @param account
	 * @param passwordDto
	 * @return
	 */
	AccPasswordDto save(AccAccountDto account, PasswordChangeDto passwordDto);
	
	/**
	 * Delete password by given account
	 * 
	 * @param account
	 */
	void delete(AccAccountDto account);
	
	/**
	 * Return password for given account
	 * 
	 * @param accountId
	 * @return
	 */
	AccPasswordDto findOneByAccount(UUID accountId);

	/**
	 * Return password for given identifier (id), if password doesn't exist
	 * create new empty password.
	 *
	 * @param codeable identifier
	 * @return
	 */
	AccPasswordDto findOrCreateByAccount(Serializable codeable);

	/**
	 * Check password matches a passwordToCheck
	 * 
	 * @param passwordToCheck
	 * @param password
	 * @return true if matches
	 */
	boolean checkPassword(GuardedString passwordToCheck, AccPasswordDto password);
	
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
