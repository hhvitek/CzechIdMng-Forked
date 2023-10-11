package eu.bcvsolutions.idm.core.api.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * {@link FlywayMigrationStrategy} itself can't be used for modular {@link Flyway} configuration. 
 * We need to use {@link FlywayMigrationStrategy} directly after module dependent {@link Flyway} is created.
 *
 * @since 12.2.x this component is {@link ApplicationContextAware} instead of {@link org.springframework.beans.factory.config.BeanPostProcessor}.
 * The reason is that due to some circular dependencies on {@link eu.bcvsolutions.idm.core.api.config.datasource.DatasourceConfig}, {@link Flyway}
 * beans could not be processed.
 *
 * This solution also uses {@link IdmFlywayComparator}, which enables us to specify order in which migrations will be executed.
 *
 * @since 13.1.0 Migration is executed in post construct method. This ensures that all migrations will be executed prior to starting application. Mainly
 * before {@link eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration}.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 * @author Radek Tomiška
 */
@Component(IdmFlywayPostProcessor.NAME)
public class IdmFlywayPostProcessor {
	
	public static final String NAME = "flywayPostProcessor";
	
	@Autowired
	private FlywayMigrationStrategy flywayMigrationStrategy;

	@Autowired
	IdmFlywayComparator flywayComparator;

	@Autowired
	List<AbstractFlywayConfiguration> flyways;


	private void executeMigration(Flyway flyway) {
		flywayMigrationStrategy.migrate(flyway);
	}

	@PostConstruct
	public void onApplicationEvent() {
		flyways.stream().map(AbstractFlywayConfiguration::createFlyway).sorted(flywayComparator).forEach(this::executeMigration);
	}
}
