# PowerShell script to copy social-commerce-shared from source to destination
# This is a Java Spring Boot shared library service (not a frontend app)
param(
    [string]$SourcePath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\social-ecommerce-ecosystem\social-commerce\social-commerce-shared",
    [string]$DestPath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\Exalt-Application-Limited\social-ecommerce-ecosystem\social-commerce\social-commerce-shared"
)

Write-Host "🔄 Starting social-commerce-shared Java service migration..." -ForegroundColor Cyan
Write-Host "Source: $SourcePath" -ForegroundColor Yellow
Write-Host "Destination: $DestPath" -ForegroundColor Yellow

# Check if source exists
if (-not (Test-Path $SourcePath)) {
    Write-Host "❌ Source path does not exist: $SourcePath" -ForegroundColor Red
    exit 1
}

# Backup the existing pom.xml (might have updated package structure)
$pomFile = Join-Path $DestPath "pom.xml"
if (Test-Path $pomFile) {
    $backupFile = Join-Path $DestPath "pom.xml.backup-destination"
    Write-Host "📦 Backing up destination pom.xml to: $backupFile" -ForegroundColor Yellow
    Copy-Item $pomFile $backupFile -Force
}

# Remove the Doc folder if it exists (placeholder folder)
$docFolder = Join-Path $DestPath "Doc"
if (Test-Path $docFolder) {
    Write-Host "🗑️ Removing placeholder Doc folder" -ForegroundColor Yellow
    Remove-Item $docFolder -Recurse -Force
}

# Copy all Java service files using robocopy
Write-Host "📋 Copying Java shared library service files..." -ForegroundColor Green
$robocopyResult = robocopy $SourcePath $DestPath /E /XO /R:3 /W:5 /NP

# Check robocopy exit codes (0-7 are success, 8+ are errors)
$exitCode = $LASTEXITCODE
if ($exitCode -ge 8) {
    Write-Host "❌ Robocopy failed with exit code: $exitCode" -ForegroundColor Red
    exit 1
} else {
    Write-Host "✅ Files copied successfully (exit code: $exitCode)" -ForegroundColor Green
}

# Verify key files exist
$keyFiles = @(
    "src\main\java\com\socialecommerceecosystem\shared\config\KafkaConfig.java",
    "src\main\java\com\socialecommerceecosystem\shared\config\RedisConfig.java",
    "src\main\java\com\socialecommerceecosystem\shared\event\DomainEvent.java",
    "src\main\java\com\socialecommerceecosystem\shared\event\EventPublisher.java",
    "src\main\java\com\socialecommerceecosystem\shared\event\OrderPlacedEvent.java",
    "src\main\java\com\socialecommerceecosystem\shared\handler\EventHandler.java"
)

Write-Host "🔍 Verifying copied files..." -ForegroundColor Cyan
$allFilesExist = $true

foreach ($file in $keyFiles) {
    $filePath = Join-Path $DestPath $file
    if (Test-Path $filePath) {
        Write-Host "✅ Found: $file" -ForegroundColor Green
    } else {
        Write-Host "❌ Missing: $file" -ForegroundColor Red
        $allFilesExist = $false
    }
}

# Check package naming compliance
Write-Host "`n🔍 Package Naming Analysis:" -ForegroundColor Cyan
$oldPackagePath = Join-Path $DestPath "src\main\java\com\socialecommerceecosystem"
$newPackagePath = Join-Path $DestPath "src\main\java\com\exalt\ecosystem\socialcommerce"

if (Test-Path $oldPackagePath) {
    Write-Host "⚠️ Old package structure found: com.socialecommerceecosystem" -ForegroundColor Yellow
    Write-Host "   This needs to be updated to: com.exalt.ecosystem.socialcommerce.shared" -ForegroundColor Yellow
    $needsPackageUpdate = $true
} else {
    Write-Host "✅ No old package structure found" -ForegroundColor Green
    $needsPackageUpdate = $false
}

