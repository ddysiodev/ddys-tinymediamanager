import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';

const tests = [];

test('addon registers all tMM providers', async () => {
  const source = await readFile('src/main/java/org/ddys/tinymediamanager/DDYSAddonProvider.java', 'utf8');
  for (const provider of [
    'DdysMovieMetadataProvider.class',
    'DdysTvShowMetadataProvider.class',
    'DdysMovieArtworkProvider.class',
    'DdysTvShowArtworkProvider.class',
    'DdysMovieTrailerProvider.class',
    'DdysTvShowTrailerProvider.class',
    'DdysMovieSubtitleProvider.class',
    'DdysTvShowSubtitleProvider.class'
  ]) {
    assert.ok(source.includes(provider), `missing ${provider}`);
  }
});

test('client handles DDYS HTTP edge cases', async () => {
  const source = await readFile('src/main/java/org/ddys/tinymediamanager/DdysClient.java', 'utf8');
  for (const fragment of [
    'timeout(Duration.ofSeconds(config.timeoutSeconds))',
    'followRedirects(HttpClient.Redirect.NORMAL)',
    'Authorization", "Bearer "',
    'Invalid DDYS API URL',
    'DDYS API returned HTTP',
    'DDYS API returned empty JSON',
    'DDYS API returned invalid JSON',
    'Thread.currentThread().interrupt()'
  ]) {
    assert.ok(source.includes(fragment), `missing ${fragment}`);
  }
});

test('mapper supports metadata, artwork, resources, and episodes', async () => {
  const source = await readFile('src/main/java/org/ddys/tinymediamanager/DdysMapper.java', 'utf8');
  for (const fragment of [
    'MediaSearchResult',
    'MediaMetadata',
    'new MediaArtwork',
    'new MediaRating',
    'new Person',
    'resourceSummary',
    'safeResourceSummary',
    'setEpisodeNumber(MediaEpisodeGroup.DEFAULT_AIRED',
    '"items", "results", "movies", "records", "list"',
    '"resources", "episodes", "playlist", "play", "urls"'
  ]) {
    assert.ok(source.includes(fragment), `missing ${fragment}`);
  }
});

test('tv episode metadata keeps the selected DDYS resource index', async () => {
  const source = await readFile('src/main/java/org/ddys/tinymediamanager/DdysTvShowMetadataProvider.java', 'utf8');
  for (const fragment of [
    'episodeIndexFrom',
    'value.indexOf(\'#\')',
    'Math.max(1, Integer.parseInt',
    'filtered.get(selected)',
    'selected + 1'
  ]) {
    assert.ok(source.includes(fragment), `missing ${fragment}`);
  }
});

test('config exposes complete scraper settings', async () => {
  const source = await readFile('src/main/java/org/ddys/tinymediamanager/DdysConfig.java', 'utf8');
  for (const fragment of [
    'apiBase',
    'siteBase',
    'MediaProviderInfo.API_KEY',
    'pageSize',
    'timeoutSeconds',
    'directOnly',
    'resourceSummary',
    'includeArtwork',
    'userAgent'
  ]) {
    assert.ok(source.includes(fragment), `missing ${fragment}`);
  }
});

for (const entry of tests) {
  await entry.fn();
}

console.log(JSON.stringify({ ok: true, tests: tests.length }, null, 2));

function test(name, fn) {
  tests.push({ name, fn });
}
