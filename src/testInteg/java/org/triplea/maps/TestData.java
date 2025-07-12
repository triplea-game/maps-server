package org.triplea.maps;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.experimental.UtilityClass;
import org.triplea.http.client.github.MapRepoListing;
import org.triplea.maps.indexing.MapIndex;

///  Example data used in tests, useful to avoid repeating large blocks of tedious data setup.
@UtilityClass
public class TestData {
  public static final MapIndex mapIndex =
      MapIndex.builder()
          .mapName("map-name-updated")
          .mapRepoUri("http-map-repo-url")
          .lastCommitDate(LocalDateTime.of(2000, 12, 1, 23, 59, 20).toInstant(ZoneOffset.UTC))
          .mapDownloadSizeInBytes(4000L)
          .downloadUri("http-map-repo-url/archives/master.zip")
          .previewImageUri("http-preview-image-url")
          .description("description-repo-1")
          .defaultBranch("master")
          .build();
  ;
  public static final MapRepoListing mapRepoListing =
      MapRepoListing.builder()
          .uri(mapIndex.getMapRepoUri())
          .defaultBranch(mapIndex.getDefaultBranch())
          .build();
}
