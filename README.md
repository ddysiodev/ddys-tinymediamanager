# ddys-tinymediamanager

低端影视 API 的官方 tinyMediaManager 第三方 scraper 插件。安装后可在 tinyMediaManager 中把 DDYS 作为电影和剧集资料源，用于搜索、刮削中文元数据、海报、背景图和资源摘要，并最终生成 Kodi/Jellyfin/Emby/Plex/Infuse 可读取的本地 NFO 媒体库。

## 功能

- 电影元数据 scraper：搜索、详情、年份、简介、评分、海报、背景图、演员、导演、地区、标签。
- 剧集元数据 scraper：剧集搜索、剧集详情、分集列表、分集资源摘要。
- 图片 scraper：电影/剧集 poster、fanart、thumb。
- 资源摘要：直链、网盘、磁力、下载页、提取码写入简介/标签，方便 NFO 留档。
- 兼容 DDYS API 常见响应：`data/items/results/list/movies/records`、多种 slug/title/poster/source 字段。
- 配置项：API Base、Site Base、API Key、分页数量、超时、是否只保留直链、是否写入资源摘要。
- 安装包：Release ZIP 内包含可安装 jar、README 和 LICENSE。

## 安装

1. 从 GitHub Release 下载 `ddys-tinymediamanager-v0.1.0.zip`。
2. 解压后把 `ddys-tinymediamanager-0.1.0.jar` 放入 tinyMediaManager 安装目录的 `addons` 文件夹。
3. 重启 tinyMediaManager。
4. 在 scraper 设置里启用 `DDYS`，按需填写 API Base 和 API Key。

默认 API Base：

```text
https://ddys.io/api/v1
```

公开读取接口默认不需要 API Key。填写 API Key 后请求会附加：

```http
Authorization: Bearer <apiKey>
```

## tinyMediaManager 兼容

适配 tinyMediaManager 5.x 第三方 scraper addon。插件通过 Java SPI 注册 `IAddonProvider`，依赖 tinyMediaManager 运行时提供的 `org.tinymediamanager:tinyMediaManager:[5.0,6.0)` API。

## 说明

- DDYS 的中文分类会写入标签；标准标题、年份、简介、图片、人员和评分写入 tinyMediaManager 原生字段。
- DDYS 资源链接不是 tinyMediaManager 的播放入口，插件会把资源写入简介摘要和分集条目，后续由 tinyMediaManager 生成 NFO。
- 如果启用 `directOnly`，网盘、磁力、下载页等非直链资源不会进入资源摘要。
