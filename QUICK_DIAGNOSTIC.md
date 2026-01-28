# 快速诊断指南

## 🚨 请告诉我具体是什么问题

为了快速帮您解决问题，请提供：

1. **错误信息**（如果有）
2. **Logcat日志**（特别是错误日志）
3. **问题出现的时机**（启动时？点击按钮时？）

---

## 🔍 快速检查清单

### 1. 检查网络权限（常见问题）

请确认 `AndroidManifest.xml` 中有网络权限：

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

如果没有，请添加。

---

### 2. 查看实时日志

在Android Studio中：
1. 打开底部"Logcat"标签
2. 运行应用
3. 过滤标签：`MorningReadingViewModel` 或 `NewsAnalysisRepository`
4. 查看错误信息

或使用命令行：
```bash
adb logcat -s MorningReadingViewModel NewsAnalysisRepository AndroidRuntime
```

---

### 3. 常见错误模式

#### 错误1：依赖注入失败
**错误信息**：`No injector factory bound` 或 `Cannot provide`
**解决**：检查Hilt配置

#### 错误2：数据库错误
**错误信息**：`Room` 或 `SQLite` 相关
**解决**：清除应用数据 `adb shell pm clear com.alphadoer.trader`

#### 错误3：网络错误
**错误信息**：`NetworkException` 或 `SocketException`
**解决**：检查网络权限和API配置

#### 错误4：空指针
**错误信息**：`NullPointerException`
**解决**：查看堆栈跟踪，找到空值位置

---

## 📱 快速测试步骤

1. **运行应用**
   ```bash
   # 在Android Studio中点击运行，或
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   adb shell am start -n com.alphadoer.trader/.MainActivity
   ```

2. **查看日志**
   ```bash
   adb logcat -c  # 清除日志
   adb logcat -s MorningReadingViewModel NewsAnalysisRepository AndroidRuntime
   ```

3. **触发问题**
   - 点击"早间信息阅读"
   - 查看日志输出

4. **复制错误信息**
   - 从Logcat中复制完整的错误堆栈
   - 发送给我

---

## 💬 请提供以下信息

1. **具体错误信息**（如果有）
2. **Logcat日志**（特别是错误部分）
3. **问题描述**（什么时候出现？做了什么操作？）
4. **截图**（如果有错误界面）

这样我就能快速帮您解决问题！
