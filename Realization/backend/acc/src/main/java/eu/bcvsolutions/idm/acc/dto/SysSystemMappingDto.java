package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.domain.IgnoreSameTypeReferenceFieldDuringImport;

/**
 * DTO for entity {@link SysSystemMapping}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Roman Kucera
 */

@Relation(collectionRelation = "systemMappings")
public class SysSystemMappingDto extends AbstractDto {

	private static final long serialVersionUID = -3263064824050858302L;

	private String name;
	private String entityType;
	@Embedded(dtoClass = SysSchemaObjectClassDto.class)
	private UUID objectClass;
	private SystemOperationType operationType;
	@Embedded(dtoClass = IdmTreeTypeDto.class)
	private UUID treeType;
	private boolean protectionEnabled = false;
	private Integer protectionInterval;
	private String canBeAccountCreatedScript;
	private String mappingContextScript;
	private boolean addContextContracts = false;
	private boolean addContextIdentityRoles = false;
	private boolean addContextIdentityRolesForSystem = false;
	private boolean addContextConnectorObject = false;
	@Embedded(dtoClass = SysSystemMappingDto.class)
	@IgnoreSameTypeReferenceFieldDuringImport
	private UUID connectedSystemMappingId;

	private AccountType accountType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public UUID getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(UUID objectClass) {
		this.objectClass = objectClass;
	}

	public SystemOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(SystemOperationType operationType) {
		this.operationType = operationType;
	}

	public UUID getTreeType() {
		return treeType;
	}

	public void setTreeType(UUID treeType) {
		this.treeType = treeType;
	}

	public boolean isProtectionEnabled() {
		return protectionEnabled;
	}

	public void setProtectionEnabled(boolean protectionEnabled) {
		this.protectionEnabled = protectionEnabled;
	}

	public Integer getProtectionInterval() {
		return protectionInterval;
	}

	public void setProtectionInterval(Integer protectionInterval) {
		this.protectionInterval = protectionInterval;
	}

	public String getCanBeAccountCreatedScript() {
		return canBeAccountCreatedScript;
	}

	public void setCanBeAccountCreatedScript(String canBeAccountCreatedScript) {
		this.canBeAccountCreatedScript = canBeAccountCreatedScript;
	}

	public String getMappingContextScript() {
		return mappingContextScript;
	}

	public void setMappingContextScript(String mappingContextScript) {
		this.mappingContextScript = mappingContextScript;
	}

	public boolean isAddContextContracts() {
		return addContextContracts;
	}

	public void setAddContextContracts(boolean addContextContracts) {
		this.addContextContracts = addContextContracts;
	}

	public boolean isAddContextIdentityRoles() {
		return addContextIdentityRoles;
	}

	public void setAddContextIdentityRoles(boolean addContextIdentityRoles) {
		this.addContextIdentityRoles = addContextIdentityRoles;
	}

	public boolean isAddContextIdentityRolesForSystem() {
		return addContextIdentityRolesForSystem;
	}

	public void setAddContextIdentityRolesForSystem(boolean addContextIdentityRolesForSystem) {
		this.addContextIdentityRolesForSystem = addContextIdentityRolesForSystem;
	}

	public boolean isAddContextConnectorObject() {
		return addContextConnectorObject;
	}

	public void setAddContextConnectorObject(boolean addContextConnectorObject) {
		this.addContextConnectorObject = addContextConnectorObject;
	}

	public UUID getConnectedSystemMappingId() {
		return connectedSystemMappingId;
	}

	public void setConnectedSystemMappingId(UUID connectedSystemMappingId) {
		this.connectedSystemMappingId = connectedSystemMappingId;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}
}
