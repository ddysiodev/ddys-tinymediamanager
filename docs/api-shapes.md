# DDYS API Shape Notes

The addon accepts several common response shapes:

```json
{ "data": [{ "slug": "movie", "title": "Title" }] }
{ "data": { "items": [] } }
{ "results": [] }
{ "movies": [] }
{ "records": [] }
```

Movie fields are read from common aliases:

```text
slug: slug, id, movie_id, movieId, vod_id
title: title, name, vod_name, cn_name, zh_name
originalTitle: original_title, originalTitle, en_name, alias
poster: poster, cover, pic, vod_pic, image, thumbnail
fanart: fanart, backdrop, background, vod_pic_slide
year: year, release_year, vod_year, date, release_date
plot: overview, intro, description, summary, content, vod_content
```

Source arrays are collected from:

```text
items, resources, episodes, playlist, play, urls, list,
online, download, downloads, cloud, netdisk, drive, magnet, magnets
```
