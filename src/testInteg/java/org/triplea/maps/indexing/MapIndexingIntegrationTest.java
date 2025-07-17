package org.triplea.maps.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.triplea.http.client.github.MapRepoListing;
import org.triplea.maps.SupportServerConfig;

/** Validate we can scrape a real map on github and scrape correct data. */
public class MapIndexingIntegrationTest {

  @Test
  void runIndexingOnTestMap() {
    SupportServerConfig supportServerConfig = new SupportServerConfig();
    supportServerConfig.setGithubMapsOrgName("triplea-maps");

    final MapIndexer mapIndexerRunner =
        MapIndexer.build(supportServerConfig.createGithubApiClient());

    final MapIndex result =
        mapIndexerRunner.apply(
            MapRepoListing.builder()
                .uri("https://github.com/triplea-maps/test-map")
                .defaultBranch("master")
                .build());

    assertThat(result.getMapRepoUri()).isEqualTo("https://github.com/triplea-maps/test-map");
    assertThat(result.getMapName()).isEqualTo("Test Map");
    assertThat(result.getLastCommitDate()).isNotNull();
    assertThat(result.getDescription()).contains("<br><b><em>by test</em></b>");
    assertThat(result.getDefaultBranch()).isEqualTo("master");
    assertThat(result.getDownloadUri())
        .isEqualTo("https://github.com/triplea-maps/test-map/archive/refs/heads/master.zip");
    assertThat(result.getMapDownloadSizeInBytes()).isGreaterThan(0);
  }
}
