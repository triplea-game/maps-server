package org.triplea.server;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.triplea.maps.MapsController;
import org.triplea.maps.indexing.MapsIndexingObjectFactory;

/**
 * Main entry-point for launching drop wizard HTTP server. This class is responsible for configuring
 * any Jersey plugins, registering resources (controllers) and injecting those resources with
 * configuration properties from 'AppConfig'.
 */
@Slf4j
public class SupportServerApplication extends Application<SupportServerConfig> {

  private static final String[] DEFAULT_ARGS = new String[] {"server", "configuration.yml"};

  /**
   * Main entry-point method, launches the drop-wizard http server. If no args are passed then will
   * use default values suitable for local development.
   */
  public static void main(final String[] args) throws Exception {
    final SupportServerApplication application = new SupportServerApplication();
    // if no args are provided then we will use default values.
    application.run(args.length == 0 ? DEFAULT_ARGS : args);
  }

  @Override
  public void initialize(final Bootstrap<SupportServerConfig> bootstrap) {
    // enable environment variables in configuration.yml
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    // Better JDBI exceptions
    bootstrap.addBundle(new JdbiExceptionsBundle());
  }

  @Override
  public void run(final SupportServerConfig configuration, final Environment environment) {
    final Jdbi jdbi =
        new JdbiFactory()
            .build(environment, configuration.getDatabase(), "postgresql-connection-pool");

    if (configuration.isMapIndexingEnabled()) {
      environment
          .lifecycle()
          .manage(MapsIndexingObjectFactory.buildMapsIndexingSchedule(configuration, jdbi));
      log.info(
          "Map indexing is enabled to run every:"
              + " {} minutes with one map indexing request every {} seconds",
          configuration.getMapIndexingPeriodMinutes(),
          configuration.getIndexingTaskDelaySeconds());
    } else {
      log.info("Map indexing is disabled");
    }
    environment.jersey().register(MapsController.build(jdbi));
  }
}
