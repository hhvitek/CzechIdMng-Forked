package eu.bcvsolutions.idm.acc.event.processor.password;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.PasswordEvent.PasswordEventType;

/**
 * Check account password never expires and remove valid till.
 *
 * @author Jirka Koula
 *
 */
@Component(AccountPasswordNeverExpiresProcessor.PROCESSOR_NAME)
@Description("Check password never expires.")
public class AccountPasswordNeverExpiresProcessor extends CoreEventProcessor<AccPasswordDto> {

	public static final String PROCESSOR_NAME = "account-password-never-expires-processor";

	public AccountPasswordNeverExpiresProcessor() {
		super(PasswordEventType.CREATE, PasswordEventType.UPDATE);
	}

	@Override
	public EventResult<AccPasswordDto> process(EntityEvent<AccPasswordDto> event) {
		AccPasswordDto passwordDto = event.getContent();
		//
		// If this password never expires, set valid till to null. Even if someone set valid till value.
		if (passwordDto.isPasswordNeverExpires()) {
			passwordDto.setValidTill(null);
		}
		//
		event.setContent(passwordDto);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return -10;
	}
}
