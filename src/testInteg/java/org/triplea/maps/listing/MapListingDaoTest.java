package org.triplea.maps.listing;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.triplea.http.client.maps.listing.MapTag;
import org.triplea.maps.IntegTestExtension;

@DataSet(value = "map_index.yml,map_tag_value.yml", useSequenceFiltering = false)
@ExtendWith(IntegTestExtension.class)
@ExtendWith(DBUnitExtension.class)
class MapListingDaoTest {

  private final MapListingDao mapListingDao;

  MapListingDaoTest(final Jdbi jdbi) {
    this.mapListingDao = new MapListingDao(jdbi);
  }

  @Test
  void verifySelect() {
    final var results = mapListingDao.fetchMapListings();
    assertThat(results).hasSize(1);
    final var mapDownloadListing = results.get(0);
    assertThat(mapDownloadListing.getMapName()).isEqualTo("map-name");
    assertThat(mapDownloadListing.getDownloadUrl())
        .isEqualTo("http-map-repo-url/archives/master.zip");
    assertThat(mapDownloadListing.getDownloadSizeInBytes()).isEqualTo(4000L);
    assertThat(mapDownloadListing.getPreviewImageUrl()).isEqualTo("http-preview-image-url");
    assertThat(mapDownloadListing.getDescription()).isEqualTo("description-repo-1");
    assertThat(mapDownloadListing.getLastCommitDateEpochMilli())
        .isEqualTo(
            LocalDateTime.of(2000, 12, 1, 23, 59, 20).toInstant(ZoneOffset.UTC).toEpochMilli());

    List<MapTag> mapTags = mapDownloadListing.getMapTags();
    assertThat(mapTags).contains(MapTag.builder().name("difficulty").value("easy").build());
    assertThat(mapTags).contains(MapTag.builder().name("era").value("ancient").build());
  }
}
