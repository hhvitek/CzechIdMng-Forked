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
	
	DOCUMENT_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "DOCUMENT client error, bad value given [%s]"),
	DOCUMENT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DOCUMENT server error with parameter [%s]");
	
	private final HttpStatus status;
	private final String message;
	
	private DocumentResultCode(HttpStatus status, String message) {
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
