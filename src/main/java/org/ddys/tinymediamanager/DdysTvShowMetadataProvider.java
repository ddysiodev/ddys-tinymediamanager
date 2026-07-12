package org.ddys.tinymediamanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;

public class DdysTvShowMetadataProvider extends DdysProviderSupport implements ITvShowMetadataProvider {
  public DdysTvShowMetadataProvider() {
    super("tvShow", "DDYS", "低端影视剧集元数据 scraper");
  }

  @Override
  public SortedSet<MediaSearchResult> search(TvShowSearchAndScrapeOptions options) throws ScrapeException {
    return search(options, MediaType.TV_SHOW, "series");
  }

  @Override
  public MediaMetadata getMetadata(TvShowSearchAndScrapeOptions options) throws ScrapeException {
    return metadata(options, MediaType.TV_SHOW);
  }

  @Override
  public List<MediaMetadata> getEpisodeList(TvShowSearchAndScrapeOptions options) throws ScrapeException {
    DdysConfig config = config();
    DdysModels.Movie show = resolveMovie(options, MediaType.TV_SHOW);
    List<DdysModels.Resource> resources = resources(show.slug, config);
    if (resources.isEmpty()) {
      return Collections.emptyList();
    }
    List<MediaMetadata> episodes = new ArrayList<>();
    int index = 1;
    for (DdysModels.Resource resource : resources) {
      if (config.directOnly && !resource.direct) {
        continue;
      }
      episodes.add(DdysMapper.toEpisodeMetadata(show, resource, 1, index, config));
      index++;
    }
    return episodes;
  }

  @Override
  public MediaMetadata getMetadata(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException {
    DdysConfig config = config();
    String slug = DdysConfig.trim(options.getIdAsString(PROVIDER_ID));
    int episodeIndex = episodeIndexFrom(slug);
    if (slug.contains("#")) {
      slug = slug.substring(0, slug.indexOf('#'));
    }
    if (slug.isBlank()) {
      Object id = options.getTvShowIds().get(PROVIDER_ID);
      slug = DdysConfig.trim(id);
    }
    if (slug.isBlank()) {
      throw new ScrapeException("DDYS episode scrape needs a show slug");
    }
    DdysModels.Movie show = DdysMapper.movieFromRoot(new DdysClient(config).movie(slug), config);
    if (show.slug.isBlank()) {
      show.slug = slug;
    }
    List<DdysModels.Resource> resources = resources(slug, config);
    if (resources.isEmpty()) {
      return DdysMapper.toEpisodeMetadata(show, new DdysModels.Resource(), 1, episodeIndex, config);
    }
    List<DdysModels.Resource> filtered = new ArrayList<>();
    for (DdysModels.Resource resource : resources) {
      if (!config.directOnly || resource.direct) {
        filtered.add(resource);
      }
    }
    if (filtered.isEmpty()) {
      return DdysMapper.toEpisodeMetadata(show, new DdysModels.Resource(), 1, episodeIndex, config);
    }
    int selected = Math.min(Math.max(episodeIndex, 1), filtered.size()) - 1;
    return DdysMapper.toEpisodeMetadata(show, filtered.get(selected), 1, selected + 1, config);
  }

  private static int episodeIndexFrom(String id) {
    String value = DdysConfig.trim(id);
    int marker = value.indexOf('#');
    if (marker < 0 || marker + 1 >= value.length()) {
      return 1;
    }
    try {
      return Math.max(1, Integer.parseInt(value.substring(marker + 1)));
    }
    catch (NumberFormatException e) {
      return 1;
    }
  }
}
