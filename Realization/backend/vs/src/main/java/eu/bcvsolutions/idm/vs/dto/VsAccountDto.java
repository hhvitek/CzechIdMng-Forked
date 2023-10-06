package eu.bcvsolutions.idm.vs.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for account in virutal system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "accounts")
@Schema(description = "Account in virtual system")
public class VsAccountDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Schema(required = true, description = "Unique account identifier. UID on system and for connector.")
	private String uid;
	private boolean enable;
	@Schema(required = true, description = "CzechIdM system identifier. UID on system and for connector.")
	private UUID systemId;
	@Schema(required = true, description = "Connector identifier. UID on system and for connector.")
	private String connectorKey;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public String getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(String connectorKey) {
		this.connectorKey = connectorKey;
	}
}
