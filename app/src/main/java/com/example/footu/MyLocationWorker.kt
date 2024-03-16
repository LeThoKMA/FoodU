package com.example.footu

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.footu.model.ShipperLocation
import com.example.footu.socket.SocketIoManage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.GsonBuilder
import org.json.JSONObject

class MyLocationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)
    override suspend fun doWork(): Result {
        Log.e("MyLocationWorker", "doWork: ", )
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("MyLocationWorker", "fail: ", )

            return Result.failure()
        }
        locationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            Log.e("MyLocationWorker", "get location: ", )

            location?.let {
                val id = MyPreference.getInstance(applicationContext)?.getUser()?.id!!
                val shipperLocation = ShipperLocation(id, it.latitude, it.longitude)
                val obj = JSONObject(
                    GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()
                        .toJson(shipperLocation),
                )
                SocketIoManage.mSocket?.emit("send_locate", obj)
            }
        }
        return Result.success()
    }

    companion object {
        // unique name for the work
        val workName = "BgLocationWorker"
        private const val TAG = "BackgroundLocationWork"
    }
}
