package com.example.footu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.footu.model.ShipperLocation
import com.example.footu.socket.SocketIoManage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

object LocationEmitter {
    @ApplicationContext
    lateinit var context: Context

    private val locationClient by lazy { LocationServices.getFusedLocationProviderClient(context) }
    fun emitLocation() {
        CoroutineScope(IO).launch {
            while (true) {
                delay(10000L)
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    CancellationTokenSource().token,
                ).addOnSuccessListener { location ->
                    Log.e("MyLocationWorker", "get location: ")

                    location?.let {
                        val id = MyPreference.getInstance()?.getUser()?.id!!
                        val shipperLocation = ShipperLocation(id, it.latitude, it.longitude)
                        val obj = JSONObject(
                            GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()
                                .toJson(shipperLocation),
                        )
                        SocketIoManage.mSocket?.emit("send_locate", obj)
                    }
                }
            }
        }
    }
}
