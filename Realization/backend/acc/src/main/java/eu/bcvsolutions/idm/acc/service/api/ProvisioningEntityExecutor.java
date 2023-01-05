package eu.bcvsolutions.idm.acc.service.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * API for entity provisioning executors
 * 
 * @author svandav
 *
 */
public interface ProvisioningEntityExecutor<DTO extends AbstractDto> extends Plugin<SystemEntityTypeRegistrable> {

	/**
	 * Do provisioning for given dto on all connected systems
	 * 
	 * @param dto
	 */
	void doProvisioning(DTO dto);

	/**
	 * Do provisioning for given account on connected system
	 * 
	 * @param account
	 */
	void doProvisioning(AccAccountDto account);

	/**
	 * Do provisioning for given account and dto. Emits ProvisioningEventType.START
	 * event.
	 * 
	 * @param account
	 * @param dto
	 * @param system
	 * @return
	 */
	void doProvisioning(AccAccountDto account, DTO dto);
	
	/**
	 * Do provisioning for given account, dto and additional parameters for the event. Emits ProvisioningEventType.START
	 * event.
	 * 
	 * @param account
	 * @param dto
	 * @param properties
	 * @return
	 */
	EventContext<AccAccountDto> doProvisioning(AccAccountDto account, DTO dto, Map<String,Serializable> properties);

	/**
	 * Do delete provisioning for given account on connected system
	 * 
	 * @param account
	 * @param dtoId
	 *            - Id of dto connected to the account. Can be null, but
	 *            provisioning archive will not have correct information.
	 */
	void doDeleteProvisioning(AccAccountDto account, UUID dtoId);

	/**
	 * 
	 * Change password for selected identity accounts.
	 * 
	 * @param identity
	 * @param passwordChange
	 * @result result for each provisioned account
	 */
	List<OperationResult> changePassword(DTO dto, PasswordChangeDto passwordChange);

	/**
	 * Do provisioning only for single attribute. For example, it is needed to
	 * change password
	 * 
	 * @param systemEntity
	 * @param mappedAttribute
	 * @param value
	 * @param system
	 * @param operationType
	 * @param dto
	 * @result result
	 */
	void doProvisioningForAttribute(SysSystemEntityDto systemEntity, AttributeMapping mappedAttribute, Object value,
			ProvisioningOperationType operationType, DTO dto);

	/**
	 * Do authenticate check for given username and password on target resource
	 * 
	 * @param username
	 * @param password
	 * @param system
	 * @param entityType
	 * @return
	 */
	IcUidAttribute authenticate(String username, GuardedString password, SysSystemDto system,
			String entityType);

	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * 
	 * @param account
	 * @param dto
	 * @param system
	 * @param entityType
	 * @return
	 */
	List<AttributeMapping> resolveMappedAttributes(AccAccountDto account, DTO dto, SysSystemDto system,
			String entityType);

	/**
	 * Create final list of attributes for provisioning.
	 * 
	 * @param identityAccount
	 * @param defaultAttributes
	 * @param overloadingAttributes
	 * @return
	 */
	List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttributeDto> overloadingAttributes, String entityType);

	/**
	 * Do provisioning for given account and identity. For internal purpose without
	 * emit event.
	 * 
	 * @param account
	 * @param identity
	 * @param system
	 * @return
	 */
	void doInternalProvisioning(AccAccountDto account, DTO dto);
	
	SysProvisioningOperationDto doInternalProvisioning(AccAccountDto account, DTO dto, boolean isDryRun);

	/**
	 * Ensure the account management for given entity. First check if can be
	 * AccAccount and relation for this entity created. If yes then will be accounts
	 * and relations on the entity created on systems (SysSystem). Ensure the delete
	 * of AccAccount too. Provisioning on the target system is not basically
	 * executed.
	 * 
	 * @param dto
	 * @return true if is provisioning required
	 */
	boolean accountManagement(DTO dto);

}