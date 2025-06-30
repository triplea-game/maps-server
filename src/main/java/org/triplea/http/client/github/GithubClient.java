package org.triplea.http.client.github;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import javax.annotation.Nonnull;

public interface GithubClient {

  static GithubClient build(String authToken, @Nonnull String org) {
    return GithubApiClient.builder()
        .uri(URI.create("https://api.github.com"))
        .org(org)
        .authToken(authToken)
        .build();
  }

  Instant getLatestCommitDate(String repoName, String branchName);

  Collection<MapRepoListing> listRepositories();
}
