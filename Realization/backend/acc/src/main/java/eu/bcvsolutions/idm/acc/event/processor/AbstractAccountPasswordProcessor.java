package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.event.processor.AbstractPasswordProcessor;

/**
 * Implementation of account password change logic. Purpose of this abstraction is that password can be now changed from various
 * concrete implementations
 *
 * @author Jirka Koula
 */
public abstract class AbstractAccountPasswordProcessor extends AbstractPasswordProcessor<AccAccountDto, AccPasswordService> implements AccountProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAccountPasswordProcessor.class);
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "acc:password-change-dto";
	//

	protected AbstractAccountPasswordProcessor(AccPasswordService passwordService, EventType... types) {
		super(passwordService, LOG, PROPERTY_PASSWORD_CHANGE_DTO, types);
	}

}
