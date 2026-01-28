# Clean Build 错误修复指南

## 问题说明

在Windows上执行 `gradlew clean` 时，可能会遇到文件锁定错误：
```
Unable to delete directory 'app\build'
Failed to delete some children. This might happen because a process has files open
```

这是Windows文件系统的常见问题，**不是代码错误**。

## 快速解决方案

### 方案1：跳过clean直接构建（推荐）
如果只是需要构建APK，可以跳过clean任务：
```powershell
.\gradlew.bat assembleDebug --no-daemon
```

### 方案2：停止Gradle Daemon后清理
```powershell
# 停止Gradle daemon
.\gradlew.bat --stop

# 等待几秒
Start-Sleep -Seconds 3

# 执行清理
.\gradlew.bat clean --no-daemon
```

### 方案3：手动删除build目录
如果Gradle无法删除，可以手动删除：
```powershell
# 停止Gradle daemon
.\gradlew.bat --stop

# 等待几秒
Start-Sleep -Seconds 3

# 手动删除build目录
Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue

# 然后正常构建
.\gradlew.bat assembleDebug --no-daemon
```

### 方案4：关闭Android Studio后清理
1. 完全关闭Android Studio
2. 等待几秒确保所有进程退出
3. 在命令行执行清理：
```powershell
.\gradlew.bat clean --no-daemon
```

### 方案5：使用Android Studio的清理功能
在Android Studio中：
1. `Build > Clean Project`
2. IDE环境通常能更好地处理文件锁定问题

## 重要说明

1. **这不是代码问题**：所有代码都是正确的
2. **不影响构建**：即使clean失败，也可以直接构建APK
3. **推荐做法**：在Android Studio中执行清理，IDE环境更稳定

## 预防措施

1. 在清理前先停止Gradle daemon
2. 关闭可能占用文件的程序（Android Studio、文件管理器等）
3. 使用 `--no-daemon` 参数避免后台进程锁定文件

---

**建议**：如果只是构建APK，直接使用 `.\gradlew.bat assembleDebug --no-daemon`，无需先执行clean。
