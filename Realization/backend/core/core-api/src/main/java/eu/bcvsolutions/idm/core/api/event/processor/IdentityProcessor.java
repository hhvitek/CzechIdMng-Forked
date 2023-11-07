package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Identity processors should implement this interface.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdentityProcessor extends PasswordManageableProcessor<IdmIdentityDto> {

}
