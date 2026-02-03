# 问题排查指南

## 🔍 如何报告问题

请提供以下信息：
1. **错误信息**：完整的错误消息或异常堆栈
2. **Logcat日志**：特别是 `MorningReadingViewModel`、`NewsAnalysisRepository`、`AndroidRuntime` 标签
3. **问题出现的时机**：启动时、点击按钮时、还是其他操作时
4. **截图**：如果有错误对话框或异常界面

---

## 🐛 常见问题及解决方案

### 问题1：应用闪退（Crash）

#### 症状：
- 应用启动后立即关闭
- 点击某个功能后闪退

#### 排查步骤：

1. **查看Logcat错误日志**
   ```bash
   adb logcat *:E
   ```
   或过滤特定标签：
   ```bash
   adb logcat -s AndroidRuntime MorningReadingViewModel NewsAnalysisRepository
   ```

2. **检查常见原因**：
   - 依赖注入失败（Hilt配置问题）
   - 数据库访问异常
   - 空指针异常（NullPointerException）
   - 网络请求异常

3. **查看崩溃堆栈**
   - 在Logcat中查找 `FATAL EXCEPTION`
   - 查看完整的堆栈跟踪

---

### 问题2：依赖注入失败

#### 症状：
- 错误信息包含：`No injector factory bound` 或 `Cannot provide without an @Inject constructor`
- ViewModel无法创建

#### 解决方案：

1. **检查Hilt配置**
   - 确保 `Application` 类有 `@HiltAndroidApp` 注解
   - 确保 `MainActivity` 有 `@AndroidEntryPoint` 注解
   - 确保所有需要的依赖都在 `AppModule` 中提供

2. **检查ViewModel注解**
   ```kotlin
   @HiltViewModel  // 确保有这个注解
   class MorningReadingViewModel @Inject constructor(...)
   ```

3. **检查Repository提供**
   ```kotlin
   @Provides
   fun provideNewsAnalysisRepository(...): NewsAnalysisRepository
   ```

---

### 问题3：数据库访问错误

#### 症状：
- 错误信息包含：`Room`、`SQLite`、`database`
- 应用在访问数据库时崩溃

#### 解决方案：

1. **检查数据库初始化**
   ```kotlin
   // 确保数据库正确初始化
   Room.databaseBuilder(...)
       .fallbackToDestructiveMigration() // 开发阶段使用
       .build()
   ```

2. **检查DAO方法**
   - 确保所有 `suspend` 函数在协程中调用
   - 确保 `Flow` 在协程中收集

3. **清除应用数据**
   ```bash
   adb shell pm clear com.alphadoer.trader
   ```

---

### 问题4：网络请求失败

#### 症状：
- API调用失败
- 网络错误

#### 解决方案：

1. **检查网络权限**
   ```xml
   <!-- AndroidManifest.xml -->
   <uses-permission android:name="android.permission.INTERNET" />
   ```

2. **检查API配置**
   - Base URL是否正确
   - API Key是否正确
   - 认证方式是否正确

3. **查看网络日志**
   - 在Logcat中查看 `OkHttp` 标签
   - 查看完整的HTTP请求和响应

---

### 问题5：UI不显示或显示错误

#### 症状：
- 界面空白
- 按钮无响应
- 数据不显示

#### 解决方案：

1. **检查Compose状态**
   - 确保 `collectAsState()` 正确调用
   - 确保状态更新正确

2. **检查ViewModel**
   - 确保 `uiState` 正确初始化
   - 确保事件处理正确

3. **查看UI日志**
   - 在Logcat中查看 `MorningReadingViewModel` 标签
   - 检查状态更新日志

---

## 🔧 快速诊断命令

### 查看应用崩溃日志
```bash
# 查看所有错误
adb logcat *:E

# 查看崩溃堆栈
adb logcat -s AndroidRuntime

# 查看应用特定日志
adb logcat -s MorningReadingViewModel NewsAnalysisRepository AI_Diagnostics
```

### 查看应用信息
```bash
# 查看已安装的应用
adb shell pm list packages | grep alphadoer

# 查看应用详细信息
adb shell dumpsys package com.alphadoer.trader
```

### 清除应用数据
```bash
# 清除应用数据（会删除所有本地数据）
adb shell pm clear com.alphadoer.trader

# 卸载应用
adb uninstall com.alphadoer.trader
```

### 重新安装应用
```bash
# 卸载
adb uninstall com.alphadoer.trader

# 重新安装
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## 📋 检查清单

### 编译问题
- [ ] 所有依赖都已正确添加
- [ ] 所有导入语句都正确
- [ ] 没有语法错误
- [ ] 没有类型错误

### 运行时问题
- [ ] Hilt配置正确
- [ ] 所有Repository都已提供
- [ ] 所有UseCase都已提供
- [ ] 数据库正确初始化
- [ ] 网络权限已添加
- [ ] API配置正确

### UI问题
- [ ] ViewModel正确注入
- [ ] UI状态正确管理
- [ ] 事件处理正确
- [ ] Compose状态正确

---

## 🆘 如果问题仍然存在

请提供以下信息：

1. **完整的错误日志**
   ```bash
   adb logcat > error_log.txt
   ```

2. **崩溃堆栈**
   - 从Logcat中复制完整的堆栈跟踪

3. **问题复现步骤**
   - 详细描述如何触发问题

4. **环境信息**
   - Android版本
   - 设备型号
   - Android Studio版本

---

## 💡 调试技巧

### 1. 添加更多日志
在关键位置添加日志：
```kotlin
Log.d("TAG", "关键信息: $variable")
```

### 2. 使用断点
在可疑代码处设置断点，逐步执行

### 3. 检查空值
```kotlin
if (value == null) {
    Log.e("TAG", "值为空!")
    return
}
```

### 4. 捕获异常
```kotlin
try {
    // 可能出错的代码
} catch (e: Exception) {
    Log.e("TAG", "错误: ${e.message}", e)
    e.printStackTrace()
}
```
