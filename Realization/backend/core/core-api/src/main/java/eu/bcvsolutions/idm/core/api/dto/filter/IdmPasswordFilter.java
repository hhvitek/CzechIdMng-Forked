package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Identity password filtering
 * 
 * @author Ondřej Kopr
 * @author Radek Tomiška
 *
 */
public class IdmPasswordFilter extends AbstractPasswordFilter {

    private UUID identityId;
    private String identityUsername;
    private Boolean identityDisabled;
	private LocalDate validTill;
	private LocalDate validFrom;
	private Boolean mustChange;

    public UUID getIdentityId() {
        return identityId;
    }

    public void setIdentityId(UUID identityId) {
        this.identityId = identityId;
    }

	public Boolean getIdentityDisabled() {
		return identityDisabled;
	}

    public void setIdentityDisabled(Boolean identityDisabled) {
		this.identityDisabled = identityDisabled;
	}

	public String getIdentityUsername() {
		return identityUsername;
	}

	public void setIdentityUsername(String identityUsername) {
		this.identityUsername = identityUsername;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}
	public Boolean getMustChange() {
		return mustChange;
	}

	public void setMustChange(Boolean mustChange) {
		this.mustChange = mustChange;
	}
	@Override
	public UUID getEntityId() { return getIdentityId(); }

	@Override
	public void setEntityId(UUID entityId) { setIdentityId(entityId); }
}
