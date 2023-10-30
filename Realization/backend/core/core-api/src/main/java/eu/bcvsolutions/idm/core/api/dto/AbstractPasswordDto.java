package eu.bcvsolutions.idm.core.api.dto;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Common password dto.
 * 
 * @author Jirka Koula
 *
 */
@Relation(collectionRelation = "passwords")
public abstract class AbstractPasswordDto extends AbstractDto  {

	private static final long serialVersionUID = 8922688931648790561L;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
