# Helper used by run-all.ps1 — runs the NestJS messaging service in the current tab/window.
param(
    [Parameter(Mandatory = $true)][string]$Dir,
    [Parameter(Mandatory = $true)][string]$Title
)
$host.UI.RawUI.WindowTitle = $Title
Set-Location $Dir

# Install dependencies on first run (node_modules missing).
if (-not (Test-Path "node_modules")) {
    Write-Host "First run: installing dependencies (npm install)..." -ForegroundColor Yellow
    npm install
}

Write-Host "=== $Title ===" -ForegroundColor Green
# `nest start` compiles TypeScript and runs the service on port 8085.
npm run start
