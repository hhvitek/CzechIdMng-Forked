package eu.bcvsolutions.idm.core.api.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.InstanceIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;

/**
 * Persisted entity event
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
@Relation(collectionRelation = "entityEvents")
public class IdmEntityEventDto extends AbstractDto implements InstanceIdentifiable {

	private static final long serialVersionUID = 1L;
	//
	@NotEmpty
	private String ownerType;
	@NotNull
	private UUID ownerId;
	private UUID superOwnerId;
	@Size(max = DefaultFieldLengths.NAME)
	private String eventType; // persisted event type
	private ConfigurationMap properties;
	private Identifiable content; // content - in current processing
	private Identifiable originalSource; // persisted content - before event starts
	private Integer processedOrder;
	private boolean closed;
	private boolean suspended;
	private ZonedDateTime executeDate;
	private ZonedDateTime eventStarted;
	private ZonedDateTime eventEnded;
	private PriorityType priority;
	@Embedded(dtoClass = IdmEntityEventDto.class)
	private UUID parent;
	private String parentEventType; // parent event type
	private UUID rootId;
	@NotNull
	private String instanceId;
	private OperationResultDto result;
	
	public IdmEntityEventDto() {
	}
	
	public IdmEntityEventDto(UUID id) {
		super(id);
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}
	
	public UUID getSuperOwnerId() {
		return superOwnerId;
	}
	
	public void setSuperOwnerId(UUID superOwnerId) {
		this.superOwnerId = superOwnerId;
	}

	public ZonedDateTime getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(ZonedDateTime executeDate) {
		this.executeDate = executeDate;
	}

	public UUID getParent() {
		return parent;
	}

	public void setParent(UUID parent) {
		this.parent = parent;
	}
	
	public OperationResultDto getResult() {
		return result;
	}
	
	public void setResult(OperationResultDto result) {
		this.result = result;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getParentEventType() {
		return parentEventType;
	}
	
	public void setParentEventType(String parentEventType) {
		this.parentEventType = parentEventType;
	}

	public Identifiable getContent() {
		return content;
	}

	public void setContent(Identifiable content) {
		this.content = content;
	}

	public Identifiable getOriginalSource() {
		return originalSource;
	}

	public void setOriginalSource(Identifiable originalSource) {
		this.originalSource = originalSource;
	}

	public Integer getProcessedOrder() {
		return processedOrder;
	}

	public void setProcessedOrder(Integer processedOrder) {
		this.processedOrder = processedOrder;
	}
	
	public ConfigurationMap getProperties() {
		if (properties == null) {
			properties = new ConfigurationMap();
		}
		return properties;
	}

	public void setProperties(ConfigurationMap properties) {
		this.properties = properties;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}
	
	@Override
	public String getInstanceId() {
		return instanceId;
	}
	
	@Override
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public PriorityType getPriority() {
		return priority;
	}
	
	public void setPriority(PriorityType priority) {
		this.priority = priority;
	}
	
	public UUID getRootId() {
		return rootId;
	}
	
	public void setRootId(UUID rootId) {
		this.rootId = rootId;
	}
	
	/**
	 * Event processing started.
	 * 
	 * @return start or {@code null} - not started 
	 * @since 10.6.0
	 */
	public ZonedDateTime getEventStarted() {
		return eventStarted;
	}
	
	/**
	 * Event processing started.
	 * 
	 * @param eventStarted start or {@code null} - not started 
	 * @since 10.6.0
	 */
	public void setEventStarted(ZonedDateTime eventStarted) {
		this.eventStarted = eventStarted;
	}
	
	/**
	 * Event processing ended.
	 * 
	 * @return end or {@code null} - not ended 
	 * @since 10.6.0
	 */
	public ZonedDateTime getEventEnded() {
		return eventEnded;
	}
	
	/**
	 * Event processing ended.
	 * @param eventEnded end or {@code null} - not ended 
	 * @since 10.6.0
	 */
	public void setEventEnded(ZonedDateTime eventEnded) {
		this.eventEnded = eventEnded;
	}
}
