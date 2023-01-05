package eu.bcvsolutions.idm.acc.domain;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * Provisioning operation "content" and state
 * 
 * @author Radek Tomiška
 *
 */
public interface ProvisioningOperation extends Auditable {

	/**
	 * Provisioning operation type
	 * 
	 * @return
	 */
	ProvisioningEventType getOperationType();

	/**
	 * Target system id
	 * 
	 * @return
	 */
	UUID getSystem();

	/**
	 * Account
	 *
	 * @return
	 */
	UUID getAccount();

	/**
	 * IdM entity type
	 * 
	 * @return
	 */
	String getEntityType();

	/**
	 * IdM entity type identifier
	 * 
	 * @return
	 */
	UUID getEntityIdentifier();


	/**
	 * Result state
	 * 
	 * @return
	 */
	OperationState getResultState();

	/**
	 * Whole result with code and exception
	 * 
	 * @return
	 */
	OperationResult getResult();

	/**
	 * Provisioning "content"
	 * 
	 * @return
	 */
	ProvisioningContext getProvisioningContext();

}