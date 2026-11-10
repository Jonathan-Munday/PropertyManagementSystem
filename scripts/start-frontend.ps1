# Starts the React app. Does not modify frontend/.env.
$ErrorActionPreference = "Stop"
$frontend = Join-Path (Split-Path -Parent $PSScriptRoot) "frontend"
Set-Location $frontend

if (-not (Test-Path ".env")) {
    Write-Error "frontend/.env is missing. Copy frontend/.env.example to frontend/.env and fill the Supabase values. Do not commit that file."
}

if (-not (Test-Path "node_modules")) {
    npm install
}

npm run dev
