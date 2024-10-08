package com.example.footu.ui.map

import android.Manifest
import android.animation.Animator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityTrackingLocationBinding
import com.example.footu.utils.LOCATION_PERMISSION_REQUEST_CODE
import com.example.footu.utils.SHIPPER_ID
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
import com.mapbox.turf.TurfMeasurement
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrackingLocationActivity : BaseActivity<ActivityTrackingLocationBinding>() {
    private val viewModel: TrackingLocationViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var point: Point
    private val locationObserver = object : LocationObserver {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            lastLocation = locationMatcherResult.enhancedLocation
        }

        override fun onNewRawLocation(rawLocation: Location) {
// no impl
        }
    }
    private var shipperLocationAnnotation: PointAnnotation? = null
    private val pointAnnotationManager by lazy {
        val annotationApi = binding.mapView.annotations
        annotationApi.createPointAnnotationManager(binding.mapView)
    }
    lateinit var pointAnnotationOptions: PointAnnotationOptions

    override fun observerData() {
        lifecycleScope.launch {
            viewModel.locationStateFlow.collect {
                if (it?.lat != null && it.long != null) {
                    addOrUpdateAnnotationToMap(it.lat, it.long)
                }
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
    }

    override fun initListener() {
    }

    private fun addOrUpdateAnnotationToMap(latitude: Double, longitude: Double) {
// Create an instance of the Annotation API and get the PointAnnotationManager.
        if (shipperLocationAnnotation == null) {
            bitmapFromDrawableRes(
                this,
                R.drawable.shipper,
            )?.let {
                pointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(longitude, latitude))
                    .withIconImage(it)

                shipperLocationAnnotation = pointAnnotationManager.create(pointAnnotationOptions)

                val cameraBoundsOptions = CameraBoundsOptions.Builder()
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
            shipperLocationAnnotation!!.point = Point.fromLngLat(longitude, latitude)
            pointAnnotationManager.update(shipperLocationAnnotation!!)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
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
                    R.drawable.ic_user_map,
                )?.let {
                    val pointAnnotation = PointAnnotationOptions().withPoint(
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

    private fun createPointAnimator(curretPosition: Point, targetPosition: Point): Animator {
        val pointEvaluator = TypeEvaluator<Point> { fraction, startValue, endValue ->
            Point.fromLngLat(
                startValue.longitude() + ((endValue.longitude() - startValue.longitude() * fraction)),
                startValue.latitude() + ((endValue.latitude() - startValue.latitude()) * fraction),
            )
        }
        return ValueAnimator.ofObject(pointEvaluator, curretPosition, targetPosition).apply {
            duration = TurfMeasurement.distance(curretPosition, targetPosition, "meters").toLong()
            interpolator = LinearInterpolator()

//            addListener(object : AnimatorListenerAdapter() {
//                override fun onAnimationEnd(animation: Animator) {
//                    super.onAnimationEnd(animation)
//                    animate()
//                }
//            })
//
//            addUpdateListener { animation ->
//                (animation.animatedValue as? Point)?.let {
//                    markerLinePointList.add(it)
//                    pointSource.geometry(it)
//                    if (++count > 1) {
//                        lineSource.geometry(LineString.fromLngLats(markerLinePointList))
//                    }
//                }
//            }
        }
    }

    override fun onStart() {
        super.onStart()
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
                            val pointAnnotation = PointAnnotationOptions().withPoint(
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

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
