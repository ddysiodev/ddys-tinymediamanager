package org.ddys.tinymediamanager;

import static org.ddys.tinymediamanager.DdysConfig.trim;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaEpisodeGroup;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.util.DateUtils;

final class DdysMapper {
  private static final Pattern YEAR = Pattern.compile("(19|20)\\d{2}");

  private DdysMapper() {
  }

  static List<DdysModels.Movie> moviesFromRoot(Object root, DdysConfig config) {
    List<DdysModels.Movie> out = new ArrayList<>();
    for (Object item : arrayItems(dataNode(root))) {
      DdysModels.Movie movie = movieFrom(item, config);
      if (!movie.isEmpty()) {
        out.add(movie);
      }
    }
    return out;
  }

  static DdysModels.Movie movieFromRoot(Object root, DdysConfig config) {
    Object data = dataNode(root);
    if (data instanceof List<?> list && !list.isEmpty()) {
      return movieFrom(list.get(0), config);
    }
    return movieFrom(data, config);
  }

  static DdysModels.Movie movieFrom(Object raw, DdysConfig config) {
    Map<String, Object> item = asMap(raw);
    DdysModels.Movie movie = new DdysModels.Movie();
    if (item.isEmpty()) {
      return movie;
    }
    movie.slug = first(item, "slug", "id", "movie_id", "movieId", "vod_id", "permalink");
    movie.title = first(item, "title", "name", "vod_name", "cn_name", "zh_name");
    movie.originalTitle = first(item, "original_title", "originalTitle", "en_name", "alias", "aka");
    movie.poster = config.absUrl(first(item, "poster", "cover", "pic", "vod_pic", "image", "thumbnail"));
    movie.fanart = config.absUrl(first(item, "fanart", "backdrop", "background", "vod_pic_slide"));
    movie.url = config.absUrl(first(item, "url", "link", "href", "page", "site_url"));
    movie.releaseDate = first(item, "release_date", "releaseDate", "date", "pubdate", "vod_pubdate");
    movie.overview = cleanHtml(first(item, "overview", "intro", "description", "summary", "content", "vod_content", "plot"));
    movie.typeName = first(item, "type_name", "typeName", "type", "category", "vod_class", "class");
    movie.remarks = first(item, "remarks", "vod_remarks", "episode", "episode_text", "note", "score", "rate");
    movie.runtime = first(item, "runtime", "duration", "vod_duration");
    movie.year = firstYear(item);
    movie.rating = firstFloat(item, "rating", "score", "rate", "douban_score", "imdb_score");
    movie.countries.addAll(strings(item, "country", "countries", "region", "area", "vod_area"));
    movie.directors.addAll(strings(item, "director", "directors", "vod_director"));
    movie.actors.addAll(strings(item, "actor", "actors", "cast", "vod_actor", "stars"));
    movie.tags.addAll(strings(item, "genre", "genres", "tag", "tags", "type", "category", "vod_class"));
    if (movie.slug.isBlank() && !movie.url.isBlank()) {
      movie.slug = movie.url.replaceAll("/+$", "").replaceAll("^.*/", "");
    }
    if (movie.title.isBlank()) {
      movie.title = movie.slug;
    }
    return movie;
  }

  static List<DdysModels.Resource> resourcesFromRoot(Object root, DdysConfig config) {
    Object data = dataNode(root);
    List<DdysModels.Resource> resources = new ArrayList<>();
    Set<String> seen = new LinkedHashSet<>();

    if (data instanceof List<?> list) {
      boolean grouped = list.stream().anyMatch(item -> !collectArrays(asMap(item)).isEmpty());
      int groupIndex = 1;
      for (Object item : list) {
        if (grouped && item instanceof Map<?, ?>) {
          Map<String, Object> group = asMap(item);
          String groupName = first(group, "name", "title", "label", "source", "type");
          if (groupName.isBlank()) {
            groupName = "线路 " + groupIndex;
          }
          addResources(resources, seen, collectArrays(group), groupName, config);
        }
        else {
          addResource(resources, seen, resourceFrom(item, "Online", config));
        }
        groupIndex++;
      }
      return resources;
    }

    Map<String, Object> map = asMap(data);
    if (!map.isEmpty()) {
      for (Map.Entry<String, List<Object>> entry : collectArraysByKey(map).entrySet()) {
        addResources(resources, seen, entry.getValue(), entry.getKey(), config);
      }
      addResource(resources, seen, resourceFrom(map, "Online", config));
    }
    return resources;
  }

