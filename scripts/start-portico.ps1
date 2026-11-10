# Starts the React app and the Java API for local development.
# The database password is requested in this window and is not written to disk.
# Process ids are stored in .portico/session.json so stop-portico.ps1 can close only these windows.
$ErrorActionPreference = "Stop"
$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
Set-Location -LiteralPath $root

$jdkHome = "C:\Program Files\Java\jdk-22"
$frontendScript = Join-Path $root "scripts\start-frontend.ps1"
$backendScript = Join-Path $root "scripts\start-backend.ps1"
$sessionPath = Join-Path $root ".portico\session.json"

function Test-LaunchedByDoubleClick {
    $current = Get-CimInstance Win32_Process -Filter "ProcessId=$PID" -ErrorAction SilentlyContinue
    if ($null -eq $current) { return $false }
    $parent = Get-CimInstance Win32_Process -Filter "ProcessId=$($current.ParentProcessId)" -ErrorAction SilentlyContinue
    if ($null -eq $parent) { return $false }
    if ($parent.Name -eq "explorer.exe") { return $true }
    if ($parent.Name -eq "cmd.exe") {
        $grand = Get-CimInstance Win32_Process -Filter "ProcessId=$($parent.ParentProcessId)" -ErrorAction SilentlyContinue
        return ($null -ne $grand -and $grand.Name -eq "explorer.exe")
    }
    return $false
}

function Get-CommandLineOf([int]$ProcessId) {
    $row = Get-CimInstance Win32_Process -Filter "ProcessId=$ProcessId" -ErrorAction SilentlyContinue
    if ($null -eq $row) { return "" }
    return [string]$row.CommandLine
}

function Get-ListenerPids([int]$Port) {
    try {
        $connections = @(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction Stop)
        return @($connections | Select-Object -ExpandProperty OwningProcess -Unique)
    } catch {
        $client = New-Object System.Net.Sockets.TcpClient
        try {
            $wait = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
            $opened = $wait.AsyncWaitHandle.WaitOne(400, $false)
            if ($opened) {
                $client.EndConnect($wait)
                return @(-1)
            }
            return @()
        } catch {
            return @()
        } finally {
            $client.Close()
        }
    }
}

function Test-ProjectProcess([int]$ProcessId) {
    if ($ProcessId -le 0) { return $false }
    $command = Get-CommandLineOf $ProcessId
    return $command.IndexOf($root, [StringComparison]::OrdinalIgnoreCase) -ge 0
}

function Find-LauncherWindow([int]$ProcessId, [string]$ScriptName) {
    $current = $ProcessId
    $guard = 0
    while ($current -gt 0 -and $guard -lt 12) {
        $command = Get-CommandLineOf $current
        if ($command.IndexOf($ScriptName, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
            return $current
        }
        $row = Get-CimInstance Win32_Process -Filter "ProcessId=$current" -ErrorAction SilentlyContinue
        if ($null -eq $row) { break }
        $parent = [int]$row.ParentProcessId
        if ($parent -eq $current) { break }
        $current = $parent
        $guard++
    }
    return $ProcessId
}

function Read-Session {
    if (-not (Test-Path -LiteralPath $sessionPath)) {
        return [pscustomobject]@{ projectRoot = $root; frontendPid = $null; backendPid = $null }
    }
    $session = Get-Content -LiteralPath $sessionPath -Raw | ConvertFrom-Json
    if ($session.projectRoot -and [string]::Compare([string]$session.projectRoot, $root, [StringComparison]::OrdinalIgnoreCase) -ne 0) {
        return [pscustomobject]@{ projectRoot = $root; frontendPid = $null; backendPid = $null }
    }
    return $session
}

function Save-Session($Session) {
    $directory = Split-Path -Parent $sessionPath
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $payload = [pscustomobject]@{
        projectRoot = $root
        frontendPid = $Session.frontendPid
        backendPid = $Session.backendPid
        updatedAt = (Get-Date).ToString("o")
    }
    $payload | ConvertTo-Json | Set-Content -LiteralPath $sessionPath -Encoding utf8
}

function Set-JavaHome {
    $javaExe = Join-Path $jdkHome "bin\java.exe"
    if (Test-Path -LiteralPath $javaExe) {
        $env:JAVA_HOME = $jdkHome
        return
    }
    if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME) -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
        return
    }
    $java = Get-Command java -ErrorAction SilentlyContinue
    if ($null -eq $java) {
        throw "Java was not found. Install JDK 22 at $jdkHome or set JAVA_HOME."
    }
    $settings = & java -XshowSettings:properties -version 2>&1 | Out-String
    if ($settings -notmatch "java\.home\s*=\s*(.+)") {
        throw "JAVA_HOME is not set and the JDK location could not be detected."
    }
    $env:JAVA_HOME = $Matches[1].Trim()
}

