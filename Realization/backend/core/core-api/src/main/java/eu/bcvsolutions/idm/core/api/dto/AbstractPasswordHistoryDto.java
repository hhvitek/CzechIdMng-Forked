package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * DTO with password history, that determines the number of unique new
 * passwords.
 * 
 * @author Jirka Koula
 *
 */

@Relation(collectionRelation = "passwordHistories")
public abstract class AbstractPasswordHistoryDto extends AbstractDto {

	private static final long serialVersionUID = 7032623383489998478L;
	@NotNull
	@JsonIgnore
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
