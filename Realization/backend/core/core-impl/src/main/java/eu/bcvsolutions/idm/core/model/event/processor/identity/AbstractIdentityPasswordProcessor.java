package eu.bcvsolutions.idm.core.model.event.processor.identity;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractPasswordProcessor;

/**
 * Implementation of password change logic. Purpose of this abstraction is that password can be now changed from various
 * concrete implementations
 *
 * @author Jirka Koula
 */
public abstract class AbstractIdentityPasswordProcessor extends AbstractPasswordProcessor<IdmIdentityDto, IdmPasswordService> implements IdentityProcessor {
	protected AbstractIdentityPasswordProcessor(IdmPasswordService passwordService, EventType... types) {
		super(passwordService, types);
	}
}
