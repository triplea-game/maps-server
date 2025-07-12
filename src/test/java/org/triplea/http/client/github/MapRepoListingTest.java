package org.triplea.http.client.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MapRepoListingTest {

  @Test
  void repoNameParsing() {
    var listing =
        MapRepoListing.builder()
            .uri(
                "https://api.github.com/repos/triplea-maps/https%3A//github.com/triplea-maps/fake-map")
            .defaultBranch("master")
            .build();

    String repoName = listing.getName();

    assertThat(repoName).isEqualTo("fake-map");
  }
}
