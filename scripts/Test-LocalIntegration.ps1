param(
    [Parameter(Mandatory)][string]$JavaHome,
    [string]$Maven = (Join-Path $PSScriptRoot '../mvnw.cmd')
)
$ErrorActionPreference = 'Stop'
$backendRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$evidencePath = Join-Path $backendRoot 'target/integration'
New-Item -ItemType Directory -Force -Path $evidencePath | Out-Null
$env:JAVA_HOME = $JavaHome
$processes = @()
function Get-FreePort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    $listener.Start(); $port = $listener.LocalEndpoint.Port; $listener.Stop(); return $port
}
function Start-TestService($name, $mainClass, $classes, $classpathFile, $arguments) {
    $info = [System.Diagnostics.ProcessStartInfo]::new()
    $info.FileName = Join-Path $JavaHome 'bin/java.exe'
    $info.WorkingDirectory = $backendRoot
    $info.UseShellExecute = $false
    $info.CreateNoWindow = $true
    $info.RedirectStandardOutput = $true
    $info.RedirectStandardError = $true
    $info.ArgumentList.Add('-cp')
    $info.ArgumentList.Add("$classes;$([System.IO.File]::ReadAllText($classpathFile).Trim())")
    $info.ArgumentList.Add($mainClass)
    foreach ($arg in $arguments) { $info.ArgumentList.Add($arg) }
    $process = [System.Diagnostics.Process]::Start($info)
    return @{ Name=$name; Process=$process; Output=$process.StandardOutput.ReadToEndAsync(); Error=$process.StandardError.ReadToEndAsync() }
}
function Request($method, $path, $data, $token, $expected) {
    $headers = @{}
    if ($token) { $headers.Authorization = "Bearer $token" }
    $params = @{Uri="http://127.0.0.1:$gatewayPort$path"; Method=$method; Headers=$headers; SkipHttpErrorCheck=$true }
    if ($null -ne $data) { $params.Body=($data | ConvertTo-Json -Depth 5 -Compress); $params.ContentType='application/json' }
    $response = Invoke-WebRequest @params
    if ($response.StatusCode -ne $expected) { throw "Expected $expected for $method $path, got $($response.StatusCode)" }
    if ($response.Content) { return ($response.Content | ConvertFrom-Json) }
}
Push-Location $backendRoot
try {
    foreach ($module in @(@{Name='account'; Pom='services/ms-account/pom.xml'}, @{Name='monolith'; Pom='pom.xml'}, @{Name='gateway'; Pom='services/api-gateway/pom.xml'})) {
        & $Maven -q -f $module.Pom '-DskipTests' compile dependency:build-classpath '-DincludeScope=test' "-Dmdep.outputFile=$evidencePath/$($module.Name).classpath"
        if ($LASTEXITCODE -ne 0) { throw "Build failed for $($module.Name)" }
    }
    $accountPort=Get-FreePort; $monolithPort=Get-FreePort; $gatewayPort=Get-FreePort
    $key=[byte[]]::new(32); [System.Security.Cryptography.RandomNumberGenerator]::Fill($key)
    $accountArgs=@("--server.port=$accountPort", '--spring.datasource.url=jdbc:h2:mem:smoke-account;MODE=MySQL;DB_CLOSE_DELAY=-1',
        '--spring.datasource.username=sa', '--spring.datasource.password=', "--account.jwt.secret=$([Convert]::ToBase64String($key))")
    $processes += Start-TestService 'account' 'com.instalert.account.AccountApplication' "$backendRoot/services/ms-account/target/classes" "$evidencePath/account.classpath" $accountArgs
    $processes += Start-TestService 'monolith' 'com.instalert_backend.InstalertBackendApplication' "$backendRoot/target/classes" "$evidencePath/monolith.classpath" @(
        "--server.port=$monolithPort", '--spring.datasource.url=jdbc:h2:mem:smoke-monolith;MODE=MySQL;DB_CLOSE_DELAY=-1',
        '--spring.datasource.username=sa', '--spring.datasource.password=', '--spring.jpa.hibernate.ddl-auto=create-drop', "--account.service.url=http://127.0.0.1:$accountPort")
    $processes += Start-TestService 'gateway' 'com.instalert.gateway.GatewayApplication' "$backendRoot/services/api-gateway/target/classes" "$evidencePath/gateway.classpath" @(
        "--server.port=$gatewayPort", "--gateway.account-url=http://127.0.0.1:$accountPort", "--gateway.monolith-url=http://127.0.0.1:$monolithPort")
    $ready=$false
    for ($attempt=0; $attempt -lt 60; $attempt++) {
        if ($processes.Where({$_.Process.HasExited}).Count -gt 0) { throw 'A test service exited during startup' }
        try {
            $probe=Invoke-WebRequest "http://127.0.0.1:$gatewayPort/api/v1/account/me" -SkipHttpErrorCheck -TimeoutSec 2
            if ($probe.StatusCode -eq 401) { $ready=$true; break }
        } catch {}
        Start-Sleep -Milliseconds 500
    }
    if (!$ready) { throw 'Account/gateway startup timed out' }
    $a=Request POST '/api/v1/authentication/sign-up' @{name='Smoke A'; email='smoke-a@example.org'; password='Passw0rd!'} $null 201
    $b=Request POST '/api/v1/authentication/sign-up' @{name='Smoke B'; email='smoke-b@example.org'; password='Passw0rd!'} $null 201
    $me=Request GET '/api/v1/account/me' $null $a.token 200
    if ($me.accountId -ne $a.accountId) { throw 'Association mismatch' }
    Request PUT "/api/v1/users/$($b.accountId)" @{email='stolen@example.org'} $a.token 403 | Out-Null
    $unchanged=Request GET '/api/v1/account/me' $null $b.token 200
    if ($unchanged.email -ne 'smoke-b@example.org') { throw 'Denied write changed data' }
    Request POST '/api/v1/authentication/sign-in' @{email='smoke-a@example.org';password='wrong'} $null 401 | Out-Null
    Request GET '/api/v1/users' $null $a.token 403 | Out-Null
    # Wait for the monolith too, then exercise all remaining contexts through the gateway.
    $ready=$false
    for ($attempt=0; $attempt -lt 30; $attempt++) {
        try { Request GET '/api/v1/incidents' $null $a.token 200 | Out-Null; $ready=$true; break } catch { Start-Sleep -Milliseconds 500 }
    }
    if (!$ready) { throw 'Monolith startup timed out' }
    Request POST '/api/v1/communities' @{name='Smoke community';description='Synthetic';isPrivate=$false;ownerId=$me.id} $a.token 201 | Out-Null
    $incident=Request POST '/api/v1/incidents' @{userId=$me.id;type='Test';severity='LOW';address='Synthetic';description='Smoke';status='ACTIVE';latitude=-12.1;longitude=-77.0;timeReported='2026-10-09T00:00:00Z'} $a.token 201
    $alert=Request POST '/api/v1/emergencies' @{userId=$me.id;type='Smoke';location='Synthetic';time='12:00';status='Activa';statusClass='badge-active'} $a.token 201
    if ($incident.userId -ne $me.id -or $alert.userId -ne $me.id) { throw 'Monolith account reference mismatch' }
    Request PATCH "/api/v1/emergencies/$($alert.id)" @{status='Cancelada';statusClass='badge-canceled'} $b.token 403 | Out-Null
    Request POST "/api/v1/authentication/$($a.accountId)/change-password" @{currentPassword='Passw0rd!';newPassword='NewPassw0rd!'} $a.token 204 | Out-Null
    Request GET '/api/v1/account/me' $null $a.token 401 | Out-Null
    Request GET '/api/v1/incidents' $null $a.token 401 | Out-Null
    [System.IO.File]::WriteAllText((Join-Path $evidencePath 'result.json'), (@{Status='passed'; Database='H2 MySQL mode'; Runtime='Java 21'; Checks=15; Timestamp=[DateTime]::UtcNow.ToString('o')} | ConvertTo-Json))
    Write-Output 'Integration passed: three separate processes, 15 API checks, synthetic in-memory data.'
} finally {
    foreach ($item in $processes) {
        if (!$item.Process.HasExited) { $item.Process.Kill($true) }
        $item.Process.WaitForExit()
        [System.IO.File]::WriteAllText((Join-Path $evidencePath "$($item.Name).stdout.log"), $item.Output.Result)
        [System.IO.File]::WriteAllText((Join-Path $evidencePath "$($item.Name).stderr.log"), $item.Error.Result)
        $item.Process.Dispose()
    }
    Pop-Location
}
