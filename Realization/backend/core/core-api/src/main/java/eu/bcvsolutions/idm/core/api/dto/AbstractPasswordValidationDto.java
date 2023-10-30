package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.api.domain.PasswordManageable;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;

/**
 * DTO for password validation.
 *
 * @author Jirka Koula
 */
public abstract class AbstractPasswordValidationDto<T extends AbstractDto & PasswordManageable> implements Serializable {

    private static final long serialVersionUID = 8063394882022081280L;
    //
    @NotNull
    @JsonDeserialize(using = GuardedStringDeserializer.class)
    private GuardedString password; // new password
    private UUID oldPassword; // old password identifier
    @JsonIgnore
    private boolean valid;

    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(GuardedString password) {
        this.password = password;
    }

    public void setPassword(String password) {
        this.password = new GuardedString(password);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public UUID getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(UUID oldPassword) {
        this.oldPassword = oldPassword;
    }

    public abstract T getEntity();
}
