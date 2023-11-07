package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordFilter;

/**
 * Service for working with password.
 * Now is password connect only to entity IdmIdentity.
 * 
 * @author Ondrej Kopr
 * @author Jirka Koula
 *
 */

public interface IdmPasswordService extends AbstractPasswordService<IdmPasswordDto, IdmIdentityDto, IdmPasswordFilter> {

	/**
	 * Return password for given identity
	 * 
	 * @param identityId
	 * @return
	 */
	IdmPasswordDto findOneByIdentity(UUID identityId);

	/**
	 * Return password for given username
	 *
	 * @param username
	 * @return
	 */
	IdmPasswordDto findOneByIdentity(String username);
	
	/**
	 * Return password for given identifier (id/ username), if password doesn't exist
	 * create new empty password.
	 *
	 * @param codeable identifier
	 * @return
	 */
	IdmPasswordDto findOrCreateByIdentity(Serializable codeable);

	/**
	 * If this username exists and password is incorrect -> increase count of unsuccessful attempts
	 *
	 * @param username
	 */
	void increaseUnsuccessfulAttempts(String username);

	/**
	 * If this username exists and the password is correct -> save timestamp of login
	 * 
	 * @param username
	 */
	void setLastSuccessfulLogin(String username);
	
	/**
	 * Increase count of unsuccessful attempts for given password dto
	 *
	 * @param passwordDto
	 * @return updated password dto
	 */
	IdmPasswordDto increaseUnsuccessfulAttempts(IdmPasswordDto passwordDto);

	/**
	 * Save timestamp of login for given password dto and set block time to null
	 * 
	 * @param passwordDto
	 * @return updated password dto
	 */
	IdmPasswordDto setLastSuccessfulLogin(IdmPasswordDto passwordDto);
}
