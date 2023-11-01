package eu.bcvsolutions.idm.core.model.event.processor.password;

import org.springframework.context.annotation.Description;

import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.AbstractPasswordService;
import eu.bcvsolutions.idm.core.model.event.PasswordEvent.PasswordEventType;

/**
 * Delete processor for password manageable entity password
 * @author Jirka Koula
 *
 */
@Description("Delete password for password manageable entity.")
public abstract class AbstractPasswordDeleteProcessor<D extends AbstractPasswordDto, S extends AbstractPasswordService<D, ?, ?>> extends CoreEventProcessor<D> {

	private final S service;

	protected AbstractPasswordDeleteProcessor(S service) {
		super(PasswordEventType.DELETE);
		this.service = service;
	}

	@Override
	public EventResult<D> process(EntityEvent<D> event) {
		D passwordDto = event.getContent();
		//
		service.deleteInternal(passwordDto);
		//
		return new DefaultEventResult<>(event, this);
	}
}
