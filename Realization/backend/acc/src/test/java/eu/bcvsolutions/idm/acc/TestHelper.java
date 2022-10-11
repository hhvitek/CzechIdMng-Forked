package eu.bcvsolutions.idm.acc;

import java.time.LocalDate;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Reuses core TestHelper and adds acc spec. methods
 * 
 * @author Radek Tomiška
 *
 */
public interface TestHelper extends eu.bcvsolutions.idm.test.api.TestHelper {

	String ATTRIBUTE_MAPPING_NAME = "__NAME__";
	String ATTRIBUTE_MAPPING_ENABLE = "__ENABLE__";
	String ATTRIBUTE_MAPPING_PASSWORD = IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME;
	String ATTRIBUTE_MAPPING_FIRSTNAME = "FIRSTNAME";
	String ATTRIBUTE_MAPPING_LASTNAME = "LASTNAME";
	String ATTRIBUTE_MAPPING_EMAIL = "EMAIL";
	String ATTRIBUTE_MAPPING_DESCRIPTION = "DESCRIP"; // Attribute isn't mapped by default

	/**
	 * Prepares conntector and system for fiven table name. Test database is used.
	 * Generated system name will be used.
	 * 
	 * @param tableName
	 *            see {@link AbstractTestResource#TABLE_NAME}
	 * @return
	 */
	SysSystemDto createSystem(String tableName);

	/**
	 * Prepares conntector and system for fiven table name. Test database is used.
	 * 
	 * @param tableName
	 *            see {@link AbstractTestResource#TABLE_NAME}
	 * @param systemName
	 * @return
	 */
	SysSystemDto createSystem(String tableName, String systemName);

	/**
	 * Prepares conntector and system for fiven table name. Test database is used.
	 * @param tableName
	 * @param systemName
	 * @param statusColumnName
	 * @param keyColumnName
	 * @return
	 */
	SysSystemDto createSystem(String tableName, String systemName, String statusColumnName, String keyColumnName);

	/**
	 * Creates system for {@link AbstractTestResource} with schema generated by given table.
	 * Test database is used. Generated system name will be used.
	 * 
	 * @see AbstractTestResource#TABLE_NAME
	 * @param withMapping
	 *            default mapping will be included
	 * @return
	 */
	SysSystemDto createTestResourceSystem(boolean withMapping);

	/**
	 * Creates system for {@link AbstractTestResource} with schema generated by given table.
	 * Test database is used.
	 * 
	 * see AbstractTestResource#TABLE_NAME
	 * 
	 * @param withMapping
	 *            default mapping will be included
	 * @param systemName
	 * @return
	 */
	SysSystemDto createTestResourceSystem(boolean withMapping, String systemName);
	
	/**
	 * Creates default provisioning mapping for the given system and entity type
	 * 
	 * @param system
	 * @return
	 */
	SysSystemMappingDto createMapping(SysSystemDto system, String entityType);

	/**
	 * Creates default provisioning mapping for the given system
	 * 
	 * @param system
	 * @return
	 */
	SysSystemMappingDto createMapping(SysSystemDto system);

	/**
	 * Creates default provisioning mapping for the given system, entity type, account type
	 * @param system
	 * @param accountType
	 * @return
	 */
	SysSystemMappingDto createMapping(SysSystemDto system, String entityType, AccountType accountType);

	/**
	 * reates default provisioning mapping for the given system, account type
	 * @param system
	 * @param accountType
	 * @return
	 */
	SysSystemMappingDto createMapping(SysSystemDto system, AccountType accountType);

	/**
	 * Returns default mapping - provisioning, identity
	 * 
	 * @see #createSystem(String, boolean)
	 * @param system
	 * @return
	 */
	SysSystemMappingDto getDefaultMapping(SysSystemDto system);

	/**
	 * Returns default mapping - provisioning, identity
	 * 
	 * @see #createSystem(String, boolean)
	 * @param systemId
	 * @return
	 */
	SysSystemMappingDto getDefaultMapping(UUID systemId);

