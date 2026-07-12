import fs from 'node:fs/promises';
import path from 'node:path';

const root = process.cwd();
const required = [
  'pom.xml',
  'package.json',
  'README.md',
  'README.en.md',
  'LICENSE',
  '.github/workflows/build.yml',
  'src/main/resources/META-INF/services/org.tinymediamanager.scraper.spi.IAddonProvider',
  'src/main/resources/org/ddys/tinymediamanager/ddys-icon.png',
  'src/main/java/org/ddys/tinymediamanager/DDYSAddonProvider.java',
  'src/main/java/org/ddys/tinymediamanager/DdysMovieMetadataProvider.java',
  'src/main/java/org/ddys/tinymediamanager/DdysTvShowMetadataProvider.java',
  'src/main/java/org/ddys/tinymediamanager/DdysMovieArtworkProvider.java',
  'src/main/java/org/ddys/tinymediamanager/DdysTvShowArtworkProvider.java',
  'src/main/java/org/ddys/tinymediamanager/DdysClient.java',
  'src/main/java/org/ddys/tinymediamanager/DdysJson.java',
  'src/main/java/org/ddys/tinymediamanager/DdysMapper.java'
];

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

async function read(file) {
  return fs.readFile(path.join(root, file), 'utf8');
}

async function exists(file) {
  try {
    await fs.access(path.join(root, file));
    return true;
  }
  catch {
    return false;
  }
}

async function listFiles(dir = root, out = []) {
  for (const entry of await fs.readdir(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) await listFiles(full, out);
    else out.push(full);
  }
  return out;
}

for (const file of required) {
  assert(await exists(file), `Missing required file: ${file}`);
}

const pkg = JSON.parse(await read('package.json'));
assert(pkg.name === 'ddys-tinymediamanager', 'package name mismatch.');
assert(pkg.version === '0.1.0', 'package version mismatch.');
assert(pkg.private === true, 'package must be private.');

const pom = await read('pom.xml');
for (const fragment of [
  '<artifactId>ddys-tinymediamanager</artifactId>',
  '<version>0.1.0</version>',
  '<tinymediamanager.version>[5.0,6.0)</tinymediamanager.version>',
  'https://gitlab.com/api/v4/projects/9945251/packages/maven',
  '<maven.compiler.release>17</maven.compiler.release>'
]) {
  assert(pom.includes(fragment), `pom.xml missing ${fragment}`);
}

const service = await read('src/main/resources/META-INF/services/org.tinymediamanager.scraper.spi.IAddonProvider');
assert(service.trim() === 'org.ddys.tinymediamanager.DDYSAddonProvider', 'SPI service file mismatch.');

const javaFiles = (await listFiles(path.join(root, 'src/main/java'))).filter((file) => file.endsWith('.java'));
assert(javaFiles.length >= 13, 'Expected full provider/source set.');
const java = (await Promise.all(javaFiles.map((file) => fs.readFile(file, 'utf8')))).join('\n');
for (const fragment of [
  'implements IAddonProvider',
  'implements IMovieMetadataProvider',
  'implements ITvShowMetadataProvider',
  'implements IMovieArtworkProvider',
  'implements ITvShowArtworkProvider',
  'implements IMovieTrailerProvider',
  'implements ITvShowTrailerProvider',
  'implements IMovieSubtitleProvider',
  'implements ITvShowSubtitleProvider',
  'HttpClient.newBuilder()',
  'Authorization", "Bearer "',
  'DDYS API returned invalid JSON',
  'MediaArtwork.getSizeOrder',
  'setEpisodeNumber(MediaEpisodeGroup.DEFAULT_AIRED',
  'resourceSummary',
  'directOnly',
  'dataNode',
  'collectArraysByKey',
  'new MediaRating'
]) {
  assert(java.includes(fragment), `Java source missing ${fragment}`);
}

const readme = await read('README.md');
for (const fragment of ['tinyMediaManager', 'addons', 'DDYS', 'NFO', 'API Base', 'Authorization: Bearer']) {
  assert(readme.includes(fragment), `README missing ${fragment}`);
}
assert(!readme.includes('## **开发打包**'), 'README contains unwanted developer packaging section.');

const workflow = await read('.github/workflows/build.yml');
for (const fragment of ['actions/setup-java', 'distribution: temurin', 'java-version: \'17\'', 'mvn -B -ntp package', 'upload-artifact']) {
  assert(workflow.includes(fragment), `Workflow missing ${fragment}`);
}

const files = await listFiles();
for (const file of files) {
  const relative = path.relative(root, file).replaceAll(path.sep, '/');
  const segments = relative.split('/');
  assert(!segments.includes('target'), `target dir leaked: ${relative}`);
  assert(!segments.includes('package'), `package dir leaked: ${relative}`);
  assert(!segments.includes('node_modules'), `node_modules leaked: ${relative}`);
  assert(!/\.(log|tmp|cache|zip|tgz)$/i.test(relative), `generated file leaked: ${relative}`);
  assert(!/(^|\/)\.env($|\.)/i.test(relative), `env file leaked: ${relative}`);
}

const textFiles = files.filter((file) => /\.(java|mjs|js|json|md|txt|properties|xml|ps1|sh|yml|yaml|gitignore)$/i.test(file));
const allText = (await Promise.all(textFiles.map((file) => fs.readFile(file, 'utf8')))).join('\n');
for (const pattern of [/ghp_[A-Za-z0-9_]+/, /github_pat_[A-Za-z0-9_]+/, /npm_[A-Za-z0-9_]+/, /sk-[A-Za-z0-9]{20,}/]) {
  assert(!pattern.test(allText), `secret-like pattern found: ${pattern}`);
}
assert(!allText.includes('\uFFFD'), 'replacement character found in text files.');

console.log(JSON.stringify({ ok: true, package: pkg.name, javaFiles: javaFiles.length }, null, 2));
