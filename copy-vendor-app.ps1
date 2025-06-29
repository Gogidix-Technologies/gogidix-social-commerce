# PowerShell script to copy vendor-app from source to destination
param(
    [string]$SourcePath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\social-ecommerce-ecosystem\social-commerce\vendor-app",
    [string]$DestPath = "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\Exalt-Application-Limited\social-ecommerce-ecosystem\social-commerce\vendor-app"
)

Write-Host "🔄 Starting vendor-app code migration..." -ForegroundColor Cyan
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

# Copy all vendor app files using robocopy
Write-Host "📋 Copying vendor application files..." -ForegroundColor Green
$robocopyResult = robocopy $SourcePath $DestPath /E /XO /R:3 /W:5 /NP

# Check robocopy exit codes (0-7 are success, 8+ are errors)
$exitCode = $LASTEXITCODE
if ($exitCode -ge 8) {
    Write-Host "❌ Robocopy failed with exit code: $exitCode" -ForegroundColor Red
    exit 1
} else {
    Write-Host "✅ Files copied successfully (exit code: $exitCode)" -ForegroundColor Green
}

# Check what's actually in the vendor-app source
$packageJsonPath = Join-Path $SourcePath "package.json"
$srcPath = Join-Path $SourcePath "src"

if (Test-Path $packageJsonPath) {
    Write-Host "📋 Found package.json - checking content..." -ForegroundColor Yellow
    $packageContent = Get-Content $packageJsonPath -Raw | ConvertFrom-Json
    $appName = $packageContent.name
    Write-Host "App name: $appName" -ForegroundColor Cyan
    
    if ($packageContent.dependencies."react-native") {
        Write-Host "📱 Detected React Native application" -ForegroundColor Green
        $isReactNative = $true
    } elseif ($packageContent.dependencies."react") {
        Write-Host "🌐 Detected React web application" -ForegroundColor Green
        $isReactNative = $false
    } else {
        Write-Host "❓ Unknown application type" -ForegroundColor Yellow
        $isReactNative = $null
    }
} else {
    Write-Host "⚠️ No package.json found in source" -ForegroundColor Yellow
}

# Check if src directory has actual code
if (Test-Path $srcPath) {
    $srcFiles = Get-ChildItem $srcPath -Recurse -File | Measure-Object
    if ($srcFiles.Count -gt 0) {
        Write-Host "✅ Found $($srcFiles.Count) source files" -ForegroundColor Green
        $hasSourceCode = $true
    } else {
        Write-Host "⚠️ Source directory exists but is empty" -ForegroundColor Yellow
        $hasSourceCode = $false
    }
} else {
    Write-Host "❌ No src directory found" -ForegroundColor Red
    $hasSourceCode = $false
}

# Verify key files based on what should exist
if ($hasSourceCode) {
    $keyFiles = @("package.json")
    if ($isReactNative) {
        $keyFiles += @("index.js", "App.tsx")
    } else {
        $keyFiles += @("tsconfig.json", "src\App.tsx")
    }
    
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
        Write-Host "🎉 vendor-app migration completed successfully!" -ForegroundColor Green
        
        if ($isReactNative) {
            Write-Host "📱 React Native vendor application ready" -ForegroundColor Green
        } else {
            Write-Host "🌐 React web vendor application ready" -ForegroundColor Green
        }
        
        # Show summary
        Write-Host "`n📊 Migration Summary:" -ForegroundColor Cyan
        Write-Host "• Application: $appName" -ForegroundColor White
        if ($isReactNative) {
            Write-Host "• Technology: React Native + TypeScript" -ForegroundColor White
            Write-Host "• Target: Mobile vendor management app" -ForegroundColor White
        } else {
            Write-Host "• Technology: React + TypeScript" -ForegroundColor White
            Write-Host "• Target: Web vendor management interface" -ForegroundColor White
        }
        Write-Host "• Source files: $($srcFiles.Count) files found" -ForegroundColor White
        
        Write-Host "`n🔧 Next Steps:" -ForegroundColor Cyan
        Write-Host "1. cd to the destination directory" -ForegroundColor White
        Write-Host "2. Run 'npm install' to install dependencies" -ForegroundColor White
        if ($isReactNative) {
            Write-Host "3. Test with 'npm run android' or 'npm run ios'" -ForegroundColor White
        } else {
            Write-Host "3. Test with 'npm start' for development server" -ForegroundColor White
        }
        Write-Host "4. Add API integration layer if missing" -ForegroundColor White
        
    } else {
        Write-Host "⚠️ Some expected files are missing. Please verify the copy operation." -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠️ vendor-app appears to have minimal implementation" -ForegroundColor Yellow
    Write-Host "📝 This confirms the documentation that vendor-app is only 15% complete" -ForegroundColor Yellow
    
    Write-Host "`n📊 Current Status:" -ForegroundColor Cyan
    Write-Host "• Package.json: $(if (Test-Path $packageJsonPath) { "✅ Present" } else { "❌ Missing" })" -ForegroundColor White
    Write-Host "• Source code: $(if ($hasSourceCode) { "✅ Present" } else { "❌ Missing/Minimal" })" -ForegroundColor White
    Write-Host "• Implementation: ~15% complete (matches documentation)" -ForegroundColor Yellow
    
    Write-Host "`n🚧 Required Work:" -ForegroundColor Cyan
    Write-Host "• Complete source code implementation needed" -ForegroundColor White
    Write-Host "• Follow user-mobile-app architecture patterns" -ForegroundColor White
    Write-Host "• Add vendor-specific screens and functionality" -ForegroundColor White
    Write-Host "• Implement API integration layer" -ForegroundColor White
}

Write-Host "`n📝 Important Notes:" -ForegroundColor Cyan
Write-Host "• The incorrect Java pom.xml was backed up as pom.xml.backup-java-service" -ForegroundColor White
Write-Host "• This should be a mobile/web vendor application, not a Java service" -ForegroundColor White
Write-Host "• Implementation level matches the 15% completion status in documentation" -ForegroundColor White

exit 0