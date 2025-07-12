package org.triplea.maps.listing;

import java.util.List;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.triplea.http.client.maps.listing.MapDownloadItem;

@AllArgsConstructor
public class MapsListingModule implements Supplier<List<MapDownloadItem>> {

  private final MapListingDao mapListingDao;

  public static MapsListingModule build(final Jdbi jdbi) {
    return new MapsListingModule(new MapListingDao(jdbi));
  }

  /** Returns data for the full set of maps available to download. */
  @Override
  public List<MapDownloadItem> get() {
    return mapListingDao.fetchMapListings();
  }
}
