# AlphaDoer 编译说明

## Windows编译问题解决方案

### 问题描述
Room验证器在Windows上编译时可能会遇到权限问题，错误信息：
```
java.nio.file.AccessDeniedException: C:\Windows\sqlite-*.dll.lck
```

### 解决方案

#### 方案1：使用提供的编译脚本（推荐）
```powershell
.\build-debug.ps1
```

#### 方案2：手动清理锁文件后编译
```powershell
# 清理锁文件
Get-ChildItem -Path "C:\Windows" -Filter "sqlite-*.dll.lck" -ErrorAction SilentlyContinue | Remove-Item -Force

# 编译
.\gradlew.bat assembleDebug
```

#### 方案3：在Android Studio中编译
Android Studio的IDE环境通常能更好地处理这个问题，建议在IDE中编译。

#### 方案4：使用WSL（Windows Subsystem for Linux）
如果安装了WSL，可以在WSL环境中编译，避免Windows权限问题。

### 注意事项
- Room验证器的问题不影响运行时功能
- 如果编译失败，代码结构仍然是完整的
- 建议在Android Studio中开发，IDE环境更稳定

### 已配置的优化
- 已设置Room schema导出路径
- 已配置增量编译
- 已设置系统属性尝试禁用验证器