  static MediaSearchResult toSearchResult(DdysModels.Movie movie, MediaType type, MediaSearchAndScrapeOptions options) {
    MediaSearchResult result = new MediaSearchResult(DdysProviderSupport.PROVIDER_ID, type);
    result.setProviderId(DdysProviderSupport.PROVIDER_ID);
    result.setId(movie.slug);
    result.setTitle(movie.title);
    result.setOriginalTitle(movie.originalTitle);
    result.setYear(movie.year);
    result.setUrl(movie.url);
    result.setOverview(movie.overview);
    result.setPosterUrl(movie.poster);
    result.calculateScore(options);
    if (movie.title.equalsIgnoreCase(options.getSearchQuery())) {
      result.setScore(1.0f);
    }
    return result;
  }

  static MediaMetadata toMetadata(DdysModels.Movie movie, List<DdysModels.Resource> resources, DdysConfig config) {
    MediaMetadata metadata = baseMetadata(movie, resources, config);
    return metadata;
  }

  static MediaMetadata toEpisodeMetadata(DdysModels.Movie show, DdysModels.Resource resource, int season, int episode, DdysConfig config) {
    MediaMetadata metadata = new MediaMetadata(DdysProviderSupport.PROVIDER_ID);
    metadata.setId(DdysProviderSupport.PROVIDER_ID, show.slug + "#" + episode);
    metadata.setTitle(resource.name.isBlank() ? show.title + " 第 " + episode + " 集" : resource.name);
    metadata.setEpisodeNumber(MediaEpisodeGroup.DEFAULT_AIRED, season, episode);
    metadata.setPlot(resourceSummary(List.of(resource), config));
    metadata.addTag("DDYS");
    metadata.addTag(resource.direct ? "DDYS 直链" : "DDYS 外部资源");
    return metadata;
  }

  static List<MediaArtwork> artwork(DdysModels.Movie movie, MediaArtworkType requested, DdysConfig config) {
    if (!config.includeArtwork) {
      return List.of();
    }
    List<MediaArtwork> out = new ArrayList<>();
    if (matches(requested, MediaArtworkType.POSTER) && !movie.poster.isBlank()) {
      out.add(art(MediaArtworkType.POSTER, movie.poster, 1000, 1500));
    }
    if (matches(requested, MediaArtworkType.BACKGROUND) && !movie.fanart.isBlank()) {
      out.add(art(MediaArtworkType.BACKGROUND, movie.fanart, 1920, 1080));
    }
    if (matches(requested, MediaArtworkType.THUMB)) {
      String thumb = !movie.fanart.isBlank() ? movie.fanart : movie.poster;
      if (!thumb.isBlank()) {
        out.add(art(MediaArtworkType.THUMB, thumb, 640, 360));
      }
    }
    return out;
  }

  private static MediaMetadata baseMetadata(DdysModels.Movie movie, List<DdysModels.Resource> resources, DdysConfig config) {
    MediaMetadata metadata = new MediaMetadata(DdysProviderSupport.PROVIDER_ID);
    metadata.setId(DdysProviderSupport.PROVIDER_ID, movie.slug);
    if (!movie.url.isBlank()) {
      metadata.setId("ddysUrl", movie.url);
    }
    metadata.setTitle(movie.title);
    metadata.setOriginalTitle(movie.originalTitle);
    metadata.setYear(movie.year);
    setDate(metadata, movie.releaseDate);
    String plot = movie.overview;
    if (config.resourceSummary && !resources.isEmpty()) {
      plot = (plot.isBlank() ? "" : plot + "\n\n") + resourceSummary(resources, config);
    }
    metadata.setPlot(plot);
    metadata.setTagline(movie.remarks);
    metadata.setRuntime(parseRuntime(movie.runtime));
    if (movie.rating > 0) {
      metadata.addRating(new MediaRating(DdysProviderSupport.PROVIDER_ID, movie.rating, 0, 10));
    }
    addPeople(metadata, Person.Type.DIRECTOR, movie.directors);
    addPeople(metadata, Person.Type.ACTOR, movie.actors);
    movie.countries.forEach(metadata::addCountry);
    metadata.addTag("DDYS");
    if (!movie.typeName.isBlank()) {
      metadata.addTag(movie.typeName);
    }
    movie.tags.forEach(metadata::addTag);
    artwork(movie, MediaArtworkType.ALL, config).forEach(metadata::addMediaArt);
    return metadata;
  }

