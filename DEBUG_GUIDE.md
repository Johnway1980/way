# Android应用调试指南

## 🚀 方法1：Android Studio直接运行（推荐）

### 步骤：
1. **连接设备或启动模拟器**
   - 使用USB连接Android设备，并启用"USB调试"
   - 或在Android Studio中启动Android模拟器

2. **运行Debug版本**
   - 点击Android Studio工具栏的绿色"运行"按钮（▶️）
   - 或使用快捷键：`Shift + F10`（Windows/Linux）或 `Ctrl + R`（Mac）
   - 选择目标设备（真机或模拟器）

3. **查看Logcat日志**
   - 在Android Studio底部打开"Logcat"标签
   - 过滤标签：输入 `MorningReadingViewModel` 或 `NewsAnalysisRepository`
   - 实时查看应用日志

### 优点：
- ✅ 不需要手动生成APK
- ✅ 可以实时查看日志
- ✅ 可以使用断点调试
- ✅ 修改代码后可以热重载

---

## 🔍 方法2：使用Logcat查看日志

### 在Android Studio中：
1. 打开底部"Logcat"标签
2. 选择正确的设备和应用进程
3. 使用过滤器：
   ```
   tag:MorningReadingViewModel | tag:NewsAnalysisRepository | tag:AI_Diagnostics
   ```

### 使用adb命令（命令行）：
```bash
# 查看所有日志
adb logcat

# 只查看特定标签
adb logcat -s MorningReadingViewModel NewsAnalysisRepository AI_Diagnostics

# 清除日志并重新开始
adb logcat -c && adb logcat -s MorningReadingViewModel NewsAnalysisRepository AI_Diagnostics

# 保存日志到文件
adb logcat > logcat.txt
```

---

## 🐛 方法3：使用断点调试

### 步骤：
1. **设置断点**
   - 在代码行号左侧点击，设置红色断点
   - 推荐在以下位置设置断点：
     - `MorningReadingViewModel.init` - 查看初始化过程
     - `MorningReadingViewModel.loadAnalysisHistory` - 查看历史加载
     - `NewsAnalysisRepositoryImpl.getAnalysisHistory` - 查看数据库访问
     - `NewsAnalysisRepositoryImpl.analyzeNews` - 查看API调用

2. **以Debug模式运行**
   - 点击工具栏的"Debug"按钮（🐛图标）
   - 或使用快捷键：`Shift + F9`（Windows/Linux）或 `Ctrl + D`（Mac）

3. **调试操作**
   - 应用会在断点处暂停
   - 可以查看变量值
   - 可以单步执行（F8）
   - 可以查看调用堆栈

---

## 📱 方法4：使用Android Studio的Profiler

### 步骤：
1. 运行应用后，点击底部"Profiler"标签
2. 可以查看：
   - CPU使用情况
   - 内存使用情况
   - 网络请求
   - 线程状态

---

## 🔧 方法5：使用adb命令直接安装和运行

### 步骤：
```bash
# 1. 编译Debug APK（不签名，用于调试）
.\gradlew.bat :app:assembleDebug

# 2. 安装到设备
adb install app\build\outputs\apk\debug\app-debug.apk

# 3. 启动应用
adb shell am start -n com.alphadoer.trader/.MainActivity

# 4. 查看日志
adb logcat -s MorningReadingViewModel NewsAnalysisRepository AI_Diagnostics
```

---

## 🎯 针对当前问题的调试步骤

### 1. 查看崩溃日志
```bash
# 查看崩溃堆栈
adb logcat *:E

# 或查看FATAL级别日志
adb logcat *:F
```

### 2. 查看特定模块日志
```bash
# 查看早间阅读模块相关日志
adb logcat -s MorningReadingViewModel NewsAnalysisRepository AI_Diagnostics AndroidRuntime
```

### 3. 在代码中添加更多日志
在以下位置添加日志：
- `MorningReadingViewModel.init` - 记录初始化开始
- `MorningReadingViewModel.loadAnalysisHistory` - 记录历史加载过程
- `NewsAnalysisRepositoryImpl.getAnalysisHistory` - 记录数据库查询

---

## 📋 常用调试命令

### 查看应用信息
```bash
# 查看已安装的应用
adb shell pm list packages | grep alphadoer

# 查看应用详细信息
adb shell dumpsys package com.alphadoer.trader

# 清除应用数据
adb shell pm clear com.alphadoer.trader
```

### 查看崩溃报告
```bash
# 查看ANR（应用无响应）报告
adb pull /data/anr/traces.txt

# 查看崩溃日志
adb logcat -b crash
```

### 重启应用
```bash
# 强制停止应用
adb shell am force-stop com.alphadoer.trader

# 启动应用
adb shell am start -n com.alphadoer.trader/.MainActivity
```

---

## 💡 调试技巧

### 1. 使用条件断点
- 右键点击断点，设置条件
- 例如：只在 `newsText.isNotEmpty()` 时暂停

### 2. 使用日志级别
```kotlin
Log.v("TAG", "详细日志")  // Verbose
Log.d("TAG", "调试日志")  // Debug
Log.i("TAG", "信息日志")  // Info
Log.w("TAG", "警告日志")  // Warning
Log.e("TAG", "错误日志")  // Error
```

### 3. 使用异常捕获
```kotlin
try {
    // 可能出错的代码
} catch (e: Exception) {
    Log.e("TAG", "错误: ${e.message}", e)
    e.printStackTrace() // 打印完整堆栈
}
```

---

## 🎯 推荐调试流程

1. **首先使用Android Studio直接运行**
   - 连接设备或启动模拟器
   - 点击"运行"按钮
   - 在Logcat中查看实时日志

2. **如果发现问题，使用断点调试**
   - 在关键位置设置断点
   - 以Debug模式运行
   - 逐步执行，查看变量值

3. **如果仍然无法定位，使用adb命令**
   - 查看完整的系统日志
   - 查看崩溃堆栈
   - 分析异常信息

---

## ⚠️ 注意事项

1. **确保设备已启用USB调试**
   - 设置 → 开发者选项 → USB调试

2. **确保应用有调试权限**
   - Debug版本自动有调试权限

3. **查看日志时注意过滤**
   - 使用标签过滤，避免日志过多

4. **保存重要日志**
   - 使用 `adb logcat > log.txt` 保存日志
