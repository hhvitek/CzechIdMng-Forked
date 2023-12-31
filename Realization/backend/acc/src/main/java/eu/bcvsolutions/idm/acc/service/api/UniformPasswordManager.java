package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import java.util.UUID;

/**
 * Manager for a uniform password of identity.
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
public interface UniformPasswordManager {

	String UNIFORM_PASSWORD_KEY = "identityUniformPassword";
	String PASSWORD_USED = "passwordUsed";
	String SUCCESS_SYSTEM_NAMES = "successSystemNames";
	String SUCCESS_SYSTEM_ENTITIES = "successSystemEntities";
	String IDM_NAME = "CzechIdM";

	/**
	 * Create a uniform password entity state for given entity.
	 * Only one state can exist for one entity in same transaction.
	 * If will already exist, then will be returned.
	 */
	IdmEntityStateDto createEntityState(AbstractDto entityDto);

	/**
	 * Called on the end of uniform password process in the transaction.
	 * Send the notification with new password to all identities where a new account was created.
	 */
	void endUniformPasswordProcess(UUID transactionId);

	/**
	 * Generate uniform password or reused it if already exists for given entity and transaction.
	 * Password is generated only once for one entity state.
	 *
	 * Password is persisted in confidential storage and deleted after the entity state is deleted.
	 * Entity state for uniform password is marked as used after generating (PASSWORD_USED).
	 */
	GuardedString generateUniformPassword(UUID entityIdentifier, Class<? extends AbstractDto> entityType, UUID transactionId);

	/**
	 * Is given system used in uniform password agenda?
	 */
	boolean isSystemInUniformPasswordAgenda(UUID systemId);

	/**
	 * Try to find a uniform password entity state for given entity and transaction.
	 */
	IdmEntityStateDto getEntityState(UUID entityIdentifier, Class<? extends AbstractDto> entityType, UUID transactionId);

	/**
	 * Find uniform password DTO by system ID.
	 */
	AccUniformPasswordDto getUniformPasswordBySystem(UUID systemId);

	/**
	 * Returns true if should be uniform password set to IdM too. A password should be set
	 * if exists some record in uniform password agenda with attribute "Change in IdM".
	 */
	boolean shouldBePasswordSetToIdM();

	/**
	 * Add system name to the uniform password entity state. Beware, only instance is changed, entity state will be not saved.
	 */
	void addSystemNameToEntityState(IdmEntityStateDto uniformPasswordState, String systemName);
}
