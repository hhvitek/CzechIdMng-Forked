package eu.bcvsolutions.idm.example.config.swagger;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Example module swagger configuration
 *
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "springdoc.swagger-ui", name = "enabled", matchIfMissing = true)
public class ExampleSwaggerConfig extends AbstractSwaggerConfig {

	@Autowired private ExampleModuleDescriptor moduleDescriptor;

	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	@Bean
	public GroupedOpenApi exampleApi() {
		return api("eu.bcvsolutions.idm.example.rest");
	}
}
