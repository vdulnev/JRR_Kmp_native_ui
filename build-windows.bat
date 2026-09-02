@echo off
setlocal EnableDelayedExpansion

REM ============================================================================
REM  Build the JRR Windows desktop app and deploy it to D:\programs\jrr
REM
REM  The desktop app is a Compose/Kotlin Desktop (jvm) target. This script asks
REM  Gradle for a self-contained app image (bundled JRE + libvlc natives), then
REM  mirrors it into the deploy dir. Launch afterwards via:
REM      D:\programs\jrr\JRRDesktop.exe
REM
REM  Optional: point -PvlcHome at a VLC install to bundle libvlc for playback.
REM  Defaults to C:\Program Files\VideoLAN\VLC (see desktopApp/build.gradle.kts).
REM ============================================================================

set "PROJECT_DIR=%~dp0"
set "DEPLOY_DIR=D:\programs\jrr"
set "APP_IMAGE=%PROJECT_DIR%desktopApp\build\compose\binaries\main\app\JRRDesktop"

echo(
echo === Building JRRDesktop app image ===
call "%PROJECT_DIR%gradlew.bat" :desktopApp:createDistributable
if errorlevel 1 (
    echo(
    echo BUILD FAILED - aborting deploy.
    exit /b 1
)

if not exist "%APP_IMAGE%" (
    echo(
    echo ERROR: expected app image not found at:
    echo   %APP_IMAGE%
    exit /b 1
)

echo(
echo === Deploying to %DEPLOY_DIR% ===
if not exist "%DEPLOY_DIR%" (
    mkdir "%DEPLOY_DIR%"
    if errorlevel 1 (
        echo ERROR: could not create %DEPLOY_DIR%
        exit /b 1
    )
)

REM /MIR mirrors the image (deletes stale files from previous deploys).
REM /NFL /NDL /NP quieten per-file logging; /NJH /NJS drop the job header/summary.
robocopy "%APP_IMAGE%" "%DEPLOY_DIR%" /MIR /NFL /NDL /NP /NJH /NJS
REM Robocopy exit codes < 8 mean success (files copied / nothing to do).
if %ERRORLEVEL% GEQ 8 (
    echo(
    echo DEPLOY FAILED - robocopy returned %ERRORLEVEL%.
    exit /b 1
)

echo(
echo === Done ===
echo Deployed to: %DEPLOY_DIR%
echo Launch with: %DEPLOY_DIR%\JRRDesktop.exe
endlocal
exit /b 0
