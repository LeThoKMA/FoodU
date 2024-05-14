package com.example.footu.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.security.SecureRandom
import kotlin.random.Random

fun generateRandomIV(): ByteArray {
    val secureRandom = SecureRandom()
    val iv = ByteArray(12) // 96 bits IV for GCM
    secureRandom.nextBytes(iv)
    return iv
}

fun nameToAvatar(name: String, width:Int, height:Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 32f
    }
    val xPos = (canvas.width / 2).toFloat()
    val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)
    canvas.drawText(name.first().toString(), xPos, yPos, paint)
    return bitmap
}

fun randomColor(): Int {
    val random = Random.Default
    val r = random.nextInt(256)
    val g = random.nextInt(256)
    val b = random.nextInt(256)
    return Color.rgb(r, g, b)
}