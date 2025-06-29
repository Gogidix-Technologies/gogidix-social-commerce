# Package Cleanup and Compilation Testing Instructions

## Overview
After the naming standardization to `com.exalt.ecosystem.socialcommerce`, we need to clean up old package directories and ensure all services compile properly.

## Current Status Analysis

### ✅ Services with Correct Package Structure Only
- **API Gateway**: `/src/main/java/com/exalt/ecosystem/socialcommerce/gateway/`
- **Commission Service**: `/src/main/java/com/exalt/ecosystem/socialcommerce/commission/`

### ⚠️ Services Requiring Cleanup
- **Product Service**: Has both new (`com/exalt/ecosystem/socialcommerce/product/`) and old (`com/socialcommerce/product/`) structures

## Cleanup Steps

### Step 1: Manual Directory Removal
Navigate to each service directory and remove old package structures:

```powershell
# For Product Service - Remove the old com/socialcommerce directory
Remove-Item -Path ".\product-service\src\main\java\com\socialcommerce" -Recurse -Force

# Check for other old patterns in remaining services:
# - com/socialecommerceecosystem/
# - com/microecommerce/
# - com/socialcommerce/ (if not part of the new exalt structure)
```

### Step 2: Services to Check and Clean

Run these commands from the social-commerce directory:

```batch
:: Check each service for old package directories
dir /s /b "commission-service\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "fulfillment-options\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "global-hq-admin\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "localization-service\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "marketplace\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "multi-currency-service\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "order-service\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "payment-gateway\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "payout-service\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "product-service\src\main\java\com\socialcommerce" 2>nul
dir /s /b "regional-admin\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "social-media-integration\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "subscription-service\src\main\java\com\socialecommerceecosystem" 2>nul
dir /s /b "vendor-onboarding\src\main\java\com\socialecommerceecosystem" 2>nul
```

### Step 3: Compilation Testing

Test each service individually:

```batch
cd api-gateway
mvn clean compile -DskipTests=true
cd ..\commission-service  
mvn clean compile -DskipTests=true
cd ..\order-service
mvn clean compile -DskipTests=true
cd ..\product-service
mvn clean compile -DskipTests=true
cd ..\marketplace
mvn clean compile -DskipTests=true
```

For Node.js services (like social-media-integration):
```batch
cd social-media-integration
npm install
npm run build
```

## Expected Results

### ✅ Successful Compilation Indicators
- Maven output: `BUILD SUCCESS`
- No compilation errors in logs
- Target directory created with compiled classes

### ❌ Common Issues to Fix
1. **Import statements** referencing old package names
2. **Configuration files** with old package references
3. **Test files** not updated to new package structure

## Compilation Status Tracking

| Service | Package Cleanup | Compilation | Status | Notes |
|---------|----------------|-------------|---------|-------|
| api-gateway | ✅ Complete | 🔄 Testing | - | Only new package structure |
| commission-service | ✅ Complete | 🔄 Testing | - | Only new package structure |
| product-service | ⚠️ Needs cleanup | ⏳ Pending | - | Remove com/socialcommerce |
| order-service | 🔄 Checking | ⏳ Pending | - | - |
| marketplace | 🔄 Checking | ⏳ Pending | - | - |
| payment-gateway | 🔄 Checking | ⏳ Pending | - | - |
| social-media-integration | 🔄 Checking | ⏳ Pending | - | Node.js service |
| fulfillment-options | 🔄 Checking | ⏳ Pending | - | - |
| localization-service | 🔄 Checking | ⏳ Pending | - | - |
| multi-currency-service | 🔄 Checking | ⏳ Pending | - | - |
| payout-service | 🔄 Checking | ⏳ Pending | - | - |
| regional-admin | 🔄 Checking | ⏳ Pending | - | - |
| global-hq-admin | 🔄 Checking | ⏳ Pending | - | - |
| subscription-service | 🔄 Checking | ⏳ Pending | - | - |
| vendor-onboarding | 🔄 Checking | ⏳ Pending | - | - |

## Next Steps

1. **Execute cleanup scripts** provided (cleanup-old-packages.ps1 and test-compilation.bat)
2. **Review compilation results** for any failures
3. **Fix import statements** in files that fail compilation
4. **Update configuration files** if they reference old package names
5. **Re-test compilation** after fixes
6. **Update this status table** with final results

## Important Notes

- **Backup before cleanup**: Consider backing up services before removing old directories
- **Test thoroughly**: Ensure all functionality works after package changes
- **Check imports**: IDE auto-imports might still reference old package names
- **Update documentation**: Any documentation referencing package names should be updated

## Tools Created

1. `cleanup-old-packages.ps1` - PowerShell script for automated cleanup and testing
2. `test-compilation.bat` - Batch script for quick compilation testing
3. This instruction document for manual process

Execute the automated scripts first, then follow up with manual verification for any services that show issues.