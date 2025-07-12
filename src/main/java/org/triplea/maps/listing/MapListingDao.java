package org.triplea.maps.listing;

import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.triplea.http.client.maps.listing.MapDownloadItem;
import org.triplea.http.client.maps.listing.MapTag;

@AllArgsConstructor
public class MapListingDao {
  private final Jdbi jdbi;

  List<MapDownloadItem> fetchMapListings() {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(
                    """
                        select
                          m.id,
                          m.map_name,
                          m.download_url,
                          m.download_size_bytes,
                          m.preview_image_url,
                          m.description,
                          m.last_commit_date,
                          t.value tag_value,
                          mtc.name tag_category
                        from map_index m
                        left join map_index_tag mit on mit.map_index_id = m.id
                        left join map_tag t on t.id = mit.map_tag_id
                        left join map_tag_category mtc on mtc.id = t.map_tag_category_id
                        order by m.map_name
                        """)
                .registerRowMapper(BeanMapper.factory(MapListingRecord.class))
                .registerRowMapper(BeanMapper.factory(MapTag.class))
                .reduceRows(
                    new HashMap<Long, MapListingRecord>(),
                    (accumulator, rowView) -> {
                      var listing =
                          accumulator.computeIfAbsent(
                              rowView.getColumn("id", Long.class),
                              id -> rowView.getRow(MapListingRecord.class));

                      if (rowView.getColumn("tag_value", String.class) != null) {
                        listing
                            .getTags()
                            .add(
                                MapTag.builder()
                                    .name(rowView.getColumn("tag_category", String.class))
                                    .value(rowView.getColumn("tag_value", String.class))
                                    .build());
                      }
                      return accumulator;
                    })
                .values()
                .stream()
                .map(MapListingRecord::toMapDownloadItem)
                .toList());
  }
}
