package org.ddys.tinymediamanager;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.tinymediamanager.scraper.exceptions.ScrapeException;

final class DdysClient {
  private final DdysConfig config;
  private final HttpClient httpClient;

  DdysClient(DdysConfig config) {
    this.config = config;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();
  }

  Object search(String query, int page, int perPage) throws ScrapeException {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("q", query);
    params.put("page", String.valueOf(page));
    params.put("per_page", String.valueOf(perPage));
    return getJson("/search", params);
  }

  Object movies(String type, int page, int perPage) throws ScrapeException {
    Map<String, String> params = new LinkedHashMap<>();
    if (type != null && !type.isBlank()) {
      params.put("type", type);
    }
    params.put("page", String.valueOf(page));
    params.put("per_page", String.valueOf(perPage));
    return getJson("/movies", params);
  }

  Object movie(String slug) throws ScrapeException {
    return getJson("/movies/" + encodePath(slug), Map.of());
  }

  Object sources(String slug) throws ScrapeException {
    return getJson("/movies/" + encodePath(slug) + "/sources", Map.of());
  }

  Object latest(int limit) throws ScrapeException {
    return getJson("/latest", Map.of("limit", String.valueOf(limit)));
  }

  Object hot(int limit) throws ScrapeException {
    return getJson("/hot", Map.of("limit", String.valueOf(limit)));
  }

  private Object getJson(String path, Map<String, String> query) throws ScrapeException {
    URI uri = URI.create(buildUrl(path, query));
    HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
        .timeout(Duration.ofSeconds(config.timeoutSeconds))
        .header("Accept", "application/json")
        .header("User-Agent", config.userAgent)
        .GET();
    if (!config.apiKey.isBlank()) {
      builder.header("Authorization", "Bearer " + config.apiKey);
    }

    try {
      HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new ScrapeException("DDYS API returned HTTP " + response.statusCode());
      }
      String body = response.body() == null ? "" : response.body().trim();
      if (body.isBlank()) {
        throw new ScrapeException("DDYS API returned empty JSON");
      }
      return DdysJson.parse(body);
    }
    catch (IOException e) {
      throw new ScrapeException(e.getMessage());
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ScrapeException("DDYS API request interrupted");
    }
    catch (IllegalArgumentException e) {
      throw new ScrapeException("DDYS API returned invalid JSON: " + e.getMessage());
    }
  }

  private String buildUrl(String path, Map<String, String> query) {
    String cleanPath = path.startsWith("/") ? path : "/" + path;
    StringBuilder url = new StringBuilder(config.apiBase).append(cleanPath);
    boolean first = true;
    for (Map.Entry<String, String> entry : query.entrySet()) {
      String value = entry.getValue();
      if (value == null || value.isBlank()) {
        continue;
      }
      url.append(first ? '?' : '&')
          .append(encodeQuery(entry.getKey()))
          .append('=')
          .append(encodeQuery(value));
      first = false;
    }
    return url.toString();
  }

  private static String encodeQuery(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private static String encodePath(String value) {
    return encodeQuery(value).replace("%2F", "/");
  }
}
