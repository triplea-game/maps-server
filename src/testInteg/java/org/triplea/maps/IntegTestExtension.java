package org.triplea.maps;

import com.github.database.rider.core.configuration.GlobalConfig;
import com.github.database.rider.junit5.DBUnitExtension;
import com.google.common.base.Preconditions;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 *
 *
 * <pre>
 * Can inject into tests:
 * (1) a "jdbi"
 * (2) a URI that is the URI of the server.
 * </pre>
 */
@ExtendWith(DBUnitExtension.class)
public class IntegTestExtension
    implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

  private static Jdbi jdbi;
  private static URI serverUri;

  protected static String getDatabaseUrl() {
    var host = System.getProperty("database_1.host");
    var port = System.getProperty("database_1.tcp.5432");
    return String.format("jdbc:postgresql://%s:%s/maps_db", host, port);
  }

  @Override
  public void beforeAll(final ExtensionContext context) {
    var host = System.getProperty("maps_1.host");
    var port = System.getProperty("maps_1.tcp.8080");
    final String localUri = String.format("http://%s:%s", host, port);
    serverUri = URI.create(localUri);

    if (jdbi == null) {
      jdbi = Jdbi.create(getDatabaseUrl(), "maps_user", "maps_user");
      jdbi.installPlugin(new SqlObjectPlugin());
    }
    GlobalConfig.instance().getDbUnitConfig().url(getDatabaseUrl());
    GlobalConfig.instance().getDbUnitConfig().user("maps_user");
    GlobalConfig.instance().getDbUnitConfig().password("maps_user");
    GlobalConfig.instance().getDbUnitConfig().driver("org.postgresql.Driver");
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    final URL cleanupFileUrl = getClass().getClassLoader().getResource("db-cleanup.sql");
    if (cleanupFileUrl != null) {
      final String cleanupSql = Files.readString(Path.of(cleanupFileUrl.toURI()));
      jdbi.withHandle(handle -> handle.execute(cleanupSql));
    }
  }

  @Override
  public boolean supportsParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext)
      throws ParameterResolutionException {

    if (parameterContext.getParameter().getType().equals(Jdbi.class)) {
      return true;
    } else if (parameterContext.getParameter().getType().equals(URI.class)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Object resolveParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext)
      throws ParameterResolutionException {

    if (parameterContext.getParameter().getType().equals(Jdbi.class)) {
      return jdbi;
    } else if (parameterContext.getParameter().getType().equals(URI.class)) {
      return Preconditions.checkNotNull(serverUri);
    } else {
      throw new IllegalStateException(
          "Unsupported parameter type for  Junit test class: "
              + parameterContext.getParameter().getType());
    }
  }
}
