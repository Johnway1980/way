# PR 草稿：EmergencyFilter 可切换放宽（非破坏性）

## 简要说明
本 PR 引入一个运行时开关 `strictFiltering`，允许在不破坏上游解析结果的前提下，选择性放宽 EmergencyFilter 的严格过滤规则以便回归验证与调试。

变更要点：
- `app/src/main/java/.../EmergencyFilter.kt`
  - 新增 `@Volatile var strictFiltering: Boolean = true`，默认保持严格行为。
  - 添加逐股诊断输出（写入 `app/diagnostic-output-emergencyfilter.txt`），并在严格/非严格模式下切换不同过滤行为。
  - 补偿逻辑：当过滤后无板块但有推荐股票时，从保留股票推导板块并保证每个板块至少 3 支相关股票。
  - 修复若干 nullable 调用与语法问题。

- `app/src/main/java/.../NewsAnalysisRepositoryImpl.kt`
  - 改进解析器：对 AI 原始文本做归一化与片段恢复，保留原始片段到 metadata，尽可能恢复 `affectedSectors` 和 `recommendedStocks`。

- 单元测试：
  - `app/src/test/java/.../ParseAndDumpNewsAnalysisTest.kt`：解析示例原始 AI 输出，生成诊断产物 `app/build/diagnostics/newsanalysis-parsed.json`。
  - `app/src/test/java/.../EmergencyFilterStrictModeTest.kt`：验证 `strictFiltering` 开关开/关的行为差异。

## 风险与兼容性
- 该开关为运行时开关，默认值不变（`true`），因此对生产行为无破坏性更改。
- 新增诊断文件只写入磁盘，不影响主流程。

## 验证步骤（本地复现）
在工程根目录下运行：

```bash
./gradlew clean
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest --tests "com.alphadoer.trader.data.util.EmergencyFilterStrictModeTest"
```

可选：查看解析产物

```bash
cat app/build/diagnostics/newsanalysis-parsed.json
cat app/diagnostic-output-emergencyfilter.txt
```

## 提交与推送命令示例

```bash
# 新建分支
git checkout -b fix/emergencyfilter-relax
# 添加变更并提交（根据实际修改的文件调整）
git add app/src/main/java/com/alphadoer/trader/data/util/EmergencyFilter.kt \
    app/src/main/java/com/alphadoer/trader/data/repository/NewsAnalysisRepositoryImpl.kt \
    app/src/test/java/com/alphadoer/trader/data/util/EmergencyFilterStrictModeTest.kt \
    app/src/test/java/com/alphadoer/trader/data/util/ParseAndDumpNewsAnalysisTest.kt \
    PR_EMERGENCYFILTER_RELAX.md
git commit -m "Add strictFiltering toggle and diagnostics for EmergencyFilter; improve parser recovery"
# 推送到远程
git push origin fix/emergencyfilter-relax

# 使用 GitHub CLI 打开 PR（可选）
gh pr create --fill --base main --head fix/emergencyfilter-relax
```

## 需要审查的点
- 确认 `strictFiltering` 的默认策略是否符合安全要求（目前为 `true`）。
- 确认诊断输出的位置与格式（`app/diagnostic-output-emergencyfilter.txt`、`app/build/diagnostics/newsanalysis-parsed.json`）。

---
如需我代为创建分支、提交并推送（需你确认远程权限），可以在此确认，我将给出具体命令并执行。 
