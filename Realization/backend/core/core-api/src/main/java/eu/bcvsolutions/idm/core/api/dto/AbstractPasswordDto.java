package eu.bcvsolutions.idm.core.api.dto;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Common password dto.
 * 
 * @author Jirka Koula
 *
 */
@Relation(collectionRelation = "passwords")
public abstract class AbstractPasswordDto extends AbstractDto implements ValidableEntity  {

	private static final long serialVersionUID = 8922688931648790561L;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;
    private LocalDate validTill;
    private LocalDate validFrom;
    private boolean mustChange = false;
    private ZonedDateTime lastSuccessfulLogin;
    private int unsuccessfulAttempts;
    private ZonedDateTime blockLoginDate = null;
    private boolean passwordNeverExpires = false;
    @JsonProperty(access = Access.WRITE_ONLY)
	private String verificationSecret;

    public ZonedDateTime getLastSuccessfulLogin() {
        return lastSuccessfulLogin;
    }

    public void setLastSuccessfulLogin(ZonedDateTime lastSuccessfulLogin) {
        this.lastSuccessfulLogin = lastSuccessfulLogin;
    }

    public int getUnsuccessfulAttempts() {
        return unsuccessfulAttempts;
    }

    public void setUnsuccessfulAttempts(int unsuccessfulAttempts) {
        this.unsuccessfulAttempts = unsuccessfulAttempts;
    }

    public void increaseUnsuccessfulAttempts() {
        this.unsuccessfulAttempts++;
    }

    public void resetUnsuccessfulAttempts() {
        unsuccessfulAttempts = 0;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public abstract UUID getEntity();

    public abstract void setEntity(UUID entity);

    @Override
    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    @Override
    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public boolean isMustChange() {
        return mustChange;
    }

    public void setMustChange(boolean mustChange) {
        this.mustChange = mustChange;
    }

	public ZonedDateTime getBlockLoginDate() {
		return blockLoginDate;
	}

	public void setBlockLoginDate(ZonedDateTime blockLoginDate) {
		this.blockLoginDate = blockLoginDate;
	}

	public boolean isPasswordNeverExpires() {
		return passwordNeverExpires;
	}

	public void setPasswordNeverExpires(boolean passwordNeverExpires) {
		this.passwordNeverExpires = passwordNeverExpires;
	}
	
	/**
	 * Two factor authentication secret.
	 * 
	 * @param verificationSecret secret
	 */
	public void setVerificationSecret(String verificationSecret) {
		this.verificationSecret = verificationSecret;
	}
	
	/**
	 * Two factor authentication secret.
	 * 
	 * @return secret
	 */
	public String getVerificationSecret() {
		return verificationSecret;
	}
}
