param(
  [switch]$InitDatabase
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

if ($InitDatabase) {
  & (Join-Path $PSScriptRoot "init-database.ps1")
}

$BackendScript = Join-Path $PSScriptRoot "start-backend.ps1"
$AdminScript = Join-Path $PSScriptRoot "start-admin-web.ps1"

Start-Process powershell -ArgumentList @("-NoExit", "-ExecutionPolicy", "Bypass", "-File", $BackendScript) -WorkingDirectory $Root
Start-Sleep -Seconds 6
Start-Process powershell -ArgumentList @("-NoExit", "-ExecutionPolicy", "Bypass", "-File", $AdminScript) -WorkingDirectory $Root

Start-Sleep -Seconds 2
& (Join-Path $PSScriptRoot "print-demo-urls.ps1")
