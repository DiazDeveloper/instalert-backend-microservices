param([string]$OutputPath = (Join-Path $PSScriptRoot '../.env.local'))
$ErrorActionPreference = 'Stop'
if (Test-Path -LiteralPath $OutputPath) { throw 'Environment file already exists; refusing to replace credentials.' }
function New-RandomValue {
    $bytes = [byte[]]::new(32)
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    return [Convert]::ToBase64String($bytes)
}
$values = @(
    "ACCOUNT_DB_PASSWORD=$(New-RandomValue)",
    "ACCOUNT_DB_ROOT_PASSWORD=$(New-RandomValue)",
    "MONOLITH_DB_PASSWORD=$(New-RandomValue)",
    "MONOLITH_DB_ROOT_PASSWORD=$(New-RandomValue)",
    "ACCOUNT_JWT_SECRET=$(New-RandomValue)"
)
[System.IO.File]::WriteAllLines([System.IO.Path]::GetFullPath($OutputPath), $values)
Write-Output 'Local environment file created. Credentials were not printed.'
