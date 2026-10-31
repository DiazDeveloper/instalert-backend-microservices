param([switch]$IncludeFrontend)
$ErrorActionPreference = 'Stop'
$backendRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
foreach ($line in [IO.File]::ReadAllLines((Join-Path $backendRoot '.env.local'))) {
    if ($line -match '^([A-Z][A-Z0-9_]*)=(.*)$') { [Environment]::SetEnvironmentVariable($Matches[1],$Matches[2],'Process') }
}
$javaHome = [IO.Path]::GetFullPath((Join-Path $backendRoot '../../../.tools/jdk21/jdk-21.0.12.1+1'))
$java = Join-Path $javaHome 'bin/java.exe'
if (!(Test-Path -LiteralPath $java)) { $java = Join-Path $env:JAVA_HOME 'bin/java.exe' }
$logRoot = Join-Path $backendRoot 'target/local-run'
New-Item -ItemType Directory -Force -Path $logRoot | Out-Null
$services = @(
    @{Name='account';Port=8081;Jar='services/ms-account/target/ms-account-1.0.0-SNAPSHOT.jar'},
    @{Name='emergencies';Port=8083;Jar='services/ms-emergencies/target/ms-emergencies-1.0.0-SNAPSHOT.jar'},
    @{Name='monolith';Port=8082;Jar='target/instalert-backend-0.0.1-SNAPSHOT.jar'},
    @{Name='gateway';Port=8080;Jar='services/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar'}
)
foreach ($service in $services) {
    $probe = [Net.Sockets.TcpClient]::new()
    try { $probe.Connect('127.0.0.1',$service.Port); $running=$true } catch { $running=$false } finally { $probe.Dispose() }
    if ($running) { Write-Output "$($service.Name): port $($service.Port) already in use; existing process preserved."; continue }
    $jar = Join-Path $backendRoot $service.Jar
    if (!(Test-Path -LiteralPath $jar)) { throw "Build missing: $jar. Package this service first." }
    $process = Start-Process -FilePath $java -ArgumentList @('-jar', $jar) -WorkingDirectory $backendRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logRoot "$($service.Name)-startup.log") -RedirectStandardError (Join-Path $logRoot "$($service.Name)-error.log")
    Write-Output "$($service.Name): PID $($process.Id), port $($service.Port)"
}
if ($IncludeFrontend) {
    $frontRoot = [IO.Path]::GetFullPath((Join-Path $backendRoot '../../Instalert-FrontEnd-main/Instalert-FrontEnd-main'))
    $node = (Get-Command node.exe).Source
    $probe = [Net.Sockets.TcpClient]::new()
    try { $probe.Connect('127.0.0.1',4200); $running=$true } catch { $running=$false } finally { $probe.Dispose() }
    if (!$running) {
        $process = Start-Process -FilePath $node -ArgumentList @('node_modules/@angular/cli/bin/ng.js','serve','--host','127.0.0.1','--port','4200') -WorkingDirectory $frontRoot -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logRoot 'frontend-startup.log') -RedirectStandardError (Join-Path $logRoot 'frontend-error.log')
        Write-Output "frontend: PID $($process.Id), http://localhost:4200"
    }
}
Write-Output "Logs: $logRoot. Startup completion must be checked in the logs."
