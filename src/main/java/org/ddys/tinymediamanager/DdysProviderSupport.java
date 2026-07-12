package org.ddys.tinymediamanager;

import java.net.URL;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.ScrapeException;

abstract class DdysProviderSupport {
  static final String PROVIDER_ID = "ddys";

  private final MediaProviderInfo providerInfo;

  DdysProviderSupport(String subId, String name, String description) {
    this.providerInfo = createProviderInfo(subId, name, description);
  }

  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  public boolean isActive() {
    return true;
  }

  protected SortedSet<MediaSearchResult> search(MediaSearchAndScrapeOptions options, MediaType mediaType, String fallbackType) throws ScrapeException {
    DdysConfig config = config();
    DdysClient client = new DdysClient(config);
    String query = DdysConfig.trim(options.getSearchQuery());
    Object root = query.isBlank() ? client.movies(fallbackType, 1, config.pageSize) : client.search(query, 1, config.pageSize);
    SortedSet<MediaSearchResult> results = new TreeSet<>();
    for (DdysModels.Movie movie : DdysMapper.moviesFromRoot(root, config)) {
      results.add(DdysMapper.toSearchResult(movie, mediaType, options));
    }
    return results;
  }

  protected MediaMetadata metadata(MediaSearchAndScrapeOptions options, MediaType mediaType) throws ScrapeException {
    DdysConfig config = config();
    DdysModels.Movie movie = resolveMovie(options, mediaType);
    List<DdysModels.Resource> resources = movie.slug.isBlank() ? List.of() : resources(movie.slug, config);
    return DdysMapper.toMetadata(movie, resources, config);
  }

  protected List<DdysModels.Resource> resources(String slug, DdysConfig config) {
    if (slug == null || slug.isBlank()) {
      return List.of();
    }
    try {
      return DdysMapper.resourcesFromRoot(new DdysClient(config).sources(slug), config);
    }
    catch (ScrapeException e) {
      return List.of();
    }
  }

  protected List<MediaArtwork> artwork(MediaSearchAndScrapeOptions options, MediaArtworkType type, MediaType mediaType) throws ScrapeException {
    DdysConfig config = config();
    DdysModels.Movie movie = resolveMovie(options, mediaType);
    return DdysMapper.artwork(movie, type, config);
  }

  protected DdysModels.Movie resolveMovie(MediaSearchAndScrapeOptions options, MediaType mediaType) throws ScrapeException {
    DdysConfig config = config();
    DdysClient client = new DdysClient(config);
    String slug = DdysConfig.trim(options.getIdAsString(PROVIDER_ID));
    if (!slug.isBlank()) {
      try {
        DdysModels.Movie movie = DdysMapper.movieFromRoot(client.movie(slug), config);
        if (!movie.isEmpty()) {
          if (movie.slug.isBlank()) {
            movie.slug = slug;
          }
          return movie;
        }
      }
      catch (ScrapeException ignored) {
        // Some installations only expose search/source endpoints. Fall through to search.
      }
    }

    String query = DdysConfig.trim(options.getSearchQuery());
    if (query.isBlank()) {
      query = slug;
    }
    if (query.isBlank()) {
      throw new ScrapeException("DDYS scraper needs a title or DDYS slug");
    }
    List<DdysModels.Movie> results = DdysMapper.moviesFromRoot(client.search(query, 1, 1), config);
    if (results.isEmpty()) {
      throw new ScrapeException("No DDYS result found for " + query);
    }
    return results.get(0);
  }

  protected DdysConfig config() {
    return DdysConfig.from(providerInfo);
  }

  private MediaProviderInfo createProviderInfo(String subId, String name, String description) {
    URL logo = DdysProviderSupport.class.getResource("/org/ddys/tinymediamanager/ddys-icon.png");
    MediaProviderInfo info = new MediaProviderInfo(PROVIDER_ID, subId, name, description, logo);
    DdysConfig.addDefaults(info);
    return info;
  }
}
