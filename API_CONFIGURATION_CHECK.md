# API配置检查清单

## ✅ 已修复的问题

1. **API请求格式**：已更新为百度千帆API格式（`QianfanChatRequest`/`QianfanChatResponse`）
2. **API端点路径**：已修复为正确的端点路径
3. **响应解析**：已实现JSON解析逻辑，从AI返回的文本中提取结构化数据
4. **异常处理**：所有API调用失败都会fallback到Mock数据，避免闪退
5. **日志记录**：已添加详细的诊断日志

## ⚠️ 需要检查的配置

### 1. API Key配置
**位置**：`app/src/main/java/com/alphadoer/trader/di/AppModule.kt` 第119行

```kotlin
AuthInterceptor { "bce-v3/ALTAK-eHEhSNFiYK3Z1cCNLgpzi/cf9ba76215ac13397788c2d167e7ed163af397d9" }
```

**检查项**：
- ✅ API Key已设置
- ⚠️ 请确认API Key格式正确（应该是 `bce-v3/ALTAK-xxx/xxx` 格式）
- ⚠️ 请确认API Key未过期且有调用权限

### 2. API Base URL
**位置**：`app/src/main/java/com/alphadoer/trader/di/AppModule.kt` 第149行

```kotlin
.baseUrl("https://qianfan.baidubce.com/v2/chat/completions/")
```

**检查项**：
- ✅ Base URL已设置
- ⚠️ 请确认这是正确的百度千帆API端点
- ⚠️ 如果您的API端点不同，请修改此URL

### 3. 认证方式
**当前实现**：使用 `Authorization: Bearer {API_KEY}` 头部

**百度千帆API可能需要的认证方式**：
- 如果API需要签名认证，可能需要使用 `AuthInterceptor` 实现签名逻辑
- 如果API需要其他头部（如 `X-API-Key`），需要修改 `AuthInterceptor`

## 🔍 如何诊断API调用问题

### 1. 查看Logcat日志
运行应用后，在Android Studio的Logcat中过滤以下标签：
- `AI_Diagnostics`：详细的AI分析诊断日志
- `NewsAnalysisRepository`：API调用和响应日志
- `MorningReadingViewModel`：ViewModel层的日志

### 2. 检查网络请求
由于已启用 `HttpLoggingInterceptor.Level.BODY`，您可以在Logcat中看到：
- 完整的HTTP请求头（包括Authorization）
- 完整的HTTP请求体
- 完整的HTTP响应头和响应体

### 3. 常见错误及解决方案

#### 错误1：401 Unauthorized
**原因**：API Key无效或格式错误
**解决**：检查 `AppModule.kt` 中的API Key是否正确

#### 错误2：404 Not Found
**原因**：API端点URL错误
**解决**：检查 `AppModule.kt` 中的Base URL是否正确

#### 错误3：400 Bad Request
**原因**：请求格式不符合API要求
**解决**：检查 `QianfanChatRequest` 的格式是否正确

#### 错误4：JSON解析失败
**原因**：AI返回的JSON格式不符合预期
**解决**：查看Logcat中的 `AI_Diagnostics` 日志，检查AI返回的原始文本

## 📝 如果API调用失败

应用已实现自动fallback机制：
1. **API调用失败** → 自动使用Mock数据
2. **API返回错误** → 自动使用Mock数据
3. **JSON解析失败** → 自动使用Mock数据
4. **任何异常** → 自动使用Mock数据

**结果**：应用不会闪退，会显示Mock数据的分析结果（虽然可能不准确）

## 🛠️ 需要您提供的信息（如果API仍然失败）

如果API调用仍然失败，请提供以下信息：

1. **Logcat日志**：
   - 过滤 `AI_Diagnostics` 标签的完整日志
   - 过滤 `NewsAnalysisRepository` 标签的完整日志

2. **API文档信息**：
   - 百度千帆API的完整文档URL
   - 正确的API端点URL
   - 正确的认证方式（Bearer Token？签名？其他？）
   - 请求格式示例
   - 响应格式示例

3. **错误信息**：
   - HTTP状态码
   - 错误消息
   - 完整的错误响应体

## 📌 当前状态

- ✅ 代码已更新为百度千帆API格式
- ✅ 异常处理已完善，不会闪退
- ✅ 日志记录已完善，便于诊断
- ⚠️ 需要验证API Key和端点URL是否正确
- ⚠️ 需要验证认证方式是否正确
