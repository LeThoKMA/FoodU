//package com.example.footu
//
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import androidx.work.WorkerParameters
//import com.example.footu.socket.SocketIoManage
//import com.example.footu.utils.CHANNEL_ID
//import com.example.footu.utils.createNotificationChanel
//
//class MyWorkManager(val appContext: Context, workerParams: WorkerParameters) :
//    androidx.work.Worker(appContext, workerParams) {
//    var socketChannel: Int = -1
//    override fun doWork(): Result {
//        socketChannel = MyPreference.getInstance()?.getUser()?.id!!
//        SocketIoManage.mSocket?.on("$socketChannel") { args ->
//            showNotification("Hello world")
//        }
//        return Result.success()
//    }
//
//    private fun showNotification(message: String) {
//        val notifyIntent = Intent(this.appContext, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        }
//        val notifyPendingIntent = PendingIntent.getActivity(
//            this.appContext,
//            0,
//            notifyIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
//        )
//        val channelId = CHANNEL_ID
//        val notificationBuilder = NotificationCompat.Builder(this.appContext, channelId)
//            .setContentIntent(notifyPendingIntent)
//            .setContentTitle("New Event")
//            .setContentText(message)
//            .setSmallIcon(R.drawable.beverage_drink_svgrepo_com)
//            .setAutoCancel(true)
//
//        val notificationManager =
//            this.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            this.appContext.createNotificationChanel()
//        }
//        notificationManager.notify(0, notificationBuilder.build())
//    }
//}
