package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * An entity on target system. Entity could be linked to idm entitites (identity accounts, groups etc.).
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_system_entity", indexes = {
		@Index(name = "ux_system_entity_type_uid", columnList = "entity_type,uid,system_id", unique = true),
		@Index(name = "idx_sys_system_entity_uid", columnList = "uid"),
		@Index(name = "idx_sys_system_entity_type", columnList = "entity_type"),
		@Index(name = "idx_sys_system_entity_system", columnList = "system_id")
		})
public class SysSystemEntity extends AbstractEntity {
	
	private static final long serialVersionUID = -8243399066902498023L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.UID)
	@Column(name = "uid", length = DefaultFieldLengths.UID, nullable = false)
	private String uid;
	
	@Audited
	@NotNull
	@Column(name = "entity_type", nullable = false)
	private String entityType;
	
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystem system;
	
	@Audited
	@NotNull
	@Column(name = "wish", nullable = false)
	private boolean wish = true; // prepared system entity for provisioning. wish = false, then entity exists on target system
	
	public SysSystemEntity() {
	}
	
	public SysSystemEntity(String uid, String entityType) {
		this.uid = uid;
		this.entityType = entityType;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getUid() {
		return uid;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}
	
	public void setWish(boolean wish) {
		this.wish = wish;
	}
	
	public boolean isWish() {
		return wish;
	}
}
