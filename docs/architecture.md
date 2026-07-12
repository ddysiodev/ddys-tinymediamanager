# 架构

`ddys-tinymediamanager` 是 tinyMediaManager 5.x 的第三方 scraper addon。

## 入口

- `DDYSAddonProvider` 通过 `META-INF/services/org.tinymediamanager.scraper.spi.IAddonProvider` 注册。
- Provider 列表包含电影/剧集 metadata、artwork、trailer、subtitle 类型。

## 核心模块

- `DdysProviderSupport`：统一创建 `MediaProviderInfo`、读取配置、构建 `DdysClient`。
- `DdysClient`：Java 17 `HttpClient` 请求 DDYS API，处理 Bearer token、超时、状态码和 JSON。
- `DdysJson`：零依赖 JSON 解析器，避免 addon jar 携带额外依赖。
- `DdysMapper`：把 DDYS API 的宽松响应映射为 tMM 的 `MediaSearchResult`、`MediaMetadata`、`MediaArtwork`。
- `DdysModels`：电影条目和资源条目的内部模型。

## 数据流

```text
tinyMediaManager UI
  -> IMovieMetadataProvider / ITvShowMetadataProvider
  -> DdysClient
  -> DDYS API
  -> DdysMapper
  -> MediaSearchResult / MediaMetadata / MediaArtwork
  -> tMM NFO/artwork workflow
```

## 边界

- API 返回空 JSON、非法 JSON、非 2xx 状态会转为 `ScrapeException`。
- API 字段名按多套常见命名读取，无法识别时返回空列表而不是抛 NPE。
- 非直链资源默认保留在资源摘要；`directOnly` 开启后过滤。
- 图片 URL 统一通过 Site Base 转绝对地址。
