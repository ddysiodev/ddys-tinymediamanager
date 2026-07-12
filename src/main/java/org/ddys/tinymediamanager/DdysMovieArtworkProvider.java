package org.ddys.tinymediamanager;

import java.util.List;

import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieArtworkProvider;

public class DdysMovieArtworkProvider extends DdysProviderSupport implements IMovieArtworkProvider {
  public DdysMovieArtworkProvider() {
    super("movie_artwork", "DDYS", "低端影视电影图片 scraper");
  }

  @Override
  public List<MediaArtwork> getArtwork(ArtworkSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return artwork(options, options.getArtworkType(), MediaType.MOVIE);
  }
}
