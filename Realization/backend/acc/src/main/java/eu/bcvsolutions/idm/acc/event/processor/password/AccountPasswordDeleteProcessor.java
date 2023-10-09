package eu.bcvsolutions.idm.acc.event.processor.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.model.event.processor.password.AbstractPasswordDeleteProcessor;

/**
 * Delete processor for {@link AccPasswordDto}
 * @author Jirka Koula
 *
 */
@Component
@Description("Delete password for account.")
public class AccountPasswordDeleteProcessor extends AbstractPasswordDeleteProcessor<AccPasswordDto, AccPasswordService> {

	private static final String PROCESSOR_NAME = "account-password-delete-processor";

	@Autowired
	public AccountPasswordDeleteProcessor(AccPasswordService service) {
		super(service);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

}
