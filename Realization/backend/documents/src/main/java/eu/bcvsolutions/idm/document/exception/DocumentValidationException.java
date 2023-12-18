package eu.bcvsolutions.idm.document.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Can be invoked when additional business logic validations are made.
 */
public class DocumentValidationException extends ResultCodeException {
	private static final long serialVersionUID = 1L;

	public DocumentValidationException(ResultCode resultCode, Map<String, Object> parameters) {
		super(resultCode, parameters);
	}
}
