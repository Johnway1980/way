# 快速修复模拟器连接问题的脚本

Write-Host "=== 模拟器连接问题修复脚本 ===" -ForegroundColor Green

# 1. 检查ADB
Write-Host "`n1. 检查ADB连接..." -ForegroundColor Yellow
$adbPath = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adbPath) {
    Write-Host "   ADB未找到，请确保Android SDK已正确安装" -ForegroundColor Red
    Write-Host "   或者使用Android Studio的Terminal（已配置ADB路径）" -ForegroundColor Yellow
} else {
    Write-Host "   ADB路径: $($adbPath.Source)" -ForegroundColor Green
}

# 2. 重启ADB服务
Write-Host "`n2. 重启ADB服务..." -ForegroundColor Yellow
try {
    & adb kill-server 2>&1 | Out-Null
    Start-Sleep -Seconds 2
    & adb start-server 2>&1 | Out-Null
    Write-Host "   ADB服务已重启" -ForegroundColor Green
} catch {
    Write-Host "   无法重启ADB，请手动执行: adb kill-server && adb start-server" -ForegroundColor Red
}

# 3. 检查设备连接
Write-Host "`n3. 检查设备连接..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
try {
    $devices = & adb devices 2>&1
    Write-Host $devices
    if ($devices -match "device$") {
        Write-Host "   ✓ 设备已连接" -ForegroundColor Green
    } else {
        Write-Host "   ✗ 未检测到设备" -ForegroundColor Red
        Write-Host "   请确保模拟器已完全启动（看到主屏幕）" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   无法检查设备，请手动执行: adb devices" -ForegroundColor Red
}

# 4. 建议
Write-Host "`n=== 建议 ===" -ForegroundColor Cyan
Write-Host "1. 如果模拟器未启动，请在Android Studio的Device Manager中启动" -ForegroundColor White
Write-Host "2. 等待模拟器完全启动（看到Android主屏幕）" -ForegroundColor White
Write-Host "3. 如果问题持续，尝试重启模拟器（Cold Boot）" -ForegroundColor White
Write-Host "4. 考虑使用真机调试（通常更稳定）" -ForegroundColor White

Write-Host "`n=== 完成 ===" -ForegroundColor Green
