# Helper used by run-all.ps1 — runs one Maven module in the current tab/window.
# Kept as a separate -File script so run-all.ps1 doesn't need fragile inline quoting.
param(
    [Parameter(Mandatory = $true)][string]$Dir,
    [Parameter(Mandatory = $true)][string]$Title
)
$host.UI.RawUI.WindowTitle = $Title
Set-Location $Dir
Write-Host "=== $Title ===" -ForegroundColor Green
# JVM flags to keep memory small on low-RAM machines:
#   -Xmx256m         cap the heap (default lets each JVM grab ~25% of RAM)
#   -XX:+UseSerialGC lightweight GC; avoids G1's large native mark stacks (the OOM you saw)
#   -Xss512k         smaller thread stacks
# Plus disable DevTools auto-restart (can race to re-bind the port during boot).
$jvm = "-Xmx256m -XX:+UseSerialGC -Xss512k -Dspring.devtools.restart.enabled=false"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.jvmArguments=$jvm"
