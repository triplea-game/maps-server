package org.triplea.http.client.github;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import org.triplea.http.client.HttpClient;

/** Can be used to interact with github's webservice API. */
class GithubApiClient implements GithubClient {

  @SuppressWarnings("InterfaceNeverImplemented")
  interface GithubApiFeignClient {
    @VisibleForTesting String LIST_REPOS_PATH = "/orgs/{org}/repos";
    @VisibleForTesting String BRANCHES_PATH = "/repos/{org}/{repo}/branches/{branch}";

    @RequestLine("GET " + LIST_REPOS_PATH)
    List<MapRepoListing> listRepos(
        @QueryMap Map<String, String> queryParams, @Param("org") String org);

    @RequestLine("GET " + BRANCHES_PATH)
    BranchInfoResponse getBranchInfo(
        @Param("org") String org, @Param("repo") String repo, @Param("branch") String branch);
  }

  private final GithubApiFeignClient githubApiFeignClient;
  @Getter private final String org;

  /**
   * @param uri The URI for githubs webservice API.
   * @param authToken Auth token that will be sent to Github for webservice calls. Can be empty, but
   *     if specified must be valid (no auth token still works, but rate limits will be more
   *     restrictive).
   * @param org Name of the github org to be queried.
   */
  @Builder
  public GithubApiClient(@Nonnull URI uri, String authToken, @Nonnull String org) {
    githubApiFeignClient =
        HttpClient.newClient(
            GithubApiFeignClient.class,
            uri,
            Strings.isNullOrEmpty(authToken)
                ? Map.of()
                : Map.of("Authorization", "token " + authToken));
    this.org = org;
  }

  /**
   * Returns a listing of the repositories within a github organization. This call handles paging,
   * it returns a complete list and may perform multiple calls to Github.
   *
   * <p>Example equivalent cUrl call:
   *
   * <p>curl https://api.github.com/orgs/triplea-maps/repos
   */
  @Override
  public Collection<MapRepoListing> listRepositories() {
    final Collection<MapRepoListing> allRepos = new HashSet<>();
    int pageNumber = 1;
    Collection<MapRepoListing> repos = listRepositories(pageNumber);
    while (!repos.isEmpty()) {
      pageNumber++;
      allRepos.addAll(repos);
      repos = listRepositories(pageNumber);
    }
    return allRepos;
  }

  private Collection<MapRepoListing> listRepositories(int pageNumber) {
    final Map<String, String> queryParams = new HashMap<>();
    queryParams.put("per_page", "100");
    queryParams.put("page", String.valueOf(pageNumber));

    return githubApiFeignClient.listRepos(queryParams, org);
  }

  @Override
  public Instant getLatestCommitDate(String repoName, String branchName) {
    return githubApiFeignClient.getBranchInfo(org, repoName, branchName).getLastCommitDate();
  }
}