  private static void setDate(MediaMetadata metadata, String value) {
    String text = trim(value);
    if (text.isBlank()) {
      return;
    }
    try {
      metadata.setReleaseDate(DateUtils.parseDate(text));
    }
    catch (ParseException ignored) {
      // tMM keeps the year separately; a non-standard date should not fail scraping.
    }
  }

  private static void addPeople(MediaMetadata metadata, Person.Type type, List<String> values) {
    for (String value : values) {
      if (!value.isBlank()) {
        metadata.addCastMember(new Person(type, value));
      }
    }
  }

  private static String resourceSummary(List<DdysModels.Resource> resources, DdysConfig config) {
    StringBuilder out = new StringBuilder("DDYS 资源");
    int index = 1;
    for (DdysModels.Resource resource : resources) {
      if (config.directOnly && !resource.direct) {
        continue;
      }
      out.append('\n')
          .append(index++)
          .append(". ")
          .append(resource.label())
          .append(resource.direct ? " [direct]" : " [external]")
          .append('\n')
          .append(resource.url);
    }
    return out.toString();
  }

  private static DdysModels.Resource resourceFrom(Object raw, String groupName, DdysConfig config) {
    DdysModels.Resource resource = new DdysModels.Resource();
    resource.groupName = groupName;
    if (raw instanceof String string) {
      resource.url = config.absUrl(string);
      resource.name = "资源";
    }
    else {
      Map<String, Object> item = asMap(raw);
      resource.url = config.absUrl(first(item, "url", "link", "href", "src", "file", "play_url", "playUrl", "download_url", "downloadUrl", "magnet", "ed2k"));
      resource.name = first(item, "name", "title", "label", "episode", "episode_name", "quality", "format");
      resource.code = first(item, "extract_code", "extractCode", "code", "password", "passcode");
    }
    resource.direct = isDirect(resource.url);
    if (resource.name.isBlank()) {
      resource.name = resource.direct ? "直链资源" : "外部资源";
    }
    return resource;
  }

  private static void addResources(List<DdysModels.Resource> resources, Set<String> seen, Collection<Object> values, String groupName, DdysConfig config) {
    for (Object value : values) {
      addResource(resources, seen, resourceFrom(value, groupName, config));
    }
  }

  private static void addResource(List<DdysModels.Resource> resources, Set<String> seen, DdysModels.Resource resource) {
    if (resource.url.isBlank() || seen.contains(resource.url)) {
      return;
    }
    seen.add(resource.url);
    resources.add(resource);
  }

  private static MediaArtwork art(MediaArtworkType type, String url, int width, int height) {
    MediaArtwork artwork = new MediaArtwork(DdysProviderSupport.PROVIDER_ID, type);
    artwork.setOriginalUrl(url);
    artwork.setPreviewUrl(url);
    artwork.setLanguage("zh");
    artwork.addImageSize(width, height, url, MediaArtwork.getSizeOrder(type, width));
    return artwork;
  }

  private static boolean matches(MediaArtworkType requested, MediaArtworkType actual) {
    return requested == null || requested == MediaArtworkType.ALL || requested == actual;
  }

