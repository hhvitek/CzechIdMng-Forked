package eu.bcvsolutions.idm.core.model.event.processor.password;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;

/**
 * Delete processor for {@link IdmPasswordDto}
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Delete password for identity.")
public class IdentityPasswordDeleteProcessor extends AbstractPasswordDeleteProcessor<IdmPasswordDto, IdmPasswordService> {

	private static final String PROCESSOR_NAME = "identity-password-delete-processor";

	@Autowired
	public IdentityPasswordDeleteProcessor(IdmPasswordService service) {
		super(service);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
