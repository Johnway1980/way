# AlphaDoer 编译脚本
# 解决Room验证器在Windows上的权限问题

Write-Host "清理Room验证器锁文件..." -ForegroundColor Yellow

# 清理Windows目录下的Room验证器锁文件
Get-ChildItem -Path "C:\Windows" -Filter "sqlite-*.dll.lck" -ErrorAction SilentlyContinue | 
    ForEach-Object {
        try {
            Remove-Item $_.FullName -Force -ErrorAction Stop
            Write-Host "已删除: $($_.Name)" -ForegroundColor Green
        } catch {
            Write-Host "无法删除: $($_.Name) - $($_.Exception.Message)" -ForegroundColor Red
        }
    }

# 设置环境变量以禁用Room验证器
$env:ROOM_DISABLE_VERIFICATION = "true"
$env:room_disableVerification = "true"

Write-Host "停止Gradle daemon..." -ForegroundColor Yellow
.\gradlew.bat --stop | Out-Null

Write-Host "开始编译..." -ForegroundColor Yellow
.\gradlew.bat assembleDebug --no-daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host "编译成功！" -ForegroundColor Green
} else {
    Write-Host "编译失败，错误代码: $LASTEXITCODE" -ForegroundColor Red
    Write-Host "提示：如果仍然失败，请在Android Studio中编译，IDE环境通常能更好地处理此问题。" -ForegroundColor Yellow
}
