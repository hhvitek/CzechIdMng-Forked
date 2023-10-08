package eu.bcvsolutions.idm.acc.service.api;

import java.io.Serializable;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccPasswordFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordService;

/**
 * Service for working with technical account password.
 *
 * @author Jirka Koula
 *
 */

public interface AccPasswordService extends AbstractPasswordService<AccPasswordDto, AccAccountDto, AccPasswordFilter> {

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

}
