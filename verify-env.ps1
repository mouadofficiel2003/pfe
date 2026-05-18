# Verifies Stage_PFE dev dependencies. Run in Windows PowerShell (outside Cursor for accurate PATH).
$ErrorActionPreference = "Continue"
Write-Host "`n=== Stage_PFE - environment check ===`n" -ForegroundColor Cyan

function Test-Cmd($name) {
    $c = Get-Command $name -ErrorAction SilentlyContinue
    if (-not $c) { return $null }
    return $c.Source
}

$rows = @()

$javaSrc = Test-Cmd "java"
if ($javaSrc) {
    $ver = (& java -version 2>&1 | Out-String).Trim() -replace "`r`n", " "
    $rows += [PSCustomObject]@{ Component = "java"; OK = $true; Detail = "$javaSrc | $ver" }
} else {
    $rows += [PSCustomObject]@{ Component = "java"; OK = $false; Detail = "Not on PATH" }
}

$javacSrc = Test-Cmd "javac"
$rows += [PSCustomObject]@{ Component = "javac (JDK)"; OK = [bool]$javacSrc; Detail = $(if ($javacSrc) { $javacSrc } else { "Missing - install full JDK 17+, not JRE only" }) }

$jh = $env:JAVA_HOME
if (-not $jh) { $jh = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User") }
if ($jh -and (Test-Path (Join-Path $jh "bin\java.exe"))) {
    $rows += [PSCustomObject]@{ Component = "JAVA_HOME"; OK = $true; Detail = $jh }
} elseif ($jh) {
    $rows += [PSCustomObject]@{ Component = "JAVA_HOME"; OK = $false; Detail = "Set but invalid: $jh" }
} else {
    $rows += [PSCustomObject]@{ Component = "JAVA_HOME"; OK = $false; Detail = "Not set (recommended for Spring / Maven)" }
}

$mvn = Test-Cmd "mvn"
$mvnDetail = if ($mvn) {
    try { (& mvn -version 2>&1 | Select-Object -First 1) + " | $mvn" } catch { $mvn }
} else {
    "Missing - install Apache Maven and add bin to PATH"
}
$rows += [PSCustomObject]@{ Component = "mvn (global)"; OK = [bool]$mvn; Detail = $mvnDetail }

$nodeSrc = Test-Cmd "node"
if ($nodeSrc) {
    $warn = ""
    if ($nodeSrc -match "Cursor") { $warn = " (warning: Cursor-bundled Node - install Node.js LTS for npm)" }
    $rows += [PSCustomObject]@{ Component = "node"; OK = $true; Detail = "$( & node -v 2>&1 ) | $nodeSrc$warn" }
} else {
    $rows += [PSCustomObject]@{ Component = "node"; OK = $false; Detail = "Missing - install Node.js LTS" }
}

$npmSrc = Test-Cmd "npm"
$rows += [PSCustomObject]@{ Component = "npm"; OK = [bool]$npmSrc; Detail = $(if ($npmSrc) { "$( & npm -v 2>&1 ) | $npmSrc" } else { "Missing - required for frontend" }) }

$gitSrc = Test-Cmd "git"
$rows += [PSCustomObject]@{ Component = "git"; OK = [bool]$gitSrc; Detail = $(if ($gitSrc) { & git --version 2>&1 } else { "Optional" }) }

$psqlSrc = Test-Cmd "psql"
$rows += [PSCustomObject]@{ Component = "psql"; OK = [bool]$psqlSrc; Detail = $(if ($psqlSrc) { & psql --version 2>&1 } else { "Optional if you use pgAdmin only" }) }

$svc = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue
$rows += [PSCustomObject]@{
    Component = "PostgreSQL Windows service"
    OK        = [bool]$svc
    Detail    = $(if ($svc) { ($svc | ForEach-Object { "$($_.Name): $($_.Status)" }) -join "; " } else { "No postgresql* service found" })
}

$rows | Format-Table -AutoSize

$need = @()
if (-not ($rows | Where-Object { $_.Component -eq "java" -and $_.OK })) { $need += "Install JDK 17+ (e.g. Eclipse Temurin) and add to PATH" }
if (-not ($rows | Where-Object { $_.Component -eq "javac (JDK)" -and $_.OK })) { $need += "Full JDK (javac) required to compile the backend" }
if (-not ($rows | Where-Object { $_.Component -eq "JAVA_HOME" -and $_.OK })) { $need += "Set JAVA_HOME to JDK root (folder that contains bin)" }
if (-not ($rows | Where-Object { $_.Component -eq "npm" -and $_.OK })) { $need += "Install Node.js LTS from https://nodejs.org/ (includes npm)" }
if (-not ($rows | Where-Object { $_.Component -eq "mvn (global)" -and $_.OK })) { $need += "Install Apache Maven (https://maven.apache.org/) and add mvn to PATH" }

if ($need.Count) {
    Write-Host "Action items:" -ForegroundColor Yellow
    $need | ForEach-Object { Write-Host "  - $_" }
} else {
    Write-Host "Core tooling looks OK." -ForegroundColor Green
}
Write-Host ""
