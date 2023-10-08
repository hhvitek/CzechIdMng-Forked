package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordHistoryDto;

/**
 * DTO with password history, that determines the number of unique new
 * passwords.
 * 
 * @author Jirka Koula
 *
 */

@Relation(collectionRelation = "passwordHistories")
public class AccPasswordHistoryDto extends AbstractPasswordHistoryDto {

	private static final long serialVersionUID = -1097132461258375616L;

	@NotNull
	@Embedded(dtoClass = AccAccountDto.class)
	private UUID account;

	public UUID getAccount() {
		return account;
	}

	public void setAccount(UUID account) {
		this.account = account;
	}

}
