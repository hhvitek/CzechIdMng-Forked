package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import java.time.ZonedDateTime;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;

/**
 * Service for create and read identity role valid requests.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmIdentityRoleValidRequestService extends ReadWriteDtoService<IdmIdentityRoleValidRequestDto, EmptyFilter>, RoleAssignmentValidRequestService<IdmIdentityRoleValidRequestDto> {
	
	IdmIdentityRoleValidRequestDto createByIdentityRoleId(UUID identityRole);

	/**
	 * Method find all {@link IdmIdentityRoleValidRequestDto} that can be process from {@value from} given in parameter.
	 * @param from
	 * @return
	 */
	List<IdmIdentityRoleValidRequestDto> findAllValidFrom(ZonedDateTime from);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequestDto} for role
	 * @param role
	 * @return
	 */
	List<IdmIdentityRoleValidRequestDto> findAllValidRequestForRoleId(UUID role);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequestDto} for identity
	 * @param identity
	 * @return
	 */
	List<IdmIdentityRoleValidRequestDto> findAllValidRequestForIdentityId(UUID identity);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequestDto} for identityRole
	 * @param identityRole
	 * @return
	 */
	List<IdmIdentityRoleValidRequestDto> findAllValidRequestForIdentityRoleId(UUID identityRole);
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequestDto} for identityContract
	 * @param identityContract
	 * @return
	 */
	List<IdmIdentityRoleValidRequestDto> findAllValidRequestForIdentityContractId(UUID identityContract);
	
	/**
	 * Remove all entities {@link IdmIdentityRoleValidRequestDto} check for null and empty list.
	 * @param entities
	 */
	void deleteAll(List<IdmIdentityRoleValidRequestDto> entities);
}
