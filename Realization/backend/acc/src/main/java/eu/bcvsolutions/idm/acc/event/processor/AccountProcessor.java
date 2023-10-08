package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.event.processor.PasswordManageableProcessor;

/**
 * Acc account's processors should implement this interface.
 * 
 * @author svandav
 * @author Jirka Koula
 *
 */
public interface AccountProcessor extends PasswordManageableProcessor<AccAccountDto> {

}
