package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO with password history, that determines the number of unique new
 * passwords.
 * 
 * @author Jirka Koula
 *
 */

@Relation(collectionRelation = "passwordHistories")
public class AccPasswordHistoryDto extends AbstractDto {

	private static final long serialVersionUID = -1097132461258375616L;
	@NotNull
	@JsonIgnore
	private String password;
	@NotNull
	@Embedded(dtoClass = AccAccountDto.class)
	private UUID account;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public UUID getAccount() {
		return account;
	}

	public void setAccount(UUID account) {
		this.account = account;
	}

}
