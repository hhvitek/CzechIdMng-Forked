package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractPasswordDto;

/**
 * Account password dto.
 * 
 * @author Jirka Koula
 *
 */
@Relation(collectionRelation = "passwords")
public class AccPasswordDto extends AbstractPasswordDto  {

	private static final long serialVersionUID = -4519101848124842809L;
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
