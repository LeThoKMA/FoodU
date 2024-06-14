package com.example.footu.ui.map

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityTrackingLocationBinding
import com.example.footu.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.example.footu.utils.SHIPPER_ID
import com.example.footu.utils.bitmapFromDrawableRes
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrackingLocationActivity :
    BaseActivity<ActivityTrackingLocationBinding>() {
    private val viewModel: TrackingLocationViewModel by viewModels()
    private var animator: ValueAnimator? = null
    private var listPoint = mutableListOf<Point>()
    private var animateDuration = 9000L
    private val speed = 11 // m/s
    private var isAnimate = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var point: Point
    private val locationObserver =
        object : LocationObserver {
            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                lastLocation = locationMatcherResult.enhancedLocation
            }

            override fun onNewRawLocation(rawLocation: Location) {
// no impl
            }
        }
    private var shipperLocationAnnotation: PointAnnotation? = null
    private val pointAnnotationManager by lazy {
        binding.mapView.annotations.createPointAnnotationManager()
    }
    private lateinit var pointAnnotationOptions: PointAnnotationOptions
    private val dialog by lazy { setupProgressDialog() }

    override fun observerData() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.locationStateFlow.collectLatest {
                if (it?.lat != null && it.long != null) {
                    listPoint.add(Point.fromLngLat(it.long, it.lat))
                    if (!isAnimate) {
                        addOrUpdateAnnotationToMap(it.lat, it.long)
                    }
                }
                dialog.dismiss()
            }
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_tracking_location
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView() {
        if (!isLocationEnabled()) {
            requestLocationEnable(this)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()
        viewModel.startFollowingShipper(intent.getIntExtra(SHIPPER_ID, -1))
        MapboxNavigationApp.current()?.registerLocationObserver(locationObserver)
        binding.mapView.getMapboxMap().apply {
            loadStyleUri(
                Style.MAPBOX_STREETS,
            )
        }
        dialog.show()
    }

    override fun initListener() {
    }

    private fun addOrUpdateAnnotationToMap(
        latitude: Double,
        longitude: Double,
    ) {
// Create an instance of the Annotation API and get the PointAnnotationManager.
        if (shipperLocationAnnotation == null) {
            bitmapFromDrawableRes(
                this,
                R.drawable.point,
            )?.let {
                pointAnnotationOptions =
                    PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(longitude, latitude))
                        .withIconImage(it)

                shipperLocationAnnotation = pointAnnotationManager.create(pointAnnotationOptions)

                val cameraBoundsOptions =
                    CameraBoundsOptions.Builder()
                        .bounds(
                            CoordinateBounds(
                                point,
                                shipperLocationAnnotation!!.point,
                                true,
                            ),
                        )
                        .minZoom(12.0)
                        .build()
// Fit camera to the bounding box
                binding.mapView.getMapboxMap().setBounds(cameraBoundsOptions)
                //    binding.mapView.getMapboxMap().setCamera()
            }
        } else {
            animateCars(
                shipperLocationAnnotation!!.point,
                Point.fromLngLat(longitude, latitude),
            )
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Kiểm tra và yêu cầu quyền truy cập vị trí nếu cần thiết
            registerLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            // Lấy vị trí thành công
            if (location != null) {
                point = Point.fromLngLat(location.longitude, location.latitude)
                bitmapFromDrawableRes(
                    this@TrackingLocationActivity,
                    R.drawable.icon_map_small,
                )?.let {
                    val pointAnnotation =
                        PointAnnotationOptions().withPoint(
                            Point.fromLngLat(
                                location.longitude,
                                location.latitude,
                            ),
                        ).withIconImage(
                            it,
                        )
                    pointAnnotationManager.create(pointAnnotation)
                }
                binding.mapView.getMapboxMap().setCamera(
                    cameraOptions {
                        center(
                            Point.fromLngLat(
                                location.longitude,
                                location.latitude,
                            ),
                        )
                        zoom(12.0)
                    },
                )
            } else {
                // Không thể lấy vị trí hiện tại
                Log.e(">>>>>", "Không thể lấy vị trí hiện tại.")
            }
        }.addOnFailureListener { e ->
            // Xảy ra lỗi khi lấy vị trí hiện tại
            Log.e(">>>>>", "Lỗi khi lấy vị trí hiện tại: ${e.message}")
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    private fun cleanAnimation() {
        animator?.apply {
            removeAllListeners()
            cancel()
        }
    }

    private fun animateCars(
        oldPoint: Point,
        nextPoint: Point,
    ) {
        cleanAnimation()
        isAnimate = true
        animator =
            ValueAnimator.ofObject(
                TypeEvaluator<Point> { fraction, startValue, endValue ->
                    Point.fromLngLat(
                        startValue.longitude() + (endValue.longitude() - startValue.longitude()) * fraction,
                        startValue.latitude() + (endValue.latitude() - startValue.latitude()) * fraction,
                    )
                },
                oldPoint,
                nextPoint,
            ).apply {
//                val distance = calculateDistance(oldPoint, nextPoint)
//
//                // Tính thời gian animation dựa trên khoảng cách và tốc độ
//                duration = (distance / speed * 1000).toLong() // chuyển sang milliseconds
                duration = animateDuration
                interpolator = LinearInterpolator()
                addUpdateListener { valueAnimator ->
                    (valueAnimator.animatedValue as Point).let {
                        shipperLocationAnnotation?.point = it
                    }
                    if (valueAnimator.animatedFraction >= 0.9f && listPoint.size >= 2) {
                        val currentPoint = shipperLocationAnnotation?.point ?: nextPoint
                        val point = listPoint[1]
                        listPoint.removeAt(0)
                        animateCars(currentPoint, point)
                    }
                }
                start()
                addUpdateListener {
                    pointAnnotationManager.update(shipperLocationAnnotation!!)
                }
                addListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
//                            if (listPoint.size >= 2) {
//                                val point = listPoint[1]
//                                listPoint.removeAt(0)
//                                animateCars(shipperLocationAnnotation?.point!!, point)
//                            } else {
                            isAnimate = false
                            // }
                        }
                    },
                )
            }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            var allPermissionsGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    // Lấy vị trí thành công
                    if (location != null) {
                        point = Point.fromLngLat(location.longitude, location.latitude)
                        bitmapFromDrawableRes(
                            this@TrackingLocationActivity,
                            R.drawable.ic_user_map,
                        )?.let {
                            val pointAnnotation =
                                PointAnnotationOptions().withPoint(
                                    Point.fromLngLat(
                                        location.longitude,
                                        location.latitude,
                                    ),
                                ).withIconImage(
                                    it,
                                )
                            pointAnnotationManager.create(pointAnnotation)
                        }
                        binding.mapView.getMapboxMap().setCamera(
                            cameraOptions {
                                center(
                                    Point.fromLngLat(
                                        location.longitude,
                                        location.latitude,
                                    ),
                                )
                                zoom(12.0)
                            },
                        )
                    } else {
                        // Không thể lấy vị trí hiện tại
                        Log.e(">>>>>", "Không thể lấy vị trí hiện tại.")
                    }
                }.addOnFailureListener { e ->
                    // Xảy ra lỗi khi lấy vị trí hiện tại
                    Log.e(">>>>>", "Lỗi khi lấy vị trí hiện tại: ${e.message}")
                }
            } else {
                // Một hoặc nhiều quyền đã bị từ chối
                // Xử lý tương ứng, ví dụ: hiển thị thông báo rằng quyền không được cấp
            }
        }
    }

    override fun onDestroy() {
        MapboxNavigationApp.current()?.unregisterLocationObserver(locationObserver)
        cleanAnimation()
        super.onDestroy()
    }

    private fun setupProgressDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setCancelable(false)

        val myLayout = LayoutInflater.from(this)
        val dialogView: View = myLayout.inflate(R.layout.fragment_progress_dialog, null)
        val textView = dialogView.findViewById<TextView>(R.id.title)
        textView.text = "Tìm kiếm vị trị shipper"
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
}
