package eu.bcvsolutions.idm.acc.dto;

import java.text.MessageFormat;
import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation_;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity_;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * DTO for {@link SysProvisioningOperation}.
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */
@Relation(collectionRelation = "provisioningOperations")
public class SysProvisioningOperationDto extends AbstractDto implements ProvisioningOperation {

	private static final long serialVersionUID = 3928289153591673531L;
	
	private ProvisioningEventType operationType;
	@JsonProperty(access = Access.READ_ONLY)
	private ProvisioningContext provisioningContext;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@Embedded(dtoClass = AccAccountDto.class)
	private UUID account;
	private String entityType;
	private UUID entityIdentifier;
	@Embedded(dtoClass = SysSystemEntityDto.class)
	private UUID systemEntity; // account uid, etc.
	private int currentAttempt = 0;
	private int maxAttempts;
	private OperationResult result;
	@Embedded(dtoClass = SysProvisioningBatchDto.class)
	private UUID batch;
	private String systemEntityUid;
	// ID of request, without DB relation on the request -> Request can be null or doesn't have to exists 
	private UUID roleRequestId;
	@JsonIgnore
	// Internal attribute, not persisted - for mark if is operation original provisioning or retry attempt.
	private boolean synchronousProvisioning = false;
	@JsonIgnore
	// Internal attribute, not persisted - for set manual event. A manual event prevents end sync to soon.
	private UUID manualEventId;
	@JsonIgnore
	// Internal attribute, not persisted - for dryRun flag - used for walking through the chain of provisioning operations without performing provisioning   
	private boolean isDryRun = false;

	public SysProvisioningOperationDto() {
	}
	
