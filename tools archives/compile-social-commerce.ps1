# Social Commerce Domain - Systematic Compilation Script
# Automated compilation, testing, and progress tracking

param(
    [switch]$SkipTests,
    [switch]$VerboseOutput,
    [string]$StartFromService = "",
    [switch]$UpdateProgress = $true
)

# Color output functions
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }
function Write-Warning { param($Message) Write-Host "⚠️ $Message" -ForegroundColor Yellow }
function Write-Info { param($Message) Write-Host "ℹ️ $Message" -ForegroundColor Cyan }
function Write-Progress { param($Message) Write-Host "🔄 $Message" -ForegroundColor Blue }

# Script start
$startTime = Get-Date
Write-Host "🚀 Social Commerce Domain Compilation Script" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host "Start Time: $($startTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Gray
Write-Host "🔗 Infrastructure Dependency: shared-infrastructure domain" -ForegroundColor Yellow

# Set JAVA_HOME environment
$JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.15.6-hotspot"
$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$JAVA_HOME\bin;$env:PATH"

Write-Success "JAVA_HOME set to: $JAVA_HOME"

# Check for shared-infrastructure domain
$sharedInfraPath = "..\..\shared-infrastructure"
Write-Progress "Checking shared-infrastructure domain dependency..."
if (Test-Path $sharedInfraPath) {
    Write-Success "Shared-infrastructure domain found at: $sharedInfraPath"
    
    # Check for key infrastructure services
    $infraServices = @("service-registry", "config-server", "api-gateway", "auth-service")
    $foundServices = @()
    
    foreach ($service in $infraServices) {
        if (Test-Path "$sharedInfraPath\$service") {
            $foundServices += $service
        }
    }
    
    if ($foundServices.Count -gt 0) {
        Write-Success "Found infrastructure services: $($foundServices -join ', ')"
    } else {
        Write-Warning "No infrastructure services found in shared-infrastructure domain"
    }
} else {
    Write-Warning "Shared-infrastructure domain not found at expected location: $sharedInfraPath"
    Write-Warning "Social-commerce services may fail at runtime without infrastructure services"
}

# Check for business domain dependencies
$warehousingPath = "..\..\warehousing"
Write-Progress "Checking warehousing domain integration..."
if (Test-Path $warehousingPath) {
    Write-Success "Warehousing domain found at: $warehousingPath"
    
    # Check for key warehousing services
    $warehousingServices = @("inventory-service", "fulfillment-service", "warehouse-management", "billing-service")
    $foundWarehousingServices = @()
    
    foreach ($service in $warehousingServices) {
        if (Test-Path "$warehousingPath\$service") {
            $foundWarehousingServices += $service
        }
    }
    
    if ($foundWarehousingServices.Count -gt 0) {
        Write-Success "Found warehousing services: $($foundWarehousingServices -join ', ')"
    } else {
        Write-Warning "No warehousing services found - order fulfillment may be limited"
    }
} else {
    Write-Warning "Warehousing domain not found at: $warehousingPath"
    Write-Warning "Order fulfillment and inventory management will not be available"
}

$courierPath = "..\..\courier-services"
Write-Progress "Checking courier-services domain integration..."
if (Test-Path $courierPath) {
    Write-Success "Courier-services domain found at: $courierPath"
    
    # Check for key courier services
    $courierServices = @("routing-service", "tracking-service", "third-party-integration", "courier-management")
    $foundCourierServices = @()
    
    foreach ($service in $courierServices) {
        if (Test-Path "$courierPath\$service") {
            $foundCourierServices += $service
        }
    }
    
    if ($foundCourierServices.Count -gt 0) {
        Write-Success "Found courier services: $($foundCourierServices -join ', ')"
    } else {
        Write-Warning "No courier services found - delivery tracking may be limited"
    }
} else {
    Write-Warning "Courier-services domain not found at: $courierPath"
    Write-Warning "Delivery and shipping functionality will not be available"
}

