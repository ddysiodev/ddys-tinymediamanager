package org.ddys.tinymediamanager;

import org.tinymediamanager.scraper.MediaProviderInfo;

final class DdysConfig {
  static final String API_BASE             = "apiBase";
  static final String SITE_BASE            = "siteBase";
  static final String PAGE_SIZE            = "pageSize";
  static final String TIMEOUT_SECONDS      = "timeoutSeconds";
  static final String DIRECT_ONLY          = "directOnly";
  static final String RESOURCE_SUMMARY     = "resourceSummary";
  static final String INCLUDE_ARTWORK      = "includeArtwork";
  static final String USER_AGENT           = "userAgent";

  final String  apiBase;
  final String  siteBase;
  final String  apiKey;
  final String  userAgent;
  final int     pageSize;
  final int     timeoutSeconds;
  final boolean directOnly;
  final boolean resourceSummary;
  final boolean includeArtwork;

  private DdysConfig(String apiBase, String siteBase, String apiKey, String userAgent, int pageSize, int timeoutSeconds, boolean directOnly,
      boolean resourceSummary, boolean includeArtwork) {
    this.apiBase = trim(apiBase).replaceAll("/+$", "");
    this.siteBase = trim(siteBase).replaceAll("/+$", "");
    this.apiKey = trim(apiKey);
    this.userAgent = trim(userAgent).isBlank() ? "ddys-tinymediamanager/0.1.1" : trim(userAgent);
    this.pageSize = clamp(pageSize, 1, 100);
    this.timeoutSeconds = clamp(timeoutSeconds, 3, 120);
    this.directOnly = directOnly;
    this.resourceSummary = resourceSummary;
    this.includeArtwork = includeArtwork;
  }

  static void addDefaults(MediaProviderInfo info) {
    info.getConfig().addText(API_BASE, "DDYS API Base", "https://ddys.io/api/v1");
    info.getConfig().addText(SITE_BASE, "DDYS Site Base", "https://ddys.io");
    info.getConfig().addText(MediaProviderInfo.API_KEY, "DDYS API Key", "", true);
    info.getConfig().addInteger(PAGE_SIZE, "每次搜索返回数量", 20);
    info.getConfig().addInteger(TIMEOUT_SECONDS, "HTTP 超时秒数", 15);
    info.getConfig().addBoolean(DIRECT_ONLY, "仅写入 m3u8/mp4/mkv 等直链资源", false);
    info.getConfig().addBoolean(RESOURCE_SUMMARY, "在简介里追加 DDYS 资源摘要", true);
    info.getConfig().addBoolean(INCLUDE_ARTWORK, "写入海报和背景图", true);
    info.getConfig().addText(USER_AGENT, "HTTP User-Agent", "ddys-tinymediamanager/0.1.1");
    info.getConfig().load();
  }

  static DdysConfig from(MediaProviderInfo info) {
    return new DdysConfig(
        value(info, API_BASE, "https://ddys.io/api/v1"),
        value(info, SITE_BASE, "https://ddys.io"),
        info.getUserApiKey(),
        value(info, USER_AGENT, "ddys-tinymediamanager/0.1.1"),
        integer(info, PAGE_SIZE, 20),
        integer(info, TIMEOUT_SECONDS, 15),
        info.getConfig().getValueAsBool(DIRECT_ONLY, false),
        info.getConfig().getValueAsBool(RESOURCE_SUMMARY, true),
        info.getConfig().getValueAsBool(INCLUDE_ARTWORK, true));
  }

  String absUrl(String url) {
    String value = trim(url);
    if (value.isBlank() || value.startsWith("http://") || value.startsWith("https://") || value.startsWith("magnet:") || value.startsWith("ed2k://")) {
      return value;
    }
    if (value.startsWith("//")) {
      return "https:" + value;
    }
    String base = value.startsWith("/api/") ? apiBase.replaceFirst("/api/v\\d+$", "") : siteBase;
    if (!value.startsWith("/")) {
      value = "/" + value;
    }
    return base + value;
  }

  private static String value(MediaProviderInfo info, String key, String fallback) {
    String value = trim(info.getConfig().getValue(key));
    return value.isBlank() ? fallback : value;
  }

  private static int integer(MediaProviderInfo info, String key, int fallback) {
    Integer value = info.getConfig().getValueAsInteger(key);
    return value == null ? fallback : value;
  }

  static String trim(Object value) {
    return value == null ? "" : value.toString().trim();
  }

  private static int clamp(int value, int min, int max) {
    if (value < min) {
      return min;
    }
    return Math.min(value, max);
  }
}
