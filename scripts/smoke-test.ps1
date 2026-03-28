param(
    [string]$BaseUrl = "http://localhost:8088",
    [string]$AdminEmail = "admin@smartcourier.local",
    [string]$AdminPassword = "Admin@12345",
    [int]$TimeoutSec = 30,
    [switch]$IncludeWriteTests
)

$ErrorActionPreference = "Stop"
$results = [System.Collections.Generic.List[object]]::new()

function Add-Result {
    param(
        [string]$Name,
        [string]$Status,
        [string]$Code,
        [string]$Details
    )
    $results.Add([pscustomobject]@{
            Check   = $Name
            Status  = $Status
            Code    = $Code
            Details = $Details
        })
}

function Read-ResponseBody {
    param($ExceptionResponse)
    if (-not $ExceptionResponse) {
        return ""
    }
    try {
        $stream = $ExceptionResponse.GetResponseStream()
        if (-not $stream) {
            return ""
        }
        $reader = New-Object System.IO.StreamReader($stream)
        return $reader.ReadToEnd()
    } catch {
        return ""
    }
}

function Invoke-Check {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = $null,
        $Body = $null,
        [int[]]$ExpectedStatus = @(200)
    )

    $uri = "$BaseUrl$Path"
    $requestArgs = @{
        Uri             = $uri
        Method          = $Method
        UseBasicParsing = $true
        TimeoutSec      = $TimeoutSec
    }
    if ($Headers) {
        $requestArgs["Headers"] = $Headers
    }
    if ($null -ne $Body) {
        $requestArgs["ContentType"] = "application/json"
        if ($Body -is [string]) {
            $requestArgs["Body"] = $Body
        } else {
            $requestArgs["Body"] = ($Body | ConvertTo-Json -Depth 10)
        }
    }

    try {
        $response = Invoke-WebRequest @requestArgs
        $statusCode = [int]$response.StatusCode
        if ($ExpectedStatus -contains $statusCode) {
            Add-Result -Name $Name -Status "PASS" -Code "$statusCode" -Details $Path
            if ($response.Content) {
                try {
                    return ($response.Content | ConvertFrom-Json)
                } catch {
                    return $response.Content
                }
            }
            return $null
        }
        Add-Result -Name $Name -Status "FAIL" -Code "$statusCode" -Details "Unexpected status for $Path"
        return $null
    } catch {
        $statusCode = "ERR"
        if ($_.Exception.Response) {
            $statusCode = [string][int]$_.Exception.Response.StatusCode.value__
        }
        $bodyText = Read-ResponseBody -ExceptionResponse $_.Exception.Response
        $detail = if ([string]::IsNullOrWhiteSpace($bodyText)) { $_.Exception.Message } else { $bodyText }
        Add-Result -Name $Name -Status "FAIL" -Code $statusCode -Details "$Path :: $detail"
        return $null
    }
}

Write-Host ""
Write-Host "SmartCourier API Smoke Test"
Write-Host "Base URL: $BaseUrl"
Write-Host "Timestamp: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')"
Write-Host ""

# Public checks
Invoke-Check -Name "Gateway API docs" -Method GET -Path "/v3/api-docs" | Out-Null
Invoke-Check -Name "Gateway services endpoint" -Method GET -Path "/gateway/services" | Out-Null
Invoke-Check -Name "Auth OpenAPI docs" -Method GET -Path "/gateway/auth/v3/api-docs" | Out-Null
Invoke-Check -Name "Admin OpenAPI docs" -Method GET -Path "/gateway/admin/v3/api-docs" | Out-Null
Invoke-Check -Name "Delivery OpenAPI docs" -Method GET -Path "/gateway/deliveries/v3/api-docs" | Out-Null
Invoke-Check -Name "Tracking OpenAPI docs" -Method GET -Path "/gateway/tracking/v3/api-docs" | Out-Null

# Auth flow
$loginResponse = Invoke-Check -Name "Auth login" -Method POST -Path "/api/auth/login" -Body @{
    email    = $AdminEmail
    password = $AdminPassword
}

if (-not $loginResponse -or -not $loginResponse.accessToken) {
    Write-Host ""
    Write-Host "Smoke Test Summary"
    $results | Format-Table -AutoSize
    Write-Host ""
    Write-Host "Result: FAIL (login failed; protected tests skipped)" -ForegroundColor Red
    exit 1
}

$accessToken = $loginResponse.accessToken
$refreshToken = $loginResponse.refreshToken
$authHeaders = @{
    Authorization = "Bearer $accessToken"
    "X-User-Role" = "ADMIN"
}

