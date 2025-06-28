package org.triplea.http.client.github;

import com.google.gson.annotations.SerializedName;
import java.net.URI;
import lombok.*;

/** Response object from Github listing the details of an organization's repositories. */
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class MapRepoListing {
  @SerializedName("html_url")
  String htmlUrl;

  @Getter String name;

  public URI getUri() {
    return URI.create(htmlUrl);
  }
}
