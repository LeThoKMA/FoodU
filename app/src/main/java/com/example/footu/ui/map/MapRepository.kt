package com.example.footu.ui.map

import com.example.footu.Response.BaseResponse
import com.example.footu.Response.PointResponse
import com.example.footu.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MapRepository @Inject constructor(private val apiService: ApiService) {
    private val dispatcher = Dispatchers.IO
    suspend fun getRouters(): Flow<BaseResponse<List<PointResponse>>> {
        return withContext(dispatcher) {
            flow { emit(apiService.getRouters()) }
        }
    }
}