package org.triplea.maps.indexing;

import io.dropwizard.lifecycle.Managed;
import java.time.Duration;
import lombok.experimental.UtilityClass;
import org.jdbi.v3.core.Jdbi;
import org.triplea.maps.MapsServerConfig;
import org.triplea.server.lib.scheduled.tasks.ScheduledTask;

@UtilityClass
public class MapsIndexingObjectFactory {
  /**
   * Factory method to create indexing task on a schedule. This does not start indexing, the
   * 'start()' method must be called for map indexing to begin.
   */
  public static Managed buildMapsIndexingSchedule(
      final MapsServerConfig configuration, final Jdbi jdbi) {

    //    var githubApiClient = configuration.createGithubApiClient();

    return ScheduledTask.builder()
        .taskName("Map-Indexing")
        .delay(Duration.ofSeconds(10))
        .period(Duration.ofMinutes(configuration.getMapIndexingPeriodMinutes()))
        //        .task(
        //            MapIndexingTaskRunner.builder()
        //                .githubApiClient(githubApiClient)
        //                .mapIndexer(
        //                    mapIndexingTask(
        //                        githubApiClient,
        //                        new SkipMapIndexingCheck(jdbi.onDemand(MapIndexDao.class))))
        //                .mapIndexDao(jdbi.onDemand(MapIndexDao.class))
        //                .indexingTaskDelaySeconds(configuration.getIndexingTaskDelaySeconds())
        //                .build())
        .build();
  }
}
