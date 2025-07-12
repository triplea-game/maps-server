package org.triplea.maps.listing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.triplea.http.client.maps.listing.MapDownloadItem;
import org.triplea.http.client.maps.listing.MapTag;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class MapListingRecord {
  String mapName;
  String downloadUrl;
  String previewImageUrl;
  String description;
  Instant lastCommitDate;
  Long downloadSizeBytes;
  @Builder.Default List<MapTag> tags = new ArrayList<>();

  @SuppressWarnings("unused")
  @JdbiConstructor
  public MapListingRecord() {}

  List<MapTag> getTags() {
    if (tags == null) {
      tags = new ArrayList<>();
    }
    return tags;
  }

  //  @Builder
  //  public MapListingRecord(
  //      @ColumnName("map_name") final String name,
  //      @ColumnName("download_url") final String downloadUrl,
  //      @ColumnName("preview_image_url") final String previewImageUrl,
  //      @ColumnName("description") final String description,
  //      @ColumnName("last_commit_date") final Instant lastCommitDate,
  //      @ColumnName("download_size_bytes") final Long downloadSizeBytes) {
  //    this.name = name;
  //    this.downloadUrl = downloadUrl;
  //    this.previewImageUrl = previewImageUrl;
  //    this.description = description;
  //    this.lastCommitDate = lastCommitDate;
  //    this.downloadSizeBytes = downloadSizeBytes;
  //  }

  MapDownloadItem toMapDownloadItem() {
    return MapDownloadItem.builder()
        .downloadUrl(downloadUrl)
        .downloadSizeInBytes(downloadSizeBytes)
        .previewImageUrl(previewImageUrl)
        .mapName(mapName)
        .lastCommitDateEpochMilli(lastCommitDate.toEpochMilli())
        .description(description)
        .mapTags(tags)
        .build();
  }
}
