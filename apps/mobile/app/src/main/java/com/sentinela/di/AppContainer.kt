package com.sentinela.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sentinela.BuildConfig
import com.sentinela.data.local.AppDatabase
import com.sentinela.data.remote.SentinelaApi
import com.sentinela.data.repository.FireRepository
import com.sentinela.data.repository.MonitoredPointRepository
import com.sentinela.domain.usecase.EvaluateProximityUseCase
import com.sentinela.domain.usecase.GetFiresUseCase
import com.sentinela.domain.usecase.GetRiskUseCase
import com.sentinela.notification.NotificationHelper
import com.sentinela.ui.alert.AlertViewModel
import com.sentinela.ui.detail.FireDetailViewModel
import com.sentinela.ui.list.FireListViewModel
import com.sentinela.ui.map.MapViewModel
import com.sentinela.ui.points.PointFormViewModel
import com.sentinela.ui.points.PointsViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    val notificationHelper = NotificationHelper(context.applicationContext)
    val firesSessionStore = FiresSessionStore()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    },
                )
            }
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(BuildConfig.SENTINELA_API_URL))
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val api: SentinelaApi = retrofit.create(SentinelaApi::class.java)

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "sentinela.db",
    ).build()

    val fireRepository = FireRepository(api)
    val monitoredPointRepository = MonitoredPointRepository(database)

    private val getFiresUseCase = GetFiresUseCase(fireRepository)
    private val getRiskUseCase = GetRiskUseCase(fireRepository)
    private val evaluateProximityUseCase = EvaluateProximityUseCase()

    val proximityManager = ProximityManager(
        evaluateProximityUseCase,
        monitoredPointRepository,
        notificationHelper,
    )

    fun mapViewModel(): MapViewModel = MapViewModel(
        getFiresUseCase = getFiresUseCase,
        getRiskUseCase = getRiskUseCase,
        pointRepository = monitoredPointRepository,
        proximityManager = proximityManager,
        firesSessionStore = firesSessionStore,
    )

    fun fireListViewModel(): FireListViewModel = FireListViewModel(
        firesSessionStore = firesSessionStore,
        pointRepository = monitoredPointRepository,
    )

    fun fireDetailViewModel(fireId: String): FireDetailViewModel = FireDetailViewModel(
        fireId = fireId,
        firesSessionStore = firesSessionStore,
    )

    fun pointsViewModel(): PointsViewModel = PointsViewModel(
        pointRepository = monitoredPointRepository,
    )

    fun pointFormViewModel(pointId: Long): PointFormViewModel = PointFormViewModel(
        pointId = pointId,
        pointRepository = monitoredPointRepository,
    )

    fun alertViewModel(): AlertViewModel = AlertViewModel(
        pointRepository = monitoredPointRepository,
        firesSessionStore = firesSessionStore,
    )

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}
