# Install EcoShare to connected mobile device via ADB
$adb = "C:\Users\punit\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apk = "$PSScriptRoot\android\app\build\outputs\apk\debug\app-debug.apk"

Write-Host "Checking for connected Android mobile devices..." -ForegroundColor Cyan

$retries = 0
$device = $null
while ($retries -lt 8) {
    $devices = & $adb devices | Where-Object { $_ -match "\tdevice" }
    if ($devices) {
        $device = $devices
        break
    }
    Start-Sleep -Seconds 1
    $retries++
}

if (-not $device) {
    Write-Host "No authorized device detected yet. Please ensure:" -ForegroundColor Yellow
    Write-Host "1. Phone is plugged in via USB" -ForegroundColor Yellow
    Write-Host "2. Phone screen is unlocked" -ForegroundColor Yellow
    Write-Host "3. USB Debugging is turned ON in Developer Options" -ForegroundColor Yellow
    Write-Host "4. 'Allow USB debugging' prompt is approved on screen" -ForegroundColor Yellow
    exit 1
}

Write-Host "Connected device found: $device" -ForegroundColor Green
Write-Host "Pushing app-debug.apk to phone..." -ForegroundColor Green
& $adb push $apk /data/local/tmp/app-debug.apk

Write-Host "Installing EcoShare package..." -ForegroundColor Green
& $adb shell "pm install -r -t /data/local/tmp/app-debug.apk"

Write-Host "Launching EcoShare on phone..." -ForegroundColor Green
& $adb shell "am start -n com.ecoshare.app/.ui.splash.SplashActivity"

Write-Host "EcoShare successfully launched on your mobile device!" -ForegroundColor Green
