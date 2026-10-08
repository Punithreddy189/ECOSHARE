@echo off
title EcoShare - Push to GitHub
echo ======================================================
echo    Pushing EcoShare Native Android to GitHub
echo    Target: https://github.com/Punithreddy189/ECOSHARE.git
echo ======================================================
echo.
set "PATH=%LOCALAPPDATA%\Programs\Git\cmd;C:\Program Files\Git\cmd;%PATH%"
git add .
git commit -m "Update EcoShare Android project"
git push -u origin main
echo.
if %ERRORLEVEL% EQU 0 (
    echo ======================================================
    echo    SUCCESS! Code successfully uploaded to GitHub!
    echo ======================================================
) else (
    echo ======================================================
    echo    Push failed or no new changes to commit.
    echo ======================================================
)
pause
