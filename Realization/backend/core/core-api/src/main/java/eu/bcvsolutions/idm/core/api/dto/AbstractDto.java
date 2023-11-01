package eu.bcvsolutions.idm.core.api.dto;

import com.google.common.base.Strings;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.Serializable;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * Common dto.
 * 
 * @author Radek Tomiška 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractDto implements BaseDto, Auditable {
	
	private static final long serialVersionUID = 7512463222974374742L;
	public static final String PROPERTY_DTO_TYPE = "_dtotype";
	//
	@JsonDeserialize(as = UUID.class)
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Unique uuid identifier. Used as identifier in rest endpoints", type = "java.util.UUID")
	private UUID id;
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private ZonedDateTime created;
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private ZonedDateTime modified;
	@Size(max = DefaultFieldLengths.NAME)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String creator;
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID creatorId;
	@Size(max = DefaultFieldLengths.NAME)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String modifier;
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID modifierId;
	@Size(max = DefaultFieldLengths.NAME)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String originalCreator;
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID originalCreatorId;
	@Size(max = DefaultFieldLengths.NAME)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String originalModifier;
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID originalModifierId;
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID transactionId;
	@JsonIgnore
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID realmId;
	//
	@JsonProperty(value = "_trimmed", access = Access.READ_ONLY)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private boolean trimmed = false;
	@JsonProperty(value = EmbeddedDto.PROPERTY_EMBEDDED, access = Access.READ_ONLY)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Map<String, BaseDto> embedded;
	@JsonProperty(value = PROPERTY_DTO_TYPE, access = Access.READ_ONLY)
	private Class<? extends BaseDto> type = this.getClass();
	@JsonProperty(value = "_permissions", access = Access.READ_ONLY)
	@Schema(
			accessMode = Schema.AccessMode.READ_ONLY,
			description = "What currently logged identity can do with given dto.")
	private Set<String> permissions;

	public AbstractDto() {
	}

	public AbstractDto(UUID id) {
		this.id = id;
	}
	
	public AbstractDto(Auditable auditable) {
		Assert.notNull(auditable, "Auditable (dto or entity) is required");
		//
		DtoUtils.copyAuditFields(auditable, this);
		this.id = auditable.getId();
		this.realmId = auditable.getRealmId();
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		try {
			this.id = EntityUtils.toUuid(id);
		} catch (ClassCastException ex) {
			throw new IllegalArgumentException("AbstractDto supports only UUID identifier. For different identifier generalize BaseEntity.", ex);
		}
	}

	@Override
	public ZonedDateTime getCreated() {
		return created;
	}

	@Override
	public void setCreated(ZonedDateTime created) {
		this.created = created;
	}
	
	@Override
	public ZonedDateTime getModified() {
		return modified;
	}

	@Override
	public void setModified(ZonedDateTime modified) {
		this.modified = modified;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public String getModifier() {
		return modifier;
	}

	@Override
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	@Override
	public String getOriginalCreator() {
		return originalCreator;
	}

	@Override
	public void setOriginalCreator(String originalCreator) {
		this.originalCreator = originalCreator;
	}

	@Override
	public String getOriginalModifier() {
		return originalModifier;
	}

	@Override
	public void setOriginalModifier(String originalModifier) {
		this.originalModifier = originalModifier;
	}
	
	@Override
	public UUID getCreatorId() {
		return creatorId;
	}

	@Override
	public void setCreatorId(UUID creatorId) {
		this.creatorId = creatorId;
	}

	@Override
	public UUID getOriginalCreatorId() {
		return originalCreatorId;
	}

	@Override
	public void setOriginalCreatorId(UUID originalCreatorId) {
		this.originalCreatorId = originalCreatorId;
	}

	@Override
	public UUID getModifierId() {
		return modifierId;
	}

	@Override
	public void setModifierId(UUID modifierId) {
		this.modifierId = modifierId;
	}

	@Override
	public UUID getOriginalModifierId() {
		return originalModifierId;
	}

	@Override
	public void setOriginalModifierId(UUID originalModifierId) {
		this.originalModifierId = originalModifierId;
	}
	
	@Override
	public UUID getTransactionId() {
		return transactionId;
	}
	
	@Override
	public void setTransactionId(UUID transactionId) {
		this.transactionId = transactionId;	
	}
	
	@Override
	public UUID getRealmId() {
		return realmId;
	}
	
	@Override
	public void setRealmId(UUID realmId) {
		this.realmId = realmId;
	}
	
	public boolean isTrimmed() {
		return trimmed;
	}

	public void setTrimmed(boolean trimmed) {
		this.trimmed = trimmed;
	}	

	public Map<String, BaseDto> getEmbedded() {
		if(embedded == null){
			embedded = new HashMap<>();
		}
		return embedded;
	}

	public void setEmbedded(Map<String, BaseDto> embedded) {
		this.embedded = embedded;
	}
	
	/**
	 * What currently logged identity can do with given dto.
	 * Returns {@code null} when permissions were not loaded.
	 * 
	 * @return
	 * @since 10.2.0
	 */
	public Set<String> getPermissions() {
		return permissions;
	}
	
	/**
	 * What currently logged identity can do with given dto.
	 * 
	 * @param permissions
	 * @since 10.2.0
	 */
	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	@Override
	public String toString() {
		if (this instanceof Codeable) {
			String code = ((Codeable) this).getCode();
			if (!Strings.isNullOrEmpty(code)) {
				// TODO: e.g. "username" in logs can be dangerous ...
				return MessageFormat.format("{0} [code={1}]", getClass().getCanonicalName(), code);
			}
		}
		return MessageFormat.format("{0} [id={1}]", getClass().getCanonicalName(), getId());
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof AbstractDto)) {
			return false;
		}
		AbstractDto that = (AbstractDto) o;
		
		EqualsBuilder builder = new EqualsBuilder();
		
		if (id == null && that.id == null) {
			builder.appendSuper(super.equals(o));
		}
		return builder
				.append(id, that.id)
				.isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				 .append(id)
				 .toHashCode();
	}
	
	/**
	 * DTO are serialized in WF and embedded objects.
	 * We need to solve legacy issues with joda (old) vs. java time (new) usage.
	 * 
	 * @param ois
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream ois) throws Exception {
		GetField readFields = ois.readFields();
		//
		id = (UUID) readFields.get("id", null);
		created = DtoUtils.toZonedDateTime(readFields.get("created", null));
		modified = DtoUtils.toZonedDateTime(readFields.get("modified", null));
		creator = (String) readFields.get("creator", null);
		creatorId = (UUID) readFields.get("creatorId", null);
		modifier = (String) readFields.get("modifier", null);
		modifierId = (UUID) readFields.get("modifierId", null);
		originalCreator = (String) readFields.get("originalCreator", null);
		originalCreatorId = (UUID) readFields.get("originalCreatorId", null);
		originalModifier = (String) readFields.get("originalModifier", null);
		originalModifierId = (UUID) readFields.get("originalModifierId", null);
		trimmed = readFields.get("trimmed", false);
		embedded = (Map<String, BaseDto>) readFields.get("embedded", null);
		transactionId = (UUID) readFields.get("transactionId", null);
		realmId = (UUID) readFields.get("realmId", null);
    }
}
