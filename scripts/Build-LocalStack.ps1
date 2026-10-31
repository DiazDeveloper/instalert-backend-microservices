param([switch]$SkipTests)
$ErrorActionPreference = 'Stop'
$backendRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$portableJava = [IO.Path]::GetFullPath((Join-Path $backendRoot '../../../.tools/jdk21/jdk-21.0.12.1+1'))
if (Test-Path -LiteralPath (Join-Path $portableJava 'bin/java.exe')) { $env:JAVA_HOME=$portableJava }
if (!$env:JAVA_HOME) { throw 'Configure JAVA_HOME with JDK 21.' }
Push-Location $backendRoot
try {
    foreach ($pom in @('services/ms-account/pom.xml','services/ms-emergencies/pom.xml','services/api-gateway/pom.xml','pom.xml')) {
        $arguments=@('-f',$pom,'package')
        if ($SkipTests) { $arguments+='-DskipTests' }
        & ./mvnw.cmd @arguments
        if ($LASTEXITCODE -ne 0) { throw "Build failed: $pom" }
    }
} finally { Pop-Location }
