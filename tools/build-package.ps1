param(
    [string]$Version = "0.1.1"
)

$ErrorActionPreference = "Stop"
if ($Version.StartsWith("v")) {
    $Version = $Version.Substring(1)
}

$Root = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$ReleaseDirPath = Join-Path $Root "..\..\releases"
New-Item -ItemType Directory -Force -Path $ReleaseDirPath | Out-Null
$ReleaseDir = (Resolve-Path -LiteralPath $ReleaseDirPath).Path
$Zip = Join-Path $ReleaseDir ("ddys-tinymediamanager-v{0}.zip" -f $Version)
$PackageRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("ddys-tinymediamanager-package-" + [guid]::NewGuid().ToString("N"))
$PackageDir = Join-Path $PackageRoot "ddys-tinymediamanager"

$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvn) {
    throw "Maven is required to build the tinyMediaManager addon package."
}

$pushed = $false
try {
    Push-Location $Root
    $pushed = $true
    & $mvn.Source -B -ntp -DskipTests package
    if ($LASTEXITCODE -ne 0) {
        throw "mvn package failed."
    }
    Pop-Location
    $pushed = $false

    New-Item -ItemType Directory -Force -Path $PackageDir | Out-Null
    $jar = Get-ChildItem -LiteralPath (Join-Path $Root "target") -Filter "ddys-tinymediamanager-*.jar" -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $jar) {
        throw "Built jar was not found."
    }

    Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $PackageDir $jar.Name) -Force
    Copy-Item -LiteralPath (Join-Path $Root "README.md") -Destination (Join-Path $PackageDir "README.md") -Force
    Copy-Item -LiteralPath (Join-Path $Root "README.en.md") -Destination (Join-Path $PackageDir "README.en.md") -Force
    Copy-Item -LiteralPath (Join-Path $Root "LICENSE") -Destination (Join-Path $PackageDir "LICENSE") -Force

    if (Test-Path -LiteralPath $Zip) {
        Remove-Item -LiteralPath $Zip -Force
    }
    Compress-Archive -Path (Join-Path $PackageDir "*") -DestinationPath $Zip -Force
    $Hash = (Get-FileHash -LiteralPath $Zip -Algorithm SHA256).Hash
    [pscustomobject]@{
        ok = $true
        package = $Zip
        sha256 = $Hash
        jar = $jar.Name
    } | ConvertTo-Json -Depth 3
} finally {
    if ($pushed) {
        Pop-Location
    }
    $tempBase = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
    $packageRootFull = [System.IO.Path]::GetFullPath($PackageRoot)
    if ($packageRootFull.StartsWith($tempBase, [System.StringComparison]::OrdinalIgnoreCase) -and
        (Test-Path -LiteralPath $PackageRoot)) {
        Remove-Item -LiteralPath $PackageRoot -Recurse -Force
    }
}
