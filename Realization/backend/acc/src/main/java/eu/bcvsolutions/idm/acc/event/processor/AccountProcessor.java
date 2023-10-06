package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Acc account's processors should implement this interface.
 * 
 * @author svandav
 * @author Jirka Koula
 *
 */
public interface AccountProcessor extends EntityEventProcessor<AccAccountDto> {

	/**
	 * Skip password validation.
	 */
	String SKIP_PASSWORD_VALIDATION = "skipPasswordValidation";

}
