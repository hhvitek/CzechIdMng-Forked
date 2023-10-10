package eu.bcvsolutions.idm.vs.dto;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for request in virtual system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "requests")
@ApiModel(description = "Request in virtual system")
public class VsRequestDto extends AbstractDto implements ExternalIdentifiable {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Unique account identifier. UID on system and for connector.")
	private String uid;
	@ApiModelProperty(required = true, notes = "CzechIdM system identifier. UID on system and for connector.")
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@ApiModelProperty(required = true, notes = "Connector identifier. UID on system and for connector.")
	private String connectorKey;
	private VsOperationType operationType;
	@NotNull
	private VsRequestState state;
	@NotNull
	private boolean executeImmediately;
	private List<IdmIdentityDto> implementers;
	@JsonIgnore
	private IcConnectorConfiguration configuration;
	private IcConnectorObject connectorObject;
	@Embedded(dtoClass = VsRequestDto.class)
	private UUID duplicateToRequest;
	@Embedded(dtoClass = VsRequestDto.class)
	private UUID previousRequest;
	private String reason;
	// ID of request, without DB relation on the request -> Request can be null or doesn't have to exist!
    private UUID roleRequestId;
    @Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@JsonProperty(access = Access.READ_ONLY)
	private BaseDto targetEntity;
	@JsonProperty(access = Access.READ_ONLY)
	private String targetEntityType;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public String getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(String connectorKey) {
		this.connectorKey = connectorKey;
	}

	public VsOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(VsOperationType operationType) {
		this.operationType = operationType;
	}

	public VsRequestState getState() {
		return state;
	}

	public void setState(VsRequestState state) {
		this.state = state;
	}

	public boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public void setExecuteImmediately(boolean executeImmediately) {
		this.executeImmediately = executeImmediately;
	}

	public List<IdmIdentityDto> getImplementers() {
		return implementers;
	}

	public void setImplementers(List<IdmIdentityDto> implementers) {
		this.implementers = implementers;
	}

	public IcConnectorConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(IcConnectorConfiguration configuration) {
		this.configuration = configuration;
	}

	public IcConnectorObject getConnectorObject() {
		return connectorObject;
	}

	public void setConnectorObject(IcConnectorObject connectorObject) {
		this.connectorObject = connectorObject;
	}

	public UUID getDuplicateToRequest() {
		return duplicateToRequest;
	}

	public void setDuplicateToRequest(UUID duplicateToRequest) {
		this.duplicateToRequest = duplicateToRequest;
	}

	public UUID getPreviousRequest() {
		return previousRequest;
	}

	public void setPreviousRequest(UUID previousRequest) {
		this.previousRequest = previousRequest;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public UUID getRoleRequestId() {
		return roleRequestId;
	}

	public void setRoleRequestId(UUID roleRequestId) {
		this.roleRequestId = roleRequestId;
	}

	public BaseDto getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(BaseDto targetEntity) {
		this.targetEntity = targetEntity;
	}

	public String getTargetEntityType() {
		return targetEntityType;
	}

	public void setTargetEntityType(String targetEntityType) {
		this.targetEntityType = targetEntityType;
	}

	/**
	 * @since 9.7.9
	 */
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	/**
	 * @since 9.7.9
	 */
	@Override
	public String getExternalId() {
		return externalId;
	}
}
