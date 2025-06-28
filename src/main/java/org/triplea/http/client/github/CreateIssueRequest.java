package org.triplea.http.client.github;

import lombok.*;
import org.triplea.java.StringUtils;

/** Represents request data to create a github issue. */
@ToString
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIssueRequest {
  private String title;
  private String body;
  private String[] labels;

  public String getTitle() {
    final int maxLength = 125;
    return title == null ? null : StringUtils.truncate(title, maxLength);
  }

  public String getBody() {
    final int maxLength = 65536;
    return body == null ? null : StringUtils.truncate(body, maxLength);
  }
}
