$adb = "C:\Users\punit\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb shell "screencap -p /sdcard/auth_screen.png"
& $adb pull /sdcard/auth_screen.png "C:\Users\punit\.gemini\antigravity-ide\brain\8d994e4b-24a3-477f-9221-008d9c41b8df\auth_screen.png"
& $adb shell "dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'"
