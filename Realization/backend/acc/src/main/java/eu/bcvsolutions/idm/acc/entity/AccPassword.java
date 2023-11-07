package eu.bcvsolutions.idm.acc.entity;

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

/**
 * Entity that store account password hashes.
 * Only password isn't audited.
 * 
 * @author Jirka Koula
 *
 */
@Entity
@Table(name = "acc_password", indexes = {
		@Index(name = "ux_acc_password_account", columnList = "account_id", unique = true)
})
public class AccPassword extends AbstractEntity implements AuditSearchable {

	private static final long serialVersionUID = -7574004980122208065L;
	@Column(name = "password")
	private String password;
	
	@Audited
	@OneToOne(optional = false)
	@JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccAccount account;

	public AccPassword() {
		// Auto-generated constructor
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AccAccount getAccount() {
		return account;
	}

	public void setAccount(AccAccount account) {
		this.account = account;
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

}
