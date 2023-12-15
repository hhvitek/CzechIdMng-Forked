package eu.bcvsolutions.idm.document.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Document configuration - cloned from tool config module
 *
 */
public interface DocumentConfiguration extends Configurable {
	
	@Override
	default String getConfigurableType() {
		return "configuration";
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		return properties;
	}
}
