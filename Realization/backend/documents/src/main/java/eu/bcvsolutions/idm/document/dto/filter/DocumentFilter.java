package eu.bcvsolutions.idm.document.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.document.domain.DocumentState;
import eu.bcvsolutions.idm.document.domain.DocumentType;
import eu.bcvsolutions.idm.document.dto.DocumentDto;

/**
 * Filter for documents
 *
 */
public class DocumentFilter extends DataFilter {

	public static final String PARAMETER_IDENTITY = "identity";
	public static final String PARAMETER_STATE = "state";
	public static final String PARAMETER_TYPE = "type";
	
	public DocumentFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public DocumentFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public DocumentFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(DocumentDto.class, data, parameterConverter);
	}

	public UUID getIdentity() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY);
	}
	
	public void setIdentity(UUID identity) {
		set(PARAMETER_IDENTITY, identity);
	}

	public DocumentState getState() {
		return getParameterConverter().toEnum(getData(), PARAMETER_STATE, DocumentState.class);
	}

	public void setState(DocumentState state) {
		set(PARAMETER_STATE, state);
	}

	public DocumentType getType() {
		return getParameterConverter().toEnum(getData(), PARAMETER_TYPE, DocumentType.class);
	}

	public void setType(DocumentType type) {
		set(PARAMETER_TYPE, type);
	}
}
