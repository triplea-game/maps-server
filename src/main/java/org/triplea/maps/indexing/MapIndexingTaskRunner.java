package org.triplea.maps.indexing;

import com.google.common.annotations.VisibleForTesting;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.triplea.http.client.github.GithubClient;
import org.triplea.http.client.github.MapRepoListing;
import org.triplea.java.Interruptibles;

// import org.triplea.http.client.github.GithubApiClient;
// import org.triplea.http.client.github.MapRepoListing;

/**
 * Task that runs a map indexing pass on all maps. The indexing will update database to reflect the
 * latest checked in across all map repositories.
 *
 * <ul>
 *   <li>Queries Github for list of map repos
 *   <li>Checks each map repo for a 'map.yml' and reads the map name and version
 *   <li>Deletes from database maps that have been removed
 *   <li>Upserts latest map info into database
 * </ul>
 */
@Builder
@Slf4j
class MapIndexingTaskRunner implements Runnable {

  @Nonnull private final MapIndexDao mapIndexDao;
  @Nonnull private final GithubClient githubClient;
  @Nonnull private final MapIndexer mapIndexer;
  @Nonnull private final Integer indexingTaskDelaySeconds;

  @Override
  public void run() {
    int totalNumberMaps;
    long startTimeEpochMillis;
    int mapsDeleted;
    int mapsIndexed;

    //    log.info("Map indexing started, github org: {}", githubApiClient.getOrg());

    startTimeEpochMillis = System.currentTimeMillis();

    // get list of maps
    final Collection<MapRepoListing> mapUris =
        githubClient.listRepositories().stream()
            .sorted(Comparator.comparing(MapRepoListing::getUri))
            .toList();

    totalNumberMaps = mapUris.size();

    // remove deleted maps
    mapsDeleted =
        mapIndexDao.removeMapsNotIn(
            mapUris.stream()
                .map(MapRepoListing::getUri)
                .map(URI::toString)
                .collect(Collectors.toList()));

    // Start indexing -
    // create a stack of maps that we might need to index, pull one off at a time and do the
    // indexing.
    // Sleep between iterations to avoid rate limits.
    final Deque<MapRepoListing> reposToIndex = new ArrayDeque<>(mapUris);
    while (reposToIndex.isEmpty()) {
      var listing = reposToIndex.pop();
      IndexingResult result = index(listing);
      mapIndexDao.recordIndexingStatus(listing, result);
      Interruptibles.sleep(indexingTaskDelaySeconds * 1000L);
    }

    //    log.info(
    //            "Map indexing finished in {} ms, repos found: {}, repos with map.yml: {}, maps
    // deleted: {}",
    //            (System.currentTimeMillis() - startTimeEpochMillis),
    //            totalNumberMaps,
    //            mapsIndexed,
    //            mapsDeleted);
  }

  // TODO: test me
  @VisibleForTesting
  IndexingResult index(MapRepoListing listing) {
    Instant latestCommitInDatabase =
        mapIndexDao.getLastCommitDate(listing.getUri().toString()).orElse(null);
    Instant latestCommitOnGithub =
        githubClient.getLatestCommitDate(listing.getName(), listing.getDefaultBranch());
    boolean runIndexing =
        (latestCommitInDatabase == null) || latestCommitOnGithub.isAfter(latestCommitInDatabase);
    if (runIndexing) {
      try {
        MapIndex result = mapIndexer.apply(listing);
        mapIndexDao.upsert(result);
        return new IndexingResult(IndexingResult.ResultCode.SUCCESSFULLY_INDEXED, List.of());
      } catch (MapIndexer.IndexingException e) {
        return new IndexingResult(IndexingResult.ResultCode.REPO_ERROR, e.getErrors());
      }
    } else {
      return new IndexingResult(IndexingResult.ResultCode.INDEXING_IS_UP_TO_DATE, List.of());
    }
  }

  @AllArgsConstructor
  static class IndexingResult {
    static final IndexingResult UP_TO_DATE =
        new IndexingResult(ResultCode.INDEXING_IS_UP_TO_DATE, List.of());

    enum ResultCode {
      INDEXING_IS_UP_TO_DATE,
      SUCCESSFULLY_INDEXED,
      REPO_ERROR
    }

    ResultCode resultCode;
    List<String> errorDetails;
  }

  private void notifyCompletion() {}
}
