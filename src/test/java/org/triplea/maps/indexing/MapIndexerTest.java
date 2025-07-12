package org.triplea.maps.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.triplea.http.client.github.MapRepoListing;

class MapIndexerTest {
  private static final Instant instant = Instant.now();

  ///  Validates we can pull index information for a single given map.
  ///
  /// Test with a bunch of mocks, not all data is pulled from mocks, we
  /// mostly verify here the computed values (eg: preview URL)
  @Test
  void verifyMapIndexingHappyCase() {
    final MapIndexer mapIndexer =
        MapIndexer.builder()
            .lastCommitDateFetcher(repoListing -> Optional.of(instant))
            .mapNameReader(mapRepoListing -> Optional.of("map name"))
            .mapDescriptionReader(mapRepoListing -> "description")
            .downloadSizeFetcher(mapRepoListing -> Optional.of(10L))
            .build();

    var listing = MapRepoListing.builder().uri("http://url").defaultBranch("main").build();

    var mapIndexingResult = mapIndexer.apply(listing);

    assertThat(mapIndexingResult.getLastCommitDate()).isEqualTo(instant);
    assertThat(mapIndexingResult.getMapName()).isEqualTo("map name");
    assertThat(mapIndexingResult.getDescription()).isEqualTo("description");
    assertThat(mapIndexingResult.getMapDownloadSizeInBytes()).isEqualTo(10L);
    assertThat(mapIndexingResult.getMapRepoUri()).isEqualTo(listing.getUri().toString());

    assertThat(mapIndexingResult.getDownloadUri())
        .isEqualTo(listing.getUri() + "/archive/refs/heads/master.zip");
    assertThat(mapIndexingResult.getPreviewImageUri())
        .isEqualTo(listing.getUri() + "/blob/master/preview.png?raw=true");
  }
}
