package org.triplea.http.client.github;

import com.google.gson.annotations.SerializedName;
import java.net.URI;
import lombok.*;

/** Response object from Github listing the details of an organization's repositories. */
@Value
@Builder
public class MapRepoListing {
  @SerializedName("html_url")
  String uri;

  @SerializedName("default_branch")
  String defaultBranch;

  public String getName() {
    return uri.substring(uri.lastIndexOf("/") + 1);
  }

  public URI getUri() {
    return URI.create(uri);
  }
}
