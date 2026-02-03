# 解决进程冲突问题

## 问题描述
`Error running 'app': Medium Phone API 36.1 is already running as process 25412.`

这表示模拟器/设备已经在运行，但Android Studio无法连接到它。

---

## 🔧 解决方案

### 方法1：使用已运行的模拟器（推荐）

1. **在Android Studio中**
   - 点击运行按钮旁边的设备选择下拉菜单
   - 选择已运行的模拟器（应该显示为"已运行"）
   - 点击运行按钮

2. **或者直接运行**
   - Android Studio应该能自动检测到已运行的模拟器
   - 直接点击运行即可

---

### 方法2：停止并重启模拟器

1. **关闭模拟器**
   - 在模拟器窗口中点击关闭按钮
   - 或使用命令：
     ```bash
     adb emu kill
     ```

2. **重新启动模拟器**
   - 在Android Studio的Device Manager中启动
   - 或使用命令：
     ```bash
     emulator -avd <AVD_NAME>
     ```

---

### 方法3：终止进程（如果模拟器卡死）

1. **Windows系统**
   ```bash
   # 查找进程
   tasklist | findstr qemu
   tasklist | findstr emulator
   
   # 终止进程（替换PID为实际进程ID）
   taskkill /F /PID 25412
   ```

2. **使用任务管理器**
   - 按 `Ctrl + Shift + Esc` 打开任务管理器
   - 找到 `qemu-system-x86_64.exe` 或 `emulator.exe`
   - 右键 → 结束任务

---

### 方法4：重启ADB服务

```bash
# 停止ADB
adb kill-server

# 启动ADB
adb start-server

# 查看连接的设备
adb devices
```

---

### 方法5：清除Android Studio缓存

1. **在Android Studio中**
   - File → Invalidate Caches / Restart
   - 选择 "Invalidate and Restart"

2. **或手动删除**
   - 关闭Android Studio
   - 删除 `.idea` 文件夹（如果存在缓存问题）

---

## 🎯 推荐操作步骤

### 快速解决（按顺序尝试）：

1. **首先尝试**：在Android Studio中选择已运行的模拟器，直接运行
   
2. **如果不行**：重启ADB
   ```bash
   adb kill-server
   adb start-server
   adb devices
   ```

3. **如果还不行**：重启模拟器
   - 关闭模拟器窗口
   - 在Device Manager中重新启动

4. **最后手段**：重启Android Studio
   - File → Invalidate Caches / Restart

---

## 📱 检查设备连接

### 查看已连接的设备：
```bash
adb devices
```

应该看到类似输出：
```
List of devices attached
emulator-5554    device
```

### 如果显示 "offline" 或 "unauthorized"：
```bash
# 重新授权（如果显示unauthorized）
# 在设备上点击"允许USB调试"

# 重启ADB
adb kill-server
adb start-server
```

---

## ⚠️ 注意事项

1. **不要同时运行多个相同模拟器实例**
   - 这会导致端口冲突

2. **确保模拟器完全启动**
   - 等待模拟器完全启动（看到主屏幕）
   - 再尝试运行应用

3. **检查端口占用**
   - 如果端口被占用，可能需要关闭其他应用

---

## 🔍 如果问题仍然存在

1. **查看完整错误信息**
   - 在Android Studio的Run窗口中查看完整错误

2. **检查模拟器状态**
   - 在Device Manager中查看模拟器状态
   - 确保模拟器正常运行

3. **尝试其他设备**
   - 如果有真机，尝试连接真机
   - 或创建新的模拟器
