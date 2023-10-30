package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;

/**
 * Service for validate password by password policy, also generate password.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Jirka Koula
 *
 */
public interface IdmPasswordPolicyService extends AbstractPasswordPolicyService<IdmPasswordValidationDto> {
}
