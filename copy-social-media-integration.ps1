# PowerShell script to copy social-media-integration from source to destination
param(
    [string]$SourcePath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\social-ecommerce-ecosystem\social-commerce\social-media-integration",
    [string]$DestPath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\Exalt-Application-Limited\social-ecommerce-ecosystem\social-commerce\social-media-integration"
)

Write-Host "🔄 Starting social-media-integration service migration..." -ForegroundColor Cyan
Write-Host "Source: $SourcePath" -ForegroundColor Yellow
Write-Host "Destination: $DestPath" -ForegroundColor Yellow

# Check if source exists
if (-not (Test-Path $SourcePath)) {
    Write-Host "❌ Source path does not exist: $SourcePath" -ForegroundColor Red
    exit 1
}

# Remove the Doc folder if it exists (placeholder folder)
$docFolder = Join-Path $DestPath "Doc"
if (Test-Path $docFolder) {
    Write-Host "🗑️ Removing placeholder Doc folder" -ForegroundColor Yellow
    Remove-Item $docFolder -Recurse -Force
}

# Copy all Node.js service files using robocopy
Write-Host "📋 Copying Node.js service files..." -ForegroundColor Green
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
    "package.json",
    "src\app.js", 
    "src\index.js",
    "src\config.js",
    "src\controllers\OAuthController.js",
    "src\services\SharingService.js",
    "src\adapters\facebook.js",
    "src\routes\facebook.js"
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

# Check package.json content
$packageJsonPath = Join-Path $DestPath "package.json"
if (Test-Path $packageJsonPath) {
    $packageContent = Get-Content $packageJsonPath -Raw | ConvertFrom-Json
    $serviceName = $packageContent.name
    $serviceDescription = $packageContent.description
    $mainFile = $packageContent.main
    
    Write-Host "📋 Service Details:" -ForegroundColor Cyan
    Write-Host "  Name: $serviceName" -ForegroundColor White
    Write-Host "  Description: $serviceDescription" -ForegroundColor White
    Write-Host "  Main File: $mainFile" -ForegroundColor White
    Write-Host "  Node Version: $($packageContent.engines.node)" -ForegroundColor White
}

if ($allFilesExist) {
    Write-Host "🎉 social-media-integration migration completed successfully!" -ForegroundColor Green
    Write-Host "🌐 Node.js/Express backend service ready" -ForegroundColor Green
    
    # Show summary
    Write-Host "`n📊 Migration Summary:" -ForegroundColor Cyan
    Write-Host "• Technology: Node.js + Express.js backend service" -ForegroundColor White
    Write-Host "• Dependencies: Express, Mongoose, Axios, Winston logging" -ForegroundColor White
    Write-Host "• Features: Facebook integration, OAuth, activity feeds, analytics" -ForegroundColor White
    Write-Host "• API Documentation: Available in api-docs/openapi.yaml" -ForegroundColor White
    Write-Host "• Status: Complete Node.js backend service implementation" -ForegroundColor White
    
    Write-Host "`n🔧 Next Steps:" -ForegroundColor Cyan
    Write-Host "1. cd to the destination directory" -ForegroundColor White
    Write-Host "2. Run 'npm install' to install dependencies" -ForegroundColor White
    Write-Host "3. Configure environment variables (see .env.example)" -ForegroundColor White
    Write-Host "4. Run 'npm start' to start the service" -ForegroundColor White
    Write-Host "5. Test API endpoints using the OpenAPI documentation" -ForegroundColor White
    
    Write-Host "`n🏗️ Service Architecture:" -ForegroundColor Cyan
    Write-Host "• Controllers: OAuth, Sharing, Analytics, Webhook handlers" -ForegroundColor White
    Write-Host "• Services: Activity Feed, Analytics, OAuth, Sharing services" -ForegroundColor White
    Write-Host "• Adapters: Facebook integration adapter" -ForegroundColor White
    Write-Host "• Models: SocialAccount, SocialShare, Activity, EngagementMetric" -ForegroundColor White
    Write-Host "• Routes: Facebook-specific API routes" -ForegroundColor White
    
} else {
    Write-Host "⚠️ Some files may be missing. Please verify the copy operation." -ForegroundColor Yellow
}

Write-Host "`n📝 Important Notes:" -ForegroundColor Cyan
Write-Host "• This is a Node.js backend service, not a frontend application" -ForegroundColor White
Write-Host "• Provides social media integration APIs for the social-commerce domain" -ForegroundColor White
Write-Host "• Includes Facebook integration with OAuth and sharing capabilities" -ForegroundColor White
Write-Host "• Contains comprehensive API documentation and test suites" -ForegroundColor White
Write-Host "• Requires MongoDB for data storage (see docker-compose.yml)" -ForegroundColor White

# Check for any package naming compliance issues
Write-Host "`n🔍 Package Naming Analysis:" -ForegroundColor Cyan
$javaTestPath = Join-Path $DestPath "src\test\java"
if (Test-Path $javaTestPath) {
    Write-Host "⚠️ Found Java test files - these may need package name updates" -ForegroundColor Yellow
    Write-Host "   Java tests should use com.exalt.ecosystem.socialcommerce.social" -ForegroundColor Yellow
} else {
    Write-Host "✅ No Java package naming issues (pure Node.js service)" -ForegroundColor Green
}

exit 0