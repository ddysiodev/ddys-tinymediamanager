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
      return DdysMapper.toEpisodeMetadata(show, new DdysModels.Resource(), 1, 1, config);
    }
    return DdysMapper.toEpisodeMetadata(show, resources.get(0), 1, 1, config);
  }
}
