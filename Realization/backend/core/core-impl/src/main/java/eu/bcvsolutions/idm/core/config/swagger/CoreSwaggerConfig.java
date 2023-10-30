package eu.bcvsolutions.idm.core.config.swagger;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
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
public class CoreSwaggerConfig extends AbstractSwaggerConfig {
	
	@Autowired private CoreModuleDescriptor moduleDescriptor;
	
	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}
	
	@Bean
	public GroupedOpenApi coreApi() {
		return api(
				"eu.bcvsolutions.idm.core.rest",
				"eu.bcvsolutions.idm.core.security",
				"eu.bcvsolutions.idm.core.scheduler",
				"eu.bcvsolutions.idm.core.eav",
				"eu.bcvsolutions.idm.core.ecm",
				"eu.bcvsolutions.idm.core.monitoring",
				"eu.bcvsolutions.idm.core.notification",
				"eu.bcvsolutions.idm.core.workflow",
				"eu.bcvsolutions.idm.core.audit");
	}
}