# Verify Java installation
Write-Progress "Verifying Java installation..."
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Java verification successful"
        if ($VerboseOutput) {
            $javaVersion | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
        }
    } else {
        Write-Error "Java verification failed"
        exit 1
    }
} catch {
    Write-Error "Java verification exception: $_"
    exit 1
}

# Define services in optimal compilation order
$services = @(
    @{
        Name = "commission-service"
        Description = "Commission calculations and processing"
        Priority = "HIGH"
        Complexity = "LOW"
        EstimatedTime = 10
        DatabaseDependent = $false
        SecurityDependent = $false
    },
    @{
        Name = "invoice-service" 
        Description = "Invoice generation and management"
        Priority = "HIGH"
        Complexity = "LOW"
        EstimatedTime = 10
        DatabaseDependent = $false
        SecurityDependent = $false
    },
    @{
        Name = "fulfillment-options"
        Description = "Delivery and fulfillment options"
        Priority = "MEDIUM"
        Complexity = "LOW"
        EstimatedTime = 10
        DatabaseDependent = $false
        SecurityDependent = $false
    },
    @{
        Name = "localization-service"
        Description = "Multi-language support and localization"
        Priority = "MEDIUM"
        Complexity = "LOW"
        EstimatedTime = 15
        DatabaseDependent = $false
        SecurityDependent = $false
    },
    @{
        Name = "multi-currency-service"
        Description = "Currency conversion and management"
        Priority = "MEDIUM"
        Complexity = "LOW"
        EstimatedTime = 15
        DatabaseDependent = $false
        SecurityDependent = $false
    },
    @{
        Name = "analytics-service"
        Description = "Business analytics and reporting"
        Priority = "HIGH"
        Complexity = "MEDIUM"
        EstimatedTime = 20
        DatabaseDependent = $true
        SecurityDependent = $false
    },
    @{
        Name = "product-service"
        Description = "Product catalog and management"
        Priority = "HIGH"
        Complexity = "MEDIUM"
        EstimatedTime = 25
        DatabaseDependent = $true
        SecurityDependent = $false
    },
    @{
        Name = "payout-service"
        Description = "Financial disbursements and payouts"
        Priority = "HIGH"
        Complexity = "MEDIUM"
        EstimatedTime = 20
        DatabaseDependent = $true
        SecurityDependent = $false
    },
    @{
        Name = "subscription-service"
        Description = "Subscription management and billing"
        Priority = "MEDIUM"
        Complexity = "MEDIUM"
        EstimatedTime = 25
        DatabaseDependent = $true
        SecurityDependent = $false
    },
    @{
        Name = "vendor-onboarding"
        Description = "Vendor registration and KYC verification"
        Priority = "HIGH"
        Complexity = "MEDIUM"
        EstimatedTime = 30
        DatabaseDependent = $true
        SecurityDependent = $true
    },
    @{
        Name = "order-service"
        Description = "Order management and lifecycle"
        Priority = "CRITICAL"
        Complexity = "HIGH"
        EstimatedTime = 45
        DatabaseDependent = $true
        SecurityDependent = $true
    },
    @{
        Name = "marketplace"
        Description = "Core marketplace functionality"
        Priority = "CRITICAL"
        Complexity = "HIGH"
        EstimatedTime = 60
        DatabaseDependent = $true
        SecurityDependent = $true
    }
)

# Filter services if starting from specific service
if ($StartFromService) {
    $startIndex = $services.FindIndex({$args[0].Name -eq $StartFromService})
    if ($startIndex -ge 0) {
        $services = $services[$startIndex..($services.Count-1)]
        Write-Info "Starting from service: $StartFromService"
    } else {
        Write-Warning "Service '$StartFromService' not found, starting from beginning"
    }
}

# Results tracking
$results = @()
$successCount = 0
$totalServices = $services.Count
$sessionStartTime = Get-Date

Write-Info "Starting compilation of $totalServices services..."

