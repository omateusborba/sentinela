package com.sentinela.data.remote

import com.sentinela.data.DEFAULT_FIRMS_SOURCE
import com.sentinela.data.remote.dto.FiresResponseDto
import com.sentinela.data.remote.dto.RiskResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SentinelaApi {

    @GET("api/fires")
    suspend fun getFires(
        @Query("bbox") bbox: String,
        @Query("days") days: Int,
        @Query("source") source: String = DEFAULT_FIRMS_SOURCE,
    ): FiresResponseDto

    @GET("api/risk")
    suspend fun getRisk(
        @Query("bbox") bbox: String,
        @Query("days") days: Int,
    ): RiskResponseDto
}
