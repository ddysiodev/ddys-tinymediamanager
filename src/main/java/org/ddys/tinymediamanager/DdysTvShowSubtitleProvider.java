package org.ddys.tinymediamanager;

import java.util.Collections;
import java.util.List;

import org.tinymediamanager.scraper.SubtitleSearchAndScrapeOptions;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowSubtitleProvider;

public class DdysTvShowSubtitleProvider extends DdysProviderSupport implements ITvShowSubtitleProvider {
  public DdysTvShowSubtitleProvider() {
    super("tvshow_subtitle", "DDYS", "低端影视剧集字幕 scraper");
  }

  @Override
  public List<SubtitleSearchResult> search(SubtitleSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return Collections.emptyList();
  }
}
