param(
    [string]$JavaHome = "C:\Program Files\Java\jdk-11"
)

$ErrorActionPreference = "Stop"

function Format-ManifestLine {
    param([string]$Line)

    $lines = @()
    $firstLength = [Math]::Min(70, $Line.Length)
    $lines += $Line.Substring(0, $firstLength)
    $offset = $firstLength
    while ($offset -lt $Line.Length) {
        $length = [Math]::Min(69, $Line.Length - $offset)
        $lines += " " + $Line.Substring($offset, $length)
        $offset += $length
    }
    return $lines
}

$workspace = Split-Path -Parent $PSScriptRoot
$classes = Join-Path $workspace "build\package-classes"
$dist = Join-Path $workspace "dist"
$distLib = Join-Path $dist "lib"
$jarPath = Join-Path $dist "base3.jar"

$javac = Join-Path $JavaHome "bin\javac.exe"
if (-not (Test-Path -LiteralPath $javac)) {
    throw "JDK 11 compiler not found: $javac"
}
$compilerVersion = & $javac -version 2>&1
if ($LASTEXITCODE -ne 0 -or "$compilerVersion" -notmatch '^javac 11\.') {
    throw "Packaging requires JDK 11; found: $compilerVersion"
}

$libraries = @(
    "sqlite-jdbc-3.8.10.1.jar",
    "commons-net-3.3.jar",
    "jsch-0.1.54.jar",
    "org.json.jar",
    "Java-WebSocket-1.5.7.jar",
    "jSerialComm-2.7.0.jar",
    "log4j-1.2.17.jar",
    "javax.jar",
    "slf4j-api-2.0.7.jar",
    "logback.jar",
    "gson-2.10.1.jar",
    "jna-platform-5.2.0.jar"
)

foreach ($library in $libraries) {
    $source = Join-Path $workspace "lib\$library"
    if (-not (Test-Path $source)) {
        throw "Missing dependency: lib\$library"
    }
}

# Use a separate, clean output directory, never the IDE's potentially newer classes.
$expectedClasses = [System.IO.Path]::GetFullPath((Join-Path $workspace "build\package-classes"))
if ([System.IO.Path]::GetFullPath($classes) -ne $expectedClasses) {
    throw "Unexpected compiler output directory: $classes"
}
if (Test-Path -LiteralPath $classes) {
    Remove-Item -LiteralPath $classes -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sourceRoot = Join-Path $workspace "src"
$sources = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.java" | ForEach-Object { $_.FullName })
if ($sources.Count -eq 0) {
    throw "No Java source files found in $sourceRoot"
}
$compileClassPath = ($libraries | ForEach-Object { Join-Path $workspace "lib\$_" }) -join ";"
Write-Host "Compiling with $compilerVersion (release 11)..."
& $javac --release 11 -encoding UTF-8 -classpath $compileClassPath -d $classes $sources
if ($LASTEXITCODE -ne 0) {
    throw "Java 11 compilation failed (exit code $LASTEXITCODE). Package not updated."
}
Get-ChildItem -LiteralPath $sourceRoot -Recurse -File | Where-Object { $_.Extension -ne ".java" } | ForEach-Object {
    $relativePath = $_.FullName.Substring($sourceRoot.Length + 1)
    $destination = Join-Path $classes $relativePath
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $destination) | Out-Null
    Copy-Item -LiteralPath $_.FullName -Destination $destination -Force
}

New-Item -ItemType Directory -Force -Path $distLib | Out-Null
foreach ($library in $libraries) {
    Copy-Item -LiteralPath (Join-Path $workspace "lib\$library") -Destination (Join-Path $distLib $library) -Force
}

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
if (Test-Path $jarPath) {
    Remove-Item $jarPath -Force
}

$classPath = ($libraries | ForEach-Object { "lib/$_" }) -join " "
$manifestLines = @(
    "Manifest-Version: 1.0",
    "Main-Class: base3.Base3",
    "Class-Path: $classPath"
) | ForEach-Object { Format-ManifestLine $_ }
$manifest = ($manifestLines -join "`r`n") + "`r`n`r`n"
$archive = [System.IO.Compression.ZipFile]::Open(
    $jarPath,
    [System.IO.Compression.ZipArchiveMode]::Create
)

try {
    $manifestEntry = $archive.CreateEntry("META-INF/MANIFEST.MF")
    $writer = [System.IO.StreamWriter]::new(
        $manifestEntry.Open(),
        [System.Text.UTF8Encoding]::new($false)
    )
    try {
        $writer.Write($manifest)
    }
    finally {
        $writer.Dispose()
    }

    Get-ChildItem $classes -Recurse -File | ForEach-Object {
        $relativePath = $_.FullName.Substring($classes.Length + 1).Replace("\", "/")
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
            $archive,
            $_.FullName,
            $relativePath,
            [System.IO.Compression.CompressionLevel]::Optimal
        ) | Out-Null
    }
}
finally {
    $archive.Dispose()
}

Write-Host "Package created: $jarPath"
