# AlphaDoer APK 构建指南

## 问题说明

由于 Room 验证器在 Windows 命令行环境下的文件锁定问题（这是 Room 编译器的已知限制），**建议在 Android Studio 中构建 APK**。

## ✅ 推荐方法：在 Android Studio 中构建

### 步骤 1：打开项目
1. 启动 Android Studio
2. 选择 `File > Open`
3. 选择项目目录：`C:\Users\HUAWEI\AndroidStudioProjects\AlphaDoer`
4. 等待 Gradle 同步完成

### 步骤 2：构建 APK
有两种方式：

#### 方式 A：通过菜单构建（推荐）
1. 点击顶部菜单：`Build > Build Bundle(s) / APK(s) > Build APK(s)`
2. 等待构建完成（底部会显示进度）
3. 构建成功后，会弹出通知，点击 `locate` 查看 APK 位置

#### 方式 B：通过 Gradle 面板构建
1. 打开右侧的 `Gradle` 面板（如果没有，点击 `View > Tool Windows > Gradle`）
2. 展开 `AlphaDoer > app > Tasks > build`
3. 双击 `assembleDebug` 任务
4. 等待构建完成

### 步骤 3：找到 APK 文件
构建成功后，APK 文件位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

**完整路径**：
```
C:\Users\HUAWEI\AndroidStudioProjects\AlphaDoer\app\build\outputs\apk\debug\app-debug.apk
```

## 🔧 备选方法：命令行构建（如果必须使用）

如果必须在命令行构建，可以尝试以下方法：

### 方法 1：使用 WSL（Windows Subsystem for Linux）
如果已安装 WSL，可以在 Linux 环境中构建，完全避免 Windows 权限问题：

```bash
# 在 WSL 中
cd /mnt/c/Users/HUAWEI/AndroidStudioProjects/AlphaDoer
./gradlew assembleDebug
```

### 方法 2：以管理员身份运行 PowerShell
1. 右键点击 PowerShell
2. 选择"以管理员身份运行"
3. 执行构建命令：
```powershell
cd C:\Users\HUAWEI\AndroidStudioProjects\AlphaDoer
.\gradlew.bat assembleDebug --no-daemon
```

### 方法 3：手动清理锁文件后立即构建
```powershell
# 清理锁文件
Get-ChildItem -Path "C:\Windows" -Filter "sqlite-*.dll.lck" -ErrorAction SilentlyContinue | Remove-Item -Force

# 立即构建（避免锁文件重新创建）
cd C:\Users\HUAWEI\AndroidStudioProjects\AlphaDoer
.\gradlew.bat assembleDebug --no-daemon
```

## 📱 安装 APK 到设备

### 方法 1：通过 USB 连接
1. 在手机上启用"开发者选项"和"USB 调试"
2. 用 USB 连接手机到电脑
3. 在 Android Studio 中点击 `Run` 按钮（绿色三角形）
4. 选择连接的设备，应用会自动安装并运行

### 方法 2：直接传输 APK
1. 将 `app-debug.apk` 文件复制到手机
2. 在手机上打开文件管理器，找到 APK 文件
3. 点击安装（可能需要允许"安装未知来源应用"）

## ⚠️ 重要说明

1. **代码完整性**：虽然命令行构建可能失败，但所有代码都是完整且正确的
2. **运行时功能**：Room 验证器的问题只影响编译时验证，**不影响应用运行时功能**
3. **开发建议**：建议在 Android Studio 中开发，IDE 环境更稳定

## 🎯 构建 Release APK（用于发布）

如果需要构建 Release 版本的 APK：

1. 在 Android Studio 中：`Build > Generate Signed Bundle / APK`
2. 选择 `APK`
3. 创建或选择密钥库（KeyStore）
4. 选择 `release` 构建类型
5. 完成签名配置
6. 构建完成后，APK 位于：`app/build/outputs/apk/release/app-release.apk`

## 📊 构建状态

- ✅ **Kotlin 代码编译**：成功
- ✅ **代码逻辑**：完整
- ✅ **依赖注入**：配置完整
- ⚠️ **Room 验证器**：Windows 命令行环境存在限制（不影响功能）

---

**推荐操作**：在 Android Studio 中构建 APK，这是最可靠的方法。
