package com.example.footu

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.footu.model.ShipperLocation
import com.example.footu.socket.SocketIoManage
import com.example.footu.utils.CHANNEL_ID
import com.example.footu.utils.ID_CHANNEL_LOCATION_SOCKET
import com.example.footu.utils.createNotificationChanel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class SocketService() : Service() {
    private val notificationId = 1
    private val locationClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            this,
        )
    }
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanel()
        }
        showNotification()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val id = intent?.getIntExtra(ID_CHANNEL_LOCATION_SOCKET, -1)
        id?.let { emitLocation(it) }
        return START_STICKY
    }

    private fun showNotification() {
        val notifyIntent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val notifyPendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val channelId = CHANNEL_ID
        val notificationBuilder =
            NotificationCompat.Builder(this, channelId)
                .setContentIntent(notifyPendingIntent)
                .setContentTitle("Food")
                .setContentText("Bạn đang chia sẻ vị trí")
                .setSmallIcon(R.drawable.beverage_drink_svgrepo_com)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW) // Set priority to low to avoid heads-up notification
        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.S
        ) {
            notificationBuilder.setForegroundServiceBehavior(
                Notification.FOREGROUND_SERVICE_IMMEDIATE,
            )
        }
        startForeground(notificationId, notificationBuilder.build())
    }

    private fun emitLocation(id: Int) {
        serviceScope.launch {
            while (isActive) {
                delay(10000L)
                if (ActivityCompat.checkSelfPermission(
                        this@SocketService,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@SocketService,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }
                locationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    CancellationTokenSource().token,
                ).addOnSuccessListener { location ->
                    location?.let {
                        val shipperLocation = ShipperLocation(id, it.latitude, it.longitude)
                        val obj =
                            JSONObject(
                                GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()
                                    .toJson(shipperLocation),
                            )
                        SocketIoManage.mSocket?.emit("send_locate", obj)
                    }
                }.await()
            }
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}