  private static Object dataNode(Object root) {
    Map<String, Object> map = asMap(root);
    if (map.isEmpty()) {
      return root;
    }
    for (String key : List.of("data", "items", "results", "movies", "records", "list")) {
      Object value = map.get(key);
      if (value != null) {
        if ("data".equals(key) && value instanceof Map<?, ?> nested) {
          Map<String, Object> nestedMap = asMap(nested);
          for (String nestedKey : List.of("items", "results", "movies", "records", "list")) {
            if (nestedMap.get(nestedKey) != null) {
              return nestedMap.get(nestedKey);
            }
          }
        }
        return value;
      }
    }
    return root;
  }

  private static List<Object> arrayItems(Object value) {
    if (value instanceof List<?> list) {
      return new ArrayList<>(list);
    }
    if (value instanceof Map<?, ?> map) {
      Map<String, Object> typed = asMap(map);
      for (String key : List.of("items", "results", "movies", "records", "list", "data")) {
        Object nested = typed.get(key);
        if (nested instanceof List<?>) {
          return arrayItems(nested);
        }
      }
    }
    return List.of();
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object value) {
    if (value instanceof Map<?, ?> map) {
      Map<String, Object> out = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (entry.getKey() != null) {
          out.put(entry.getKey().toString(), entry.getValue());
        }
      }
      return out;
    }
    return Map.of();
  }

  private static String first(Map<String, Object> map, String... keys) {
    for (String key : keys) {
      Object value = map.get(key);
      if (value == null) {
        continue;
      }
      if (value instanceof Collection<?> collection) {
        for (Object item : collection) {
          String text = trim(item);
          if (!text.isBlank()) {
            return text;
          }
        }
      }
      String text = trim(value);
      if (!text.isBlank()) {
        return text;
      }
    }
    return "";
  }

  private static List<String> strings(Map<String, Object> map, String... keys) {
    Set<String> out = new LinkedHashSet<>();
    for (String key : keys) {
      Object value = map.get(key);
      if (value instanceof Collection<?> collection) {
        collection.stream().map(DdysConfig::trim).filter(text -> !text.isBlank()).forEach(out::add);
      }
      else {
        for (String part : trim(value).split("[,，/、|]")) {
          String text = part.trim();
          if (!text.isBlank()) {
            out.add(text);
          }
        }
      }
    }
    return new ArrayList<>(out);
  }

  private static int firstYear(Map<String, Object> map) {
    String text = first(map, "year", "release_year", "vod_year", "date", "release_date", "releaseDate");
    Matcher matcher = YEAR.matcher(text);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group());
    }
    return 0;
  }

  private static float firstFloat(Map<String, Object> map, String... keys) {
    String text = first(map, keys).replace(",", ".").replaceAll("[^0-9.]", "");
    if (text.isBlank()) {
      return 0;
    }
    try {
      return Float.parseFloat(text);
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  private static int parseRuntime(String runtime) {
    Matcher matcher = Pattern.compile("\\d+").matcher(trim(runtime));
    if (matcher.find()) {
      return Integer.parseInt(matcher.group());
    }
    return 0;
  }

  private static String cleanHtml(String value) {
    return trim(value).replaceAll("<[^>]+>", " ").replace("&nbsp;", " ").replace("&amp;", "&").replaceAll("\\s+", " ").trim();
  }

  private static Map<String, List<Object>> collectArraysByKey(Map<String, Object> group) {
    Map<String, List<Object>> out = new LinkedHashMap<>();
    for (String key : List.of("items", "resources", "episodes", "playlist", "play", "urls", "list", "online", "download", "downloads", "cloud", "netdisk",
        "drive", "magnet", "magnets")) {
      Object value = group.get(key);
      if (value instanceof List<?> list) {
        out.put(key, new ArrayList<>(list));
      }
    }
    return out;
  }

  private static List<Object> collectArrays(Map<String, Object> group) {
    List<Object> out = new ArrayList<>();
    collectArraysByKey(group).values().forEach(out::addAll);
    return out;
  }

  private static boolean isDirect(String url) {
    String lower = trim(url).toLowerCase();
    return lower.matches(".*\\.(m3u8|mp4|m4v|mkv|mov|flv|avi|ts|webm|mpd)([?#].*)?$");
  }
}
