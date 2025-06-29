@echo off
echo Social Commerce Domain - Simple Compilation Script
echo ===============================================
echo Starting compilation at %date% %time%
echo.

set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.15.6-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME set to: %JAVA_HOME%
echo.

:: Set current directory
cd /d "C:\Users\frich\Desktop\Micro-Social-Ecommerce-Ecosystems\Exalt-Application-Limited\social-ecommerce-ecosystem\social-commerce"

:: Services to compile in priority order
set services=commission-service invoice-service fulfillment-options localization-service multi-currency-service analytics-service product-service payout-service subscription-service vendor-onboarding order-service marketplace

echo Starting systematic compilation...
echo.

set /a success_count=0
set /a total_count=0

for %%s in (%services%) do (
    echo ================================================================================
    echo Compiling service: %%s
    echo ================================================================================
    
    set /a total_count+=1
    
    if exist "%%s" (
        cd "%%s"
        
        if exist "pom.xml" (
            echo Running Maven clean compile for %%s...
            echo.
            
            call mvnw.cmd clean compile
            
            if %ERRORLEVEL% EQU 0 (
                echo SUCCESS: %%s compiled successfully!
                set /a success_count+=1
                echo [%date% %time%] SUCCESS: %%s >> ..\compilation-log.txt
            ) else (
                echo FAILED: %%s compilation failed with error level %ERRORLEVEL%
                echo [%date% %time%] FAILED: %%s >> ..\compilation-log.txt
            )
        ) else (
            echo SKIPPED: No pom.xml found in %%s
            echo [%date% %time%] SKIPPED: %%s - No pom.xml >> ..\compilation-log.txt
        )
        
        cd ..
    ) else (
        echo SKIPPED: Directory %%s not found
        echo [%date% %time%] SKIPPED: %%s - Directory not found >> ..\compilation-log.txt
    )
    
    echo.
    echo ================================================================================
    echo.
)

echo.
echo ================================================================================
echo COMPILATION SESSION SUMMARY
echo ================================================================================
echo Total Services: %total_count%
echo Successful: %success_count%
echo Session completed at %date% %time%
echo.
echo Check compilation-log.txt for detailed results
echo ================================================================================

pause