if (Test-Path $newPackagePath) {
    Write-Host "✅ New package structure found: com.exalt.ecosystem.socialcommerce" -ForegroundColor Green
    $hasNewPackage = $true
} else {
    Write-Host "⚠️ New package structure not found: com.exalt.ecosystem.socialcommerce" -ForegroundColor Yellow
    $hasNewPackage = $false
}

if ($allFilesExist) {
    Write-Host "🎉 social-commerce-shared migration completed successfully!" -ForegroundColor Green
    Write-Host "☕ Java Spring Boot shared library service ready" -ForegroundColor Green
    
    # Show summary
    Write-Host "`n📊 Migration Summary:" -ForegroundColor Cyan
    Write-Host "• Technology: Java 17 + Spring Boot 3.1.0 shared library" -ForegroundColor White
    Write-Host "• Type: Shared configuration and event handling service" -ForegroundColor White
    Write-Host "• Features: Kafka, Redis, Resilience4j configs, Domain events" -ForegroundColor White
    Write-Host "• Components: Event publisher, event handlers, shared configurations" -ForegroundColor White
    Write-Host "• Status: Complete Java service implementation" -ForegroundColor White
    
    Write-Host "`n🏗️ Service Architecture:" -ForegroundColor Cyan
    Write-Host "• Config: Kafka, Redis, Database optimization, Monitoring, Resilience4j" -ForegroundColor White
    Write-Host "• Events: Order, Payment, Product, User, Commission events" -ForegroundColor White
    Write-Host "• Handlers: Event processing and distribution" -ForegroundColor White
    Write-Host "• Purpose: Shared infrastructure components for all social-commerce services" -ForegroundColor White
    
    if ($needsPackageUpdate) {
        Write-Host "`n🔧 Package Standardization Needed:" -ForegroundColor Yellow
        Write-Host "• Current: com.socialecommerceecosystem.shared" -ForegroundColor Yellow
        Write-Host "• Target: com.exalt.ecosystem.socialcommerce.shared" -ForegroundColor Yellow
        Write-Host "• Action: Run package standardization script" -ForegroundColor Yellow
    } else {
        Write-Host "`n✅ Package Structure:" -ForegroundColor Green
        Write-Host "• Already using standardized com.exalt package structure" -ForegroundColor Green
    }
    
    Write-Host "`n🔧 Next Steps:" -ForegroundColor Cyan
    Write-Host "1. cd to the destination directory" -ForegroundColor White
    if ($needsPackageUpdate) {
        Write-Host "2. Run package standardization to update to com.exalt structure" -ForegroundColor White
        Write-Host "3. Update pom.xml groupId to com.exalt.ecosystem.socialcommerce" -ForegroundColor White
        Write-Host "4. Run 'mvn clean compile' to test compilation" -ForegroundColor White
    } else {
        Write-Host "2. Run 'mvn clean compile' to test compilation" -ForegroundColor White
        Write-Host "3. Verify shared library is available for other services" -ForegroundColor White
    }
    Write-Host "$(if ($needsPackageUpdate) { '5' } else { '4' }). Deploy as shared library for social-commerce services" -ForegroundColor White
    
} else {
    Write-Host "⚠️ Some files may be missing. Please verify the copy operation." -ForegroundColor Yellow
}

Write-Host "`n📝 Important Notes:" -ForegroundColor Cyan
Write-Host "• This is a Java Spring Boot shared library service" -ForegroundColor White
Write-Host "• Provides shared configurations and event handling for social-commerce domain" -ForegroundColor White
Write-Host "• Contains Kafka, Redis, and monitoring configurations" -ForegroundColor White
Write-Host "• Implements domain events for inter-service communication" -ForegroundColor White
Write-Host "• Should be built and deployed as a shared dependency" -ForegroundColor White

if ($needsPackageUpdate) {
    Write-Host "`n⚠️ Package Naming Compliance:" -ForegroundColor Yellow
    Write-Host "• Service needs package standardization to com.exalt structure" -ForegroundColor White
    Write-Host "• This aligns with the social-commerce domain naming standards" -ForegroundColor White
    Write-Host "• Update required for consistency with other services" -ForegroundColor White
}

exit 0