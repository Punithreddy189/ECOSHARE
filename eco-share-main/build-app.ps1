$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
$env:ANDROID_HOME = "C:\Users\punit\AppData\Local\Android\Sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Set-Location "$PSScriptRoot\android"
Write-Host "Building EcoShare Native Android APK with JDK 21..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug --no-daemon
