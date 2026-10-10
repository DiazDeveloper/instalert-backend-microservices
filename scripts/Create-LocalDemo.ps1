# Explicit local-only demo fixture. Does not bypass JWT, modify roles or reset accounts.
$ErrorActionPreference = 'Stop'
$api = 'http://localhost:8080/api/v1/authentication'
$credentials = @{email='demo@instalert.local';password='DemoInstalert2026!'}
$registration = $credentials.Clone()
$registration.name = 'Cuenta de demostración'
$response = Invoke-WebRequest -Uri "$api/sign-up" -Method Post -ContentType 'application/json' -Body ($registration | ConvertTo-Json) -SkipHttpErrorCheck
if ($response.StatusCode -notin 201,409) { throw "Demo registration failed: HTTP $($response.StatusCode)" }
$login = Invoke-WebRequest -Uri "$api/sign-in" -Method Post -ContentType 'application/json' -Body ($credentials | ConvertTo-Json) -SkipHttpErrorCheck
if ($login.StatusCode -ne 200) { throw 'A different demo account already exists; credentials were not replaced.' }
Write-Output 'Local demo account ready. Uses the real Account authentication and citizen permissions.'