# Progress tracking function
function Update-ProgressFile {
    param($Service, $Status, $Details = "")
    
    if (-not $UpdateProgress) { return }
    
    $progressFile = "PROGRESS_TRACKING_DASHBOARD.md"
    if (Test-Path $progressFile) {
        $content = Get-Content $progressFile -Raw
        $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        
        # Update the live tracking section
        $newEntry = "`n### **[$timestamp] - $($Service.Name)**`n- Action: $Status`n- Description: $($Service.Description)`n- Details: $Details`n"
        
        # Add to execution log
        $content = $content -replace "(\*\[Additional entries will be added in real-time during execution\]\*)", "$newEntry`$1"
        
        # Update current activity
        $currentActivity = "$Status for $($Service.Name)"
        $content = $content -replace "(\*\*Current Activity:\*\*) \[TO BE UPDATED\]", "`$1 $currentActivity"
        
        Set-Content $progressFile $content
    }
}

# Main compilation loop
foreach ($service in $services) {
    $serviceName = $service.Name
    $description = $service.Description
    $priority = $service.Priority
    $complexity = $service.Complexity
    $estimatedTime = $service.EstimatedTime
    
    Write-Host "`n" + "="*80 -ForegroundColor Gray
    Write-Progress "Compiling: $serviceName"
    Write-Host "Description: $description" -ForegroundColor Gray
    Write-Host "Priority: $priority | Complexity: $complexity | Est. Time: $estimatedTime min" -ForegroundColor Gray
    
    Update-ProgressFile $service "Starting compilation"
    
    # Check if service directory exists
    if (-not (Test-Path $serviceName)) {
        Write-Warning "Directory not found: $serviceName"
        $results += @{
            Service = $serviceName
            Status = "SKIPPED"
            Reason = "Directory not found"
            Priority = $priority
            Complexity = $complexity
            Duration = 0
        }
        Update-ProgressFile $service "SKIPPED - Directory not found"
        continue
    }
    
    # Navigate to service directory
    Push-Location $serviceName
    
    try {
        # Check if pom.xml exists
        if (-not (Test-Path "pom.xml")) {
            Write-Warning "No pom.xml found in $serviceName"
            $results += @{
                Service = $serviceName
                Status = "SKIPPED"
                Reason = "No pom.xml found"
                Priority = $priority
                Complexity = $complexity
                Duration = 0
            }
            Update-ProgressFile $service "SKIPPED - No pom.xml found"
            Pop-Location
            continue
        }
        
        $compileStartTime = Get-Date
        
        # Check for known issues
        $knownIssues = @()
        if ($service.DatabaseDependent) {
            $knownIssues += "Database configuration may need adjustment"
        }
        if ($service.SecurityDependent) {
            $knownIssues += "Security bean dependencies may be missing"
        }
        
        # Check for infrastructure dependencies
        if (Get-Content "pom.xml" | Select-String "eureka-client|discovery-client" -Quiet) {
            $knownIssues += "Requires Eureka service registry from shared-infrastructure"
        }
        if (Get-Content "pom.xml" | Select-String "spring-cloud-config" -Quiet) {
            $knownIssues += "Requires Config Server from shared-infrastructure"
        }
        
        if ($knownIssues.Count -gt 0) {
            Write-Warning "Known potential issues:"
            $knownIssues | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
        }
        
        # Run Maven clean compile
        Write-Progress "Running Maven clean compile..."
        
        $compileOutput = @()
        $compileSuccess = $false
        
        try {
            # Capture both stdout and stderr
            $pinfo = New-Object System.Diagnostics.ProcessStartInfo
            $pinfo.FileName = "cmd"
            $pinfo.Arguments = "/c `"set JAVA_HOME=$JAVA_HOME && mvnw.cmd clean compile`""
            $pinfo.UseShellExecute = $false
            $pinfo.RedirectStandardOutput = $true
            $pinfo.RedirectStandardError = $true
            $pinfo.CreateNoWindow = $true
            
            $process = New-Object System.Diagnostics.Process
            $process.StartInfo = $pinfo
            $process.Start() | Out-Null
            
            $stdout = $process.StandardOutput.ReadToEnd()
            $stderr = $process.StandardError.ReadToEnd()
            $process.WaitForExit()
            
            $compileOutput = $stdout + $stderr
            $compileSuccess = ($process.ExitCode -eq 0)
            
        } catch {
            $compileOutput = "Exception during compilation: $_"
            $compileSuccess = $false
        }
        
        $compileDuration = (Get-Date) - $compileStartTime
        
        if ($compileSuccess) {
            Write-Success "$serviceName compiled successfully! (Duration: $($compileDuration.TotalMinutes.ToString('F1')) min)"
            $successCount++
            
            $results += @{
                Service = $serviceName
                Status = "SUCCESS"
                Reason = "Compiled successfully"
                Priority = $priority
                Complexity = $complexity
                Duration = $compileDuration.TotalMinutes
                Output = $compileOutput
            }
            
            Update-ProgressFile $service "COMPILED SUCCESSFULLY" "Duration: $($compileDuration.TotalMinutes.ToString('F1')) min"
            
            # Run tests if compilation succeeded and not skipping tests
            if (-not $SkipTests) {
                Write-Progress "Running tests for $serviceName..."
                
                try {
                    $testProcess = Start-Process -FilePath "cmd" -ArgumentList "/c", "`"set JAVA_HOME=$JAVA_HOME && mvnw.cmd test`"" -NoNewWindow -Wait -PassThru
                    
                    if ($testProcess.ExitCode -eq 0) {
                        Write-Success "Tests passed for $serviceName"
                        Update-ProgressFile $service "TESTS PASSED"
                    } else {
                        Write-Warning "Tests failed for $serviceName (compilation OK)"
                        Update-ProgressFile $service "TESTS FAILED (compilation OK)"
                    }
                } catch {
                    Write-Warning "Exception during testing: $_"
                }
            }
            
        } else {
            Write-Error "$serviceName compilation failed"
            
            # Analyze common failure patterns
            $errorAnalysis = ""
            if ($compileOutput -match "package javax\.|import javax\.") {
                $errorAnalysis = "javax to jakarta migration needed"
            } elseif ($compileOutput -match "Cannot resolve symbol|package does not exist") {
                $errorAnalysis = "Missing dependencies or import issues"
            } elseif ($compileOutput -match "Access is denied|Permission denied") {
                $errorAnalysis = "File permission or access issues"
            } elseif ($compileOutput -match "JAVA_HOME") {
                $errorAnalysis = "Java environment configuration issue"
            } else {
                $errorAnalysis = "Unknown compilation error"
            }
            
            Write-Error "Error Analysis: $errorAnalysis"
            
            if ($VerboseOutput) {
                Write-Host "Compilation Output:" -ForegroundColor Red
                $compileOutput -split "`n" | Select-Object -Last 20 | ForEach-Object { 
                    Write-Host "  $_" -ForegroundColor DarkRed 
                }
            }
            
            $results += @{
                Service = $serviceName
                Status = "FAILED"
                Reason = $errorAnalysis
                Priority = $priority
                Complexity = $complexity
                Duration = $compileDuration.TotalMinutes
                Output = $compileOutput
                Error = $errorAnalysis
            }
            
            Update-ProgressFile $service "COMPILATION FAILED" "Error: $errorAnalysis"
        }
        
    } catch {
        Write-Error "Exception during compilation process: $_"
        $results += @{
            Service = $serviceName
            Status = "ERROR"
            Reason = "Exception: $_"
            Priority = $priority
            Complexity = $complexity
            Duration = 0
        }
        Update-ProgressFile $service "ERROR" "Exception: $_"
    } finally {
        Pop-Location
    }
    
    # Brief pause between services
    Start-Sleep -Seconds 1
}

