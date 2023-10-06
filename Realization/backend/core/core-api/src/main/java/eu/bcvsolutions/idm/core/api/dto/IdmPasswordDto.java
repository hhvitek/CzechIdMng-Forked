package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Identity password dto.
 * 
 * @author Jirka Koula
 *
 */
@Relation(collectionRelation = "passwords")
public class IdmPasswordDto extends AbstractPasswordDto implements ValidableEntity  {

	private static final long serialVersionUID = -5705631874717976613L;
	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
    private UUID identity;

    public UUID getIdentity() {
        return identity;
    }

    public void setIdentity(UUID identity) {
        this.identity = identity;
    }

	@Override
	public UUID getEntity() {
		return getIdentity();
	}

	@Override
	public void setEntity(UUID entity) {
		setIdentity(entity);
	}
}
