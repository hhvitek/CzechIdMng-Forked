package eu.bcvsolutions.idm.core.api.event.processor;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Entity processor for password manageable entity processors should implement this interface.
 * 
 * @author Jirka Koula
 *
 */
public interface PasswordManageableProcessor<T extends Serializable> extends EntityEventProcessor<T> {

	/**
	 * Skip password validation.
	 * 
	 * @since 10.5.0
	 */
	String SKIP_PASSWORD_VALIDATION = "skipPasswordValidation";
}
