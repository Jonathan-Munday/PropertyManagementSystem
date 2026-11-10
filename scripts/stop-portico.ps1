# Stops the Portico windows recorded by start-portico.ps1.
# The session file stores process ids only. A recorded id is stopped only when its
# command line still belongs to this project. Other Java, Node, and PowerShell processes are left running.
$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$sessionPath = Join-Path $root ".portico\session.json"

function Get-CommandLineOf([int]$ProcessId) {
    $row = Get-CimInstance Win32_Process -Filter "ProcessId=$ProcessId" -ErrorAction SilentlyContinue
    if ($null -eq $row) { return "" }
    return [string]$row.CommandLine
}

function Stop-RecordedWindow([string]$Role, $ProcessId) {
    if ($null -eq $ProcessId -or [int]$ProcessId -le 0) {
        Write-Host "$Role was not recorded."
        return
    }
    $pidValue = [int]$ProcessId
    $running = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
    if ($null -eq $running) {
        Write-Host "$Role is not running."
        return
    }
    $command = Get-CommandLineOf $pidValue
    $belongsHere = $command.IndexOf($root, [StringComparison]::OrdinalIgnoreCase) -ge 0
    if (-not $belongsHere) {
        Write-Host "PID $pidValue is no longer the Portico $Role window. It was left running."
        return
    }
    Write-Host "Stopping Portico $Role (PID $pidValue)..."
    & taskkill.exe /PID $pidValue /T | Out-Host
    Start-Sleep -Seconds 3
    $stillRunning = Get-Process -Id $pidValue -ErrorAction SilentlyContinue
    if ($null -ne $stillRunning) {
        & taskkill.exe /PID $pidValue /T /F | Out-Host
    }
}

if (-not (Test-Path -LiteralPath $sessionPath)) {
    Write-Host "No Portico session file was found at .portico\session.json."
    Write-Host "Nothing was stopped. This script does not search the machine for Java or Node."
    exit 0
}

$session = Get-Content -LiteralPath $sessionPath -Raw | ConvertFrom-Json
if ($session.projectRoot -and [string]::Compare([string]$session.projectRoot, $root, [StringComparison]::OrdinalIgnoreCase) -ne 0) {
    Write-Host "The session file belongs to a different project folder. Nothing was stopped."
    exit 1
}

Stop-RecordedWindow "API" $session.backendPid
Stop-RecordedWindow "frontend" $session.frontendPid

$session.frontendPid = $null
$session.backendPid = $null
$session.updatedAt = (Get-Date).ToString("o")
$session | ConvertTo-Json | Set-Content -LiteralPath $sessionPath -Encoding utf8
Write-Host "Portico development windows recorded by the launcher have been closed."
