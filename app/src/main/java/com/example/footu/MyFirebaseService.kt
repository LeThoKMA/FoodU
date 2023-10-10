package com.example.footu

import android.Manifest
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.footu.ui.login.SplashScreen
import com.example.footu.utils.CHANNEL_ID
import com.example.footu.utils.createNotificationChanel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseService() : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.notification != null) {
            showNotification("${message.notification?.body} đã thanh toán đơn hàng", 1)
        } else {
            if (message.data.containsKey("newOrder")) {
                showNotification("Có đơn hàng mới", 0)
            } else {
                showNotification("${message.data["customer"]} đã thanh toán đơn hàng", 1)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun showNotification(message: String, type: Int) {
        val notifyIntent = if (!isAppIsInBackground(this) && type == 1) {
            Intent(
                this,
                MainActivity::class.java,
            ).putExtra("type", 1)
        } else {
            Intent(this, SplashScreen::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            this,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val channelId = CHANNEL_ID
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentIntent(notifyPendingIntent)
            .setContentTitle("Food")
            .setContentText(message)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setSmallIcon(R.drawable.beverage_drink_svgrepo_com)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChanel()
        }

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MyFirebaseService,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(99, notificationBuilder.build())
        }

//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            createNotificationChanel()
//        }
//        notificationManager.notify(0, notificationBuilder.build())

        // Issue the notification.
    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = (context.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
        val runningProcesses = am.runningAppProcesses
        try {
            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isInBackground
    }
}
