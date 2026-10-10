$ErrorActionPreference = 'Stop'
$manifest = Join-Path $PSScriptRoot '../target/local-run/processes.json'
if (!(Test-Path -LiteralPath $manifest)) { Write-Output 'No prepared local processes recorded.'; exit }
foreach ($record in (Get-Content -LiteralPath $manifest -Raw | ConvertFrom-Json)) {
    $taskProcess = Get-Process -Id $record.Pid -ErrorAction SilentlyContinue
    if (!$taskProcess) { continue }
    # A reused PID must never terminate a different program.
    if ($taskProcess.ProcessName -ne $record.ProcessName -or $taskProcess.StartTime.ToUniversalTime().ToString('o') -ne $record.StartTime) {
        Write-Output "Skipping stale process record for $($record.Service)."
        continue
    }
    $taskProcess.Kill($true)
    Write-Output "$($record.Service) stopped."
}
