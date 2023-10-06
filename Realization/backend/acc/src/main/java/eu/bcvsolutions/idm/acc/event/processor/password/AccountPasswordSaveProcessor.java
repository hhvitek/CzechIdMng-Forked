package eu.bcvsolutions.idm.acc.event.processor.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.PasswordEvent.PasswordEventType;

/**
 * Processor for persist password for account.
 *
 * @author Jirka Koula
 *
 */
@Component
@Description("Persists password for account.")
public class AccountPasswordSaveProcessor extends CoreEventProcessor<AccPasswordDto> {

	private static final String PROCESSOR_NAME = "account-password-save-processor";
	@Autowired
	private AccPasswordService service;

	public AccountPasswordSaveProcessor() {
		super(PasswordEventType.CREATE, PasswordEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccPasswordDto> process(EntityEvent<AccPasswordDto> event) {
		AccPasswordDto passwordDto = event.getContent();
		//
		passwordDto = service.saveInternal(passwordDto);
		//
		event.setContent(passwordDto);
		//
		return new DefaultEventResult<>(event, this);
	}
}
