@echo off
REM =====================================================================
REM ChainOps Startup Script for Windows
REM =====================================================================

echo ==========================================================
echo   Starting ChainOps – Supply Chain Management System      
echo ==========================================================

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java is not found in PATH. Please install JDK 17 or higher.
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven is not found in PATH. Please install Apache Maven 3.8+.
    pause
    exit /b 1
)

echo Compiling and launching JavaFX application...
call mvn clean compile javafx:run
if %errorlevel% neq 0 (
    echo Execution failed. Please verify MySQL connection in db.properties.
    pause
)
