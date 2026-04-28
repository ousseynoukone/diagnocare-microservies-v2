Write-Host "Waiting for services to be ready..."
Start-Sleep -Seconds 15

$response = Invoke-RestMethod -Uri "http://localhost:8765/api/v1/auth/login" -Method Post -ContentType "application/json" -InFile "scratch\login.json"
$token = $response.data.token

Write-Host "Fetching ML metadata for diseases..."
try {
    $metadataResponse = Invoke-RestMethod -Uri "http://localhost:8765/api/v1/diagnocare/pathologies/ml-metadata" -Method Get -Headers @{Authorization="Bearer $token"}
    Write-Host "Metadata retrieved successfully:"
    $metadataResponse | ConvertTo-Json -Depth 5 | Write-Host
} catch {
    Write-Host "Error fetching metadata: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.ReadToEnd() | Write-Host
    }
}
