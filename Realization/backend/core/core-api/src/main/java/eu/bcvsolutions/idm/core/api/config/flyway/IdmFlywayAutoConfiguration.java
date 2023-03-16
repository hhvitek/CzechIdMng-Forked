package eu.bcvsolutions.idm.core.api.config.flyway;

import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;

import eu.bcvsolutions.idm.core.api.config.datasource.DatasourceConfig;

/**
 * FlywayAutoConfiguration extension added support for multi modular {@link Flyway} configuration.
 * 
 * @author Radek Tomiška
 */
@SuppressWarnings("deprecation") // third party warning
@ConditionalOnClass(Flyway.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({ DatasourceConfig.class, HibernateJpaAutoConfiguration.class })
public class IdmFlywayAutoConfiguration extends FlywayAutoConfiguration {
	
	/**
	 * Support for multi modular {@link Flyway} configuration
	 * 
	 * @author Radek Tomiška
	 */
	@Configuration
	@Import({DatasourceConfig.class})
	@EnableConfigurationProperties({ DataSourceProperties.class, FlywayProperties.class })
	public static class IdmFlywayConfiguration extends FlywayAutoConfiguration.FlywayConfiguration {

		private FlywayProperties properties;
		private DataSourceProperties dataSourceProperties;
		private ResourceLoader resourceLoader;
		private ObjectProvider<DataSource> dataSource;
		private ObjectProvider<DataSource> flywayDataSource;
		private ObjectProvider<FlywayConfigurationCustomizer> fluentConfigurationCustomizers;
		private ObjectProvider<JavaMigration> javaMigrations;
		private ObjectProvider<Callback> callbacks;
		public IdmFlywayConfiguration(
				FlywayProperties properties,
				DataSourceProperties dataSourceProperties,
				ResourceLoader resourceLoader,
				ObjectProvider<DataSource> dataSource,
				@FlywayDataSource ObjectProvider<DataSource> flywayDataSource,
				ObjectProvider<FlywayConfigurationCustomizer> fluentConfigurationCustomizers,
				ObjectProvider<JavaMigration> javaMigrations,
				ObjectProvider<Callback> callbacks) {
			super();
			this.properties = properties;
			this.dataSourceProperties = dataSourceProperties;
			this.resourceLoader = resourceLoader;
			this.dataSource = dataSource;
			this.flywayDataSource = flywayDataSource;
			this.fluentConfigurationCustomizers = fluentConfigurationCustomizers;
			this.javaMigrations = javaMigrations;
			this.callbacks = callbacks;
		}
		
		private static FlywayProperties addProperty(FlywayProperties properties, String table,
													String location, boolean baselineOnMigrate) {
			properties.setCheckLocation(false);
			properties.setTable(table);
			properties.setLocations(List.of(location));
			properties.setBaselineOnMigrate(baselineOnMigrate);
			//
			return properties;
		}
		
		/**
		 * Creates module dependent {@link Flyway} configuration.
         *
		 * @param table
		 * @param location
		 * @param baselineOnMigrate
		 * @return
		 */
		public Flyway createFlyway(String table, String location, boolean baselineOnMigrate) {
			return super.flyway(addProperty(properties, table, location, baselineOnMigrate),
					dataSourceProperties, resourceLoader,
					dataSource, flywayDataSource, fluentConfigurationCustomizers, javaMigrations, callbacks);
		}
	}
}
