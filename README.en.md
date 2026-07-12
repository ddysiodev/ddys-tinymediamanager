# ddys-tinymediamanager

Official tinyMediaManager scraper addon for the DDYS API. It adds DDYS as a movie and TV show metadata source for Chinese metadata, posters, fanart, resource summaries, and NFO library workflows.

## Features

- Movie metadata scraper: search, details, year, plot, rating, poster, fanart, cast, directors, countries, and tags.
- TV show metadata scraper: show search, show details, episode list, and episode resource summaries.
- Artwork scrapers: poster, fanart, and thumb entries for movies and TV shows.
- Resource summaries: direct media links, cloud drive links, magnets, download pages, and extraction codes can be written into metadata summaries.
- Broad DDYS API shape support: `data/items/results/list/movies/records` and common slug/title/poster/source fields.
- Settings: API Base, Site Base, API Key, page size, timeout, direct-only mode, and resource summary toggle.
- Release package: installable jar, README, and LICENSE.

## Installation

1. Download `ddys-tinymediamanager-v0.1.1.zip` from GitHub Releases.
2. Extract it and place `ddys-tinymediamanager-0.1.1.jar` into the tinyMediaManager `addons` folder.
3. Restart tinyMediaManager.
4. Enable `DDYS` in scraper settings and configure API Base/API Key if needed.

Default API Base:

```text
https://ddys.io/api/v1
```

Public read endpoints do not require an API Key by default. When configured, requests include:

```http
Authorization: Bearer <apiKey>
```
