package com.alphadoer.trader.data.util

import com.alphadoer.trader.domain.model.RecommendedStock

/**
 * 板块股票池
 * 预置的按板块分类的股票推荐池，用于数量补足
 */
object SectorStockPool {
    
    /**
     * 获取指定板块的候选股票列表
     * @param sectorName 板块名称
     * @return 该板块的候选股票列表
     */
    fun getStocksForSector(sectorName: String): List<RecommendedStock> {
        return stockPool[sectorName] ?: emptyList()
    }
    
    /**
     * 获取所有支持的板块名称
     */
    fun getSupportedSectors(): Set<String> = stockPool.keys
    
    /**
     * 板块股票映射池
     * 格式：板块名称 -> 候选股票列表
     */
    private val stockPool: Map<String, List<RecommendedStock>> = mapOf(
        // 人工智能/AI板块
        "人工智能" to listOf(
            RecommendedStock(
                stockCode = "002230",
                stockName = "科大讯飞",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "AI语音技术领先，大模型研发投入持续增加",
                confidence = 0.75,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "人工智能"
            ),
            RecommendedStock(
                stockCode = "000977",
                stockName = "浪潮信息",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "AI服务器龙头企业",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "人工智能"
            ),
            RecommendedStock(
                stockCode = "002415",
                stockName = "海康威视",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "AI视觉技术领先",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "人工智能"
            ),
            RecommendedStock(
                stockCode = "300496",
                stockName = "中科创达",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "智能操作系统和AI算法平台",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "人工智能"
            ),
            RecommendedStock(
                stockCode = "688111",
                stockName = "金山办公",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "办公软件AI应用",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "人工智能"
            )
        ),
        
        // 半导体/芯片板块
        "半导体" to listOf(
            RecommendedStock(
                stockCode = "600745",
                stockName = "闻泰科技",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "半导体封装和通讯终端",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "半导体"
            ),
            RecommendedStock(
                stockCode = "600584",
                stockName = "长电科技",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "芯片封装测试龙头",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "半导体"
            ),
            RecommendedStock(
                stockCode = "002049",
                stockName = "紫光国微",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "安全芯片和智能卡芯片",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "半导体"
            ),
            RecommendedStock(
                stockCode = "002371",
                stockName = "北方华创",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "半导体设备制造",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "半导体"
            ),
            RecommendedStock(
                stockCode = "688981",
                stockName = "中芯国际",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "集成电路晶圆代工",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "半导体"
            )
        ),
        
        // 稀土及新材料板块
        "稀土及新材料" to listOf(
            RecommendedStock(
                stockCode = "600111",
                stockName = "北方稀土",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "稀土行业龙头",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "稀土及新材料"
            ),
            RecommendedStock(
                stockCode = "000831",
                stockName = "五矿稀土",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "稀土分离和深加工",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "稀土及新材料"
            ),
            RecommendedStock(
                stockCode = "600259",
                stockName = "广晟有色",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "稀土开采和冶炼",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "稀土及新材料"
            ),
            RecommendedStock(
                stockCode = "600392",
                stockName = "盛和资源",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "稀土采选和冶炼分离",
                confidence = 0.60,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "稀土及新材料"
            ),
            RecommendedStock(
                stockCode = "000970",
                stockName = "中科三环",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "稀土永磁材料",
                confidence = 0.60,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "稀土及新材料"
            )
        ),
        
        // 新能源板块
        "新能源" to listOf(
            RecommendedStock(
                stockCode = "300750",
                stockName = "宁德时代",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "动力电池龙头",
                confidence = 0.75,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "新能源"
            ),
            RecommendedStock(
                stockCode = "002594",
                stockName = "比亚迪",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "新能源汽车和电池",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "新能源"
            ),
            RecommendedStock(
                stockCode = "300014",
                stockName = "亿纬锂能",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "锂离子电池制造",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "新能源"
            ),
            RecommendedStock(
                stockCode = "002812",
                stockName = "恩捷股份",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "锂电池隔膜",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "新能源"
            ),
            RecommendedStock(
                stockCode = "002460",
                stockName = "赣锋锂业",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "锂资源开发",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "新能源"
            )
        ),
        
        // 通信板块
        "通信" to listOf(
            RecommendedStock(
                stockCode = "000063",
                stockName = "中兴通讯",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "5G和通信设备",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "通信"
            ),
            RecommendedStock(
                stockCode = "600522",
                stockName = "中天科技",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "光通信和电力传输",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "通信"
            ),
            RecommendedStock(
                stockCode = "002396",
                stockName = "星网锐捷",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "网络通信设备",
                confidence = 0.60,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "通信"
            ),
            RecommendedStock(
                stockCode = "300308",
                stockName = "中际旭创",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "光模块制造",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "通信"
            ),
            RecommendedStock(
                stockCode = "600050",
                stockName = "中国联通",
                market = "SH",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "电信运营商",
                confidence = 0.60,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "通信"
            )
        ),
        
        // 工业互联网板块
        "工业互联网" to listOf(
            RecommendedStock(
                stockCode = "300166",
                stockName = "东方国信",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "工业互联网平台和工业大数据",
                confidence = 0.75,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "工业互联网"
            ),
            RecommendedStock(
                stockCode = "002410",
                stockName = "广联达",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "建筑信息化和工业软件",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "工业互联网"
            ),
            RecommendedStock(
                stockCode = "300496",
                stockName = "中科创达",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "智能操作系统和工业物联网",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "工业互联网"
            ),
            RecommendedStock(
                stockCode = "002268",
                stockName = "卫士通",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "工业互联网安全",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "工业互联网"
            ),
            RecommendedStock(
                stockCode = "300379",
                stockName = "东方通",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "中间件和工业互联网平台",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "工业互联网"
            )
        ),
        
        // 数据服务板块
        "数据服务" to listOf(
            RecommendedStock(
                stockCode = "300033",
                stockName = "同花顺",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "金融数据服务和数据标注",
                confidence = 0.75,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "数据服务"
            ),
            RecommendedStock(
                stockCode = "300059",
                stockName = "东方财富",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "金融数据服务和数据咨询",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "数据服务"
            ),
            RecommendedStock(
                stockCode = "002230",
                stockName = "科大讯飞",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "AI数据标注和数据集服务",
                confidence = 0.70,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "数据服务"
            ),
            RecommendedStock(
                stockCode = "300229",
                stockName = "拓尔思",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "大数据和文本挖掘服务",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "数据服务"
            ),
            RecommendedStock(
                stockCode = "300271",
                stockName = "华宇软件",
                market = "SZ",
                recommendation = RecommendedStock.RecommendationType.WATCH,
                reason = "数据治理和数据服务",
                confidence = 0.65,
                riskLevel = RecommendedStock.RiskLevel.MEDIUM,
                sectorName = "数据服务"
            )
        )
    )
}
