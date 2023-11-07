package eu.bcvsolutions.idm.core.model.event.processor.password;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;

/**
 * Processor for persist password for identity.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Jirka Koula
 *
 */
@Component
@Description("Persists password for identity.")
public class IdentityPasswordSaveProcessor extends AbstractPasswordSaveProcessor<IdmPasswordDto, IdmPasswordService> {

	private static final String PROCESSOR_NAME = "identity-password-save-processor";

	public IdentityPasswordSaveProcessor(IdmPasswordService service) {
		super(service);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
