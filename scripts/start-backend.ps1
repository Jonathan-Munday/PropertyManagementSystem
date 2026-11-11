# Starts the Portico API. Does not print database credentials and does not write a .env file.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$statusPath = Join-Path $root ".portico\api-status.json"

function Write-ApiStatus([string]$State, $ExitCode) {
    $directory = Split-Path -Parent $statusPath
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $payload = [pscustomobject]@{
        state = $State
        exitCode = $ExitCode
        updatedAt = (Get-Date).ToString("o")
    }
    $payload | ConvertTo-Json | Set-Content -LiteralPath $statusPath -Encoding utf8
}

function Require-Env([string]$Name) {
    $value = [Environment]::GetEnvironmentVariable($Name, "Process")
    if ([string]::IsNullOrWhiteSpace($value)) {
        Write-Error "Missing $Name. The API will not start with a default database. Set it in this PowerShell window first. The steps are in docs/database.md."
    }
}

Write-ApiStatus "starting" $null
try {
    Require-Env "SPRING_DATASOURCE_URL"
    Require-Env "SPRING_DATASOURCE_USERNAME"
    Require-Env "SPRING_DATASOURCE_PASSWORD"
    Require-Env "SUPABASE_JWT_ISSUER"
    Require-Env "SUPABASE_JWKS_URI"
    Require-Env "SUPABASE_JWT_AUDIENCE"
    Require-Env "PORTICO_CORS_ORIGINS"

    if ($env:SUPABASE_JWT_AUDIENCE -ne "authenticated") {
        Write-Error "SUPABASE_JWT_AUDIENCE must be the single word authenticated."
    }

    $jdbcUrl = $env:SPRING_DATASOURCE_URL
    if ($jdbcUrl -notmatch '^jdbc:postgresql://([^/:?]+)(?::(\d+))?/([^?]+)') {
        Write-Error "SPRING_DATASOURCE_URL must be a PostgreSQL JDBC URL with a host and database name."
    }
    $databaseHost = $Matches[1]
    $databasePort = if ([string]::IsNullOrWhiteSpace($Matches[2])) { "5432" } else { $Matches[2] }
    $databaseName = $Matches[3]
    $databaseUser = $env:SPRING_DATASOURCE_USERNAME
    $sslRequired = $jdbcUrl -match '(?i)(?:^|[?&])sslmode=(require|verify-ca|verify-full)(?:&|$)'

    if ($databaseHost -match '^(?i)(localhost|127\.0\.0\.1)$' -and $env:PORTICO_DATASOURCE_ALLOW_LOCAL -ne "true") {
        Write-Error "Refusing host $databaseHost. Portico will not fall back to a local PostgreSQL database."
    }
    if ($databaseHost -like "*.pooler.supabase.com" -and $databaseUser -notlike "postgres.*") {
        Write-Error "Host $databaseHost is the Supabase session pooler. The username must be postgres.<project-ref>, not '$databaseUser'."
    }
    if ($databaseHost -like "db.*.supabase.co" -and $databaseUser -like "postgres.*") {
        Write-Error "Host $databaseHost is a direct Supabase connection. Use the username postgres, not the session-pooler username."
    }
    if ($databaseHost -like "*.supabase.co" -or $databaseHost -like "*.pooler.supabase.com") {
        if (-not $sslRequired) {
            Write-Error "The Supabase connection must include sslmode=require."
        }
        if ($databasePort -eq "6543") {
            Write-Error "Port 6543 is the Supabase transaction pooler. Use the session pooler on port 5432."
        }
    }

    if ([string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        $java = Get-Command java -ErrorAction SilentlyContinue
        if ($null -eq $java) {
            Write-Error "JAVA_HOME is not set and java was not found on PATH. Install JDK 21 or newer."
        }
        $settings = & java -XshowSettings:properties -version 2>&1 | Out-String
        if ($settings -notmatch "java\.home\s*=\s*(.+)") {
            Write-Error "JAVA_HOME is not set and the JDK location could not be detected."
        }
        $env:JAVA_HOME = $Matches[1].Trim()
        Write-Host "JAVA_HOME was unset. Using $($env:JAVA_HOME)"
    }

    Write-Host "Starting the Portico API on http://localhost:8080"
    Write-Host "Database host: $databaseHost"
    Write-Host "Database port: $databasePort"
    Write-Host "Database name: $databaseName"
    Write-Host "Database user: $databaseUser"
    Write-Host "SSL required: $sslRequired"
    Write-Host "Database password: present in this process only. It is not printed."
    Write-Host "The local profile is not active."
    Write-Host "Flyway applies pending migrations. It will not clean or reset the database."

    $wrapper = Join-Path $root "mvnw.cmd"
    if (Test-Path $wrapper) {
        # clean drops stale Flyway scripts left in target/ after a migration file is renamed or removed.
        & $wrapper clean spring-boot:run
        $code = $LASTEXITCODE
        if ($code -ne 0) {
            Write-ApiStatus "failed" $code
            exit $code
        }
        exit 0
    }

    $mvn = Get-Command mvn -ErrorAction SilentlyContinue
    if ($null -ne $mvn) {
        & mvn clean spring-boot:run
        $code = $LASTEXITCODE
        if ($code -ne 0) {
            Write-ApiStatus "failed" $code
            exit $code
        }
        exit 0
    }

    Write-Error "Neither mvnw.cmd nor mvn was found. Install Maven 3.9 or newer, then run this script again."
} catch {
    Write-ApiStatus "failed" 1
    throw
}
