# Android KeyStore 创建指南

## 密钥库创建对话框填写说明

### 1. Key Store Path（密钥库路径）
**填写**：选择一个安全的保存位置

**推荐路径**：
```
C:\Users\HUAWEI\AndroidStudioProjects\AlphaDoer\app\keystore\alphadoer.jks
```

**或者**：
```
C:\Users\HUAWEI\AndroidStudioProjects\AlphaDoer\app\alphadoer-release-key.jks
```

**说明**：
- 点击右侧的文件夹图标选择保存位置
- 文件名建议使用 `.jks` 或 `.keystore` 扩展名
- **重要**：请妥善保管此文件，丢失后无法恢复！

---

### 2. Password（密钥库密码）
**填写**：
- **Password**：输入一个强密码（至少 8 位，建议包含字母、数字、特殊字符）
- **Confirm**：再次输入相同密码确认

**示例密码**（请使用您自己的密码）：
```
AlphaDoer2024!@#
```

**说明**：
- 密码用于保护整个密钥库
- 请务必记住此密码，忘记后无法恢复
- 建议将密码保存在安全的地方

---

### 3. Key（密钥信息）

#### Alias（别名）
**填写**：`key0`（默认值即可，或改为 `alphadoer`）

**说明**：
- 这是密钥在密钥库中的标识名称
- 可以使用默认值 `key0`，或自定义为 `alphadoer`、`release` 等

#### Password（密钥密码）
**填写**：
- **Password**：输入密钥密码（可以与密钥库密码相同，也可以不同）
- **Confirm**：再次输入确认

**建议**：
- 可以与密钥库密码相同，方便记忆
- 或者使用不同的密码增强安全性

#### Validity (years)（有效期）
**填写**：`25`（默认值即可）

**说明**：
- Android 要求密钥有效期至少 25 年
- 默认值 25 年已经满足要求
- 可以保持默认值不变

---

### 4. Certificate（证书信息）

这是用于标识应用发布者的信息，可以填写真实信息或测试信息：

#### First and Last Name（姓名）
**填写**：`AlphaDoer` 或您的真实姓名

**示例**：
```
AlphaDoer
```

#### Organizational Unit（组织单位）
**填写**：`Development` 或您的部门名称

**示例**：
```
Development
```

#### Organization（组织）
**填写**：`AlphaDoer` 或您的公司名称

**示例**：
```
AlphaDoer
```

#### City or Locality（城市）
**填写**：您的城市名称

**示例**：
```
Beijing
```

#### State or Province（省/州）
**填写**：您的省份或州名称

**示例**：
```
Beijing
```

#### Country Code (XX)（国家代码）
**填写**：`CN`（中国）或其他国家代码

**说明**：
- 使用 ISO 3166-1 alpha-2 标准的两字母国家代码
- 中国：`CN`
- 美国：`US`
- 完整列表：https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2

---

## 完整填写示例

### 基本信息
```
Key Store Path: C:\Users\HUAWEI\AndroidStudioProjects\AlphaDoer\app\keystore\alphadoer.jks
Password: [您的密码，至少8位]
Confirm: [再次输入相同密码]
```

### 密钥信息
```
Alias: key0
Password: [可以与密钥库密码相同]
Confirm: [再次输入]
Validity: 25
```

### 证书信息
```
First and Last Name: AlphaDoer
Organizational Unit: Development
Organization: AlphaDoer
City or Locality: Beijing
State or Province: Beijing
Country Code: CN
```

---

## 填写完成后

1. 点击 **OK** 按钮创建密钥库
2. 密钥库文件将保存在您指定的路径
3. 后续构建 Release APK 时会使用此密钥库

---

## 重要提醒

### ⚠️ 安全注意事项

1. **备份密钥库文件**
   - 将 `.jks` 文件备份到安全位置（如云盘、U盘）
   - 丢失密钥库文件后，无法更新已发布的应用

2. **保存密码**
   - 将密钥库密码和密钥密码保存在安全的地方
   - 忘记密码后无法恢复

3. **不要提交到 Git**
   - 在 `.gitignore` 中添加密钥库文件路径
   - 不要将密钥库文件提交到版本控制系统

4. **生产环境建议**
   - 使用强密码
   - 定期备份密钥库
   - 限制密钥库文件的访问权限

---

## 添加到 .gitignore

创建或编辑 `.gitignore` 文件，添加：

```
# Keystore files
*.jks
*.keystore
app/keystore/
```

---

## 后续使用

创建密钥库后，在构建 Release APK 时：
1. Android Studio 会记住密钥库路径
2. 只需输入密码即可签名
3. 或者配置 `gradle.properties` 自动签名（不推荐，安全性较低）

---

**填写完成后，点击 OK 即可创建密钥库！** ✅
