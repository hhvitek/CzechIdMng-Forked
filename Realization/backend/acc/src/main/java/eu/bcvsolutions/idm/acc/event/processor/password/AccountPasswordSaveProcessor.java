package eu.bcvsolutions.idm.acc.event.processor.password;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccPasswordDto;
import eu.bcvsolutions.idm.acc.service.api.AccPasswordService;
import eu.bcvsolutions.idm.core.model.event.processor.password.AbstractPasswordSaveProcessor;

/**
 * Processor for persist password for account.
 *
 * @author Jirka Koula
 *
 */
@Component
@Description("Persists password for account.")
public class AccountPasswordSaveProcessor extends AbstractPasswordSaveProcessor<AccPasswordDto, AccPasswordService> {

	private static final String PROCESSOR_NAME = "account-password-save-processor";

	public AccountPasswordSaveProcessor(AccPasswordService service) { super(service); }

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
