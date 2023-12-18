package eu.bcvsolutions.idm.document.dto.filter;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.rest.AbstractPluggableFilterTranslator;
import eu.bcvsolutions.idm.core.api.service.LookupService;

@Component("documentPluggableFilterTranslator")
public class DocumentPluggableFilterTranslator extends AbstractPluggableFilterTranslator<DocumentFilter> {
	protected DocumentPluggableFilterTranslator(LookupService lookupService, ObjectMapper objectMapper) {
		super(lookupService, objectMapper);
	}

	@Override
	protected DocumentFilter transformInternal(DocumentFilter filter, MultiValueMap<String, Object> parameters) {
		return new DocumentFilter(parameters, getParameterConverter());
	}

	@Override
	protected DocumentFilter getEmptyFilter() {
		return new DocumentFilter();
	}
}