	public SysProvisioningOperationDto(UUID id) {
		super(id);
	}

	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}

	public ProvisioningContext getProvisioningContext() {
		return provisioningContext;
	}

	public void setProvisioningContext(ProvisioningContext provisioningContext) {
		this.provisioningContext = provisioningContext;
	}

	public UUID getEntityIdentifier() {
		return entityIdentifier;
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}
	
	public boolean isDryRun() {
		return isDryRun;
	}

	public void setDryRun(boolean isDryRun) {
		this.isDryRun = isDryRun;
	}

	/**
	 * systemDto is get by {@link DtoUtils#getEmbedded}
	 */
	@Override
	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	@Override
	public UUID getAccount() {
		return account;
	}

	public void setAccount(UUID account) {
		this.account = account;
	}

	@Override
	public String getEntityType() {
		return entityType;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public UUID getSystemEntity() {
		return systemEntity;
	}
	
	public void setSystemEntity(UUID systemEntity) {
		this.systemEntity = systemEntity;
	}
	
	public UUID getRoleRequestId() {
		return roleRequestId;
	}

	public void setRoleRequestId(UUID roleRequestId) {
		this.roleRequestId = roleRequestId;
	}

	@Override
	public OperationState getResultState() {
		if (this.result == null) {
			return null;
		}
		return getResult().getState();
	}
	
	public int getCurrentAttempt() {
		return currentAttempt;
	}

	public void setCurrentAttempt(int currentAttempt) {
		this.currentAttempt = currentAttempt;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	@Override
	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult result) {
		this.result = result;
	}

	public UUID getBatch() {
		return batch;
	}

	public void setBatch(UUID batch) {
		this.batch = batch;
	}
	
	public void increaseAttempt() {
		this.currentAttempt++;
	}
	
	@JsonProperty(access = Access.READ_ONLY)
	public String getSystemEntityUid() {
		return systemEntityUid;
	}

	public void setSystemEntityUid(String systemEntityUid) {
		this.systemEntityUid = systemEntityUid;
	}
	
	public boolean isSynchronousProvisioning() {
		return synchronousProvisioning;
	}

	public void setSynchronousProvisioning(boolean synchronousProvisioning) {
		this.synchronousProvisioning = synchronousProvisioning;
	}

	public UUID getManualEventId() {
		return manualEventId;
	}

	public void setManualEventId(UUID manualEventId) {
		this.manualEventId = manualEventId;
	}

	/**
	 * New {@link SysProvisioningOperationDto} builder.
	 * 
	 * @author Radek Tomiška
	 *
	 */
	public static class Builder {
		private ProvisioningEventType operationType;
		private ProvisioningContext provisioningContext;
		private UUID entityIdentifier;
		private UUID system;
		// Add system DTO to the embedded ... for optimization 
		private SysSystemDto systemDto;
		private UUID account;
		private String entityType;
		private UUID systemEntity;
		private String systemEntityUid;
		private UUID roleRequestId;
		
		public Builder setOperationType(ProvisioningEventType operationType) {
			this.operationType = operationType;
			return this;
		}
		
		/**
		 * Maps {@linkAccountOperationType} to {@link ProvisioningEventType}.
		 * @param operationType
		 * @return
		 */
		public Builder setOperationType(ProvisioningOperationType operationType) {
			switch (operationType) {
				case CREATE: {
					this.operationType = ProvisioningEventType.CREATE;
					break;
				}
				case UPDATE: {
					this.operationType = ProvisioningEventType.UPDATE;
					break;
				}
				case DELETE: {
					this.operationType = ProvisioningEventType.DELETE;
					break;
				}
				default: {
					throw new UnsupportedOperationException(MessageFormat.format("Account operation type [{}] is not supported for provisioning", operationType));
				}
			}
			
			return this;
		}
		
		public Builder setProvisioningContext(ProvisioningContext provisioningContext) {
			this.provisioningContext = provisioningContext;
			return this;
		}
		
		
		public Builder setEntityIdentifier(UUID entityIdentifier) {
			this.entityIdentifier = entityIdentifier;
			return this;
		}
		
		public Builder setSystemEntity(SysSystemEntityDto systemEntity) {
			
			SysSystemDto systemDto = DtoUtils.getEmbedded(systemEntity, SysSystemEntity_.system.getName(), SysSystemDto.class, null);
			if(systemDto != null) {
				this.systemDto = systemDto;
			}
			this.system = systemEntity.getSystem();
			this.entityType = systemEntity.getEntityType();
			this.systemEntity = systemEntity.getId();
			this.systemEntityUid = systemEntity.getUid();
			return this;
		}
		
		public Builder setEntityType(String entityType) {
			this.entityType = entityType;
			return this;
		}
		
		public Builder setSystem(UUID system) {
			this.system = system;
			return this;
		}

		public Builder setAccount(UUID account) {
			this.account = account;
			return this;
		}
		
		public Builder setSystemEntity(UUID systemEntity) {
			this.systemEntity = systemEntity;
			return this;
		}
		
		public Builder setRoleRequestId(UUID roleRequestId) {
			this.roleRequestId = roleRequestId;
			return this;
		}
		
		/**
		 * Returns newly constructed SysProvisioningOperation object.
		 * 
		 * @return
		 */
		public SysProvisioningOperationDto build() {
			SysProvisioningOperationDto provisioningOperation = new SysProvisioningOperationDto();
			provisioningOperation.setOperationType(operationType);
			if (systemDto != null) {
				// Add system DTO to the embedded ... for optimization 
				provisioningOperation.getEmbedded().put(SysProvisioningOperation_.system.getName(), systemDto);
			}
			provisioningOperation.setSystem(system);
			provisioningOperation.setSystemEntity(systemEntity);
			provisioningOperation.setSystemEntityUid(systemEntityUid);
			provisioningOperation.setEntityType(entityType);
			provisioningOperation.setEntityIdentifier(entityIdentifier);
			provisioningOperation.setProvisioningContext(provisioningContext);
			provisioningOperation.setRoleRequestId(roleRequestId);
			provisioningOperation.setAccount(account);
		 
			return provisioningOperation;
		}
	}
}
