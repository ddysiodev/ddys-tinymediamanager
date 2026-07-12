package org.ddys.tinymediamanager;

import java.util.List;

import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowArtworkProvider;

public class DdysTvShowArtworkProvider extends DdysProviderSupport implements ITvShowArtworkProvider {
  public DdysTvShowArtworkProvider() {
    super("tvshow_artwork", "DDYS", "低端影视剧集图片 scraper");
  }

  @Override
  public List<MediaArtwork> getArtwork(ArtworkSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return artwork(options, options.getArtworkType(), MediaType.TV_SHOW);
  }
}
