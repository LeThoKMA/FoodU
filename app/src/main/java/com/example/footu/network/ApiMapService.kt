package com.example.footu.network

import com.example.footu.Response.DirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiMapService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
    ): DirectionsResponse
}