Invoke-Check -Name "Auth me" -Method GET -Path "/api/auth/me" -Headers $authHeaders | Out-Null
Invoke-Check -Name "Auth roles" -Method GET -Path "/api/auth/roles" -Headers $authHeaders | Out-Null
if ($refreshToken) {
    Invoke-Check -Name "Auth refresh" -Method POST -Path "/api/auth/refresh" -Body @{ refreshToken = $refreshToken } | Out-Null
}

# Admin checks
Invoke-Check -Name "Admin users" -Method GET -Path "/api/admin/users" -Headers $authHeaders | Out-Null
Invoke-Check -Name "Admin hubs" -Method GET -Path "/api/admin/hubs" -Headers $authHeaders | Out-Null
Invoke-Check -Name "Admin customer deliveries" -Method GET -Path "/api/admin/deliveries/customer" -Headers $authHeaders | Out-Null
Invoke-Check -Name "Admin delivery exceptions" -Method GET -Path "/api/admin/deliveries" -Headers $authHeaders | Out-Null
Invoke-Check -Name "Admin reports" -Method GET -Path "/api/admin/reports" -Headers $authHeaders | Out-Null
Invoke-Check -Name "Admin reports daily" -Method GET -Path "/api/admin/reports/daily" -Headers $authHeaders | Out-Null
Invoke-Check -Name "Admin reports sla" -Method GET -Path "/api/admin/reports/sla" -Headers $authHeaders | Out-Null

# Delivery and tracking checks
Invoke-Check -Name "Delivery estimate" -Method POST -Path "/api/deliveries/estimate" -Headers $authHeaders -Body @{
    packageWeightKg = 1.5
    serviceType     = "STANDARD"
} | Out-Null

$myDeliveries = Invoke-Check -Name "Delivery my list" -Method GET -Path "/api/deliveries/my" -Headers $authHeaders
Invoke-Check -Name "Tracking health" -Method GET -Path "/api/tracking/health" -Headers $authHeaders | Out-Null

$firstDelivery = $null
if ($myDeliveries -is [System.Array] -and $myDeliveries.Count -gt 0) {
    $firstDelivery = $myDeliveries[0]
} elseif ($myDeliveries -and $myDeliveries.id) {
    $firstDelivery = $myDeliveries
}

if ($firstDelivery) {
    Invoke-Check -Name "Delivery details (first)" -Method GET -Path "/api/deliveries/$($firstDelivery.id)" -Headers $authHeaders | Out-Null
    Invoke-Check -Name "Tracking timeline (first)" -Method GET -Path "/api/tracking/$($firstDelivery.id)/timeline" -Headers $authHeaders | Out-Null
    Invoke-Check -Name "Tracking latest (first)" -Method GET -Path "/api/tracking/$($firstDelivery.id)/latest" -Headers $authHeaders | Out-Null
    if ($firstDelivery.trackingNumber) {
        Invoke-Check -Name "Tracking by tracking number" -Method GET -Path "/api/tracking/$($firstDelivery.trackingNumber)" -Headers $authHeaders | Out-Null
        Invoke-Check -Name "Tracking events by tracking number" -Method GET -Path "/api/tracking/$($firstDelivery.trackingNumber)/events" -Headers $authHeaders | Out-Null
    }
}

if ($IncludeWriteTests) {
    $newDelivery = Invoke-Check -Name "Delivery create (write test)" -Method POST -Path "/api/deliveries" -Headers $authHeaders -Body @{
        senderName         = "Smoke Sender"
        receiverName       = "Smoke Receiver"
        receiverPhone      = "9999999999"
        pickupAddress      = "Alpha Street"
        destinationAddress = "Beta Street"
        packageWeightKg    = 2.5
        packageType        = "BOX"
        serviceType        = "STANDARD"
    } -ExpectedStatus @(200, 201)

    if ($newDelivery -and $newDelivery.id) {
        Invoke-Check -Name "Delivery book (write test)" -Method POST -Path "/api/deliveries/$($newDelivery.id)/book" -Headers $authHeaders | Out-Null
        Invoke-Check -Name "Delivery mark picked-up (write test)" -Method POST -Path "/api/deliveries/$($newDelivery.id)/status/picked-up" -Headers $authHeaders | Out-Null
        Invoke-Check -Name "Tracking timeline for new delivery (write test)" -Method GET -Path "/api/tracking/$($newDelivery.id)/timeline" -Headers $authHeaders | Out-Null
    }
}

Write-Host ""
Write-Host "Smoke Test Summary"
$results | Format-Table -AutoSize

$failCount = @($results | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host ""
if ($failCount -eq 0) {
    Write-Host "Result: PASS (all endpoint checks successful)" -ForegroundColor Green
    exit 0
}

Write-Host "Result: FAIL ($failCount endpoint check(s) failed)" -ForegroundColor Red
exit 1
