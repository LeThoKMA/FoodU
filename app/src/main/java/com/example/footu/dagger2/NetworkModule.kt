//package com.example.footu.dagger2
//
//import android.content.Context
//import android.preference.PreferenceManager
//import com.example.footu.network.ApiService
//import dagger.Module
//import dagger.Provides
//import dagger.Reusable
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.util.concurrent.TimeUnit
//
///**
// * Module which provides all required dependencies about network
// */
//@Module
//// Safe here as we are dealing with a Dagger 2 module
//@Suppress("unused")
//object NetworkModule {
//
//    var mToken = ""
//    private const val TIME_OUT: Long = 10
//    private var mLanguage = "vi"
//
//    /**
//     * Provides the Post service implementation.
//     * @param retrofit the Retrofit object used to instantiate the service
//     * @return the Post service implementation.
//     */
//    @Provides
//    @Reusable
//    @JvmStatic
//    internal fun providePostApi(retrofit: Retrofit): ApiService {
//        return retrofit.create(ApiService::class.java)
//    }
//
//    /**
//     * Provides the Retrofit object.
//     * @return the Retrofit object
//     */
//    @Provides
//    @Reusable
//    @JvmStatic
//    internal fun provideRetrofitInterface(@ApplicationContext context: Context): Retrofit {
//        val mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
//
//        if (mToken == "") {
//            mToken = mPrefs.getString("PREF_ACCESS_TOKEN", "") ?: ""
//        }
//
//        val httpClient = OkHttpClient.Builder()
//        httpClient.connectTimeout(TIME_OUT, TimeUnit.SECONDS)
//        httpClient.readTimeout(TIME_OUT, TimeUnit.SECONDS)
//
//        httpClient.addInterceptor { chain ->
//            val request = chain.request().newBuilder()
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Accept", "*/*")
//                .addHeader("Authorization", "Bearer $mToken")
//                .addHeader("Accept-Language", mLanguage)
//                .build()
//            chain.proceed(request)
//        }
//
//        val logging = HttpLoggingInterceptor()
//        logging.level = HttpLoggingInterceptor.Level.BODY
//        httpClient.addInterceptor(logging) // <-- this is the important line!
//
//        return Retrofit.Builder()
//            .baseUrl("https://d061-58-186-177-10.ngrok-free.app/api/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(httpClient.build())
//            .build()
//    }
//}
