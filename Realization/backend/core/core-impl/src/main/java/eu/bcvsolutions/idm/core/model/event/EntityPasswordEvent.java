package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.PasswordManageable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for password manageable entity
 * 
 * @author Jirka Koula
 *
 */
public class EntityPasswordEvent<DTO extends AbstractDto & PasswordManageable> extends CoreEvent<DTO> {

	private static final long serialVersionUID = -5451829322067201263L;

	/**
	 * Supported password manageable entity events
	 *
	 */
	public enum EntityPasswordEventType implements EventType {
		PASSWORD, // password change
		PASSWORD_EXPIRED
	}

	public EntityPasswordEvent(EntityPasswordEventType operation, DTO content) {
		super(operation, content);
	}

	public EntityPasswordEvent(EntityPasswordEventType operation, DTO content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}