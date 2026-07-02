# Helper used by run-all.ps1 - runs one module in the current tab/window.
# Kept as a separate -File script so run-all.ps1 does not need fragile inline quoting.
param(
    [Parameter(Mandatory = $true)][string]$Dir,
    [Parameter(Mandatory = $true)][string]$Title
)
$host.UI.RawUI.WindowTitle = $Title
Set-Location $Dir

# Low-RAM flags: small heap + lightweight SerialGC (avoids G1's native mark stacks
# that were OOM-ing) + small thread stacks.
$jvmArgs = @("-Xmx256m", "-XX:+UseSerialGC", "-Xss512k", "-Dspring.devtools.restart.enabled=false")

# Run the built JAR directly with java (ONE JVM per service). "mvnw spring-boot:run"
# keeps the Maven JVM alive AND forks the app JVM = 2 JVMs each, which low-RAM machines
# can't afford. If the jar isn't built yet, build it once (Maven exits after packaging).
$jar = Get-ChildItem "target\*.jar" -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notlike "*.original" } | Select-Object -First 1
if (-not $jar) {
    Write-Host "=== $Title - building jar (one time)... ===" -ForegroundColor Yellow
    .\mvnw.cmd -q -DskipTests package
    $jar = Get-ChildItem "target\*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike "*.original" } | Select-Object -First 1
}
Write-Host "=== $Title ===" -ForegroundColor Green
java @jvmArgs -jar $jar.FullName
