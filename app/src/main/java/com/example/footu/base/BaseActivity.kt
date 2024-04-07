package com.example.footu.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.example.footu.R
import com.example.footu.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<BINDING : ViewDataBinding> :
    AppCompatActivity() {

    lateinit var binding: BINDING
    var loadingDialog: AlertDialog? = null

    private var mLastClickTime: Long = 0

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = DataBindingUtil.setContentView(this, getContentLayout())
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        loadingDialog = setupProgressDialog()
        observerDefaultLiveData(initViewModel())
        initView()
        initListener()
        observerData()
    }

    abstract fun observerData()

    abstract fun getContentLayout(): Int

    abstract fun initView()

    abstract fun initListener()

    abstract fun initViewModel(): BaseViewModel

    open fun isDoubleClick(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return true
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return false
    }

    private fun observerDefaultLiveData(viewModel: BaseViewModel) {
        viewModel.apply {
            isLoading.observe(this@BaseActivity) {
                if (it) {
                    loadingDialog?.show()
                } else {
                    loadingDialog?.dismiss()
                }
            }
            errorMessage.observe(this@BaseActivity) {
                if (it != null) {
                    showError(it.toInt())
                }
            }
            responseMessage.observe(this@BaseActivity) {
                showError(it.toString())
            }
        }
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
//        val errorSnackbar = Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
//        errorSnackbar.setAction("", null)
//        errorSnackbar.show()
    }

    private fun showError(@StringRes id: Int) {
        val errorSnackbar = Snackbar.make(binding.root, id, Snackbar.LENGTH_LONG)
        errorSnackbar.setAction("", null)
        errorSnackbar.show()
    }

    private fun setupProgressDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setCancelable(false)

        val myLayout = LayoutInflater.from(this)
        val dialogView: View = myLayout.inflate(R.layout.fragment_progress_dialog, null)

        builder.setView(dialogView)

        val dialog: AlertDialog = builder.create()
        val window: Window? = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    fun transparentStatusbar() {
        window.statusBarColor = 0x00000000
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    fun setColorForStatusBar(color: Int) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, color)
    }

    fun setLightIconStatusBar(isLight: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isLight) {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                )
            } else {
                window.decorView.windowInsetsController?.setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = window
            val decorView = window.decorView
            val wic = WindowInsetsControllerCompat(window, decorView)
            wic.isAppearanceLightStatusBars = isLight // true or false as desired.

            // And then you can set any background color to the status bar.
//                window.statusBarColor = Color.WHITE
        }
    }
//    protected fun paddingStatusBar(view: View) {
//        view.setPadding(view.paddingStart, CommonUtils.getStatusBarHeight(this), view.paddingEnd, view.paddingBottom)
//    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun registerLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) || shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        ),
                        LOCATION_PERMISSION_REQUEST_CODE,
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                        LOCATION_PERMISSION_REQUEST_CODE,
                    )
                }
            }
        }
    }

    protected fun requestLocationEnable(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        if (intent.resolveActivity(packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Không thể mở cài đặt vị trí", Toast.LENGTH_SHORT)
                .show()
        }
    }

    protected fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        loadingDialog?.dismiss()
        binding.unbind()
        super.onDestroy()
    }
}