	/**
	 * Assing system to given role with default mapping (provisioning, identity)
	 * 
	 * @see #getDefaultMapping(SysSystem)
	 * @param role
	 * @param system
	 * @return
	 */
	SysRoleSystemDto createRoleSystem(IdmRoleDto role, SysSystemDto system);

	/**
	 * Assing system to given role with mapping (provisioning, identity)
	 * @param role
	 * @param system
	 * @param accountType
	 * @return
	 */
	SysRoleSystemDto createRoleSystem(IdmRoleDto role, SysSystemDto system, AccountType accountType);

	/**
	 * Find account on target system
	 * 
	 * @param uid
	 * @return
	 */
	TestResource findResource(String uid);
	
	/**
	 * Delete all data from the target system
	 */
	void deleteAllResourceData();
	
	/**
	 * Saves resource on target system
	 * 
	 * TODO: support merge (persist only now) 
	 * 
	 * @param uid
	 * @return
	 */
	TestResource saveResource(TestResource testResource);

	/**
	 * Creates system entity (IDENTITY) with random name on given system
	 * 
	 * @param system
	 * @return
	 */
	SysSystemEntityDto createSystemEntity(SysSystemDto system);

	/**
	 * Create {@link AccAccountDto} and {@link AccIdentityAccountDto} for given
	 * system and identity.
	 * 
	 * @param system
	 * @param identity
	 * @return
	 */
	AccIdentityAccountDto createIdentityAccount(SysSystemDto system, IdmIdentityDto identity);

	/**
	 * Create {@link AccAccountDto} and {@link AccIdentityAccountDto} for given
	 * system and identity.
	 *
	 * @param systemEntityType
	 * @param objectClass
	 * @return
	 */
	SysSystemMappingDto createMappingSystem(String systemEntityType, SysSchemaObjectClassDto objectClass);

	/**
	 * Start synchronization by given sync config
	 * 
	 * @param syncConfigCustom
	 */
	void startSynchronization(AbstractSysSyncConfigDto syncConfigCustom);
	
	/**
	 * Schema is generated in lower case for postgresql.
	 * 
	 * TODO: can be solved by hibernate naming strategy, but this is just for tests ...
	 * 
	 * @param columnName
	 * @return
	 */
	String getSchemaColumnName(String columnName);

	/**
	 * Check result of sync
	 * 
	 * @param config
	 * @param actionType
	 * @param count
	 * @param resultType
	 * @return
	 */
	SysSyncLogDto checkSyncLog(AbstractSysSyncConfigDto config, SynchronizationActionType actionType, int count,
			OperationResultType resultType);

	/**
	 * Manual delete. Because previous tests didn't make a delete well.
	 */
	void cleaner();

	/**
	 * Create system owner by identity
	 * @param system
	 * @param owner
	 * @return
	 */
	SysSystemOwnerDto createSystemOwner(SysSystemDto system, IdmIdentityDto owner);

	/**
	 * Create system owner by role
	 * @param system
	 * @param owner
	 * @return
	 */
	SysSystemOwnerRoleDto createSystemOwnerRole(SysSystemDto system, IdmRoleDto owner);

	/**
	 * Create direct (without role request) account role assignment.
	 *
	 * @param accountId
	 * @param roleId
	 * @return
	 */
    AccAccountRoleAssignmentDto createAccountRoleAssignment(UUID accountId, UUID roleId);

	void deleteSystem(UUID systemId);

    AccAccountRoleAssignmentDto createAccountRoleAssignment(AccAccountDto accAccountDto, IdmRoleDto roleA);

    /**
     * Create direct (without role request) account role assignment with validity.
     *
     * @param accountId
     * @param roleId
     * @param from
     * @param to
     * @return
     */
    AccAccountRoleAssignmentDto createAccountRoleAssignment(UUID accountId, UUID roleId, LocalDate from, LocalDate to);

    /**
     * Directly (without role request) remove assigned account role.
     *
     * @param roleAssignment
     */
    void removeAccountRoleAssignment(AccAccountRoleAssignmentDto roleAssignment);

