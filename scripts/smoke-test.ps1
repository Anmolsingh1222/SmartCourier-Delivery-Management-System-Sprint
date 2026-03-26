param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "1) Logging in as admin..."
$loginBody = @{ email = "admin@smartcourier.local"; password = "Admin@12345" } | ConvertTo-Json
$loginResp = Invoke-RestMethod -Method Post -Uri "$BaseUrl/gateway/auth/login" -ContentType "application/json" -Body $loginBody
$token = $loginResp.accessToken
if (-not $token) { throw "No access token received" }
$headers = @{ Authorization = "Bearer $token" }

Write-Host "2) Creating delivery as admin (for smoke test)..."
$createBody = @{
  senderName = "Smoke Sender"
  receiverName = "Smoke Receiver"
  receiverPhone = "9999999999"
  pickupAddress = "Alpha Street"
  destinationAddress = "Beta Street"
  packageWeightKg = 2.5
  packageType = "BOX"
  serviceType = "STANDARD"
} | ConvertTo-Json
$delivery = Invoke-RestMethod -Method Post -Uri "$BaseUrl/gateway/deliveries" -Headers $headers -ContentType "application/json" -Body $createBody

Write-Host "3) Booking delivery..."
Invoke-RestMethod -Method Post -Uri "$BaseUrl/gateway/deliveries/$($delivery.id)/book" -Headers $headers | Out-Null

Write-Host "4) Marking picked up (admin transition)..."
Invoke-RestMethod -Method Post -Uri "$BaseUrl/gateway/deliveries/$($delivery.id)/status/picked-up" -Headers $headers | Out-Null

Write-Host "5) Fetching tracking timeline..."
$timeline = Invoke-RestMethod -Method Get -Uri "$BaseUrl/gateway/tracking/$($delivery.id)/timeline" -Headers $headers

Write-Host "Smoke test complete."
Write-Host "Delivery ID: $($delivery.id)"
Write-Host "Tracking Number: $($delivery.trackingNumber)"
Write-Host "Timeline Events: $($timeline.Count)"
