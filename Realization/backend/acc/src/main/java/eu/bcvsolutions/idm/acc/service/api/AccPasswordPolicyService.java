package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordPolicyService;

/**
 * Service for valdiate password by password policy, also generate password.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface AccPasswordPolicyService extends AbstractPasswordPolicyService<AccPasswordValidationDto> {
}
