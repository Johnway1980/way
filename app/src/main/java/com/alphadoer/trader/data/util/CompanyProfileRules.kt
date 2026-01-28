package com.alphadoer.trader.data.util

/**
 * 公司特征规则库
 * 定义公司的特征标签和触发条件，用于股票推荐的相关性评分
 */
object CompanyProfileRules {
    
    /**
     * 公司规则数据类
     */
    data class CompanyRule(
        val companyCode: String,
        val companyName: String,
        val businessDomain: String? = null, // 业务领域（如：金融、工业、制造、科技等）
        val primaryBusinessDomain: String? = null, // 核心业务领域（如：金融、工业制造、云计算、数据服务等）
        val coreBusinessTags: List<String> = emptyList(), // 核心业务标签（如：金融IT、工业软件、数据标注等）
        val coreBusiness: String? = null, // 核心业务描述（用于强关联校验）
        val excludedNewsThemes: List<String> = emptyList(), // 明确排除的新闻主题（跨领域强过滤）
        val tags: List<String>, // 公司特征标签
        val positiveTriggers: List<String>, // 正面触发器关键词
        val negativeTriggers: List<String> // 负面触发器关键词
    )
    
    /**
     * 获取指定股票代码的规则
     */
    fun getRule(companyCode: String): CompanyRule? {
        return rules[companyCode]
    }
    
    /**
     * 获取所有规则
     */
    fun getAllRules(): Map<String, CompanyRule> = rules
    
