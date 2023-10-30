package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dto for system owner - role
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
@Relation(collectionRelation = "systemOwnersRoles")
public class SysSystemOwnerRoleDto extends AbstractDto implements ExternalIdentifiable, Requestable {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.NAME)
	@Schema(description = "Unique external identifier.")
	private String externalId;
	@NotNull
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@NotNull
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID ownerRole; // guarantee as role
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity

	/**
	 * System
	 *
	 * @return UUID of system
	 */
	public UUID getSystem() {
		return system;
	}

	/**
	 * System
	 *
	 * @param system UUID of system
	 */
	public void setSystem(UUID system) {
		this.system = system;
	}

	/**
	 * Owner as role
	 *
	 * @return UUID of role
	 */
	public UUID getOwnerRole() {
		return ownerRole;
	}

	/**
	 * Owner as role
	 *
	 * @param ownerRole UUID of role
	 */
	public void setOwnerRole(UUID ownerRole) {
		this.ownerRole = ownerRole;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public UUID getRequestItem() {
		return requestItem;
	}

	@Override
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}

}
