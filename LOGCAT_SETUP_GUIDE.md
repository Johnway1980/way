# Logcat 配置指南

## 问题：Logcat 显示 "No connected devices"

这表示没有设备连接到 Android Studio，需要先连接设备才能查看日志。

---

## 🔧 解决方案

### 步骤1：连接设备

#### 选项A：使用模拟器（推荐）

1. **启动模拟器**
   - 在 Android Studio 中：View → Tool Windows → Device Manager
   - 或点击工具栏右侧的设备管理器图标
   - 找到您的模拟器（如 "Medium Phone API 36.1"）
   - 点击播放按钮 ▶️ 启动模拟器

2. **等待模拟器完全启动**
   - 等待看到 Android 主屏幕
   - 通常需要 1-3 分钟

3. **验证连接**
   - 在 Logcat 的设备下拉菜单中应该能看到模拟器
   - 例如：`emulator-5554` 或 `Medium Phone API 36.1`

#### 选项B：使用真机

1. **连接手机**
   - 使用 USB 线连接 Android 手机到电脑
   - 在手机上启用"USB调试"
     - 设置 → 关于手机 → 连续点击"版本号"7次（启用开发者选项）
     - 设置 → 开发者选项 → 启用"USB调试"

2. **授权连接**
   - 手机上会弹出"允许USB调试"对话框
   - 勾选"始终允许"并点击"确定"

3. **验证连接**
   - 在 Logcat 的设备下拉菜单中应该能看到您的手机
   - 例如：`device-xxxxx` 或手机型号

---

### 步骤2：在 Logcat 中选择设备

1. **点击设备下拉菜单**
   - 在 Logcat 顶部，点击显示 "No connected devices" 的下拉菜单

2. **选择设备**
   - 如果设备已连接，会显示在列表中
   - 选择您的设备（模拟器或真机）

3. **如果仍然显示 "No connected devices"**
   - 继续看下面的故障排除步骤

---

### 步骤3：配置 Logcat 过滤器

1. **使用默认过滤器**
   - 当前显示：`package:mine`（只显示您的应用日志）
   - 这是推荐的设置

2. **创建自定义过滤器**
   - 点击过滤器输入框右侧的 "+" 图标
   - 或点击过滤器下拉菜单 → "Edit Filter Configuration"
   - 创建新过滤器：
     - **Name**: `MorningReading`（过滤器名称）
     - **Log Tag**: `MorningReadingViewModel|NewsAnalysisRepository|AI_Diagnostics`
     - **Log Level**: `Debug` 或 `Verbose`
     - 点击 "OK"

3. **选择过滤器**
   - 在过滤器下拉菜单中选择您创建的过滤器
   - 或使用 `package:mine` 查看所有应用日志

---

## 🔍 故障排除

### 如果设备下拉菜单仍然显示 "No connected devices"

#### 方法1：重启 ADB

1. **在 Android Studio 的 Terminal 中执行**：
   ```bash
   adb kill-server
   adb start-server
   adb devices
   ```

2. **检查输出**
   - 应该看到您的设备列表
   - 例如：
     ```
     List of devices attached
     emulator-5554    device
     ```

3. **刷新 Logcat**
   - 点击 Logcat 中的刷新按钮（圆形箭头图标）
   - 或关闭并重新打开 Logcat 窗口

#### 方法2：检查设备连接

1. **在 Terminal 中执行**：
   ```bash
   adb devices
   ```

2. **如果显示设备但状态为 "offline"**：
   ```bash
   adb kill-server
   adb start-server
   # 等待几秒后
   adb devices
   ```

3. **如果显示 "unauthorized"**：
   - 在手机上点击"允许USB调试"
   - 勾选"始终允许"

#### 方法3：重新启动 Android Studio

1. **完全关闭 Android Studio**
2. **重新打开项目**
3. **等待设备连接**
4. **打开 Logcat 查看**

---

## 📱 验证配置

### 检查清单：

- [ ] 设备已连接（模拟器或真机）
- [ ] 在 Logcat 的设备下拉菜单中能看到设备
- [ ] 设备状态显示为 "device"（不是 "offline" 或 "unauthorized"）
- [ ] 过滤器已配置（`package:mine` 或自定义过滤器）
- [ ] 应用已安装并运行在设备上

### 测试日志输出：

1. **运行应用**
   - 在 Android Studio 中点击运行按钮
   - 或使用快捷键：`Shift + F10`

2. **查看 Logcat**
   - 应该能看到应用启动的日志
   - 特别是 `MorningReadingViewModel` 的初始化日志

3. **触发操作**
   - 点击"早间信息阅读"
   - 应该能看到相关的日志输出

---

## 🎯 推荐配置

### 设备选择：
- **开发阶段**：使用模拟器（方便测试）
- **测试阶段**：使用真机（更真实的环境）

### 过滤器配置：
- **日常开发**：`package:mine`（查看所有应用日志）
- **调试特定模块**：自定义过滤器（如 `MorningReadingViewModel`）

### 日志级别：
- **开发调试**：`Verbose` 或 `Debug`（查看详细信息）
- **问题排查**：`Error` 或 `Warning`（只看错误）

---

## 💡 使用技巧

### 1. 清除日志
- 点击垃圾桶图标清除当前日志
- 或使用快捷键：`Ctrl + L`

### 2. 暂停/恢复日志
- 点击暂停按钮（两个竖线）暂停日志输出
- 再次点击恢复日志输出

### 3. 搜索日志
- 在过滤器输入框中输入关键词
- 例如：`MorningReadingViewModel` 或 `error`

### 4. 保存日志
- 右键点击日志区域
- 选择 "Save Logcat to File"
- 保存为文本文件

---

## 🆘 如果问题仍然存在

1. **检查 Android SDK 是否正确安装**
   - File → Settings → Appearance & Behavior → System Settings → Android SDK
   - 确保 "Android SDK Platform-Tools" 已安装

2. **检查 USB 驱动（真机）**
   - 确保已安装手机厂商的 USB 驱动
   - 或使用通用 Android USB 驱动

3. **尝试其他 USB 端口或 USB 线（真机）**
   - 某些 USB 端口可能有问题
   - 某些 USB 线只能充电，不能传输数据

4. **检查防火墙或安全软件**
   - 可能阻止了 ADB 连接
   - 临时禁用防火墙测试
