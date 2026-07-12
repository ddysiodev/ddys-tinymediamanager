package org.ddys.tinymediamanager;

import java.util.Collections;
import java.util.List;

import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.SubtitleSearchAndScrapeOptions;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieSubtitleProvider;

public class DdysMovieSubtitleProvider extends DdysProviderSupport implements IMovieSubtitleProvider {
  public DdysMovieSubtitleProvider() {
    super("movie_subtitle", "DDYS", "低端影视电影字幕 scraper");
  }

  @Override
  public List<SubtitleSearchResult> search(SubtitleSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return Collections.emptyList();
  }
}
