package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * DTO with password history, that determines the number of unique new
 * passwords.
 * 
 * @author Ondrej Kopr
 *
 */

@Relation(collectionRelation = "passwordHistories")
public class IdmPasswordHistoryDto extends AbstractPasswordHistoryDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}

}
