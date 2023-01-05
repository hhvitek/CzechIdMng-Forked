package eu.bcvsolutions.idm.acc.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;

/**
 * Relation between an account and definition of form-attribution. It is an elementary part
 * of the account form "sub-definition".
 * 
 * @author Tomáš Doischer
 *
 */
@Entity
@Audited
@Table(name = "acc_schema_form_attribute", indexes = {
		@Index(name = "idx_acc_schema_form_att_def", columnList = "attribute_id"),
		@Index(name = "idx_acc_schema_form_acc", columnList = "schema_id"),
		@Index(name = "ux_acc_schema_form_att_s_a", columnList = "attribute_id, schema_id", unique = true) })
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AccSchemaFormAttribute extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(optional = false)
	@JoinColumn(name = "attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmFormAttribute formAttribute;

	@ManyToOne(optional = false)
	@JoinColumn(name = "schema_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSchemaObjectClass schema;

	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "default_value", nullable = true)
	private String defaultValue;
	
	@NotNull
	@Column(name = "required", nullable = false)
	private boolean required;
	
	@NotNull
	@Column(name = "validation_unique", nullable = false)
	private boolean unique;
	
	@Column(name = "validation_max", nullable = true, precision = 38, scale = 4)
	private BigDecimal max;
	
	@Column(name = "validation_min", nullable = true, precision = 38, scale = 4)
	private BigDecimal min;
	
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "validation_regex", nullable = true, length = DefaultFieldLengths.DESCRIPTION)
	private String regex;
	
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "validation_message", nullable = true, length = DefaultFieldLengths.DESCRIPTION)
	private String validationMessage;

	public IdmFormAttribute getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(IdmFormAttribute formAttribute) {
		this.formAttribute = formAttribute;
	}

	public SysSchemaObjectClass getSchema() {
		return schema;
	}

	public void setSchema(SysSchemaObjectClass schema) {
		this.schema = schema;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(BigDecimal max) {
		this.max = max;
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		this.min = min;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}
	
	/**
	 * Custom message, when validation fails - localization key can be used.
	 * 
	 * @return
	 */
	public String getValidationMessage() {
		return validationMessage;
	}
	
	/**
	 * Custom message, when validation fails - localization key can be used.
	 * 
	 * @param validationMessage
	 */
	public void setValidationMessage(String validationMessage) {
		this.validationMessage = validationMessage;
	}
}
