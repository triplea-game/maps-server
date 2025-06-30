package org.triplea.maps.indexing.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;
import org.triplea.http.client.github.MapRepoListing;

class DownloadUriCalculatorTest {

  @Test
  void verifyDownloadUriCalculation() {
    final var mapRepoListing =
        MapRepoListing.builder()
            .uri("https://github.com/triplea-maps/test-map")
            .name("repo name")
            .build();

    final String result = new DownloadUriCalculator().apply(mapRepoListing);

    assertThat(
        result, is("https://github.com/triplea-maps/test-map/archive/refs/heads/master.zip"));
  }
}
