package com.example.footu.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

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
