package eu.bcvsolutions.idm.document.entity;

import java.util.UUID;

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
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.domain.DocumentType;

@Entity
@Table(name = "document", indexes = {
		@Index(name = "document_document_unique_uuid", columnList = "uuid", unique = true)
})
public class DocumentEntity extends AbstractEntity {
	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name = "uuid", nullable = false)
	private UUID uuid;
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = DefaultFieldLengths.ENUMARATION)
	private DocumentType type;
	@NotNull
	@Positive
	@Column(name = "number", nullable = false)
	private Integer number;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "first_name", length = DefaultFieldLengths.NAME, nullable = false)
	private String firstName;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "last_name", length = DefaultFieldLengths.NAME, nullable = false)
	private String lastName;
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "state", nullable = false, length = DefaultFieldLengths.ENUMARATION)
	private DocumentState state;
	@NotNull
	@ManyToOne(optional = false) // annotations cloned from IdmIdentity class
	@JoinColumn(name = "identity_id", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentity identity;

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public DocumentType getType() {
		return type;
	}

	public void setType(DocumentType type) {
		this.type = type;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public DocumentState getState() {
		return state;
	}

	public void setState(DocumentState state) {
		this.state = state;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}
}
