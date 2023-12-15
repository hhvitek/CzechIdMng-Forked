package eu.bcvsolutions.idm.document.config.swagger;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.document.DocumentModuleDescriptor;

/**
 * Document module swagger configuration - cloned from example module
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "springdoc.swagger-ui", name = "enabled", matchIfMissing = true)
public class DocumentSwaggerConfig extends AbstractSwaggerConfig {

	@Autowired private DocumentModuleDescriptor moduleDescriptor;

	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	@Bean
	public GroupedOpenApi documentApi() {
		return api("eu.bcvsolutions.idm.document.rest");
	}
}
