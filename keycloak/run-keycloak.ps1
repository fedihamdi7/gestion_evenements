# ---------------------------------------------------------------------------
# Runs a LOCAL Keycloak (no Docker, no install) using the Java 17 you already have.
# First run downloads the Keycloak ZIP (~120 MB) next to this script and unzips it.
# It imports the "gestion-evenements" realm (roles ADMIN/ORGANISATEUR/PARTICIPANT
# + the service-utilisateurs client) automatically.
#
# Usage:   right-click > Run with PowerShell    OR    ./keycloak/run-keycloak.ps1
# Admin console:  http://localhost:8089   (login: admin / admin)
# NOTE: port 8089 (pas 8080) car Oracle Database occupe le port 8080 sur cette machine.
# Stop:    press Ctrl+C in this window.
# ---------------------------------------------------------------------------

$ErrorActionPreference = "Stop"
$version = "26.1.4"
$here    = Split-Path -Parent $MyInvocation.MyCommand.Path
$zip     = Join-Path $here "keycloak-$version.zip"
$kcDir   = Join-Path $here "keycloak-$version"

# 1. Download + unzip Keycloak once
if (-not (Test-Path $kcDir)) {
    if (-not (Test-Path $zip)) {
        Write-Host "Downloading Keycloak $version (one time, ~120 MB)..." -ForegroundColor Cyan
        $url = "https://github.com/keycloak/keycloak/releases/download/$version/keycloak-$version.zip"
        Invoke-WebRequest -Uri $url -OutFile $zip
    }
    Write-Host "Unzipping Keycloak..." -ForegroundColor Cyan
    Expand-Archive -Path $zip -DestinationPath $here -Force
}

# 2. Copy the realm file into Keycloak's import folder
$importDir = Join-Path $kcDir "data\import"
New-Item -ItemType Directory -Force -Path $importDir | Out-Null
Copy-Item (Join-Path $here "realm-export.json") (Join-Path $importDir "realm-export.json") -Force

# 3. First admin user (Keycloak 26 bootstrap variables)
$env:KC_BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:KC_BOOTSTRAP_ADMIN_PASSWORD = "admin"

# Cap Keycloak's heap (low-RAM machines) — plenty for a demo realm.
$env:JAVA_OPTS_KC_HEAP = "-Xms64m -Xmx512m"

# 4. Start Keycloak in dev mode on port 8080 and import the realm
Write-Host "Starting Keycloak on http://localhost:8089 (admin/admin). Ctrl+C to stop." -ForegroundColor Green
& (Join-Path $kcDir "bin\kc.bat") start-dev --http-port 8089 --import-realm
