package org.triplea.maps.indexing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.triplea.http.client.github.MapRepoListing;

public interface MapIndexDao {

  /** Upserts a map indexing result into the map_index table. */
  @SqlUpdate(
      "insert into map_index("
          + "    map_name, repo_url, default_branch, description, "
          + "    download_url, preview_image_url, download_size_bytes, last_commit_date)\n"
          + "values("
          + "     :mapName, :mapRepoUri, :defaultBranch, :description, "
          + "     :downloadUri, :previewImageUri, :mapDownloadSizeInBytes, :lastCommitDate)\n"
          + "on conflict(repo_url)\n"
          + "do update set\n"
          + "   map_name = :mapName,"
          + "   description = :description,"
          + "   default_branch = :defaultBranch,"
          + "   download_url = :downloadUri,"
          + "   preview_image_url = :previewImageUri,"
          + "   download_size_bytes = :mapDownloadSizeInBytes,"
          + "   last_commit_date = :lastCommitDate")
  void upsert(@BindBean MapIndex mapIndex);

  /** Deletes maps that are not in the parameter list from the map_index table. */
  @SqlUpdate("delete from map_index where repo_url not in(<mapUriList>)")
  int removeMapsNotIn(@BindList("mapUriList") List<String> mapUriList);

  @SqlQuery("select last_commit_date from map_index where repo_url = :repoUrl")
  Optional<Instant> getLastCommitDate(@Bind("repoUrl") String repoUrl);

  // TODO: implement & test
  void recordIndexingStatus(MapRepoListing listing, MapIndexingTaskRunner.IndexingResult status);
}
