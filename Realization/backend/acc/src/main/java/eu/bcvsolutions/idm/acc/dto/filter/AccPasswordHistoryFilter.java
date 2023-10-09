package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.AbstractPasswordHistoryFilter;

/**
 * Filter for account password history
 *
 * @author Jirka Koula
 *
 */
public class AccPasswordHistoryFilter extends AbstractPasswordHistoryFilter {
	
	private UUID accountId;

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	@Override
	public void setEntityId(UUID entityId) {
		setAccountId(entityId);
	}
}
