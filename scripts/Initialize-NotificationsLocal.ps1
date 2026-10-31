# Run once before starting Communities. Never changes existing records.
$ErrorActionPreference='Stop'
$backendRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$mysql='C:/Program Files/MySQL/MySQL Server 9.7/bin/mysql.exe'
if (!(Test-Path -LiteralPath $mysql)) { throw 'Install MySQL client or update the mysql path in this local helper.' }
$password=Read-Host 'Local MySQL root password' -AsSecureString
$ptr=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
try {
 $env:MYSQL_PWD=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
 Get-Content -Raw (Join-Path $backendRoot 'src/main/resources/community-notifications.sql') | & $mysql -u root -h localhost instalert_db
 if($LASTEXITCODE -ne 0){throw 'Schema initialization failed'}
 foreach($table in @('notification_contacts','community_notification_preferences','community_processed_alert_events','community_alert_recipients','community_alert_inbox')){
  & $mysql -u root -h localhost -e "GRANT SELECT, INSERT, UPDATE, DELETE ON instalert_db.$table TO 'instalert_monolith_app'@'localhost'"
  if($LASTEXITCODE -ne 0){throw "Grant failed for $table"}
 }
} finally {Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue;[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)}
$envFile=Join-Path $backendRoot '.env.local'
if(!([IO.File]::ReadAllText($envFile) -match '(?m)^NOTIFICATIONS_INTERNAL_KEY=')){
 $bytes=New-Object byte[] 32; $rng=[Security.Cryptography.RandomNumberGenerator]::Create(); try {$rng.GetBytes($bytes)} finally {$rng.Dispose()}
 [IO.File]::AppendAllText($envFile,"`nNOTIFICATIONS_INTERNAL_KEY=$([BitConverter]::ToString($bytes).Replace('-',''))`n")
}
Write-Output 'Local notification schema and service configuration ready. Rebuild/restart Communities and Emergencies.'
