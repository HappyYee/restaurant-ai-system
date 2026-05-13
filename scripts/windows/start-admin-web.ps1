param(
  [string]$ApiBaseUrl = "http://127.0.0.1:8080/api"
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$AdminWeb = Join-Path $Root "admin-web"
$EnvFile = Join-Path $PSScriptRoot "env.demo.ps1"

if (Test-Path $EnvFile) {
  . $EnvFile
}

if (-not $env:VITE_API_BASE_URL) {
  $env:VITE_API_BASE_URL = $ApiBaseUrl
}

Set-Location $AdminWeb
if (-not (Test-Path (Join-Path $AdminWeb "node_modules"))) {
  Write-Host "Installing admin-web dependencies..." -ForegroundColor Cyan
  npm install
}

Write-Host "Starting admin web: http://127.0.0.1:5173" -ForegroundColor Green
npm run dev -- --host 0.0.0.0
