package eu.bcvsolutions.idm.core.api.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

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
			// TODO: throw exception
			return null;
		}
		//
		String table = environment.getProperty(prefix + ".table");
		String location = null;
		try {
			location = environment.getProperty(prefix + ".locations");
		} catch (IllegalArgumentException e) {
			/**
			 * This is a workaroung for the issue with usign ${} in properties file. The PropertyResolver
			 * will try to resolve the ${} and will throw an exception if it can't find the value.
			 * But this value is is resolved much later.
			 */
			String[] messageSplit = e.getMessage().split("\"");
			if (messageSplit.length > 1) {
				location = messageSplit[1];
			}
		}
		Boolean baselineOnMigrate = environment.getProperty(prefix + ".baselineOnMigrate", Boolean.class);
		return flywayConfiguration.createFlyway(table, location, baselineOnMigrate);
	}

	public abstract String getPropertyPrefix();
}