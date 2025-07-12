package org.triplea.maps.indexing.tasks;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.base.Strings;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DownloadSizeFetcherTest {

  /**
   * In this test we stub the downloaded content to be a fixed size string. We then verify the
   * reported download size matches teh byte length of the string.
   */
  @Test
  @DisplayName("Happy case, return content size downloaded")
  void returnContentSizeDownloaded() {
    final DownloadSizeFetcher downloadSizeFetcher = new DownloadSizeFetcher();
    final String contentsString = "this is a test";
    downloadSizeFetcher.setDownloadFunction(uri -> asInputStream(contentsString));

    final Optional<Long> result = downloadSizeFetcher.apply(URI.create("htttps://fake-uri"));

    assertThat(
        "The 'contentString' was 'fake' downloaded, reported download size should match the "
            + "byte length of that string.",
        result,
        isPresentAndIs((long) contentsString.getBytes(StandardCharsets.UTF_8).length));
  }

  private static InputStream asInputStream(final String inputString) {
    return new ByteArrayInputStream(
        Strings.nullToEmpty(inputString).getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @DisplayName("Error case, error during download returns empty optional")
  void returnEmptyOnErrorDownloading() {
    final DownloadSizeFetcher downloadSizeFetcher = new DownloadSizeFetcher();
    downloadSizeFetcher.setDownloadFunction(
        uri -> {
          throw new IOException("test");
        });

    final Optional<Long> result = downloadSizeFetcher.apply(URI.create("htttps://fake-uri"));

    assertThat(result, isEmpty());
  }
}
