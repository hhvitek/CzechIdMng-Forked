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
 * Delete processor for {@link AccPasswordDto}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Delete password for account.")
public class AccountPasswordDeleteProcessor extends CoreEventProcessor<AccPasswordDto> {

	private static final String PROCESSOR_NAME = "account-password-delete-processor";
	@Autowired
	private AccPasswordService service;

	public AccountPasswordDeleteProcessor() {
		super(PasswordEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AccPasswordDto> process(EntityEvent<AccPasswordDto> event) {
		AccPasswordDto passwordDto = event.getContent();
		//
		service.deleteInternal(passwordDto);
		//
		return new DefaultEventResult<>(event, this);
	}
}
