param(
    [string]$ComposeFile = "infra/docker-compose.full.yml",
    [string]$GatewayBaseUrl = "http://localhost:8088",
    [string]$EurekaUrl = "http://localhost:8762/eureka/apps",
    [string]$RabbitMqUrl = "http://localhost:15672",
    [string]$ZipkinUrl = "http://localhost:9411",
    [string]$SonarQubeStatusUrl = "http://localhost:9000/api/system/status",
    [string]$AdminEmail = "admin@smartcourier.local",
    [string]$AdminPassword = "Admin@12345",
    [string]$MysqlContainer = "smartcourier-mysql",
    [int]$StartupTimeoutSeconds = 420,
    [int]$PollIntervalSeconds = 8,
    [switch]$SkipDatabaseCheck,
    [switch]$SkipObservabilityCheck
)

$ErrorActionPreference = "Stop"
$results = [System.Collections.Generic.List[object]]::new()
$token = $null

function Add-Result {
    param(
        [string]$Name,
        [string]$Status,
        [string]$Details
    )
    $results.Add([pscustomobject]@{
            Check   = $Name
            Status  = $Status
            Details = $Details
        })
}

function Run-Check {
    param(
        [string]$Name,
        [scriptblock]$Action
    )
    try {
        $details = & $Action
        Add-Result -Name $Name -Status "PASS" -Details ([string]$details)
    } catch {
        $message = $_.Exception.Message
        if ([string]::IsNullOrWhiteSpace($message)) {
            $message = "$_"
        }
        Add-Result -Name $Name -Status "FAIL" -Details $message
    }
}

