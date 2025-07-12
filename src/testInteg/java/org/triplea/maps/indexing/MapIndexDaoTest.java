package org.triplea.maps.indexing;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import com.github.npathai.hamcrestopt.OptionalMatchers;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.triplea.maps.IntegTestExtension;
import org.triplea.maps.TestData;

@DataSet(value = "map_index.yml", useSequenceFiltering = false)
@ExtendWith(IntegTestExtension.class)
@ExtendWith(DBUnitExtension.class)
class MapIndexDaoTest {

  private final MapIndexDao mapIndexDao;

  MapIndexDaoTest(Jdbi jdbi) {
    mapIndexDao = jdbi.onDemand(MapIndexDao.class);
  }

  @Test
  @ExpectedDataSet(value = "expected/map_index_upsert_updated.yml", orderBy = "id")
  void upsertUpdatesRecords() {
    mapIndexDao.upsert(
        TestData.mapIndex.toBuilder()
            .mapName("map-name-updated")
            .mapDownloadSizeInBytes(8000L)
            .build());
  }

  @Test
  @ExpectedDataSet("expected/map_index_post_remove.yml")
  void removeMaps() {
    mapIndexDao.removeMapsNotIn(List.of("http-map-repo-url"));
  }

  @Test
  void getLastCommitDate() {
    assertThat(
        mapIndexDao.getLastCommitDate(TestData.mapIndex.getMapRepoUri()),
        isPresentAndIs(LocalDateTime.of(2000, 12, 1, 23, 59, 20).toInstant(ZoneOffset.UTC)));

    assertThat(
        "Map repo URL does not exist",
        mapIndexDao.getLastCommitDate("http://map-repo-url-DNE"),
        OptionalMatchers.isEmpty());
  }
}
