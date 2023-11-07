package eu.bcvsolutions.idm.core.security.api.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import eu.bcvsolutions.idm.core.security.api.domain.TwoFactorAuthenticationType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Two factor registration confirmation.
 * 
 * @author Radek Tomiška
 * @since 10.7.0
 */
public class TwoFactorRegistrationConfirmDto {

	@NotNull
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Two factor verification secret.")
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private GuardedString verificationSecret;
	@NotNull
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Two factor authentication verify 6-digit code.", example = "123456")
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	private GuardedString verificationCode;
	@NotNull
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Two factor authentication type.", example = "APPLICATION")
	private TwoFactorAuthenticationType twoFactorAuthenticationType;
	
	public GuardedString getVerificationSecret() {
		return verificationSecret;
	}
	
	public void setVerificationSecret(GuardedString verificationSecret) {
		this.verificationSecret = verificationSecret;
	}
	
	public void setVerificationCode(GuardedString verificationCode) {
		this.verificationCode = verificationCode;
	}
	
	public GuardedString getVerificationCode() {
		return verificationCode;
	}
	
	public TwoFactorAuthenticationType getTwoFactorAuthenticationType() {
		return twoFactorAuthenticationType;
	}
	
	public void setTwoFactorAuthenticationType(TwoFactorAuthenticationType twoFactorAuthenticationType) {
		this.twoFactorAuthenticationType = twoFactorAuthenticationType;
	}
}
