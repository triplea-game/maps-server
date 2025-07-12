package org.triplea.maps.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.triplea.http.client.github.GithubClient;
import org.triplea.http.client.github.MapRepoListing;
import org.triplea.maps.IntegTestExtension;

@ExtendWith(IntegTestExtension.class)
@AllArgsConstructor
public class IndexingEndToEndTest {
  final Jdbi jdbi;

  static final MapRepoListing TEST_MAP =
      MapRepoListing.builder()
          .uri("https://github.com/triplea-maps/test-map")
          .defaultBranch("master")
          .build();

  /**
   * In this test we will be scraping the 'test' repository from Github and will validate that data
   * lands in database. Uses a real github client & real database.
   */
  @Test
  void verifyIndexing() {
    var githubClient = GithubClient.build("", "triplea-maps");
    MapIndexDao dao = jdbi.onDemand(MapIndexDao.class);
    MapIndexingTaskRunner taskRunner =
        new MapIndexingTaskRunner(dao, githubClient, MapIndexer.build(githubClient), 0);

    assertThat(testMapExistsInDatabase()).isFalse();

    taskRunner.index(TEST_MAP);

    assertThat(testMapExistsInDatabase()).isTrue();
  }

  private boolean testMapExistsInDatabase() {
    String query =
        """
                select 1
                from map_index
                where repo_url = 'https://github.com/triplea-maps/test-map'
                """;
    return jdbi.withHandle(handle -> handle.createQuery(query).mapTo(Integer.class).findFirst())
        .isPresent();
  }
}
