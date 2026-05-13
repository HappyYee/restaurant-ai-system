param(
  [string]$MysqlUser = "root",
  [string]$MysqlPassword = "",
  [string]$MysqlHost = "localhost",
  [string]$MysqlPort = "3306"
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$SchemaFile = Join-Path $Root "database\schema.sql"
$DataFile = Join-Path $Root "database\init_data.sql"

if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
  throw "mysql command not found. Install MySQL 8 and add its bin directory to PATH."
}

if (-not $MysqlPassword) {
  $SecurePassword = Read-Host "MySQL password for $MysqlUser" -AsSecureString
  $MysqlPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecurePassword)
  )
}

$env:MYSQL_PWD = $MysqlPassword

Write-Host "Importing schema..." -ForegroundColor Cyan
Get-Content -Raw $SchemaFile | mysql -h $MysqlHost -P $MysqlPort -u $MysqlUser --default-character-set=utf8mb4

Write-Host "Importing demo data..." -ForegroundColor Cyan
Get-Content -Raw $DataFile | mysql -h $MysqlHost -P $MysqlPort -u $MysqlUser --default-character-set=utf8mb4

Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
Write-Host "Database ready: restaurant_ai" -ForegroundColor Green
