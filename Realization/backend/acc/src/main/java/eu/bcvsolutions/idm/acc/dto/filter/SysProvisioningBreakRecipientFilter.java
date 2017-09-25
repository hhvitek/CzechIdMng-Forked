package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for provisionign break recipients.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class SysProvisioningBreakRecipientFilter implements BaseFilter {

	private UUID breakConfigId;
	private UUID identityId;

	public UUID getBreakConfigId() {
		return breakConfigId;
	}

	public void setBreakConfigId(UUID breakConfigId) {
		this.breakConfigId = breakConfigId;
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

}
