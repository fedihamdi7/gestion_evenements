# Helper used by run-all.ps1 — runs one Maven module in the current tab/window.
# Kept as a separate -File script so run-all.ps1 doesn't need fragile inline quoting.
param(
    [Parameter(Mandatory = $true)][string]$Dir,
    [Parameter(Mandatory = $true)][string]$Title
)
$host.UI.RawUI.WindowTitle = $Title
Set-Location $Dir
Write-Host "=== $Title ===" -ForegroundColor Green
# Disable Spring DevTools auto-restart for orchestrated runs: it can race to re-bind the
# port during boot and cause an intermittent "Unable to start web server". (No effect on
# modules that don't use DevTools.)
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=-Dspring.devtools.restart.enabled=false"