# Calculate session metrics
$endTime = Get-Date
$totalDuration = $endTime - $sessionStartTime
$averageTimePerService = if ($totalServices -gt 0) { $totalDuration.TotalMinutes / $totalServices } else { 0 }

# Print comprehensive summary
Write-Host "`n" + "="*80 -ForegroundColor Green
Write-Host "📊 COMPILATION SESSION SUMMARY" -ForegroundColor Green
Write-Host "="*80 -ForegroundColor Green

Write-Host "Session Duration: $($totalDuration.TotalMinutes.ToString('F1')) minutes" -ForegroundColor Cyan
Write-Host "Total Services: $totalServices" -ForegroundColor Cyan
Write-Host "Successful: $successCount" -ForegroundColor Green
Write-Host "Failed: $($totalServices - $successCount)" -ForegroundColor Red
Write-Host "Success Rate: $([math]::Round(($successCount/$totalServices)*100, 2))%" -ForegroundColor Cyan
Write-Host "Average Time Per Service: $($averageTimePerService.ToString('F1')) minutes" -ForegroundColor Cyan

# Detailed results by priority
Write-Host "`n📋 DETAILED RESULTS BY PRIORITY:" -ForegroundColor Yellow

$priorityGroups = $results | Group-Object Priority
foreach ($group in $priorityGroups) {
    $priority = $group.Name
    $services = $group.Group
    $successful = ($services | Where-Object { $_.Status -eq "SUCCESS" }).Count
    $total = $services.Count
    
    Write-Host "`n$priority PRIORITY SERVICES ($successful/$total successful):" -ForegroundColor Cyan
    
    foreach ($result in $services | Sort-Object Service) {
        $status = $result.Status
        $color = switch ($status) {
            "SUCCESS" { "Green" }
            "FAILED" { "Red" }
            "SKIPPED" { "Yellow" }
            "ERROR" { "Magenta" }
            default { "White" }
        }
        
        $durationText = if ($result.Duration -gt 0) { " ($($result.Duration.ToString('F1'))m)" } else { "" }
        Write-Host "  $($result.Service): $status$durationText" -ForegroundColor $color
        
        if ($result.Reason -and $result.Status -ne "SUCCESS") {
            Write-Host "    → $($result.Reason)" -ForegroundColor Gray
        }
    }
}

