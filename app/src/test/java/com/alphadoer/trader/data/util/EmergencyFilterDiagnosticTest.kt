package com.alphadoer.trader.data.util

import com.alphadoer.trader.domain.model.AffectedSector
import com.alphadoer.trader.domain.model.NewsAnalysis
import com.alphadoer.trader.domain.model.RecommendedStock
import org.junit.Test
import java.io.File

class EmergencyFilterDiagnosticTest {

    @Test
    fun runDiagnostic() {
        // Try to find the ai_inner_unescaped_raw.txt file by walking up from the current working dir
        fun findRawFile(): File? {
            var dir = File(System.getProperty("user.dir"))
            repeat(6) {
                val candidate = File(dir, "app/ai_inner_unescaped_raw.txt")
                if (candidate.exists()) return candidate
                val candidate2 = File(dir, "ai_inner_unescaped_raw.txt")
                if (candidate2.exists()) return candidate2
                dir = dir.parentFile ?: return null
            }
            // try common user workspace location as fallback
            val userHome = System.getProperty("user.home")
            val fallback = File(userHome, "AndroidStudioProjects/AlphaDoer/app/ai_inner_unescaped_raw.txt")
            if (fallback.exists()) return fallback
            // absolute fallback for common dev path on this machine
            val abs = File("C:/Users/HUAWEI/AndroidStudioProjects/AlphaDoer/app/ai_inner_unescaped_raw.txt")
            if (abs.exists()) return abs
            return null
        }

        // 首先尝试从 test resources classpath 读取（CI/Gradle 测试运行会把 src/test/resources 加入 classpath）
        val classResource = javaClass.classLoader.getResourceAsStream("ai_inner_unescaped_raw.txt")
        val rawFromResource = classResource?.bufferedReader()?.use { it.readText() }
        val rawFile = findRawFile()
        val raw = rawFromResource ?: rawFile?.readText() ?: ""

        // 简单从 raw 文本中抽取推荐股票（如果存在）
        val regex = Regex("\"stockCode\"\\s*:\\s*\"([^\"]+)\".*?\"stockName\"\\s*:\\s*\"([^\"]+)\"", RegexOption.DOT_MATCHES_ALL)
        val stocks = regex.findAll(raw).map { m ->
            val code = m.groupValues[1]
            val name = m.groupValues[2]
            RecommendedStock(
                stockCode = code,
                stockName = name,
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.BUY,
                reason = "(diagnostic-imported)",
                confidence = 0.5,
                targetPrice = null,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = null
            )
        }.toList()

        // 构造一个 NewsAnalysis（尽量保留原始文本作为 newsContent）
        val analysis = NewsAnalysis(
            id = "diag-1",
            newsContent = raw,
            summary = raw.take(200),
            sentiment = NewsAnalysis.Sentiment.POSITIVE,
            confidence = 0.85,
            keyPoints = emptyList(),
            affectedSectors = emptyList(),
            recommendedStocks = stocks,
            riskWarnings = emptyList(),
            recommendations = emptyList(),
            analysisType = NewsAnalysis.AnalysisType.QUICK,
            createdAt = System.currentTimeMillis(),
            metadata = null
        )

        println("[DIAG] raw-length=" + raw.length)
        println("[DIAG] parsed original recommendedStocks.count=${stocks.size}")
        stocks.forEach { println("[DIAG] orig-stock: ${it.stockCode} | ${it.stockName}") }

        // 调用 EmergencyFilter
        val filtered = EmergencyFilter.filterAnalysisResult(raw, analysis)

        println("[DIAG] filtered recommendedStocks.count=${filtered.recommendedStocks.size}")
        filtered.recommendedStocks.forEach { println("[DIAG] filtered-stock: ${it.stockCode} | ${it.stockName} | sector=${it.sectorName}") }

        println("[DIAG] original affectedSectors.count=${analysis.affectedSectors.size}")
        println("[DIAG] final affectedSectors.count=${filtered.affectedSectors.size}")
        filtered.affectedSectors.forEach { s: AffectedSector ->
            println("[DIAG] final-sector: code=${s.sectorCode} name=${s.sectorName} impact=${s.impactLevel} related=${s.relatedStocks}")
        }

        // dump to a file for easier inspection
        val out = File(System.getProperty("user.dir"), "app/diagnostic-output.txt")
        out.parentFile?.mkdirs()
        out.writeText(buildString {
            appendLine("RAW_LENGTH=${raw.length}")
            appendLine("ORIG_STOCKS=${stocks.size}")
            stocks.forEach { appendLine("ORIG:${it.stockCode}|${it.stockName}|${it.sectorName}") }
            appendLine("FILTERED_STOCKS=${filtered.recommendedStocks.size}")
            filtered.recommendedStocks.forEach { appendLine("FIL:${it.stockCode}|${it.stockName}|${it.sectorName}") }
            appendLine("FINAL_SECTORS=${filtered.affectedSectors.size}")
            filtered.affectedSectors.forEach { appendLine("SEC:${it.sectorCode}|${it.sectorName}|${it.relatedStocks}") }
        })

        // 追加逐股诊断（使用与 EmergencyFilter 一致的简单启发式判断），以便文件中保留每支股票的剔除/保留原因
        try {
            val appendFile = File(System.getProperty("user.dir"), "app/diagnostic-output.txt")
            appendFile.appendText("\n# per-stock diagnostics:\n")

            // 与 EmergencyFilter 中相同的启发式规则（简化）
            val techStockCodes = listOf(
                "300750", "002415", "000977", "600584", "002230",
                "688981", "300014", "002304", "000063", "600703"
            )
            val stockNameKeywords = listOf("科技", "电子", "软件", "通信", "芯片", "半导体", "人工智能")
            val spaceKeywords = listOf("星舰", "星链", "卫星", "发射", "航天", "太空", "星座")

            stocks.forEach { s ->
                val isBlacklisted = listOf("000001","600000","000002","600519","000858").contains(s.stockCode)
                val isTech = techStockCodes.contains(s.stockCode) || stockNameKeywords.any { kw -> s.stockName.contains(kw, ignoreCase = true) }
                val isSpace = (!s.sectorName.isNullOrBlank() && spaceKeywords.any { kw -> s.sectorName.contains(kw, ignoreCase = true) })
                        || spaceKeywords.any { kw -> s.stockName.contains(kw, ignoreCase = true) }

                val reason = when {
                    isBlacklisted -> "blacklisted"
                    isTech -> "kept:tech"
                    isSpace -> "kept:space"
                    else -> "filtered:not-tech-or-space"
                }

                appendFile.appendText("diag:${s.stockCode} | ${s.stockName} | sector=${s.sectorName} | reason=$reason\n")
            }
        } catch (e: Exception) {
            println("[DIAG] failed to append per-stock diagnostics: ${e.message}")
        }

    }
}
