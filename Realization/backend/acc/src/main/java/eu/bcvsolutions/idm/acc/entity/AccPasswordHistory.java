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

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Password history determines the number of unique new passwords
 * This entity isn't audited, Entity itself is an audit.
 *
 * @author Jirka Koula
 *
 */

@Entity
@Table(name = "acc_password_history", indexes = {
		@Index(name = "idx_acc_password_account", columnList = "account_id")
		})
public class AccPasswordHistory extends AbstractEntity {

	private static final long serialVersionUID = -210515213563491735L;
	@NotEmpty
	@Column(name = "password", nullable = false)
	private String password;

	@ManyToOne(optional = false)
	@JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccAccount account;

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

}
