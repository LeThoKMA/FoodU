package com.example.footu.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.example.footu.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import kotlin.random.Random

fun generateRandomIV(): ByteArray {
    val secureRandom = SecureRandom()
    val iv = ByteArray(12) // 96 bits IV for GCM
    secureRandom.nextBytes(iv)
    return iv
}

fun nameToAvatar(
    name: String,
    width: Int,
    height: Int,
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint =
        Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 32f
        }
    val xPos = (canvas.width / 2).toFloat()
    val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)
    canvas.drawText(name.first().uppercaseChar().toString(), xPos, yPos, paint)
    return bitmap
}

fun randomColor(): Int {
    val random = Random.Default
    val r = random.nextInt(256)
    val g = random.nextInt(256)
    val b = random.nextInt(256)
    return Color.rgb(r, g, b)
}

fun optimizeAndConvertImageToByteArray(bitmap: Bitmap): ByteArray? {
    // Kích thước tối đa mong muốn của ảnh
    val maxWidth = 800
    val maxHeight = 800

    // Tính toán kích thước mới dựa trên tỉ lệ khung hình
    var width = bitmap.width
    var height = bitmap.height
    val ratio = width.toFloat() / height
    if (width > maxWidth || height > maxHeight) {
        if (ratio > 1) {
            width = maxWidth
            height = (width / ratio).toInt()
        } else {
            height = maxHeight
            width = (height * ratio).toInt()
        }
    }

    // Thay đổi kích thước ảnh
    val newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

    // Chuyển đổi ảnh thành byte array
    val baos = ByteArrayOutputStream()
    newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
    val byteArray = baos.toByteArray()

    // Giải phóng bộ nhớ của bitmap
    newBitmap.recycle()
    return byteArray
}

fun isVideoFile(
    context: Context,
    uri: Uri,
): Boolean {
    val contentResolver: ContentResolver = context.contentResolver
    val type = contentResolver.getType(uri)
    return type != null && type.startsWith("video")
}

@SuppressLint("Range")
fun createFileFromUri(
    context: Context,
    uri: Uri?,
): File? {
    val contentResolver = context.contentResolver

    // Truy vấn thông tin về tài nguyên từ URI
    val cursor = contentResolver.query(uri!!, null, null, null, null)
    try {
        if (cursor != null && cursor.moveToFirst()) {
            val displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))

            // Tạo đối tượng File mới
            val file =
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), displayName)

            // Đọc dữ liệu từ InputStream của URI và ghi vào file mới
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream: OutputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            // Đóng luồng
            inputStream.close()
            outputStream.close()

            // Trả về đối tượng File đã tạo
            return file
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }

    // Trả về null nếu có lỗi
    return null
}

fun convertVideoToByteArray(
    context: Context,
    videoUri: Uri?,
): ByteArray? {
    val contentResolver = context.contentResolver
    var inputStream: InputStream? = null
    return try {
        // Mở InputStream từ URI
        inputStream = contentResolver.openInputStream(videoUri!!)

        // Đọc dữ liệu từ InputStream và chuyển đổi thành mảng byte
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead)
        }
        byteArrayOutputStream.toByteArray()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

fun convertUriToBitmap(
    context: Context,
    imageUri: Uri?,
): Bitmap? {
    val contentResolver = context.contentResolver
    var inputStream: InputStream? = null
    return try {
        // Mở InputStream từ URI
        inputStream = imageUri?.let { contentResolver.openInputStream(it) }

        // Đọc dữ liệu từ InputStream và chuyển đổi thành đối tượng Bitmap
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

suspend fun getVideoFileSize(
    uri: Uri,
    context: Context,
): Long? {
    return withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        var fileSize: Long? = null
        val cursor = contentResolver.query(uri, null, null, null, null, null)

        cursor?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex != -1) {
                fileSize = cursor.getLong(sizeIndex)
            }
        }

        return@withContext fileSize
    }
}

fun bitmapFromDrawableRes(
    context: Context,
    @DrawableRes resourceId: Int,
) = convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
    if (sourceDrawable == null) {
        return null
    }
    return if (sourceDrawable is BitmapDrawable) {
        sourceDrawable.bitmap
    } else {
// copying drawable object to not manipulate on the same reference
        val constantState = sourceDrawable.constantState ?: return null
        val drawable = constantState.newDrawable().mutate()
        val bitmap: Bitmap =
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
}

fun makerNumber(
    context: Context,
    number: Int,
): Bitmap? {
    val bitmap = bitmapFromDrawableRes(context, R.drawable.ic_map_number)
    val canvas = bitmap?.let { Canvas(it) }
    val paint =
        Paint().apply {
            textSize = 24f
            color = Color.RED
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
    canvas?.drawText(
        number.toString(),
        bitmap.width.div(2f),
        bitmap.height.div(3f),
        paint,
    )
    return bitmap
}

