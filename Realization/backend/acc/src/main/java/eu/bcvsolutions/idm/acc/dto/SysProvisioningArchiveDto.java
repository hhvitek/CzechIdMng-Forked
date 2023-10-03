package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;

/**
 * DTO for {@link SysProvisioningArchive}
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Relation(collectionRelation = "provisioningArchives")
public class SysProvisioningArchiveDto extends AbstractDto implements ProvisioningOperation {

	private static final long serialVersionUID = 9129849089102546294L;

	private ProvisioningEventType operationType;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@JsonProperty(access = Access.READ_ONLY)
	private ProvisioningContext provisioningContext;
	private String entityType;
	private UUID entityIdentifier;
	private String systemEntityUid; // account uid, etc.
	private OperationResult result;
	// ID of request, without DB relation on the request -> Request can be null or doesn't have to exist!
    private UUID roleRequestId;
	@Embedded(dtoClass = AccAccountDto.class)
	private UUID account;

	public ProvisioningEventType getOperationType() {
		return operationType;
	}

	public void setOperationType(ProvisioningEventType operationType) {
		this.operationType = operationType;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public ProvisioningContext getProvisioningContext() {
		return provisioningContext;
	}

	public void setProvisioningContext(ProvisioningContext provisioningContext) {
		this.provisioningContext = provisioningContext;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public UUID getEntityIdentifier() {
		return entityIdentifier;
	}

	public void setEntityIdentifier(UUID entityIdentifier) {
		this.entityIdentifier = entityIdentifier;
	}

	public String getSystemEntityUid() {
		return systemEntityUid;
	}

	public void setSystemEntityUid(String systemEntityUid) {
		this.systemEntityUid = systemEntityUid;
	}

	@Override
	public OperationResult getResult() {
		return result;
	}

	public void setResult(OperationResult result) {
		this.result = result;
	}
	
	public UUID getRoleRequestId() {
		return roleRequestId;
	}

	public void setRoleRequestId(UUID roleRequestId) {
		this.roleRequestId = roleRequestId;
	}

	public UUID getAccount() {
		return account;
	}

	public void setAccount(UUID account) {
		this.account = account;
	}

	/**
	 * New {@link SysProvisioningArchiveDto} builder.
	 * 
	 * @author Radek Tomiška
	 *
	 */
	public static class Builder {
		private ProvisioningEventType operationType;
		private UUID system;
		private ProvisioningContext provisioningContext;
		private String entityType;
		private UUID entityIdentifier;
		private String systemEntityUid;
		private OperationResult result;
		private UUID account;
		public Builder() {
		}
		
		public Builder(ProvisioningOperation provisioningOperation) {
			this.operationType = provisioningOperation.getOperationType();
			this.system = provisioningOperation.getSystem();
			this.provisioningContext = provisioningOperation.getProvisioningContext();
			this.entityType = provisioningOperation.getEntityType();
			this.entityIdentifier = provisioningOperation.getEntityIdentifier();
			this.result = provisioningOperation.getResult();
			this.account = provisioningOperation.getAccount();
		}
		
		public Builder setOperationType(ProvisioningEventType operationType) {
			this.operationType = operationType;
			return this;
		}
		
		public Builder setSystem(SysSystemDto system) {
			if (system != null) {
				this.system = system.getId();				
			}
			return this;
		}

		public Builder setAccount(AccAccountDto account) {
			if (account != null) {
				this.account = account.getId();
			}
			return this;
		}
		
		public Builder setProvisioningContext(ProvisioningContext provisioningContext) {
			this.provisioningContext = provisioningContext;
			return this;
		}
		
		public Builder setEntityType(String entityType) {
			this.entityType = entityType;
			return this;
		}
		
		public Builder setEntityIdentifier(UUID entityIdentifier) {
			this.entityIdentifier = entityIdentifier;
			return this;
		}
		
		public Builder setSystemEntityUid(String systemEntityUid) {
			this.systemEntityUid = systemEntityUid;
			return this;
		}
		
		public Builder setResult(OperationResult result) {
			this.result = result;
			return this;
		}
		
		/**
		 * Returns newly constructed SysProvisioningArchive object.
		 * 
		 * @return
		 */
		public SysProvisioningArchiveDto build() {
			SysProvisioningArchiveDto provisioningArchive = new SysProvisioningArchiveDto();
			provisioningArchive.setOperationType(operationType);
			provisioningArchive.setSystem(system);
			provisioningArchive.setSystemEntityUid(systemEntityUid);
			provisioningArchive.setEntityType(entityType);
			provisioningArchive.setEntityIdentifier(entityIdentifier);
			provisioningArchive.setProvisioningContext(provisioningContext);
			provisioningArchive.setResult(result);
			provisioningArchive.setAccount(account);
			return provisioningArchive;
		}
	}

	@Override
	public OperationState getResultState() {
		if (result != null) {
			return result.getState();
		}
		return null;
	}
}
