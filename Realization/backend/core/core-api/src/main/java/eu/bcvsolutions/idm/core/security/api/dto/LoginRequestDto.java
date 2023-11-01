package eu.bcvsolutions.idm.core.security.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedStringDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Identity login request.
 * 
 * @author Radek Tomiška
 *
 */
public class LoginRequestDto {

	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identity username.", example = "admin")
	private String username;
	@JsonProperty(access = Access.WRITE_ONLY)
	@JsonDeserialize(using = GuardedStringDeserializer.class)
	@Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Identity password.", type = "java.lang.String", example = "admin")
	private GuardedString password;
	
	public LoginRequestDto() {
	}
	
	public LoginRequestDto(String username, GuardedString password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
