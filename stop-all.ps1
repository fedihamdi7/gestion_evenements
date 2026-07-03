# =============================================================================
# Stops every service started by run-all.ps1 by killing whatever listens on each port.
# (Keycloak runs from its own window; close that window or Ctrl+C it as well.)
# =============================================================================
$ports = @{
    "config-server"        = 8888
    "eureka-server"        = 8761
    "service-utilisateurs" = 8081
    "service-evenements"   = 8082
    "service-avis"         = 8083
    "service-reservation"  = 8084
    "service-messaging"    = 8085
    "api-gateway"          = 9090
    "keycloak"             = 8089
}

foreach ($name in $ports.Keys) {
    $port = $ports[$name]
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    if ($conn) {
        try {
            Stop-Process -Id $conn.OwningProcess -Force -ErrorAction Stop
            Write-Host "Stopped $name (port $port)." -ForegroundColor Green
        } catch {
            Write-Host "Could not stop $name (port $port): $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "$name (port $port) was not running." -ForegroundColor DarkGray
    }
}
