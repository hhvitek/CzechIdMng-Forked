package eu.bcvsolutions.idm.acc.dto.filter;

import java.time.ZonedDateTime;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for password history
 *
 * @author Jirka Koula
 *
 */
public class AccPasswordHistoryFilter implements BaseFilter {
	
	private UUID accountId;
	private ZonedDateTime from;
    private ZonedDateTime till;
    private String creator;

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public ZonedDateTime getFrom() {
		return from;
	}

	public void setFrom(ZonedDateTime from) {
		this.from = from;
	}

	public ZonedDateTime getTill() {
		return till;
	}

	public void setTill(ZonedDateTime till) {
		this.till = till;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

}
