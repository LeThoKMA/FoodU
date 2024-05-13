package com.example.footu.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.random.Random

const val currencyUnit = "₫"

fun Activity.hideSoftKeyboard() {
    currentFocus?.let {
        val inputMethodManager = ContextCompat.getSystemService(
            this,
            InputMethodManager::class.java,
        )
        inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Int?.padZero(): String {
    return String.format("%06d", this)
}

fun Number?.formatToPrice(
    stringFormat: String = "0.##",
    currency: String = currencyUnit,
    isShowCurrency: Boolean = true,
): String {
    if (this == null) return "0$currencyUnit"
    val format = DecimalFormat(stringFormat)
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance("VND")
    val number: String = format.format(this)
    if (isShowCurrency) {
        return number.toDouble().decimalFormat() + currency
    }
    return number.toDouble().decimalFormat()
}

fun Double.decimalFormat(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

@SuppressLint("WrongConstant")
fun Context.createNotificationChanel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "food_app"
        val descriptionText = "hello world"
        val importance = NotificationManager.IMPORTANCE_MAX
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun ByteArray.byteArrayToString(): String {
    return Base64.encodeToString(this, Base64.DEFAULT)
}

fun ImageView.nameToAvatar(name: String) {
    val random = Random.Default
    val r = random.nextInt(256)
    val g = random.nextInt(256)
    val b = random.nextInt(256)
    val randomColor = Color.rgb(r, g, b)
    val bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 14f
    }
    canvas.drawText(name.first().toString(), bitmap.width / 2f, bitmap.height / 2f, paint)
    setImageBitmap(bitmap)
    setBackgroundColor(randomColor)
}
