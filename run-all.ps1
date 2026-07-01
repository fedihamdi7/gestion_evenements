# =============================================================================
# Starts the WHOLE stack locally, STRICTLY ONE BY ONE, in the correct order.
# Each service waits until its port is live before the next one starts, so the
# boot order is guaranteed (no race / "messed up" startups).
#
#   Order:  config-server -> eureka -> keycloak -> microservices -> gateway
#
# Each service runs in its OWN TAB of a single Windows Terminal window (so you can
# read each log). If Windows Terminal (wt) is not installed, it falls back to one
# separate window per service.
#
# Prerequisites:
#   - MySQL running (Windows service "MySQL84", root/root) for users + avis services.
#   - Java 17 on PATH (services use the bundled Maven wrapper, no Maven install needed).
#
# Usage:   ./run-all.ps1            (starts everything incl. Keycloak)
#          ./run-all.ps1 -NoKeycloak
# =============================================================================
param(
    [switch]$NoKeycloak
)

$ErrorActionPreference = "Stop"
$root   = Split-Path -Parent $MyInvocation.MyCommand.Path
$helper = Join-Path $root "scripts\start-module.ps1"

# Use Windows Terminal tabs if available, otherwise separate windows.
$useTabs = [bool](Get-Command wt -ErrorAction SilentlyContinue)
if ($useTabs) { Write-Host "Windows Terminal found - each service opens in its own TAB." -ForegroundColor DarkCyan }
else          { Write-Host "Windows Terminal not found - each service opens in its own WINDOW." -ForegroundColor DarkYellow }

# Every port this script will use, in start order.
$allPorts = [ordered]@{
    "config-server"        = 8888
    "eureka-server"        = 8761
    "keycloak"             = 8080
    "service-utilisateurs" = 8081
    "service-evenements"   = 8082
    "service-avis"         = 8083
    "service-reservation"  = 8084
    "service-messaging"    = 8085
    "api-gateway"          = 9090
}

# Pre-flight: if a port is already taken, a service would crash with
# "Unable to start web server". Catch that NOW with a clear message.
$busy = @()
foreach ($name in $allPorts.Keys) {
    if ($name -eq "keycloak" -and $NoKeycloak) { continue }
    $port = $allPorts[$name]
    if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
        $busy += "$name (port $port)"
    }
}
if ($busy.Count -gt 0) {
    Write-Host "`nThese ports are already in use, so startup would fail:" -ForegroundColor Red
    $busy | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host "Run  ./stop-all.ps1  (or close the old service windows) first, then run this again.`n" -ForegroundColor Yellow
    exit 1
}

# Launches a powershell process (a tab in the shared WT window, or a new window).
function Launch {
    param([string]$Title, [string[]]$PsArgs)
    if ($useTabs) {
        # -w 0 = reuse the same Windows Terminal window and add a new tab to it.
        wt -w 0 new-tab --title $Title powershell @PsArgs
    } else {
        Start-Process powershell -ArgumentList $PsArgs
    }
    Start-Sleep -Milliseconds 800   # give the tab/window a moment to spawn
}

function Start-ModuleStep {
    param([string]$Dir, [string]$Title, [int]$Port, [int]$TimeoutSec = 180)
    $path = Join-Path $root $Dir
    Launch -Title $Title -PsArgs @("-NoExit", "-ExecutionPolicy", "Bypass", "-File", $helper, "-Dir", $path, "-Title", $Title)
    $null = Wait-Port -Port $Port -Name $Title -TimeoutSec $TimeoutSec
}

# Same idea for the NestJS (Node) messaging service — launched via npm, not Maven.
function Start-NodeStep {
    param([string]$Dir, [string]$Title, [int]$Port, [int]$TimeoutSec = 240)
    $path = Join-Path $root $Dir
    $nodeHelper = Join-Path $root "scripts\start-messaging.ps1"
    Launch -Title $Title -PsArgs @("-NoExit", "-ExecutionPolicy", "Bypass", "-File", $nodeHelper, "-Dir", $path, "-Title", $Title)
    $null = Wait-Port -Port $Port -Name $Title -TimeoutSec $TimeoutSec
}

function Wait-Port {
    param([int]$Port, [string]$Name, [int]$TimeoutSec = 180)
    Write-Host "Waiting for $Name (port $Port)..." -ForegroundColor Cyan
    $sw = [Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $TimeoutSec) {
        if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) {
            Write-Host "  OK - $Name is up.`n" -ForegroundColor Green
            return $true
        }
        Start-Sleep -Seconds 2
    }
    Write-Host "  WARNING - $Name not up after $TimeoutSec s (port $Port). Continuing anyway.`n" -ForegroundColor Yellow
    return $false
}

Write-Host "=== Starting Gestion d'Evenements (local, one by one) ===`n" -ForegroundColor Yellow

# 1. Config Server FIRST — every other service fetches its config from it.
Start-ModuleStep -Dir "config-server" -Title "1-config-server" -Port 8888

# 2. Eureka registry.
Start-ModuleStep -Dir "eureka-server" -Title "2-eureka-server" -Port 8761

# 3. Keycloak (login server). First run downloads ~120 MB, so allow more time.
if (-not $NoKeycloak) {
    Launch -Title "3-keycloak" -PsArgs @("-NoExit", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $root "keycloak\run-keycloak.ps1"))
    Wait-Port -Port 8080 -Name "3-keycloak" -TimeoutSec 300 | Out-Null
}

Start-ModuleStep -Dir "api-gateway" -Title "8-api-gateway" -Port 9090
# 4. Microservices — one by one (service-evenements downloads embedded Mongo on first run).
Start-ModuleStep -Dir "service-utilisateurs" -Title "4-service-utilisateurs" -Port 8081
Start-ModuleStep -Dir "service-evenements"   -Title "5-service-evenements"   -Port 8082 -TimeoutSec 300
Start-ModuleStep -Dir "service-avis"         -Title "6-service-avis"         -Port 8083
Start-ModuleStep -Dir "ReservationEvenement" -Title "7-service-reservation"  -Port 8084

# NestJS instant-messaging service (needs MySQL). First run does `npm install`.
Start-NodeStep   -Dir "service-messaging"    -Title "8-service-messaging"   -Port 8085

# 5. API Gateway last (single entry point + Swagger UI).

Write-Host "All services are up. Useful URLs:" -ForegroundColor Yellow
Write-Host "  Eureka dashboard : http://localhost:8761"
Write-Host "  API Gateway      : http://localhost:9090"
Write-Host "  Swagger (all API): http://localhost:9090/swagger-ui.html"
Write-Host "  Keycloak admin   : http://localhost:8080  (admin/admin)"
Write-Host "`nTo stop everything: run  ./stop-all.ps1" -ForegroundColor Yellow
