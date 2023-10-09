package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Filter for identity password history
 *
 * @author Ondrej Kopr
 * @author Jirka Koula
 *
 */
public class IdmPasswordHistoryFilter extends AbstractPasswordHistoryFilter {
	
	private UUID identityId;
	private String identityUsername;

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public String getIdentityUsername() {
		return identityUsername;
	}

	public void setIdentityUsername(String identityUsername) {
		this.identityUsername = identityUsername;
	}

	@Override
	public void setEntityId(UUID entityId) {
		setIdentityId(entityId);
	}
}
