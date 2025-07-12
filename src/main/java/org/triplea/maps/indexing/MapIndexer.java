package org.triplea.maps.indexing;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import org.triplea.http.client.github.GithubClient;
import org.triplea.http.client.github.MapRepoListing;
import org.triplea.maps.indexing.tasks.*;

/** MapIndexer physically fetches the data required to index a map. */
@Builder
class MapIndexer {
  @Nonnull private final Function<MapRepoListing, Optional<Instant>> lastCommitDateFetcher;
  @Nonnull private final Function<MapRepoListing, Optional<String>> mapNameReader;
  @Nonnull private final Function<MapRepoListing, String> mapDescriptionReader;
  @Nonnull private final Function<URI, Optional<Long>> downloadSizeFetcher;

  static MapIndexer build(final GithubClient githubApiClient) {
    return MapIndexer.builder()
        .lastCommitDateFetcher(CommitDateFetcher.builder().githubClient(githubApiClient).build())
        .mapNameReader(MapNameReader.builder().build())
        .mapDescriptionReader(new MapDescriptionReader())
        .downloadSizeFetcher(new DownloadSizeFetcher())
        .build();
  }

  @Getter
  static class IndexingException extends RuntimeException {
    private final List<String> errors;

    public IndexingException(List<String> errors) {
      super(String.join(", ", errors));
      this.errors = errors;
    }

    public IndexingException(String error) {
      super(error);
      this.errors = List.of(error);
    }
  }

  public MapIndex apply(final MapRepoListing mapRepoListing) throws IndexingException {
    List<String> errorCollector = new ArrayList<>();

    Instant lastCommitDateOnRepo = lastCommitDateFetcher.apply(mapRepoListing).orElse(null);
    if (lastCommitDateOnRepo == null) {
      errorCollector.add(
          """
          Failed to fetch the date of the last change to the repository. This is an unexpected error. Check server logs.
          """);
    }

    final String mapName = mapNameReader.apply(mapRepoListing).orElse(null);
    if (mapName == null) {
      errorCollector.add(
          String.format(
              """
          Failed to read map-name. Expected to read attribute "map_name" in a 'map.yml' located at: %s
          The file might not exist, or the attribute might not exist in the file (check spelling, check indentation)
          """,
              MapNameReader.computeMapYamlLocation(mapRepoListing)));
    }

    final String description = mapDescriptionReader.apply(mapRepoListing);

    final String previewImageUri =
        mapRepoListing.getUri().toString() + "/blob/master/preview.png?raw=true";

    final String downloadUri =
        mapRepoListing.getUri().toString() + "/archive/refs/heads/master.zip";

    final Long downloadSize = downloadSizeFetcher.apply(URI.create(downloadUri)).orElse(null);
    if (downloadSize == null) {
      errorCollector.add(
          """
          Failed to download the map and compute the file size. This is an unexpected error. Check server logs.
          """);
    }

    if (!errorCollector.isEmpty()) {
      throw new IndexingException(errorCollector);
    } else {
      return MapIndex.builder()
          .mapName(mapName)
          .mapRepoUri(mapRepoListing.getUri().toString())
          .lastCommitDate(lastCommitDateOnRepo)
          .description(description)
          .defaultBranch(mapRepoListing.getDefaultBranch())
          .downloadUri(downloadUri)
          .previewImageUri(previewImageUri)
          .mapDownloadSizeInBytes(downloadSize)
          .build();
    }
  }
}
