# Lance le frontend Vite (npm run dev).
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Host "npm introuvable. Installez Node.js LTS (https://nodejs.org/) puis rouvrez le terminal." -ForegroundColor Red
    exit 1
}

if (-not (Test-Path ".\node_modules")) {
    Write-Host "Installation des dépendances (npm install)..." -ForegroundColor Yellow
    npm install
}

npm run dev