    /**
     * Creates an account. It will create a corresponding system with mapping and roles
     * and an identity with a contract.
     *
     * @return
     */
    AccAccountDto createAccount();

    /**
     * Assignes a role to an account via a role request.
     *
     * @param accAccountDto
     * @param waitTillRequestExecuted - will wait for 150 ms up to 5 times till the role request is executed
     * @param roleIds
     */
    void assignRoleToAccountViaRequest(AccAccountDto accAccountDto, boolean waitTillRequestExecuted, UUID... roleIds);

    /**
     * Assignes a role to an account via a role request.
     *
     * @param accAccountDto
     * @param validFrom
     * @param validTill
     * @param waitTillRequestExecuted - will wait for 150 ms up to 5 times till the role request is executed
     * @param roleIds
     */
    void assignRoleToAccountViaRequest(AccAccountDto accAccountDto, LocalDate validFrom, LocalDate validTill, boolean waitTillRequestExecuted, UUID... roleIds);

    /**
     * Updates an assigned role via a role request.
     *
     * @param accAccountDto
     * @param validFrom
     * @param validTill
     * @param waitTillRequestExecuted - will wait for 150 ms up to 5 times till the role request is executed
     * @param roleAssignment
     */
    void updateAssignedAccountRoleViaRequest(AccAccountDto accAccountDto, LocalDate validFrom, LocalDate validTill, boolean waitTillRequestExecuted, AccAccountRoleAssignmentDto roleAssignment);

    /**
     * Updates an assigned role via a role request.
     *
     * @param accAccountDto
     * @param validFrom
     * @param validTill
     * @param waitTillRequestExecuted - will wait for 150 ms up to 5 times till the role request is executed
     * @param roleId
     */
    void updateAssignedAccountRoleViaRequest(AccAccountDto accAccountDto, LocalDate validFrom, LocalDate validTill, boolean waitTillRequestExecuted, UUID roleId);

    /**
     * Removes an assigned account role.
     *
     * @param accAccountDto
     * @param waitTillRequestExecuted - will wait for 150 ms up to 5 times till the role request is executed
     * @param roleIds
     */
    void removeRoleFromAccountViaRequest(AccAccountDto accAccountDto, boolean waitTillRequestExecuted, UUID... roleIds);

    /**
     * Returns the id of the identity which is the owner of the account.
     *
     * @param accountId
     * @return identityId
     */
    UUID getAccountOwner(UUID accountId);

    /**
     * Creates a concept role request for an account.
     *
     * @param requestId
     * @param roleId
     * @param accountId
     * @param roleAssignmentId
     * @param operationType
     * @param validFrom
     * @param validTill
     * @return
     */
    AccAccountConceptRoleRequestDto createAccountConceptRoleRequest(UUID requestId, UUID roleId, UUID accountId,
			UUID roleAssignmentId, ConceptRoleRequestOperation operationType, LocalDate validFrom, LocalDate validTill);

    /**
     * Creates a concept role request for an account.
     *
     * @param requestId
     * @param roleId
     * @param accountId
     * @param roleAssignmentId
     * @param operationType
     * @return
     */
    AccAccountConceptRoleRequestDto createAccountConceptRoleRequest(UUID requestId, UUID roleId, UUID accountId,
			UUID roleAssignmentId, ConceptRoleRequestOperation operationType);

    /**
     * Creates a concept role request for an account with ADD type.
     *
     * @param requestId
     * @param roleId
     * @param accountId
     * @return
     */
    AccAccountConceptRoleRequestDto createAccountConceptRoleRequest(UUID requestId, UUID roleId, UUID accountId);

    /**
     * Creates a role request for identity.
     *
     * @param identityId
     * @param executeImmediately
     * @return
     */
    IdmRoleRequestDto createRoleRequest(UUID identityId, boolean executeImmediately);

    /**
     * Creates a role request for identity which will be executed immediately.
     *
     * @param identityId
     * @return
     */
    IdmRoleRequestDto createRoleRequest(UUID identityId);
}
