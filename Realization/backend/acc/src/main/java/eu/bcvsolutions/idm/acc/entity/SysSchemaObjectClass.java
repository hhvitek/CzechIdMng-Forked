package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import javax.validation.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * This entity keeps relation on object class in resource
 * 
 * @author svandav
 * @author Tomáš Doischer
 *
 */
@Entity
@Table(name = "sys_schema_obj_class", indexes = {
		@Index(name = "ux_schema_class_name_sys", columnList = "object_class_name,system_id", unique = true)})
public class SysSchemaObjectClass extends AbstractEntity implements FormableEntity {

	private static final long serialVersionUID = -3578470144453764063L;

	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "object_class_name", length = DefaultFieldLengths.NAME, nullable = false)
	private String objectClassName;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystem system;

	@Audited
	@Column(name = "container", nullable = false)
	private boolean container = false;

	@Audited
	@Column(name = "auxiliary", nullable = false)
	private boolean auxiliary = false;

	public String getObjectClassName() {
		return objectClassName;
	}

	public void setObjectClassName(String objectClassName) {
		this.objectClassName = objectClassName;
	}

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	/**
	 * True if this can contain other object classes.
	 */
	public boolean isContainer() {
		return container;
	}

	public void setContainer(boolean container) {
		this.container = container;
	}

	/**
	 * Returns flag indicating whether this is a definition of auxiliary object
	 * class. Auxiliary object classes define additional characteristics of the
	 * object.
	 */
	public boolean isAuxiliary() {
		return auxiliary;
	}

	public void setAuxiliary(boolean auxiliary) {
		this.auxiliary = auxiliary;
	}
}
