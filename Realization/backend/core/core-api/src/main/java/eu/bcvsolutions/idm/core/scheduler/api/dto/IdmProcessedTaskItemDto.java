package eu.bcvsolutions.idm.core.scheduler.api.dto;

import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * LRT item - contains processed item state.
 * 
 * @author Jan Helbich
 *
 */
@Relation(collectionRelation = "longRunningTaskItems")
public class IdmProcessedTaskItemDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	@JsonDeserialize(as = UUID.class)
	private UUID referencedEntityId;
	private String referencedDtoType;
	private OperationResult operationResult;
	@Embedded(dtoClass = IdmLongRunningTaskDto.class)
	private UUID longRunningTask;
	@Embedded(dtoClass = IdmScheduledTaskDto.class)
	private UUID scheduledTaskQueueOwner;
	@JsonProperty(access = Access.READ_ONLY)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Related entity was already deleted.")
	private transient boolean deleted;

	public UUID getReferencedEntityId() {
		return referencedEntityId;
	}

	public void setReferencedEntityId(UUID referencedEntityId) {
		this.referencedEntityId = referencedEntityId;
	}

	public String getReferencedDtoType() {
		return referencedDtoType;
	}

	public void setReferencedDtoType(String referencedDtoType) {
		this.referencedDtoType = referencedDtoType;
	}

	public OperationResult getOperationResult() {
		return operationResult;
	}

	public void setOperationResult(OperationResult operationResult) {
		this.operationResult = operationResult;
	}

	public UUID getLongRunningTask() {
		return longRunningTask;
	}

	public void setLongRunningTask(UUID longRunningTask) {
		this.longRunningTask = longRunningTask;
	}

	public UUID getScheduledTaskQueueOwner() {
		return scheduledTaskQueueOwner;
	}

	public void setScheduledTaskQueueOwner(UUID scheduledTaskQueueOwner) {
		this.scheduledTaskQueueOwner = scheduledTaskQueueOwner;
	}
	
	/**
	 * Related entity was already deleted.
	 * 
	 * @return true - deleted
	 * @since 11.2.0
	 */
	public boolean isDeleted() {
		return deleted;
	}
	
	/**
	 * Related entity was already deleted.
	 * 
	 * @param deleted true - deleted
	 * @since 11.2.0
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
