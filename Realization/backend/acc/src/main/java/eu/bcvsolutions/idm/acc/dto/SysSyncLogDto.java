package eu.bcvsolutions.idm.acc.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * DTO for {@link SysSyncLog}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "synchronizationLogs")
public class SysSyncLogDto extends AbstractDto implements Loggable {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SysSyncLogDto.class);
	private static final long serialVersionUID = -4364209149375365217L;
	
	@Embedded(dtoClass = SysSyncConfigDto.class)
	private UUID synchronizationConfig;
	private boolean running;
	private boolean containsError;
	private ZonedDateTime started;
	private ZonedDateTime ended;
	private String token;
	private String log;
	private List<SysSyncActionLogDto> syncActionLogs;

	public UUID getSynchronizationConfig() {
		return synchronizationConfig;
	}

	public void setSynchronizationConfig(UUID synchronizationConfig) {
		this.synchronizationConfig = synchronizationConfig;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isContainsError() {
		return containsError;
	}

	public void setContainsError(boolean containsError) {
		this.containsError = containsError;
	}

	public ZonedDateTime getStarted() {
		return started;
	}

	public void setStarted(ZonedDateTime started) {
		this.started = started;
	}

	public ZonedDateTime getEnded() {
		return ended;
	}

	public void setEnded(ZonedDateTime ended) {
		this.ended = ended;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	@Override
	public String addToLog(String text) {
		if (text != null) {
			LOG.info(text);
			StringBuilder builder = new StringBuilder();
			if (this.log != null) {
				builder.append(this.log);
				builder.append("\n" + Loggable.LOG_SEPARATOR + "\n");
			}
			builder.append(text);
			this.setLog(builder.toString());
		}
		return this.getLog();
	}

	public List<SysSyncActionLogDto> getSyncActionLogs() {
		if (this.syncActionLogs == null) {
			this.syncActionLogs = new ArrayList<>();
		}
		return syncActionLogs;
	}

	public void setSyncActionLogs(List<SysSyncActionLogDto> syncActionLogs) {
		this.syncActionLogs = syncActionLogs;
	}
}
