package org.ddys.tinymediamanager;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.scraper.spi.IAddonProvider;

public class DDYSAddonProvider implements IAddonProvider {
  @Override
  public List<Class<? extends IMediaProvider>> getAddonClasses() {
    List<Class<? extends IMediaProvider>> addons = new ArrayList<>();
    addons.add(DdysMovieMetadataProvider.class);
    addons.add(DdysTvShowMetadataProvider.class);
    addons.add(DdysMovieArtworkProvider.class);
    addons.add(DdysTvShowArtworkProvider.class);
    addons.add(DdysMovieTrailerProvider.class);
    addons.add(DdysTvShowTrailerProvider.class);
    addons.add(DdysMovieSubtitleProvider.class);
    addons.add(DdysTvShowSubtitleProvider.class);
    return addons;
  }
}