# Generate actionable next steps
Write-Host "`n🎯 ACTIONABLE NEXT STEPS:" -ForegroundColor Cyan

$failedCritical = $results | Where-Object { $_.Status -eq "FAILED" -and $_.Priority -eq "CRITICAL" }
$failedHigh = $results | Where-Object { $_.Status -eq "FAILED" -and $_.Priority -eq "HIGH" }
$failedMedium = $results | Where-Object { $_.Status -eq "FAILED" -and $_.Priority -eq "MEDIUM" }

if ($failedCritical.Count -gt 0) {
    Write-Host "`n🚨 CRITICAL FAILURES - Address immediately:" -ForegroundColor Red
    foreach ($service in $failedCritical) {
        Write-Host "  1. Fix $($service.Service): $($service.Reason)" -ForegroundColor Red
    }
}

if ($failedHigh.Count -gt 0) {
    Write-Host "`n⚠️ HIGH PRIORITY FAILURES - Address today:" -ForegroundColor Yellow
    foreach ($service in $failedHigh) {
        Write-Host "  • Fix $($service.Service): $($service.Reason)" -ForegroundColor Yellow
    }
}

if ($successCount -eq $totalServices) {
    Write-Host "`n🎉 PERFECT SUCCESS! All services compiled successfully!" -ForegroundColor Green
    Write-Host "🚀 Ready for integration testing and deployment!" -ForegroundColor Green
    Write-Host "`nRecommended next steps:" -ForegroundColor Cyan
    Write-Host "  1. Run integration tests across services" -ForegroundColor White
    Write-Host "  2. Start Docker containerization" -ForegroundColor White
    Write-Host "  3. Deploy to staging environment" -ForegroundColor White
} elseif ($successCount / $totalServices -ge 0.8) {
    Write-Host "`n🎯 EXCELLENT PROGRESS! 80%+ success rate achieved!" -ForegroundColor Green
    Write-Host "Focus on fixing remaining issues for production readiness." -ForegroundColor Green
} elseif ($successCount / $totalServices -ge 0.6) {
    Write-Host "`n👍 GOOD PROGRESS! Over 60% success rate." -ForegroundColor Yellow
    Write-Host "Continue with systematic issue resolution." -ForegroundColor Yellow
} else {
    Write-Host "`n🔧 NEEDS ATTENTION: Success rate below 60%." -ForegroundColor Red
    Write-Host "Focus on resolving fundamental environment or configuration issues." -ForegroundColor Red
}