function Invoke-WithRetry {
    param(
        [scriptblock]$Action,
        [int]$TimeoutSeconds,
        [int]$IntervalSeconds,
        [string]$FailureMessage = "Operation timed out"
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null

    while ((Get-Date) -lt $deadline) {
        try {
            return & $Action
        } catch {
            $lastError = $_
            Start-Sleep -Seconds $IntervalSeconds
        }
    }

    if ($lastError) {
        throw "$FailureMessage. Last error: $($lastError.Exception.Message)"
    }
    throw $FailureMessage
}

Write-Host ""
Write-Host "SmartCourier Health Check"
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')"
Write-Host ""

Run-Check "Docker CLI available" {
    $null = docker --version
    "docker command OK"
}

Run-Check "Docker engine running" {
    $serverVersion = docker version --format "{{.Server.Version}}" 2>$null
    if (-not $serverVersion) {
        throw "Docker engine not reachable"
    }
    "Server version: $serverVersion"
}

Run-Check "Compose file valid" {
    docker compose -f $ComposeFile config -q | Out-Null
    "compose config OK"
}

Run-Check "Compose services up" {
    $ps = docker compose -f $ComposeFile ps --format json
    if (-not $ps) {
        throw "No compose services found"
    }

    $rows = $ps | ConvertFrom-Json
    if ($rows -isnot [System.Array]) {
        $rows = @($rows)
    }

    $down = @($rows | Where-Object { $_.State -ne "running" })
    if ($down.Count -gt 0) {
        $names = ($down | ForEach-Object { $_.Name + ":" + $_.State }) -join ", "
        throw "Non-running services: $names"
    }

    "All services running ($($rows.Count))"
}

Run-Check "Gateway API docs reachable" {
    $statusCode = Invoke-WithRetry -TimeoutSeconds $StartupTimeoutSeconds -IntervalSeconds $PollIntervalSeconds -FailureMessage "Gateway docs did not become reachable in time" -Action {
        (Invoke-WebRequest -UseBasicParsing -Uri "$GatewayBaseUrl/v3/api-docs" -TimeoutSec 20).StatusCode
    }
    if ($statusCode -ne 200) {
        throw "Expected 200, got $statusCode"
    }
    "HTTP $statusCode"
}

if (-not $SkipObservabilityCheck) {
    Run-Check "RabbitMQ UI reachable" {
        $statusCode = Invoke-WithRetry -TimeoutSeconds $StartupTimeoutSeconds -IntervalSeconds $PollIntervalSeconds -FailureMessage "RabbitMQ UI did not become reachable in time" -Action {
            (Invoke-WebRequest -UseBasicParsing -Uri $RabbitMqUrl -TimeoutSec 20).StatusCode
        }
        if ($statusCode -lt 200 -or $statusCode -ge 400) {
            throw "Expected 2xx/3xx, got $statusCode"
        }
        "HTTP $statusCode"
    }

    Run-Check "Zipkin UI reachable" {
        $statusCode = Invoke-WithRetry -TimeoutSeconds $StartupTimeoutSeconds -IntervalSeconds $PollIntervalSeconds -FailureMessage "Zipkin UI did not become reachable in time" -Action {
            (Invoke-WebRequest -UseBasicParsing -Uri $ZipkinUrl -TimeoutSec 20).StatusCode
        }
        if ($statusCode -lt 200 -or $statusCode -ge 400) {
            throw "Expected 2xx/3xx, got $statusCode"
        }
        "HTTP $statusCode"
    }

    Run-Check "SonarQube API reachable" {
        $statusText = Invoke-WithRetry -TimeoutSeconds $StartupTimeoutSeconds -IntervalSeconds $PollIntervalSeconds -FailureMessage "SonarQube status API did not become reachable in time" -Action {
            $resp = Invoke-RestMethod -Uri $SonarQubeStatusUrl -TimeoutSec 20
            if (-not $resp.status) {
                throw "status field missing"
            }
            if ($resp.status -eq "DOWN") {
                throw "status is DOWN"
            }
            return $resp.status
        }
        "status=$statusText"
    }
}

Run-Check "Eureka reachable + services registered" {
    $required = @("API-GATEWAY", "AUTH-SERVICE", "ADMIN-SERVICE", "DELIVERY-SERVICE", "TRACKING-SERVICE")
    $names = Invoke-WithRetry -TimeoutSeconds $StartupTimeoutSeconds -IntervalSeconds $PollIntervalSeconds -FailureMessage "Required Eureka services were not registered in time" -Action {
        $resp = Invoke-WebRequest -UseBasicParsing -Uri $EurekaUrl -Headers @{ Accept = "application/json" } -TimeoutSec 20
        if ($resp.StatusCode -ne 200) {
            throw "Expected 200, got $($resp.StatusCode)"
        }
        $json = $resp.Content | ConvertFrom-Json
        $apps = @($json.applications.application)
        if ($apps.Count -eq 0) {
            throw "No applications registered yet"
        }

        $currentNames = @($apps | ForEach-Object { $_.name })
        $missingNow = @($required | Where-Object { $currentNames -notcontains $_ })
        if ($missingNow.Count -gt 0) {
            throw "Still missing: $($missingNow -join ', ')"
        }
        return $currentNames
    }

    $required = @("API-GATEWAY", "AUTH-SERVICE", "ADMIN-SERVICE", "DELIVERY-SERVICE", "TRACKING-SERVICE")
    $missing = @($required | Where-Object { $names -notcontains $_ })
    if ($missing.Count -gt 0) {
        throw "Missing Eureka registrations: $($missing -join ', ')"
    }

    "Registered: $($names -join ', ')"
}

Run-Check "Admin login via gateway" {
    $login = Invoke-WithRetry -TimeoutSeconds $StartupTimeoutSeconds -IntervalSeconds $PollIntervalSeconds -FailureMessage "Admin login did not become available in time" -Action {
        $body = @{
            email    = $AdminEmail
            password = $AdminPassword
        } | ConvertTo-Json
        Invoke-RestMethod -Uri "$GatewayBaseUrl/api/auth/login" -Method POST -ContentType "application/json" -Body $body -TimeoutSec 30
    }
    if (-not $login.accessToken) {
        throw "Access token missing in login response"
    }

    $script:token = $login.accessToken
    "Login OK. Role: $($login.profile.role)"
}

Run-Check "Protected /api/auth/me works" {
    if (-not $script:token) {
        throw "Token unavailable from login step"
    }

    $headers = @{ Authorization = "Bearer $script:token" }
    $me = Invoke-RestMethod -Uri "$GatewayBaseUrl/api/auth/me" -Headers $headers -Method GET -TimeoutSec 30
    if (-not $me.email) {
        throw "Profile response missing email"
    }
    "Authenticated as $($me.email)"
}

Run-Check "Protected /api/admin/users works" {
    if (-not $script:token) {
        throw "Token unavailable from login step"
    }

    $headers = @{
        Authorization = "Bearer $script:token"
        "X-User-Role" = "ADMIN"
    }
    $users = Invoke-RestMethod -Uri "$GatewayBaseUrl/api/admin/users" -Headers $headers -Method GET -TimeoutSec 30
    $count = @($users).Count
    "Users visible: $count"
}

if (-not $SkipDatabaseCheck) {
    Run-Check "Database auth_db users table check" {
        $query = "USE auth_db; SELECT COUNT(*) AS users_count FROM users;"
        $output = docker exec $MysqlContainer sh -lc "MYSQL_PWD=root mysql -uroot -N -e '$query'" 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "mysql query failed: $output"
        }

        $count = [int]($output | Select-Object -Last 1).Trim()
        if ($count -lt 1) {
            throw "users table has no records"
        }
        "users_count=$count"
    }
}

Write-Host ""
Write-Host "Health Check Summary"
$results | Format-Table -AutoSize

$failed = @($results | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host ""
if ($failed -eq 0) {
    Write-Host "Result: PASS (all checks successful)" -ForegroundColor Green
    exit 0
}

Write-Host "Result: FAIL ($failed check(s) failed)" -ForegroundColor Red
exit 1
