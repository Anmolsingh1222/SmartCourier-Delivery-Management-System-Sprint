# Export OpenAPI specs from all services
$baseUrl = "http://localhost:8088"
$outputDir = "$PSScriptRoot/../docs/api-specs"

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$services = @{
    "auth-service"     = "/gateway/auth/v3/api-docs"
    "delivery-service" = "/gateway/deliveries/v3/api-docs"
    "tracking-service" = "/gateway/tracking/v3/api-docs"
    "admin-service"    = "/gateway/admin/v3/api-docs"
}

foreach ($svc in $services.GetEnumerator()) {
    $url = "$baseUrl$($svc.Value)"
    $file = "$outputDir/$($svc.Key).json"
    try {
        Invoke-WebRequest -Uri $url -OutFile $file
        Write-Host "✅ Saved $($svc.Key) → $file"
    } catch {
        Write-Host "❌ Failed $($svc.Key): $_"
    }
}

Write-Host ""
Write-Host "Done. Import any .json file into https://editor.swagger.io to view/edit."
