@echo off
echo Testing compilation of social-commerce services after naming standardization...
echo.

set BASE_PATH=C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\Exalt-Application-Limited\social-ecommerce-ecosystem\social-commerce

:: API Gateway
echo Testing API Gateway...
cd /d "%BASE_PATH%\api-gateway"
mvn clean compile -DskipTests=true -q
if %ERRORLEVEL% EQU 0 (
    echo ✓ API Gateway compilation successful
) else (
    echo ✗ API Gateway compilation failed
)
echo.

:: Commission Service
echo Testing Commission Service...
cd /d "%BASE_PATH%\commission-service"
mvn clean compile -DskipTests=true -q
if %ERRORLEVEL% EQU 0 (
    echo ✓ Commission Service compilation successful
) else (
    echo ✗ Commission Service compilation failed
)
echo.

:: Order Service
echo Testing Order Service...
cd /d "%BASE_PATH%\order-service"
mvn clean compile -DskipTests=true -q
if %ERRORLEVEL% EQU 0 (
    echo ✓ Order Service compilation successful
) else (
    echo ✗ Order Service compilation failed
)
echo.

:: Product Service
echo Testing Product Service...
cd /d "%BASE_PATH%\product-service"
mvn clean compile -DskipTests=true -q
if %ERRORLEVEL% EQU 0 (
    echo ✓ Product Service compilation successful
) else (
    echo ✗ Product Service compilation failed
)
echo.

:: Marketplace Service
echo Testing Marketplace Service...
cd /d "%BASE_PATH%\marketplace"
mvn clean compile -DskipTests=true -q
if %ERRORLEVEL% EQU 0 (
    echo ✓ Marketplace Service compilation successful
) else (
    echo ✗ Marketplace Service compilation failed
)
echo.

:: Social Media Integration
echo Testing Social Media Integration...
cd /d "%BASE_PATH%\social-media-integration"
if exist package.json (
    npm install --silent
    if %ERRORLEVEL% EQU 0 (
        echo ✓ Social Media Integration npm install successful
    ) else (
        echo ✗ Social Media Integration npm install failed
    )
) else (
    mvn clean compile -DskipTests=true -q
    if %ERRORLEVEL% EQU 0 (
        echo ✓ Social Media Integration compilation successful
    ) else (
        echo ✗ Social Media Integration compilation failed
    )
)
echo.

echo Compilation testing complete!
echo Check individual service directories for detailed error logs if any failures occurred.
pause