function Start-PorticoWindow([string]$Title, [string]$ScriptPath) {
    $escapedRoot = $root.Replace("'", "''")
    $escapedScript = $ScriptPath.Replace("'", "''")
    $command = @"
`$Host.UI.RawUI.WindowTitle = '$Title'
Remove-Item Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
Set-Location -LiteralPath '$escapedRoot'
& '$escapedScript'
"@
    # The frontend call removes the password before npm starts.
    # The backend call below does not use this function, so its password stays in that window only.
    return Start-Process -FilePath "powershell.exe" -WorkingDirectory $root -PassThru -WindowStyle Normal -ArgumentList @(
        "-NoExit",
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-Command",
        $command
    )
}

function Write-ApiStatus([string]$State, $ExitCode) {
    $path = Join-Path $root ".portico\api-status.json"
    $directory = Split-Path -Parent $path
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $payload = [pscustomobject]@{
        state = $State
        exitCode = $ExitCode
        updatedAt = (Get-Date).ToString("o")
    }
    $payload | ConvertTo-Json | Set-Content -LiteralPath $path -Encoding utf8
}

function Test-ApiHealthy {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 3
        return ($response.StatusCode -eq 200 -and $response.Content -match '"status"\s*:\s*"UP"')
    } catch {
        return $false
    }
}

function Wait-ForApi([int]$ProcessId) {
    $statusPath = Join-Path $root ".portico\api-status.json"
    $deadline = (Get-Date).AddMinutes(8)
    Write-Host "Waiting until the API is healthy at http://localhost:8080/actuator/health"
    Write-Host "Maven may take a few minutes. Opening the API window is not treated as success."
    while ((Get-Date) -lt $deadline) {
        if (-not (Get-Process -Id $ProcessId -ErrorAction SilentlyContinue)) {
            throw "The API window closed before the API became healthy. Startup failed."
        }
        if (Test-Path -LiteralPath $statusPath) {
            try {
                $status = Get-Content -LiteralPath $statusPath -Raw | ConvertFrom-Json
            } catch {
                $status = $null
            }
            if ($null -ne $status -and $status.state -eq "failed") {
                throw "The API exited with an error (exit code $($status.exitCode)). Startup failed. Read the Portico API window. The database was not cleaned or reset."
            }
        }
        if (Test-ApiHealthy) {
            Write-Host "The API is healthy."
            return
        }
        Start-Sleep -Seconds 3
    }
    throw "The API did not become healthy within 8 minutes. Startup failed. Read the Portico API window."
}

function Require-LauncherSetting([string[]]$Names) {
    $missing = @()
    foreach ($name in $Names) {
        $value = [Environment]::GetEnvironmentVariable($name, "Process")
        if ([string]::IsNullOrWhiteSpace($value)) {
            $missing += $name
        }
    }
    if ($missing.Count -gt 0) {
        $list = $missing -join ", "
        throw "Missing $list. Set each one in this PowerShell window before starting Portico. The database password is entered at the prompt and is not saved. The variable names are documented in docs/database.md."
    }
}

function Resolve-Component([int]$Port, [string]$ScriptName, [string]$Label) {
    $listeners = @(Get-ListenerPids $Port)
    if ($listeners.Count -eq 0) {
        return [pscustomobject]@{ Running = $false; Owned = $false; WindowPid = $null }
    }
    if ($listeners -contains -1) {
        throw "Port $Port is already in use, but this script cannot see which program owns it. $Label was not started."
    }
    foreach ($listener in $listeners) {
        if (-not (Test-ProjectProcess ([int]$listener))) {
            $name = (Get-Process -Id ([int]$listener) -ErrorAction SilentlyContinue).ProcessName
            throw "Port $Port is already used by $name (PID $listener). That is not this Portico project, so $Label was not started."
        }
    }
    $owner = [int]$listeners[0]
    return [pscustomobject]@{
        Running = $true
        Owned = $true
        WindowPid = (Find-LauncherWindow $owner $ScriptName)
    }
}

$keepLauncherOpen = Test-LaunchedByDoubleClick
try {
    Write-Host "Portico project: $root"
    Set-JavaHome
    & (Join-Path $env:JAVA_HOME "bin\java.exe") -version
    if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
        throw "npm was not found. Install Node.js, then run this script again."
    }
    if (-not (Test-Path -LiteralPath (Join-Path $root "frontend\.env"))) {
        throw "frontend/.env is missing. The launcher will not create or overwrite it."
    }
    if (-not (Test-Path -LiteralPath $frontendScript) -or -not (Test-Path -LiteralPath $backendScript)) {
        throw "The frontend or backend startup script is missing from scripts."
    }

    $session = Read-Session
    $frontend = Resolve-Component 5173 "start-frontend.ps1" "The React app"
    if ($frontend.Running) {
        Write-Host "React is already running at http://localhost:5173"
        $session | Add-Member -NotePropertyName frontendPid -NotePropertyValue $frontend.WindowPid -Force
    } else {
        Write-Host "Starting the React frontend in a new window..."
        $frontendProcess = Start-PorticoWindow "Portico Frontend" $frontendScript
        $session | Add-Member -NotePropertyName frontendPid -NotePropertyValue $frontendProcess.Id -Force
        Write-Host "Frontend window PID $($frontendProcess.Id)"
    }
    Save-Session $session

    $apiWindowPid = $null
    $backend = Resolve-Component 8080 "start-backend.ps1" "The Java API"
    if ($backend.Running) {
        if (-not (Test-ApiHealthy)) {
            throw "Port 8080 is open for this project, but http://localhost:8080/actuator/health is not UP. Startup failed."
        }
        Write-Host "The API is already healthy at http://localhost:8080"
        Write-Host "The database password was not requested."
        $session | Add-Member -NotePropertyName backendPid -NotePropertyValue $backend.WindowPid -Force
        Save-Session $session
    } else {
        Require-LauncherSetting @(
            "SPRING_DATASOURCE_URL",
            "SPRING_DATASOURCE_USERNAME",
            "SUPABASE_JWT_ISSUER",
            "SUPABASE_JWKS_URI",
            "SUPABASE_JWT_AUDIENCE"
        )
        Write-Host "Starting the Java API. Enter the Supabase database password. It will not be displayed or saved."
        $secure = Read-Host "Supabase database password" -AsSecureString
        if ($null -eq $secure -or $secure.Length -eq 0) {
            throw "A database password is required to start the API."
        }
        $pointer = [IntPtr]::Zero
        try {
            $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
            $env:SPRING_DATASOURCE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)

            $escapedRoot = $root.Replace("'", "''")
            $escapedBackend = $backendScript.Replace("'", "''")
            $backendCommand = @"
`$Host.UI.RawUI.WindowTitle = 'Portico API'
Set-Location -LiteralPath '$escapedRoot'
& '$escapedBackend'
"@
            Write-ApiStatus "starting" $null
            Write-Host "Starting the Java API in a new window. The password is not printed."
            $backendProcess = Start-Process -FilePath "powershell.exe" -WorkingDirectory $root -PassThru -WindowStyle Normal -ArgumentList @(
                "-NoExit",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-Command",
                $backendCommand
            )
            $apiWindowPid = $backendProcess.Id
            $session | Add-Member -NotePropertyName backendPid -NotePropertyValue $backendProcess.Id -Force
            Save-Session $session
            Write-Host "API window PID $($backendProcess.Id)"
        } finally {
            if ($pointer -ne [IntPtr]::Zero) {
                [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
            }
            Remove-Item Env:SPRING_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
            if ($null -ne $secure) { $secure.Dispose() }
        }
    }

    if ($null -ne $apiWindowPid) {
        Wait-ForApi $apiWindowPid
    }

    Write-Host ""
    Write-Host "Portico is running. Leave the Portico Frontend and Portico API windows open."
    Write-Host "Frontend: http://localhost:5173"
    Write-Host "API:      http://localhost:8080"
    Write-Host "Health:   http://localhost:8080/actuator/health"
    Write-Host "Stop:     .\scripts\stop-portico.ps1"
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($keepLauncherOpen) {
        Read-Host "Press Enter to close this window. Application windows that already opened will stay open"
    }
    exit 1
}

if ($keepLauncherOpen) {
    Read-Host "Press Enter to close this launcher window. The application windows stay open"
}
