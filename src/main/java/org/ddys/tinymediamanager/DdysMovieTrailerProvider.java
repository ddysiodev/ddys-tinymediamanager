package org.ddys.tinymediamanager;

import java.util.Collections;
import java.util.List;

import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.TrailerSearchAndScrapeOptions;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieTrailerProvider;

public class DdysMovieTrailerProvider extends DdysProviderSupport implements IMovieTrailerProvider {
  public DdysMovieTrailerProvider() {
    super("movie_trailer", "DDYS", "低端影视电影预告片 scraper");
  }

  @Override
  public List<MediaTrailer> getTrailers(TrailerSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return Collections.emptyList();
  }
}
