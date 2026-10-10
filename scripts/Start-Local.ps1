param(
    [Parameter(Mandatory)][ValidateSet('Account','Monolith','Gateway')][string]$Service
)
$ErrorActionPreference = 'Stop'
$backendRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$settingsPath = Join-Path $backendRoot '.env.local'
if (!(Test-Path -LiteralPath $settingsPath)) { throw 'Missing .env.local. Configure the local connections first.' }
foreach ($line in [System.IO.File]::ReadAllLines($settingsPath)) {
    if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
$portableJava = Join-Path $backendRoot '../../../.tools/jdk21/jdk-21.0.12.1+1'
if (Test-Path -LiteralPath (Join-Path $portableJava 'bin/java.exe')) {
    $env:JAVA_HOME = [System.IO.Path]::GetFullPath($portableJava)
}
if (!$env:JAVA_HOME -or !(Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/java.exe'))) { throw 'Set JAVA_HOME to JDK 21.' }
Push-Location $backendRoot
try {
    switch ($Service) {
        'Account' { & ./mvnw.cmd -f services/ms-account/pom.xml spring-boot:run }
        'Monolith' { & ./mvnw.cmd spring-boot:run }
        'Gateway' { & ./mvnw.cmd -f services/api-gateway/pom.xml spring-boot:run }
    }
    if ($LASTEXITCODE -ne 0) { throw "$Service exited with code $LASTEXITCODE" }
} finally { Pop-Location }
