package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * <i>SysSystemMapping</i> is responsible for mapping attribute to entity
 * type and operations (Provisioning, Reconciliace, Synchronisation) to idm
 * entity
 * 
 * @author svandav
 * @author Roman Kucera
 *
 */
@Entity
@Table(name = "sys_system_mapping", indexes = {
		@Index(name = "ux_sys_s_mapping_name", columnList = "name, object_class_id", unique = true),
		@Index(name = "idx_sys_s_mapping_o_c_id", columnList = "object_class_id"),
		@Index(name = "idx_sys_s_mapping_o_type", columnList = "operation_type"),
		@Index(name = "idx_sys_s_mapping_e_type", columnList = "entity_type"),
		@Index(name = "idx_sys_s_mapping_c_s_mapping", columnList = "connected_system_mapping_id")
		})
public class SysSystemMapping extends AbstractEntity {

	private static final long serialVersionUID = -8492560756893726050L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	@Audited
	@NotNull
	@Column(name = "entity_type", nullable = false)
	private String entityType;
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "object_class_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSchemaObjectClass objectClass;
	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false)
	private SystemOperationType operationType;
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "tree_type_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmTreeType treeType;
	@Audited
	@Column(name = "protection_enabled", nullable = true)
	private boolean protectionEnabled = false;
	@Audited
	@Column(name = "protection_interval", nullable = true)
	private Integer protectionInterval;
	@Audited
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "can_be_acc_created_script")
	private String canBeAccountCreatedScript;
	@Audited
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "mapping_context_script")
	private String mappingContextScript;
	@Audited
	@Column(name = "add_context_contracts", nullable = false)
	// Add all identity contracts.
	private boolean addContextContracts = false;
	@Audited
	@Column(name = "add_context_identity_roles", nullable = false)
	// Add all identity roles.
	private boolean addContextIdentityRoles = false;
	@Audited
	@Column(name = "add_context_roles_sys", nullable = false)
	// Add identity roles only for this system.
	private boolean addContextIdentityRolesForSystem = false;
	@Audited
	@Column(name = "add_context_con_obj", nullable = false)
	// Add all connector object (calls connector).
	private boolean addContextConnectorObject = false;
	@Audited
	@ManyToOne
	@JoinColumn(name = "connected_system_mapping_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystemMapping connectedSystemMappingId;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false)
	private AccountType accountType;

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public SystemOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(SystemOperationType operationType) {
		this.operationType = operationType;
	}

	@JsonIgnore
	public SysSystem getSystem() {
		if (objectClass == null) {
			return null;
		}
		return objectClass.getSystem();
	}

	public void setObjectClass(SysSchemaObjectClass objectClass) {
		this.objectClass = objectClass;
	}
	
	public SysSchemaObjectClass getObjectClass() {
		return objectClass;
	}

	public IdmTreeType getTreeType() {
		return treeType;
	}

	public void setTreeType(IdmTreeType treeType) {
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

	public void setMappingContextScript(String provisioningContextScript) {
		this.mappingContextScript = provisioningContextScript;
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

	public SysSystemMapping getConnectedSystemMappingId() {
		return connectedSystemMappingId;
	}

	public void setConnectedSystemMappingId(SysSystemMapping connectedSystemMappingId) {
		this.connectedSystemMappingId = connectedSystemMappingId;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}
}
