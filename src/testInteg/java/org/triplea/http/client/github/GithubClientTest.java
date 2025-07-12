package org.triplea.http.client.github;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GithubClientTest {

  /// In this test we use a real github client to exercise each webservice call
  @Test
  void integTest() {
    var client = GithubClient.build("", "triplea-maps");
    assertThat(client.listRepositories()).isNotEmpty();
    assertThat(client.getLatestCommitDate("test-map", "master")).isNotNull();
  }
}
