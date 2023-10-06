package eu.bcvsolutions.idm.acc.entity;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Entity that store account password hashes.
 * Only password isn't audited.
 * 
 * @author Ondrej Kopr
 *
 */
@Entity
@Table(name = "acc_password", indexes = {
		@Index(name = "ux_acc_password_account", columnList = "account_id", unique = true)
})
public class AccPassword extends AbstractEntity implements ValidableEntity, AuditSearchable {

	private static final long serialVersionUID = -7574004980122208065L;
	@Column(name = "password")
	private String password;
	
	@Audited
	@OneToOne(optional = false)
	@JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccAccount account;
	
	@Audited
	@Column(name = "valid_till")
	private LocalDate validTill;
	
	@Audited
	@Column(name = "valid_from")
	private LocalDate validFrom;
	
	@Audited
	@Column(name = "must_change")
	private boolean mustChange = false;

	@Audited
	@Column(name = "last_successful_login")
	private ZonedDateTime lastSuccessfulLogin;

	@Audited
	@Column(name = "unsuccessful_attempts", nullable = false)
	private int unsuccessfulAttempts;

	@Audited
	@Column(name = "block_login_date")
	private ZonedDateTime blockLoginDate;

	@Audited
	@Column(name = "password_never_expires")
	private boolean passwordNeverExpires = false;
	
	@Column(name = "verification_secret")
	private String verificationSecret;

	public AccPassword() {
		// Auto-generated constructor
	}
	
	public ZonedDateTime getBlockLoginDate() {
		return blockLoginDate;
	}

	public void setBlockLoginDate(ZonedDateTime blockLoginDate) {
		this.blockLoginDate = blockLoginDate;
	}

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
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

	public AccAccount getAccount() {
		return account;
	}

	public void setAccount(AccAccount account) {
		this.account = account;
	}

	public boolean isMustChange() {
		return mustChange;
	}

	public void setMustChange(boolean mustChange) {
		this.mustChange = mustChange;
	}

	@Override
	public String getOwnerId() {
		return account.getId().toString();
	}

	@Override
	public String getOwnerCode() { return null; }

	@Override
	public String getOwnerType() {
		return AccAccount.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		return this.getId().toString();
	}

	@Override
	public String getSubOwnerCode() {
		return null;
	}

	@Override
	public String getSubOwnerType() {
		return AccPassword.class.getName();
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
