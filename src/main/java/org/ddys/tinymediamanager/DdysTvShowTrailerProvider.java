package org.ddys.tinymediamanager;

import java.util.Collections;
import java.util.List;

import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.scraper.TrailerSearchAndScrapeOptions;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowTrailerProvider;

public class DdysTvShowTrailerProvider extends DdysProviderSupport implements ITvShowTrailerProvider {
  public DdysTvShowTrailerProvider() {
    super("tvshow_trailer", "DDYS", "低端影视剧集预告片 scraper");
  }

  @Override
  public List<MediaTrailer> getTrailers(TrailerSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return Collections.emptyList();
  }
}
