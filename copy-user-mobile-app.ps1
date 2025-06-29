# PowerShell script to copy user-mobile-app from source to destination
param(
    [string]$SourcePath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\social-ecommerce-ecosystem\social-commerce\user-mobile-app",
    [string]$DestPath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\Exalt-Application-Limited\social-ecommerce-ecosystem\social-commerce\user-mobile-app"
)

Write-Host "🔄 Starting user-mobile-app code migration..." -ForegroundColor Cyan
Write-Host "Source: $SourcePath" -ForegroundColor Yellow
Write-Host "Destination: $DestPath" -ForegroundColor Yellow

# Check if source exists
if (-not (Test-Path $SourcePath)) {
    Write-Host "❌ Source path does not exist: $SourcePath" -ForegroundColor Red
    exit 1
}

# Backup the existing pom.xml (Java Spring Boot file that shouldn't be there)
$pomFile = Join-Path $DestPath "pom.xml"
if (Test-Path $pomFile) {
    $backupFile = Join-Path $DestPath "pom.xml.backup-java-service"
    Write-Host "📦 Backing up incorrect Java pom.xml to: $backupFile" -ForegroundColor Yellow
    Copy-Item $pomFile $backupFile -Force
}

# Remove the Doc folder if it exists (placeholder folder)
$docFolder = Join-Path $DestPath "Doc"
if (Test-Path $docFolder) {
    Write-Host "🗑️ Removing placeholder Doc folder" -ForegroundColor Yellow
    Remove-Item $docFolder -Recurse -Force
}

# Copy all React Native files using robocopy
Write-Host "📋 Copying React Native application files..." -ForegroundColor Green
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
    "App.tsx", 
    "index.js",
    "src\store\index.ts",
    "src\screens\HomeScreen.tsx"
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

if ($allFilesExist) {
    Write-Host "🎉 user-mobile-app migration completed successfully!" -ForegroundColor Green
    Write-Host "📱 React Native 0.71.7 + TypeScript application ready" -ForegroundColor Green
    
    # Show summary
    Write-Host "`n📊 Migration Summary:" -ForegroundColor Cyan
    Write-Host "• Technology: React Native 0.71.7 + TypeScript" -ForegroundColor White
    Write-Host "• Dependencies: Redux Toolkit, React Navigation, Axios" -ForegroundColor White
    Write-Host "• Features: Multi-currency support, Shopping cart, Order history" -ForegroundColor White
    Write-Host "• Status: 60% complete (architecture ready, needs API integration)" -ForegroundColor White
    
    Write-Host "`n🔧 Next Steps:" -ForegroundColor Cyan
    Write-Host "1. cd to the destination directory" -ForegroundColor White
    Write-Host "2. Run 'npm install' to install dependencies" -ForegroundColor White
    Write-Host "3. Add API integration layer (similar to user-web-app)" -ForegroundColor White
    Write-Host "4. Test React Native setup with 'npm run android' or 'npm run ios'" -ForegroundColor White
    
} else {
    Write-Host "⚠️ Some files may be missing. Please verify the copy operation." -ForegroundColor Yellow
}

Write-Host "`n📝 Important Notes:" -ForegroundColor Cyan
Write-Host "• The incorrect Java pom.xml was backed up as pom.xml.backup-java-service" -ForegroundColor White
Write-Host "• This is a React Native mobile app, not a Java service" -ForegroundColor White
Write-Host "• The app uses com.exalt package structure in TypeScript interfaces" -ForegroundColor White

exit 0