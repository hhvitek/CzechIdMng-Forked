package eu.bcvsolutions.idm.document.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.document.dto.DocumentDto;

/**
 * Filter for example products
 *
 */
public class DocumentFilter extends DataFilter {

	public static final String PARAMETER_UUID = "uuid";
	
	public DocumentFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public DocumentFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public DocumentFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(DocumentDto.class, data, parameterConverter);
	}

	public UUID getUuid() {
		return getParameterConverter().toUuid(getData(), PARAMETER_UUID);
	}
	
	public void setUuid(UUID uuid) {
		set(PARAMETER_UUID, uuid);
	}
}
