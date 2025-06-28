package org.triplea.maps;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.triplea.http.client.GenericServerResponse;
import org.triplea.http.client.maps.admin.MapTagAdminClient;
import org.triplea.http.client.maps.admin.MapTagMetaData;
import org.triplea.http.client.maps.admin.UpdateMapTagRequest;
import org.triplea.maps.tags.MapTagAdminModule;
import org.triplea.maps.tags.UpdateMapTagResult;

@jakarta.ws.rs.Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MapTagAdminController {
  private final MapTagAdminModule mapTagAdminModule;

  public static MapTagAdminController build(final Jdbi jdbi) {
    return new MapTagAdminController(MapTagAdminModule.build(jdbi));
  }

  /**
   * Returns data about map tags, which ones exist and their allowed values. This is useful to
   * moderators to select new tag values per map.
   */
  @GET
  @Path(MapTagAdminClient.GET_MAP_TAGS_META_DATA_PATH)
  //  @RolesAllowed(UserRole.MODERATOR)
  public List<MapTagMetaData> fetchAvailableMapTags() {
    return mapTagAdminModule.fetchMapTags();
  }

  /**
   * Upserts a tag value for a map. If the tag already exists its value is overwritten, otherwise a
   * new map-tag-value combination is created.
   */
  @POST
  @Path(MapTagAdminClient.UPDATE_MAP_TAG_PATH)
  //  @RolesAllowed(UserRole.MODERATOR)
  public GenericServerResponse updateMapTag(final UpdateMapTagRequest updateMapTagRequest) {
    final UpdateMapTagResult result = mapTagAdminModule.updateMapTag(updateMapTagRequest);

    return GenericServerResponse.builder()
        .success(result.isSuccess())
        .message(result.getMessage())
        .build();
  }
}
