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
$classes = Join-Path $workspace "bin"
$dist = Join-Path $workspace "dist"
$distLib = Join-Path $dist "lib"
$jarPath = Join-Path $dist "base3.jar"

if (-not (Test-Path (Join-Path $classes "base3\Base3.class"))) {
    throw "Missing bin\base3\Base3.class. Compile the Java project in VS Code first."
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

New-Item -ItemType Directory -Force -Path $distLib | Out-Null
foreach ($library in $libraries) {
    $source = Join-Path $workspace "lib\$library"
    if (-not (Test-Path $source)) {
        throw "Missing dependency: lib\$library"
    }
    Copy-Item $source (Join-Path $distLib $library) -Force
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
