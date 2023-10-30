package eu.bcvsolutions.idm.acc.config.swagger;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;

/**
 * Core module swagger configuration
 *
 * @author Radek Tomiška
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class AccSwaggerConfig extends AbstractSwaggerConfig {

	@Autowired private AccModuleDescriptor moduleDescriptor;

	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	@Bean
	public GroupedOpenApi accApi() {
		return api("eu.bcvsolutions.idm.acc");
	}
}
