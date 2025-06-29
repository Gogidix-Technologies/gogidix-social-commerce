# PowerShell script to clean up old package directories after naming standardization

Write-Host "Starting cleanup of old package directories..." -ForegroundColor Green

# Base path for social-commerce services
$basePath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\Exalt-Application-Limited\social-ecommerce-ecosystem\social-commerce"

# List of services to clean up
$services = @(
    "api-gateway",
    "commission-service", 
    "fulfillment-options",
    "global-hq-admin",
    "localization-service",
    "marketplace",
    "multi-currency-service",
    "order-service",
    "payment-gateway",
    "payout-service",
    "product-service",
    "regional-admin",
    "social-commerce-production",
    "social-commerce-shared",
    "social-commerce-staging",
    "social-media-integration",
    "subscription-service",
    "user-mobile-app",
    "user-web-app",
    "vendor-app",
    "vendor-onboarding"
)

$cleanupLog = @()

foreach ($service in $services) {
    $servicePath = Join-Path $basePath $service
    $javaPath = Join-Path $servicePath "src\main\java\com"
    
    if (Test-Path $servicePath) {
        Write-Host "Processing $service..." -ForegroundColor Yellow
        
        # Look for old package directories to remove
        $oldPackagePaths = @(
            Join-Path $javaPath "socialecommerceecosystem",
            Join-Path $javaPath "microecommerce",
            Join-Path $javaPath "socialcommerce" # Only if not containing exalt structure
        )
        
        foreach ($oldPath in $oldPackagePaths) {
            if (Test-Path $oldPath) {
                try {
                    # Check if this is the new standardized path
                    if ($oldPath.EndsWith("socialcommerce") -and (Test-Path (Join-Path $oldPath "..\exalt"))) {
                        Write-Host "  Skipping new standardized path: $oldPath" -ForegroundColor Blue
                        continue
                    }
                    
                    Write-Host "  Removing old package directory: $oldPath" -ForegroundColor Red
                    Remove-Item -Path $oldPath -Recurse -Force
                    $cleanupLog += "✓ Removed: $oldPath"
                } catch {
                    Write-Host "  Error removing $oldPath : $($_.Exception.Message)" -ForegroundColor Red
                    $cleanupLog += "✗ Error removing: $oldPath - $($_.Exception.Message)"
                }
            }
        }
        
        # Test compilation for each service
        Write-Host "  Testing compilation for $service..." -ForegroundColor Cyan
        Set-Location $servicePath
        
        try {
            $compileResult = & mvn clean compile -DskipTests=true -q 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  ✓ Compilation successful for $service" -ForegroundColor Green
                $cleanupLog += "✓ Compilation successful: $service"
            } else {
                Write-Host "  ✗ Compilation failed for $service" -ForegroundColor Red
                $cleanupLog += "✗ Compilation failed: $service"
                $cleanupLog += "  Error details: $compileResult"
            }
        } catch {
            Write-Host "  ✗ Error during compilation for $service : $($_.Exception.Message)" -ForegroundColor Red
            $cleanupLog += "✗ Compilation error: $service - $($_.Exception.Message)"
        }
    } else {
        Write-Host "Service directory not found: $servicePath" -ForegroundColor Magenta
        $cleanupLog += "? Service not found: $service"
    }
    Write-Host ""
}

# Generate cleanup report
$reportPath = Join-Path $basePath "package-cleanup-report.md"
$reportContent = @"
# Package Cleanup and Compilation Report
Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

## Summary
This report shows the results of cleaning up old package directories and testing compilation after the naming standardization.

## Services Processed
$($services.Count) services were processed for package cleanup and compilation testing.

## Results
$($cleanupLog | ForEach-Object { "- $_" } | Out-String)

## Next Steps
1. Review any compilation failures listed above
2. Fix any import statements that may still reference old package names
3. Update any configuration files that may reference old package names
4. Re-run compilation tests for failed services after fixes

## Notes
- Old package directories (com.socialecommerceecosystem, com.microecommerce) have been removed
- New standardized package structure (com.exalt.ecosystem.socialcommerce) is now in use
- All services should now use the consistent naming convention
"@

$reportContent | Out-File -FilePath $reportPath -Encoding UTF8

Write-Host "Cleanup complete! Report saved to: $reportPath" -ForegroundColor Green
Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
$cleanupLog | ForEach-Object { Write-Host "  $_" }