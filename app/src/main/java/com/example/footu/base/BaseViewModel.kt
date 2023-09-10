package com.example.footu.base

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.footu.R
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import javax.net.ssl.HttpsURLConnection

abstract class BaseViewModel : ViewModel() {
    var isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val errorMessage: MutableLiveData<Int?> = MutableLiveData()
    val responseMessage: MutableLiveData<String?> = MutableLiveData()

    protected fun onRetrievePostListStart() {
        isLoading.postValue(true)
        errorMessage.postValue(null)
    }

    protected fun onRetrievePostListFinish() {
        isLoading.postValue(false)
//        Handler(Looper.getMainLooper()).postDelayed({
//            isLoading.postValue(false)
//        }, 500)
    }

    protected fun handleApiError(error: Throwable?) {
        if (error == null) {
            errorMessage.postValue(R.string.api_default_error)
            return
        }

        if (error is HttpException) {
            Log.w("ERROR", error.message() + error.code())
            when (error.code()) {
                HttpURLConnection.HTTP_BAD_REQUEST -> try {
                    responseMessage.postValue(error.message())
                } catch (e: IOException) {
                    e.printStackTrace()
                    responseMessage.postValue(error.message)
                }
                HttpsURLConnection.HTTP_UNAUTHORIZED -> errorMessage.postValue(R.string.str_authe)
                HttpsURLConnection.HTTP_FORBIDDEN, HttpsURLConnection.HTTP_INTERNAL_ERROR, HttpsURLConnection.HTTP_NOT_FOUND -> responseMessage.postValue(
                    error.message,
                )
                else -> responseMessage.postValue(error.message)
            }
        } else if (error is SocketTimeoutException) {
            errorMessage.postValue(R.string.text_all_has_error_timeout)
        } else if (error is IOException) {
            Log.e("TAG", error.message.toString())
            errorMessage.postValue(R.string.text_all_has_error_network)
        } else {
            if (!TextUtils.isEmpty(error.message)) {
                responseMessage.postValue(error.message)
            } else {
                errorMessage.postValue(R.string.text_all_has_error_please_try)
            }
        }
    }

    fun toMultipartBody(name: String, file: File): MultipartBody.Part {
        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, file.name, reqFile)
    }

    fun toMultipartBody1(name: String, file: File): MultipartBody.Part {
        val reqFile = file.asRequestBody("video/*, image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(name, file.name, reqFile)
    }
}
