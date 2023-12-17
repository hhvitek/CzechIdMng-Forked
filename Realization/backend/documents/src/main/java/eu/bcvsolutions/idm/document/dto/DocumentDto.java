package eu.bcvsolutions.idm.document.dto;

import java.util.UUID;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import org.springframework.hateoas.server.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.domain.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Document dto
 *
 */
@Relation(collectionRelation = "documents")
@Schema(description = "Document")
public class DocumentDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	private UUID uuid;
	@NotNull
	private DocumentType type;
	@NotNull
	@Positive
	private Integer number;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String firstName;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String lastName;
	@NotNull
	private DocumentState state;
	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity; // entity and dto attributes must have exact same name

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

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}
}
