$ErrorActionPreference = "Stop"

$LanIp = Get-NetIPAddress -AddressFamily IPv4 |
  Where-Object {
    $_.IPAddress -ne "127.0.0.1" -and
    $_.IPAddress -notlike "169.254.*" -and
    $_.PrefixOrigin -ne "WellKnown"
  } |
  Select-Object -First 1 -ExpandProperty IPAddress

if (-not $LanIp) {
  $LanIp = "YOUR_WINDOWS_LAN_IP"
}

Write-Host ""
Write-Host "Local demo URLs" -ForegroundColor Green
Write-Host "Backend API:   http://127.0.0.1:8080/api"
Write-Host "Admin web:     http://127.0.0.1:5173"
Write-Host "Admin web LAN: http://$LanIp`:5173"
Write-Host ""
Write-Host "Miniapp backend URL for real-device debugging:" -ForegroundColor Yellow
Write-Host "restaurant_api_base_url = http://$LanIp`:8080/api"
Write-Host ""
Write-Host "Admin account: admin / 123456"
