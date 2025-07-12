package org.triplea.maps.indexing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.triplea.http.client.github.GithubClient;
import org.triplea.http.client.github.MapRepoListing;
import org.triplea.maps.IntegTestExtension;
import org.triplea.maps.TestData;

@AllArgsConstructor
@ExtendWith(IntegTestExtension.class)
class MapIndexingTaskRunnerTest {
  final Jdbi jdbi;

  /// Run the indexing task loop and validate that we get a successful index result, then repeat and
  /// verify we get a 'map index is up to date'
  ///
  /// Uses a real database, a mock map indexer and a mock Github client
  @Test
  void repeatedIndexingResults() {
    MapRepoListing listing = TestData.mapRepoListing;

    // set up a mock github client that always returns the 'last' commit date for a repo.
    GithubClient mockClient = Mockito.mock(GithubClient.class);

    when(mockClient.getLatestCommitDate(anyString(), anyString()))
        .thenReturn(TestData.mapIndex.getLastCommitDate());
    MapIndexer mapIndexer = Mockito.mock(MapIndexer.class);
    when(mapIndexer.apply(listing)).thenReturn(TestData.mapIndex);

    MapIndexingTaskRunner runner =
        new MapIndexingTaskRunner(jdbi.onDemand(MapIndexDao.class), mockClient, mapIndexer, 0);

    MapIndexingTaskRunner.IndexingResult result = runner.index(listing);

    assertThat(result.resultCode)
        .isEqualTo(MapIndexingTaskRunner.IndexingResult.ResultCode.SUCCESSFULLY_INDEXED);

    result = runner.index(listing);

    assertThat(result.resultCode)
        .isEqualTo(MapIndexingTaskRunner.IndexingResult.ResultCode.INDEXING_IS_UP_TO_DATE);
  }
}
