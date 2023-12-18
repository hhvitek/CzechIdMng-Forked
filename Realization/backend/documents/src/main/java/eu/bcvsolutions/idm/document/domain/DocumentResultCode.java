package eu.bcvsolutions.idm.document.domain;

import org.springframework.http.HttpStatus;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;

/**
 * Enum class for formatting response messages (mainly errors). 
 * Every enum contains a string message and corresponding https HttpStatus code.
 * 
 * Used http codes:
 * - 2xx - success
 * - 4xx - client errors (validations, conflicts ...)
 * - 5xx - server errors
 */
public enum DocumentResultCode implements ResultCode {

	DOCUMENT_VALIDATION_IDENTITY_NOT_EXISTS_ERROR(HttpStatus.BAD_REQUEST, "Given identity [%s] does not exist"),
	DOCUMENT_VALIDATION_IDENTITY_STATE_TYPE_ERROR(
			HttpStatus.BAD_REQUEST,
			"Given combination of identity [%s], state [%s] and type [%s] is invalid. You can invalidate already existing document id [%s]."
	);
	
	private final HttpStatus status;
	private final String message;
	
	DocumentResultCode(HttpStatus status, String message) {
		this.message = message;
		this.status = status;
	}
	
	public String getCode() {
		return this.name();
	}
	
	public String getModule() {
		return "documents";
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}
}
