param(
  [string]$MysqlPassword = "",
  [string]$DeepSeekApiKey = "",
  [string]$DeepSeekModel = "deepseek-v4-pro"
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$Backend = Join-Path $Root "backend"
$EnvFile = Join-Path $PSScriptRoot "env.demo.ps1"

if (Test-Path $EnvFile) {
  . $EnvFile
}

if ($MysqlPassword) {
  $env:MYSQL_PASSWORD = $MysqlPassword
}
if (-not $env:MYSQL_PASSWORD) {
  $SecurePassword = Read-Host "MySQL password" -AsSecureString
  $env:MYSQL_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecurePassword)
  )
}

if ($DeepSeekApiKey) {
  $env:DEEPSEEK_API_KEY = $DeepSeekApiKey
}
if (-not $env:DEEPSEEK_MODEL) {
  $env:DEEPSEEK_MODEL = $DeepSeekModel
}

if (-not $env:MYSQL_USERNAME) { $env:MYSQL_USERNAME = "root" }
if (-not $env:MYSQL_HOST) { $env:MYSQL_HOST = "localhost" }
if (-not $env:MYSQL_PORT) { $env:MYSQL_PORT = "3306" }
if (-not $env:MYSQL_DATABASE) { $env:MYSQL_DATABASE = "restaurant_ai" }

Write-Host "Starting Spring Boot backend: http://127.0.0.1:8080" -ForegroundColor Green
Set-Location $Backend
& .\mvnw.cmd spring-boot:run
