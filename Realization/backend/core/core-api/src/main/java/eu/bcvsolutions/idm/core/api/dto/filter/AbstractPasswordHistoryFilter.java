package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Filter for password history
 *
 * @author Jirka Koula
 */
public abstract class AbstractPasswordHistoryFilter implements BaseFilter {

	private ZonedDateTime from;
	private ZonedDateTime till;
	private String creator;

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

	public abstract void setEntityId(UUID entityId);
}
