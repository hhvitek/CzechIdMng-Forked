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
 * Processor for persist password for password manageable entity.
 *
 * @author Jirka Koula
 *
 */
@Description("Persists password for password manageable entity.")
public class AbstractPasswordSaveProcessor <D extends AbstractPasswordDto, S extends AbstractPasswordService<D, ?, ?>> extends CoreEventProcessor<D> {

	private final S service;

	public AbstractPasswordSaveProcessor(S service) {
		super(PasswordEventType.CREATE, PasswordEventType.UPDATE);
		this.service = service;
	}

	@Override
	public EventResult<D> process(EntityEvent<D> event) {
		D passwordDto = event.getContent();
		//
		passwordDto = service.saveInternal(passwordDto);
		//
		event.setContent(passwordDto);
		//
		return new DefaultEventResult<>(event, this);
	}
}
