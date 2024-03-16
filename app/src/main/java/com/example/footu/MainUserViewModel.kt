package com.example.footu

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.example.footu.model.RegisterFirebaseModel
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainUserViewModel @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext context: Context,
) : BaseViewModel() {
    fun registerFirebase(request: RegisterFirebaseModel) {
        viewModelScope.launch {
            flow { emit(apiService.registerFirebase(request)) }
                .onStart { }
                .onCompletion { }
                .catch { handleApiError(it) }
                .collect {}
        }
    }
}
