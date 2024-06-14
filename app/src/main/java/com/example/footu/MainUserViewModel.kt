package com.example.footu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.example.footu.model.DetailItemChoose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainUserViewModel
    @Inject
    constructor(
        private val sharePref: EncryptPreference,
    ) : BaseViewModel() {
        val itemsChoose = hashMapOf<Pair<Int, Int>, DetailItemChoose>()
        private val _totalPrice = MutableLiveData<Int>()
        val totalPrice: LiveData<Int> = _totalPrice
        val user = sharePref.getUser()

        fun handleItemsCart(items: List<DetailItemChoose>) {
            viewModelScope.launch {
                items.forEach {
                    if (itemsChoose.containsKey(Pair(it.id, it.size?.ordinal))) {
                        val oldCount = itemsChoose[Pair(it.id, it.size?.ordinal)]?.count ?: 0
                        itemsChoose[Pair(it.id, it.size?.ordinal)]?.count = oldCount.plus(it.count)
                    } else {
                        itemsChoose[Pair(it.id, it.size?.ordinal ?: 1)] = it
                    }
                }
                _totalPrice.postValue(itemsChoose.values.sumOf { it.totalPrice })
            }
        }

        fun onChangeItem(item: DetailItemChoose) {
            itemsChoose.remove(Pair(item.id, item.size?.ordinal))
        }

        fun resetCart() {
            itemsChoose.clear()
            _totalPrice.value = 0
        }
    }
