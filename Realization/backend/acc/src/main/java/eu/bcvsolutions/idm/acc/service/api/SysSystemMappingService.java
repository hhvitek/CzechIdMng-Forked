package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.MappingContext;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;

/**
 * System entity handling service
 * 
 * @author svandav
 * @author Ondrej Husnik
 *
 */
public interface SysSystemMappingService extends EventableDtoService<SysSystemMappingDto, SysSystemMappingFilter>, CloneableService<SysSystemMappingDto> {

	String ENABLE_AUTOMATIC_CREATION_OF_MAPPING = "acc:enable_automatic_creation_of_mapping";

	/**
	 * Find system mapping by given attributes
	 * 
	 * @param system
	 * @param operation
	 * @param entityType
	 * @return
	 */
	List<SysSystemMappingDto> findBySystem(SysSystemDto system, SystemOperationType operation, String entityType);
	
	/**
	 * Find system mapping by given attributes
	 * 
	 * @param systemId
	 * @param operation
	 * @param entityType
	 * @return
	 */
	List<SysSystemMappingDto> findBySystemId(UUID systemId, SystemOperationType operation, String entityType);
	
	/**
	 * Find system mapping by given attributes
	 * 
	 * @param objectClass
	 * @param operation
	 * @param entityType
	 * @return
	 */
	List<SysSystemMappingDto> findByObjectClass(SysSchemaObjectClassDto objectClass, SystemOperationType operation, String entityType);

	/**
	 * Is enabled protection of account against delete.
	 * 
	 * @param account
	 * @return
	 */
	boolean isEnabledProtection(AccAccountDto account);

	/**
	 * Interval of protection against account delete.
	 * 
	 * @param account
	 * @return
	 */
	Integer getProtectionInterval(AccAccountDto account);

	
	/**
	 * Validate, if system mapping has attribute as identifier.
	 * 
	 * @param systemMapingId
	 */
	void validate (UUID systemMapingId);

	/**
	 * Call script "Can be account created?" on the mapping.
	 * 
	 * @param uid
	 * @param dto
	 * @param script
	 * @param system
	 * @return true or false
	 */
	boolean canBeAccountCreated(String uid, AbstractDto dto, String script, SysSystemDto system);

	/**
	 * Find provisioning mapping on the given system and for entity type.
	 *
	 * @param systemId
	 * @param entityType
	 * @param mappingId
	 * @return
	 */
	SysSystemMappingDto findProvisioningMapping(UUID systemId, String entityType, UUID mappingId);
	
	/**
	 * Duplication of mapping attributes.
	 * 
	 * @since 10.1.0
	 * 
	 * @param id
	 * @param schema
	 * @param schemaAttributesIds
	 * @param mappedAttributesIds
	 * @param usedInSameSystem 
	 * @return
	 */
	SysSystemMappingDto duplicateMapping(UUID id, SysSchemaObjectClassDto schema, Map<UUID, UUID> schemaAttributesIds,
			Map<UUID, UUID> mappedAttributesIds, boolean usedInSameSystem);

	/**
	 * The context for the system mapping.
	 *
	 * @param mapping
	 * @param systemEntity
	 * @param dto
	 * @param system
	 * @return
	 */
	MappingContext getMappingContext(SysSystemMappingDto mapping, SysSystemEntityDto systemEntity, AbstractDto dto, SysSystemDto system);
}
