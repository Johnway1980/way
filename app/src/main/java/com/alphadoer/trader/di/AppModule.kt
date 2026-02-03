package com.alphadoer.trader.di

import android.content.Context
import androidx.room.Room
import com.alphadoer.trader.data.local.dao.AIAnalysisCacheDao
import com.alphadoer.trader.data.local.dao.MistakePatternDao
import com.alphadoer.trader.data.local.dao.ProcessStateDao
import com.alphadoer.trader.data.local.dao.SectorDao
import com.alphadoer.trader.data.local.dao.StepConfigDao
import com.alphadoer.trader.data.local.dao.StockDao
import com.alphadoer.trader.data.local.dao.TradeJournalDao
import com.alphadoer.trader.data.local.dao.TradeMistakeDao
import com.alphadoer.trader.data.local.dao.TradeRecordDao
import com.alphadoer.trader.data.local.database.AlphaDoerDatabase
import com.alphadoer.trader.data.remote.api.AIService
import com.alphadoer.trader.data.remote.interceptor.AuthInterceptor
import com.alphadoer.trader.data.remote.interceptor.ErrorHandlingInterceptor
import com.alphadoer.trader.data.repository.NewsAnalysisRepositoryImpl
import com.alphadoer.trader.data.repository.ProcessRepositoryImpl
import com.alphadoer.trader.data.repository.ReviewRepositoryImpl
import com.alphadoer.trader.data.repository.SettingsRepositoryImpl
import com.alphadoer.trader.data.repository.StatisticsRepositoryImpl
import com.alphadoer.trader.data.repository.StockRepositoryImpl
import com.alphadoer.trader.data.repository.SectorSelectionRepositoryImpl
import com.alphadoer.trader.data.repository.TradeJournalRepositoryImpl
import com.alphadoer.trader.data.repository.TradeRecordRepositoryImpl
import com.alphadoer.trader.domain.repository.NewsAnalysisRepository
import com.alphadoer.trader.domain.repository.ProcessRepository
import com.alphadoer.trader.domain.repository.ReviewRepository
import com.alphadoer.trader.domain.repository.SettingsRepository
import com.alphadoer.trader.domain.repository.StatisticsRepository
import com.alphadoer.trader.domain.repository.StockRepository
import com.alphadoer.trader.domain.repository.SectorSelectionRepository
import com.alphadoer.trader.domain.repository.TradeJournalRepository
import com.alphadoer.trader.domain.repository.TradeRecordRepository
import com.alphadoer.trader.domain.usecase.process.ProcessManager
import com.alphadoer.trader.domain.usecase.process.ProcessManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ========== Database ==========
    // ========== Validator ========== 
    @Provides
    @Singleton
    fun provideStockRecommendationValidator(): com.alphadoer.trader.data.util.StockRecommendationValidator =
        com.alphadoer.trader.data.util.StockRecommendationValidator()
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AlphaDoerDatabase =
        Room.databaseBuilder(
            context,
            AlphaDoerDatabase::class.java,
            AlphaDoerDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // 开发阶段使用，生产环境应实现Migration
            .build()

    @Provides
    fun provideTradeJournalDao(database: AlphaDoerDatabase): TradeJournalDao =
        database.tradeJournalDao()

    @Provides
    fun provideTradeRecordDao(database: AlphaDoerDatabase): TradeRecordDao =
        database.tradeRecordDao()

    @Provides
    fun provideAIAnalysisCacheDao(database: AlphaDoerDatabase): AIAnalysisCacheDao =
        database.aiAnalysisCacheDao()

    @Provides
    fun provideStockDao(database: AlphaDoerDatabase): StockDao =
        database.stockDao()

    @Provides
    fun provideSectorDao(database: AlphaDoerDatabase): SectorDao =
        database.sectorDao()

    @Provides
    fun provideMistakePatternDao(database: AlphaDoerDatabase): MistakePatternDao =
        database.mistakePatternDao()
    
    @Provides
    fun provideProcessStateDao(database: AlphaDoerDatabase): ProcessStateDao =
        database.processStateDao()
    
    @Provides
            fun provideStepConfigDao(database: AlphaDoerDatabase): StepConfigDao =
                database.stepConfigDao()

            @Provides
            fun provideTradeMistakeDao(database: AlphaDoerDatabase): TradeMistakeDao =
                database.tradeMistakeDao()

    // ========== Network ==========
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // 改为BODY级别以查看完整请求和响应
        }

    @Provides
    @Singleton
    fun provideErrorHandlingInterceptor(): ErrorHandlingInterceptor =
        ErrorHandlingInterceptor()

    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor =
        // 百度千帆API Key（格式：bce-v3/ALTAK-xxx/xxx）
        AuthInterceptor { "bce-v3/ALTAK-eHEhSNFiYK3Z1cCNLgpzi/cf9ba76215ac13397788c2d167e7ed163af397d9" }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        errorHandlingInterceptor: ErrorHandlingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 连接超时：30秒
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // 读取超时：60秒（AI生成需要时间）
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 写入超时：30秒
            .addInterceptor(authInterceptor)
            .addInterceptor(errorHandlingInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            // 百度千帆 API 基础域名；具体路径在 AIService 的 @POST 中配置
            // Retrofit 要求 baseUrl 以 "/" 结尾
            .baseUrl("https://qianfan.baidubce.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    fun provideAIService(retrofit: Retrofit): AIService =
        retrofit.create(AIService::class.java)

    // ========== Repository ==========
    @Provides
    fun provideTradeJournalRepository(
        repository: TradeJournalRepositoryImpl
    ): TradeJournalRepository = repository
    
    @Provides
    fun provideNewsAnalysisRepository(
        repository: NewsAnalysisRepositoryImpl
    ): NewsAnalysisRepository = repository
    
    @Provides
    fun provideStockRepository(
        repository: StockRepositoryImpl
    ): StockRepository = repository

    @Provides
    fun provideSectorSelectionRepository(
        repository: SectorSelectionRepositoryImpl
    ): SectorSelectionRepository = repository
    
    @Provides
    fun provideProcessRepository(
        repository: ProcessRepositoryImpl
    ): ProcessRepository = repository
    
    // ========== UseCase ==========
    @Provides
    @Singleton
    fun provideProcessManager(
        manager: ProcessManagerImpl
    ): ProcessManager = manager
    
            @Provides
            fun provideTradeRecordRepository(
                repository: TradeRecordRepositoryImpl
            ): TradeRecordRepository = repository

            @Provides
            fun provideReviewRepository(
                repository: ReviewRepositoryImpl
            ): ReviewRepository = repository

            @Provides
            fun provideStatisticsRepository(
                repository: StatisticsRepositoryImpl
            ): StatisticsRepository = repository

            @Provides
            fun provideSettingsRepository(
                repository: SettingsRepositoryImpl
            ): SettingsRepository = repository

    // ========== UseCase ==========
    @Provides
    fun provideAnalyzeTradeMistakesUseCase(
        reviewRepository: ReviewRepository,
        tradeRecordRepository: TradeRecordRepository
    ): com.alphadoer.trader.domain.usecase.review.AnalyzeTradeMistakesUseCase =
        com.alphadoer.trader.domain.usecase.review.AnalyzeTradeMistakesUseCase(
            reviewRepository,
            tradeRecordRepository
        )

    @Provides
    fun provideIdentifyMistakePatternsUseCase(
        reviewRepository: ReviewRepository
    ): com.alphadoer.trader.domain.usecase.review.IdentifyMistakePatternsUseCase =
        com.alphadoer.trader.domain.usecase.review.IdentifyMistakePatternsUseCase(reviewRepository)

    @Provides
    fun provideGenerateDailySummaryUseCase(
        reviewRepository: ReviewRepository,
        tradeRecordRepository: TradeRecordRepository
    ): com.alphadoer.trader.domain.usecase.review.GenerateDailySummaryUseCase =
        com.alphadoer.trader.domain.usecase.review.GenerateDailySummaryUseCase(
            reviewRepository,
            tradeRecordRepository
        )

    @Provides
    fun provideCreateTomorrowPlanUseCase(
        reviewRepository: ReviewRepository
    ): com.alphadoer.trader.domain.usecase.review.CreateTomorrowPlanUseCase =
        com.alphadoer.trader.domain.usecase.review.CreateTomorrowPlanUseCase(reviewRepository)

    // ========== Statistics UseCases ==========
    @Provides
    fun provideCalculatePerformanceMetricsUseCase(
        statisticsRepository: com.alphadoer.trader.domain.repository.StatisticsRepository,
        tradeRecordRepository: TradeRecordRepository
    ): com.alphadoer.trader.domain.usecase.statistics.CalculatePerformanceMetricsUseCase =
        com.alphadoer.trader.domain.usecase.statistics.CalculatePerformanceMetricsUseCase(
            statisticsRepository,
            tradeRecordRepository
        )

    // ========== Settings UseCases ==========
    @Provides
    fun provideGetUserSettingsUseCase(
        settingsRepository: SettingsRepository
    ): com.alphadoer.trader.domain.usecase.settings.GetUserSettingsUseCase =
        com.alphadoer.trader.domain.usecase.settings.GetUserSettingsUseCase(settingsRepository)

    @Provides
    fun provideUpdateAppearanceUseCase(
        settingsRepository: SettingsRepository
    ): com.alphadoer.trader.domain.usecase.settings.UpdateAppearanceUseCase =
        com.alphadoer.trader.domain.usecase.settings.UpdateAppearanceUseCase(settingsRepository)

    // ========== News Analysis UseCases ==========
    @Provides
    fun provideAnalyzeNewsUseCase(
        newsAnalysisRepository: NewsAnalysisRepository
    ): com.alphadoer.trader.domain.usecase.AnalyzeNewsUseCase =
        com.alphadoer.trader.domain.usecase.AnalyzeNewsUseCase(newsAnalysisRepository)

    @Provides
    fun provideGetAnalysisHistoryUseCase(
        newsAnalysisRepository: NewsAnalysisRepository
    ): com.alphadoer.trader.domain.usecase.GetAnalysisHistoryUseCase =
        com.alphadoer.trader.domain.usecase.GetAnalysisHistoryUseCase(newsAnalysisRepository)

    @Provides
    fun provideSaveAnalysisResultUseCase(
        newsAnalysisRepository: NewsAnalysisRepository
    ): com.alphadoer.trader.domain.usecase.SaveAnalysisResultUseCase =
        com.alphadoer.trader.domain.usecase.SaveAnalysisResultUseCase(newsAnalysisRepository)

    @Provides
    fun provideSummarizeNewsAndSectorsUseCase(
        aiService: AIService,
        newsAnalysisRepository: NewsAnalysisRepository,
        sectorSelectionRepository: SectorSelectionRepository
    ): com.alphadoer.trader.domain.usecase.SummarizeNewsAndSectorsUseCase =
        com.alphadoer.trader.domain.usecase.SummarizeNewsAndSectorsUseCase(
            aiService,
            newsAnalysisRepository,
            sectorSelectionRepository
        )

    // ========== Trading UseCases ==========
    @Provides
    fun provideRecordTradeUseCase(
        tradeRecordRepository: TradeRecordRepository
    ): com.alphadoer.trader.domain.usecase.trading.RecordTradeUseCase =
        com.alphadoer.trader.domain.usecase.trading.RecordTradeUseCase(tradeRecordRepository)

    @Provides
    fun provideUpdateTradeUseCase(
        tradeRecordRepository: TradeRecordRepository
    ): com.alphadoer.trader.domain.usecase.trading.UpdateTradeUseCase =
        com.alphadoer.trader.domain.usecase.trading.UpdateTradeUseCase(tradeRecordRepository)

    @Provides
    fun provideGetTradesByDateUseCase(
        tradeRecordRepository: TradeRecordRepository
    ): com.alphadoer.trader.domain.usecase.trading.GetTradesByDateUseCase =
        com.alphadoer.trader.domain.usecase.trading.GetTradesByDateUseCase(tradeRecordRepository)

    @Provides
    fun provideGetTradingStatisticsUseCase(
        tradeRecordRepository: TradeRecordRepository
    ): com.alphadoer.trader.domain.usecase.trading.GetTradingStatisticsUseCase =
        com.alphadoer.trader.domain.usecase.trading.GetTradingStatisticsUseCase(tradeRecordRepository)

    @Provides
    fun provideCalculateProfitLossUseCase(
        tradeRecordRepository: TradeRecordRepository
    ): com.alphadoer.trader.domain.usecase.trading.CalculateProfitLossUseCase =
        com.alphadoer.trader.domain.usecase.trading.CalculateProfitLossUseCase(tradeRecordRepository)

    // ========== PreMarketPlan UseCases ==========
    @Provides
    fun provideSavePreMarketPlanUseCase(
        tradeJournalRepository: TradeJournalRepository
    ): com.alphadoer.trader.domain.usecase.SavePreMarketPlanUseCase =
        com.alphadoer.trader.domain.usecase.SavePreMarketPlanUseCase(tradeJournalRepository)

    @Provides
    fun provideGetPreMarketPlanUseCase(
        tradeJournalRepository: TradeJournalRepository
    ): com.alphadoer.trader.domain.usecase.GetPreMarketPlanUseCase =
        com.alphadoer.trader.domain.usecase.GetPreMarketPlanUseCase(tradeJournalRepository)

    // ========== AuctionObservation UseCases ==========
    @Provides
    fun provideSaveAuctionObservationUseCase(
        tradeJournalRepository: TradeJournalRepository
    ): com.alphadoer.trader.domain.usecase.SaveAuctionObservationUseCase =
        com.alphadoer.trader.domain.usecase.SaveAuctionObservationUseCase(tradeJournalRepository)

    // ========== Review UseCases ==========
    @Provides
    fun provideGenerateMarketReviewUseCase(
        reviewRepository: ReviewRepository
    ): com.alphadoer.trader.domain.usecase.review.GenerateMarketReviewUseCase =
        com.alphadoer.trader.domain.usecase.review.GenerateMarketReviewUseCase(reviewRepository)
}
