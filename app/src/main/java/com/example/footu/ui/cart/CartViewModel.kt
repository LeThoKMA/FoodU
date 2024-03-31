package com.example.footu.ui.cart

import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.example.footu.model.DetailItemChoose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor() : BaseViewModel() {
    private val _listState = MutableStateFlow(mutableListOf<DetailItemChoose>())
    val listState: StateFlow<MutableList<DetailItemChoose>> = _listState
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
    fun setupData(list: MutableList<DetailItemChoose>, price: Int) {
        _listState.value = list
        _uiState.update {
            it.copy(items = list, totalPrice = price)
        }
    }

    fun removeItem(index: Int) {
        viewModelScope.launch {
            _listState.value.apply { removeAt(index) }
            val tmpList = _uiState.value.items.apply { removeAt(index) }
            var totalPrice = 0
            tmpList.forEach { totalPrice += it.totalPrice }
            _uiState.update {
                it.copy(items = tmpList, totalPrice = totalPrice)
            }
        }
    }

    data class UiState(
        val items: MutableList<DetailItemChoose> = mutableListOf<DetailItemChoose>(),
        val totalPrice: Int = 0,
    )
}