# Save detailed results to log files
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# Compilation results log
$logContent = @"
Social Commerce Domain Compilation Results
Generated: $(Get-Date)
Session Duration: $($totalDuration.TotalMinutes.ToString('F1')) minutes
JAVA_HOME: $JAVA_HOME

SUMMARY:
Total Services: $totalServices
Successful: $successCount  
Failed: $($totalServices - $successCount)
Success Rate: $([math]::Round(($successCount/$totalServices)*100, 2))%
Average Time Per Service: $($averageTimePerService.ToString('F1')) minutes

DETAILED RESULTS:
"@

foreach ($result in $results | Sort-Object Priority, Service) {
    $logContent += "`n$($result.Service): $($result.Status) ($($result.Priority))"
    $logContent += "`n  Complexity: $($result.Complexity)"
    $logContent += "`n  Duration: $($result.Duration.ToString('F1')) minutes"
    if ($result.Reason) {
        $logContent += "`n  Reason: $($result.Reason)"
    }
    if ($result.Error) {
        $logContent += "`n  Error Details: $($result.Error)"
    }
    $logContent += "`n"
}

$logContent | Out-File -FilePath "compilation-results-$timestamp.log" -Encoding UTF8

# Error details log for failed services
$failedServices = $results | Where-Object { $_.Status -eq "FAILED" -or $_.Status -eq "ERROR" }
if ($failedServices.Count -gt 0) {
    $errorLogContent = @"
Social Commerce Domain - Error Details Log
Generated: $(Get-Date)

FAILED SERVICES DETAILED OUTPUT:
"@

    foreach ($service in $failedServices) {
        $errorLogContent += "`n" + "="*80
        $errorLogContent += "`nSERVICE: $($service.Service)"
        $errorLogContent += "`nSTATUS: $($service.Status)"
        $errorLogContent += "`nREASON: $($service.Reason)"
        $errorLogContent += "`nPRIORITY: $($service.Priority)"
        $errorLogContent += "`n" + "-"*80
        if ($service.Output) {
            $errorLogContent += "`nCOMPILATION OUTPUT:"
            $errorLogContent += "`n$($service.Output)"
        }
        $errorLogContent += "`n"
    }

    $errorLogContent | Out-File -FilePath "compilation-errors-$timestamp.log" -Encoding UTF8
    Write-Info "Error details saved to: compilation-errors-$timestamp.log"
}

Write-Info "Compilation results saved to: compilation-results-$timestamp.log"

# Update progress dashboard with final summary
Update-ProgressFile @{Name="SESSION_COMPLETE"; Description="Compilation session completed"} "SESSION COMPLETED" "Success Rate: $([math]::Round(($successCount/$totalServices)*100, 2))%"

Write-Host "`n✅ Social Commerce compilation script completed!" -ForegroundColor Green
Write-Host "Session End Time: $($endTime.ToString('yyyy-MM-dd HH:mm:ss'))" -ForegroundColor Gray

# Return exit code based on success rate
if ($successCount / $totalServices -ge 0.8) {
    exit 0  # Success
} elseif ($successCount / $totalServices -ge 0.5) {
    exit 1  # Partial success
} else {
    exit 2  # Needs attention
}