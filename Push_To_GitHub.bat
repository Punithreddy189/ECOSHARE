@echo off
title EcoShare - Push All to GitHub
echo ======================================================
echo    Pushing Entire EcoShare Suite to GitHub
echo    Target: https://github.com/Punithreddy189/ECOSHARE.git
echo ======================================================
echo.
set "PATH=%LOCALAPPDATA%\Programs\Git\cmd;C:\Program Files\Git\cmd;%PATH%"
git add .
git commit -m "Update EcoShare project"
git push -u origin main --force
echo.
if %ERRORLEVEL% EQU 0 (
    echo ======================================================
    echo    SUCCESS! Entire project uploaded to GitHub!
    echo ======================================================
) else (
    echo ======================================================
    echo    Push failed. Check GitHub credentials above.
    echo ======================================================
)
pause
