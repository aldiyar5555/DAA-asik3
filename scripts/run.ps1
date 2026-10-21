$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$out = Join-Path $root "out"
$testOut = Join-Path $root "out-test"
$lib = Join-Path $root "lib"
New-Item -ItemType Directory -Force -Path $out, $testOut | Out-Null

$sources = Get-ChildItem -Path (Join-Path $root "src\main\java") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& javac -encoding UTF-8 -d $out $sources
if ($LASTEXITCODE -ne 0) { throw "Main compilation failed" }

$standalone = Join-Path $lib "junit-platform-console-standalone-1.10.2.jar"
if (Test-Path $standalone) {
    $tests = Get-ChildItem -Path (Join-Path $root "src\test\java") -Recurse -Filter *.java | ForEach-Object { $_.FullName }
    & javac -encoding UTF-8 -d $testOut -cp "$out;$standalone" $tests
    if ($LASTEXITCODE -ne 0) { throw "Test compilation failed" }
    & java -jar $standalone execute --class-path "$out;$testOut" --scan-class-path
    if ($LASTEXITCODE -ne 0) { throw "Tests failed" }
}

Write-Host "===== BENCHMARK ====="
& java -cp $out benchmark.metrics.BenchmarkRunner