    /**
     * 公司规则库
     * 格式：股票代码 -> 公司规则
     */
    private val rules: Map<String, CompanyRule> = mapOf(
        // 科大讯飞（002230）
        "002230" to CompanyRule(
            companyCode = "002230",
            companyName = "科大讯飞",
            tags = listOf("自主可控", "国产大模型", "AI语音", "教育信息化"),
            positiveTriggers = listOf("人工智能", "大模型", "语音识别", "教育", "国产化"),
            negativeTriggers = listOf("国外芯片放宽", "技术封锁解除")
        ),
        
        // 闻泰科技（600745）
        "600745" to CompanyRule(
            companyCode = "600745",
            companyName = "闻泰科技",
            tags = listOf("半导体封装", "通讯终端", "ODM"),
            positiveTriggers = listOf("芯片进口", "半导体", "5G", "手机"),
            negativeTriggers = listOf("贸易限制", "出口管制")
        ),
        
        // 浪潮信息（000977）
        "000977" to CompanyRule(
            companyCode = "000977",
            companyName = "浪潮信息",
            tags = listOf("AI服务器", "数据中心", "云计算"),
            positiveTriggers = listOf("人工智能", "AI", "服务器", "数据中心", "云计算"),
            negativeTriggers = listOf("芯片禁运", "技术制裁")
        ),
        
        // 海康威视（002415）
        "002415" to CompanyRule(
            companyCode = "002415",
            companyName = "海康威视",
            businessDomain = "科技",
            primaryBusinessDomain = "安防监控",
            coreBusinessTags = listOf("安防监控", "视频监控", "视觉分析"),
            coreBusiness = "安防监控",
            excludedNewsThemes = listOf("航天", "航天科技", "卫星", "卫星通信"),
            tags = listOf("AI视觉", "安防监控", "视频监控"),
            positiveTriggers = listOf("人工智能", "AI视觉", "安防", "监控", "智慧城市"),
            negativeTriggers = listOf("出口管制", "实体清单")
        ),

        // 金山办公（688111）
        "688111" to CompanyRule(
            companyCode = "688111",
            companyName = "金山办公",
            businessDomain = "科技",
            primaryBusinessDomain = "办公软件",
            coreBusinessTags = listOf("办公软件", "WPS", "协同办公", "文档处理"),
            coreBusiness = "办公软件",
            excludedNewsThemes = listOf("航天", "航天科技", "卫星", "卫星通信", "军工"),
            tags = listOf("办公软件", "WPS", "协同办公", "云办公"),
            positiveTriggers = listOf("办公软件", "WPS", "协同办公", "云办公", "国产软件", "信创"),
            negativeTriggers = listOf("航天", "卫星", "火箭", "军工")
        ),
        
        // 长电科技（600584）
        "600584" to CompanyRule(
            companyCode = "600584",
            companyName = "长电科技",
            tags = listOf("芯片封装", "半导体封装测试"),
            positiveTriggers = listOf("芯片", "半导体", "封装", "AI芯片"),
            negativeTriggers = listOf("贸易限制")
        ),
        
        // 北方稀土（600111）
        "600111" to CompanyRule(
            companyCode = "600111",
            companyName = "北方稀土",
            tags = listOf("稀土", "稀土开采", "稀土冶炼"),
            positiveTriggers = listOf("稀土", "出口管制", "新材料", "新能源"),
            negativeTriggers = listOf("稀土价格下跌", "需求下降")
        ),
        
        // 宁德时代（300750）
        "300750" to CompanyRule(
            companyCode = "300750",
            companyName = "宁德时代",
            tags = listOf("动力电池", "锂电池", "新能源汽车"),
            positiveTriggers = listOf("新能源", "新能源汽车", "电池", "锂电池", "电动车"),
            negativeTriggers = listOf("产能过剩", "价格战")
        ),
        
        // 比亚迪（002594）
        "002594" to CompanyRule(
            companyCode = "002594",
            companyName = "比亚迪",
            tags = listOf("新能源汽车", "动力电池", "电动汽车"),
            positiveTriggers = listOf("新能源", "新能源汽车", "电动汽车", "电池"),
            negativeTriggers = listOf("竞争加剧", "价格战")
        ),
        
        // 中兴通讯（000063）
        "000063" to CompanyRule(
            companyCode = "000063",
            companyName = "中兴通讯",
            tags = listOf("5G", "通信设备", "网络设备"),
            positiveTriggers = listOf("5G", "通信", "网络建设", "基站"),
            negativeTriggers = listOf("技术制裁", "出口限制")
        ),
        
        // 中芯国际（688981）
        "688981" to CompanyRule(
            companyCode = "688981",
            companyName = "中芯国际",
            businessDomain = "科技",
            tags = listOf("芯片制造", "晶圆代工", "集成电路"),
            positiveTriggers = listOf("芯片", "半导体", "晶圆", "国产芯片"),
            negativeTriggers = listOf("技术封锁", "设备禁运")
        ),
        
        // 同花顺（300033）- 金融领域
        "300033" to CompanyRule(
            companyCode = "300033",
            companyName = "同花顺",
            businessDomain = "金融",
            primaryBusinessDomain = "金融",
            coreBusinessTags = listOf("金融IT", "炒股软件", "金融数据服务", "证券交易软件"),
            tags = listOf("金融数据", "炒股软件", "金融信息服务"),
            positiveTriggers = listOf("金融", "证券", "股票", "投资", "金融数据"),
            negativeTriggers = listOf("工业", "制造", "制造业", "工业数据", "工业互联网")
        ),
        
        // 东方财富（300059）- 金融领域
        "300059" to CompanyRule(
            companyCode = "300059",
            companyName = "东方财富",
            businessDomain = "金融",
            primaryBusinessDomain = "金融",
            coreBusinessTags = listOf("金融IT", "财经门户", "金融数据服务", "证券资讯"),
            tags = listOf("金融数据", "财经门户", "金融信息服务"),
            positiveTriggers = listOf("金融", "证券", "股票", "投资", "金融数据"),
            negativeTriggers = listOf("工业", "制造", "制造业", "工业数据", "工业互联网")
        ),
        
        // 恒生电子（600570）- 金融领域
        "600570" to CompanyRule(
            companyCode = "600570",
            companyName = "恒生电子",
            businessDomain = "金融",
            primaryBusinessDomain = "金融",
            coreBusinessTags = listOf("金融IT", "证券交易系统", "银行核心系统", "金融软件"),
            tags = listOf("金融IT", "证券软件", "银行软件"),
            positiveTriggers = listOf("金融", "证券", "银行", "金融IT", "交易系统"),
            negativeTriggers = listOf("工业", "制造", "制造业", "工业数据", "工业互联网", "工业软件")
        ),
        
        // 东方国信（300166）- 工业互联网领域
        "300166" to CompanyRule(
            companyCode = "300166",
            companyName = "东方国信",
            businessDomain = "工业",
            primaryBusinessDomain = "工业制造",
            coreBusinessTags = listOf("工业互联网", "工业大数据", "工业软件", "智能制造平台"),
            tags = listOf("工业互联网", "工业大数据", "工业软件"),
            positiveTriggers = listOf("工业", "制造", "工业数据", "工业互联网", "智能制造"),
            negativeTriggers = listOf("金融", "证券", "投资")
        ),
        
        // 广联达（002410）- 工业/建筑领域
        "002410" to CompanyRule(
            companyCode = "002410",
            companyName = "广联达",
            businessDomain = "工业",
            primaryBusinessDomain = "工业制造",
            coreBusinessTags = listOf("工业软件", "建筑信息化", "BIM", "建筑数字化"),
            tags = listOf("建筑信息化", "工业软件", "BIM"),
            positiveTriggers = listOf("工业", "制造", "建筑", "工业软件", "数字化转型"),
            negativeTriggers = listOf("金融", "证券")
        ),
        
        // 拓尔思（300229）- 数据服务领域
        "300229" to CompanyRule(
            companyCode = "300229",
            companyName = "拓尔思",
            businessDomain = "科技",
            primaryBusinessDomain = "数据服务",
            coreBusinessTags = listOf("数据服务", "文本挖掘", "大数据", "数据标注"),
            tags = listOf("大数据", "文本挖掘", "数据服务"),
            positiveTriggers = listOf("数据", "数据服务", "数据标注", "数据挖掘", "大数据"),
            negativeTriggers = listOf("金融", "证券", "投资")
        ),
        
        // 华宇软件（300271）- 数据服务领域
        "300271" to CompanyRule(
            companyCode = "300271",
            companyName = "华宇软件",
            businessDomain = "科技",
            primaryBusinessDomain = "数据服务",
            coreBusinessTags = listOf("数据服务", "数据治理", "数据标注", "政务数据"),
            tags = listOf("数据治理", "数据服务", "政务信息化"),
            positiveTriggers = listOf("数据", "数据服务", "数据治理", "数据标注"),
            negativeTriggers = listOf("金融", "证券")
        ),
        
        // 科大讯飞（002230）- 更新业务领域
        "002230" to CompanyRule(
            companyCode = "002230",
            companyName = "科大讯飞",
            businessDomain = "科技",
            primaryBusinessDomain = "数据服务",
            coreBusinessTags = listOf("数据标注", "AI语音", "大模型", "数据服务"),
            tags = listOf("自主可控", "国产大模型", "AI语音", "教育信息化", "数据标注"),
            positiveTriggers = listOf("人工智能", "大模型", "语音识别", "教育", "国产化", "数据标注", "数据服务"),
            negativeTriggers = listOf("国外芯片放宽", "技术封锁解除")
        ),
        
        // 浪潮信息（000977）- 更新业务领域
        "000977" to CompanyRule(
            companyCode = "000977",
            companyName = "浪潮信息",
            businessDomain = "科技",
            primaryBusinessDomain = "云计算",
            coreBusinessTags = listOf("云计算", "AI服务器", "数据中心", "服务器"),
            tags = listOf("AI服务器", "数据中心", "云计算"),
            positiveTriggers = listOf("人工智能", "AI", "服务器", "数据中心", "云计算"),
            negativeTriggers = listOf("芯片禁运", "技术制裁")
        ),
        
        // 中芯国际（688981）- 更新业务领域
        "688981" to CompanyRule(
            companyCode = "688981",
            companyName = "中芯国际",
            businessDomain = "科技",
            primaryBusinessDomain = "半导体制造",
            coreBusinessTags = listOf("芯片制造", "晶圆代工", "集成电路", "半导体"),
            tags = listOf("芯片制造", "晶圆代工", "集成电路"),
            positiveTriggers = listOf("芯片", "半导体", "晶圆", "国产芯片"),
            negativeTriggers = listOf("技术封锁", "设备禁运")
        )
    )
}
