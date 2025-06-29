# Build Social Commerce Services One by One
param(
    [switch]$SkipTests = $true,
    [switch]$StopOnError = $false
)

$ErrorActionPreference = "Continue"
$rootPath = "C:\Users\frich\Desktop\Exalt-Application-Limited\Exalt-Application-Limited\social-ecommerce-ecosystem\social-commerce"

Write-Host "=== BUILDING SOCIAL COMMERCE SERVICES ===" -ForegroundColor Cyan
Write-Host "Root Path: $rootPath" -ForegroundColor Yellow
Write-Host "Skip Tests: $SkipTests" -ForegroundColor Yellow

# Get all directories that have pom.xml
$services = Get-ChildItem -Path $rootPath -Directory | Where-Object {
    Test-Path (Join-Path $_.FullName "pom.xml")
} | Select-Object -ExpandProperty Name | Sort-Object

Write-Host "Found $($services.Count) services to build" -ForegroundColor Yellow

$results = @()
$successCount = 0
$failCount = 0

foreach ($service in $services) {
    $servicePath = Join-Path $rootPath $service
    
    Write-Host "`nBuilding: $service" -ForegroundColor Cyan
    Write-Host "Path: $servicePath" -ForegroundColor Gray
    
    Push-Location $servicePath
    try {
        # Build command
        $buildArgs = @("clean", "compile")
        if ($SkipTests) {
            $buildArgs += "-DskipTests"
        }
        
        # Execute build
        $buildOutput = & mvn $buildArgs 2>&1
        $exitCode = $LASTEXITCODE
        
        if ($exitCode -eq 0) {
            Write-Host "[SUCCESS] $service compiled successfully" -ForegroundColor Green
            $successCount++
            $results += @{ Service = $service; Status = "SUCCESS"; Message = "Compiled successfully" }
        } else {
            Write-Host "[FAILED] $service compilation failed" -ForegroundColor Red
            $failCount++
            
            # Save error log
            $errorLog = Join-Path $servicePath "build-error-new.log"
            $buildOutput | Out-File $errorLog -Encoding UTF8
            
            # Extract error messages
            $errorLines = $buildOutput | Where-Object { $_ -match "ERROR" } | Select-Object -First 5
            Write-Host "First errors:" -ForegroundColor Red
            $errorLines | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
            
            $results += @{ Service = $service; Status = "FAILED"; Message = "See $errorLog" }
            
            if ($StopOnError) {
                Write-Host "Stopping due to error (StopOnError = true)" -ForegroundColor Yellow
                break
            }
        }
    }
    catch {
        Write-Host "[EXCEPTION] $service : $_" -ForegroundColor Red
        $failCount++
        $results += @{ Service = $service; Status = "EXCEPTION"; Message = $_.ToString() }
        
        if ($StopOnError) {
            Write-Host "Stopping due to exception (StopOnError = true)" -ForegroundColor Yellow
            break
        }
    }
    finally {
        Pop-Location
    }
}

# Summary
Write-Host "`n=== BUILD SUMMARY ===" -ForegroundColor Cyan
Write-Host "Total Services: $($services.Count)" -ForegroundColor Yellow
Write-Host "Success: $successCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red

# Detailed results
Write-Host "`n=== DETAILED RESULTS ===" -ForegroundColor Cyan
$results | ForEach-Object {
    $color = switch ($_.Status) {
        "SUCCESS" { "Green" }
        "FAILED" { "Red" }
        "EXCEPTION" { "Magenta" }
        default { "White" }
    }
    Write-Host "$($_.Service): $($_.Status) - $($_.Message)" -ForegroundColor $color
}

# Save results to file
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$reportPath = Join-Path $rootPath "build-report-$timestamp.json"
$results | ConvertTo-Json -Depth 10 | Out-File $reportPath -Encoding UTF8
Write-Host "`nReport saved to: $reportPath" -ForegroundColor Cyan

# Return summary
return @{
    TotalServices = $services.Count
    Success = $successCount
    Failed = $failCount
    Results = $results
}
