package com.example.footu.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.Response.CategoryResponse
import com.example.footu.base.BaseViewModel
import com.example.footu.model.Item
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        val apiService: ApiService,
    ) : BaseViewModel() {
        val dataItems = MutableLiveData<ArrayList<Item>>()

        private val _banners = MutableLiveData<List<String>>()
        val banner: LiveData<List<String>> = _banners

        private val _category = MutableLiveData<List<CategoryResponse>?>()
        val category: MutableLiveData<List<CategoryResponse>?> = _category

        val message = MutableLiveData<String>()

        init {
            fetchItems()
        }

        private fun fetchItems() {
            viewModelScope.launch(Dispatchers.IO) {
                flow {
                    emit(
                        Triple(
                            apiService.getItems(),
                            apiService.getBannerList(),
                            apiService.getCategory(),
                        ),
                    )
                }.onStart { onRetrievePostListStart() }
                    .catch { handleApiError(it) }
                    .onCompletion { onRetrievePostListFinish() }
                    .collect {
                        dataItems.postValue(it.first.data as ArrayList<Item>?)
                        it.second.data?.let { _banners.postValue(it) }
                        it.third.data?.let { _category.postValue(it) }
                    }
            }
        }

        fun loadMoreItems(page: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                flow { emit(apiService.getItems(page)) }
                    .catch { handleApiError(it) }
                    .collect {
                        if (!it.data.isNullOrEmpty()) {
                            dataItems.postValue(it.data as ArrayList<Item>?)
                        }
                    }
            }
        }

        fun getProductByType(id: Int) {
            viewModelScope.launch {
                flow { emit(apiService.getProductByType(id)) }
                    .onStart { onRetrievePostListStart() }
                    .onCompletion { onRetrievePostListFinish() }
                    .collect {
                        dataItems.postValue(it.data as ArrayList<Item>?)
                    }
            }
        }
    }
