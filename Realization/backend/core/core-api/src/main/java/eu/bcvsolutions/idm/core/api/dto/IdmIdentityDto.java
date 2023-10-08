package eu.bcvsolutions.idm.core.api.dto;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Contextable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalCodeable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.PasswordManageable;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Dto for identity.
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "identities")
@ApiModel(description = "Identity domain object")
public class IdmIdentityDto extends FormableDto implements Disableable, Codeable, ExternalCodeable, ExternalIdentifiable, Contextable, PasswordManageable {

	private static final long serialVersionUID = 1L;
	//
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Unique identity username. Could be used as identifier in rest endpoints")
	private String username;	
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "External code.")
	private String externalCode;
	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private transient GuardedString password;	
	@Size(max = DefaultFieldLengths.NAME)
	private String firstName;
	@Size(max = DefaultFieldLengths.NAME)
	private String lastName;
	@Email
	@Size(max = DefaultFieldLengths.EMAIL_ADDRESS)
	@ApiModelProperty(notes = "Email", dataType = "email")
	private String email;
	@Size(max = 30)
	@ApiModelProperty(notes = "Phone")
	private String phone;
	@Size(max = 100)
	private String titleBefore;
	@Size(max = 100)
	private String titleAfter;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@JsonProperty(access = Access.READ_ONLY)
	@Deprecated // since 7.6.0. - use state property
	private boolean disabled;
	@JsonProperty(access = Access.READ_ONLY)
	private IdentityState state;
	private transient ZonedDateTime blockLoginDate = null;
	@JsonIgnore
	private Map<String, Object> context = null;
	@ApiModelProperty(notes = "Projection - entity will be created / edited by given form.")
	@Embedded(dtoClass = IdmFormProjectionDto.class)
	private UUID formProjection;
	/**
	 * Additional information about identity password (validity etc.).
	 * 
	 * @since 11.0.0
	 */
	@JsonProperty(access = Access.READ_ONLY)
	private transient IdmPasswordDto passwordMetadata;

	public IdmIdentityDto() {
	}
	
	public IdmIdentityDto(UUID id) {
		super(id);
	}
	
	public IdmIdentityDto(String username) {
		this.username = username;
	}
	
	public IdmIdentityDto(Auditable auditable) {
		super(auditable);
	}
	
	public IdmIdentityDto(UUID id, String username) {
		super(id);
		this.username = username;
	}
	
	public IdmIdentityDto(Auditable auditable, String username) {
		super(auditable);
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	@Override
	@JsonIgnore
	public String getCode() {
		return getUsername();
	}
	
	public void setUsername(String username) {
		this.username = username;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTitleBefore() {
		return titleBefore;
	}

	public void setTitleBefore(String titleBefore) {
		this.titleBefore = titleBefore;
	}

	public String getTitleAfter() {
		return titleAfter;
	}

	public void setTitleAfter(String titleAfter) {
		this.titleAfter = titleAfter;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDisabled() {
		return state == null ? disabled : state.isDisabled();
	}

	/**
	 * Sets state to {@link IdentityState#DISABLED} (true given) or {@code null} (true given)
	 * 
	 * @deprecated since 7.6.0  - use {@link #setState(IdentityState)} directly
	 */
	@Override
	@Deprecated
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		if (disabled) {
			state = IdentityState.DISABLED;
		}  else {
			// state will be evaluated, when identity is saved again
			state = null;
		}
	}
	
	public void setPassword(GuardedString password) {
		this.password = password;
	}
	
	public GuardedString getPassword() {
		return password;
	}
	
	public IdentityState getState() {
		return state;
	}
	
	public void setState(IdentityState state) {
		this.state = state;
	}

	public ZonedDateTime getBlockLoginDate() {
		return blockLoginDate;
	}

	public void setBlockLoginDate(ZonedDateTime blockLoginDate) {
		this.blockLoginDate = blockLoginDate;
	}

	@Override
	public String getExternalCode() {
		return externalCode;
	}

	@Override
	public void setExternalCode(String externalCode) {
		this.externalCode = externalCode;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@JsonIgnore
	public Map<String, Object> getContext() {
		return context;
	}

	@JsonIgnore
	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
	
	/**
	 * Entity will be created / edited by form projection.
	 * 
	 * @return defined projection
	 * @since 10.2.0
	 */
	public UUID getFormProjection() {
		return formProjection;
	}
	
	/**
	 * Entity will be created / edited by form projection.
	 * 
	 * @param formProjection projection
	 * @since 10.2.0
	 */
	public void setFormProjection(UUID formProjection) {
		this.formProjection = formProjection;
	}
	
	/**
	 * Additional information about identity password.
	 * 
	 * @return password metadata (without secrets)
	 * @since 11.0.0
	 */
	public IdmPasswordDto getPasswordMetadata() {
		return passwordMetadata;
	}
	
	/**
	 * Additional information about identity password.
	 * 
	 * @param passwordMetadata password metadata (without secrets)
	 * @since 11.0.0
	 */
	public void setPasswordMetadata(IdmPasswordDto passwordMetadata) {
		this.passwordMetadata = passwordMetadata;
	}

	@Override
	public String toString() {
		return "IdmIdentityDto [username=" + username + "]";
	}

	@Override
	public String getName() {
		return getUsername();
	}
}
