package eu.bcvsolutions.idm.core.api.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySourcesPropertyResolver;

/**
 * Module dependent {@link Flyway} configuration.
 * 
 * @author Radek Tomiška
 */
public abstract class AbstractFlywayConfiguration {

	@Autowired
	IdmFlywayAutoConfiguration.IdmFlywayConfiguration flywayConfiguration;
	@Autowired
	Environment environment;

	/**
	 * Returns {@link Flyway} configured to specific module
	 * 
	 * @return
	 */
	public Flyway createFlyway() {
		String prefix = getPropertyPrefix();
		if (prefix == null) {
			throw new IllegalArgumentException("Property prefix is required!");
		}
		//
		String table = getPropertyResolver().getProperty(prefix + ".table");
		String location = getPropertyResolver().getProperty(prefix + ".locations");
		Boolean baselineOnMigrate = getPropertyResolver().getProperty(prefix + ".baselineOnMigrate", Boolean.class, Boolean.FALSE);
		return flywayConfiguration.createFlyway(table, location, baselineOnMigrate);
	}

	public abstract String getPropertyPrefix();

	private PropertyResolver getPropertyResolver() {
		if (this.environment instanceof ConfigurableEnvironment) {
			PropertySourcesPropertyResolver resolver = new PropertySourcesPropertyResolver(
					((ConfigurableEnvironment) this.environment).getPropertySources());
			resolver.setIgnoreUnresolvableNestedPlaceholders(true);
			return resolver;
		}
		return this.environment;
	}
}