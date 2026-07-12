package org.ddys.tinymediamanager;

import java.util.SortedSet;

import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;

public class DdysMovieMetadataProvider extends DdysProviderSupport implements IMovieMetadataProvider {
  public DdysMovieMetadataProvider() {
    super("movie", "DDYS", "低端影视电影元数据 scraper");
  }

  @Override
  public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions options) throws ScrapeException {
    return search(options, MediaType.MOVIE, "movie");
  }

  @Override
  public MediaMetadata getMetadata(MovieSearchAndScrapeOptions options) throws ScrapeException {
    return metadata(options, MediaType.MOVIE);
  }
}
