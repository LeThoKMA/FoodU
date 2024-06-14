package com.example.footu.ui.map

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ItemViewPointBinding
import com.example.footu.databinding.MapboxActivityTurnByTurnExperienceBinding
import com.example.footu.model.OrderShipModel
import com.example.footu.model.PointWithId
import com.example.footu.ui.map.MultipleRouterViewModel.UiState
import com.example.footu.ui.map.dialog.DetailBillBottomSheetDialog
import com.example.footu.utils.bitmapFromDrawableRes
import com.example.footu.utils.makerNumber
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.dropin.map.MapViewObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MultipleRouterActivity :
    BaseActivity<MapboxActivityTurnByTurnExperienceBinding>(),
    NavigationRouterCallback {
    private val viewModel: MultipleRouterViewModel by viewModels()
    private val viewAnnotations =
        hashMapOf<PointWithId, Pair<ItemViewPointBinding, ScreenCoordinate>?>()
    private var mapView: MapView? = null
    private val pointAnnotationManager by lazy {
        mapView?.annotations?.createPointAnnotationManager()?.apply {
            addClickListener(
                OnPointAnnotationClickListener { pointAnnotation ->
                    // Chuyển đổi tọa độ thành vị trí pixel trên màn hình
                    val screenCoordinate =
                        mapView?.getMapboxMap()?.pixelForCoordinate(pointAnnotation.point)
                    val pointWithId =
                        viewAnnotations.keys.find { it.point == pointAnnotation.point }
                    pointWithId?.id?.let {
                        viewModel.handleEvent(MultipleRouterViewModel.Event.DetailBill(it))
                    }
                    false
                },
            )
        }
    }
    private var lastLocation: Location? = null

    private val locationObserver =
        object : LocationObserver {
            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                lastLocation = locationMatcherResult.enhancedLocation
                viewModel.handleEvent(MultipleRouterViewModel.Event.Position(lastLocation))
            }

            override fun onNewRawLocation(rawLocation: Location) {
// no impl
            }
        }

    private val mapViewObserver =
        object : MapViewObserver() {
            override fun onAttached(mapView: MapView) {
                super.onAttached(mapView)
                this@MultipleRouterActivity.mapView = mapView
            }

            override fun onDetached(mapView: MapView) {
                super.onDetached(mapView)
                this@MultipleRouterActivity.mapView = null
            }
        }

    override fun observerData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::handleUiState)
            }
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.mapbox_activity_turn_by_turn_experience
    }

    override fun initView() {
        binding.navigationView.registerMapObserver(mapViewObserver)
        MapboxNavigationApp.current()?.registerLocationObserver(locationObserver)
    }

    override fun initListener() {
        binding.imvBack.setOnClickListener {
            finish()
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    private fun requestRoutes(points: List<PointWithId>) {
//        var randoms = points.subList(1, points.size - 1).shuffled()
//        randoms = randoms.toMutableList()
//        randoms.add(0, points[0])
//        randoms.add(points[0])
//        randoms.forEachIndexed { index, point ->
//            if (index != points.lastIndex && index != 0) {
//                addMakerWithPoint(point.point, index)
//                viewAnnotations[point] = null
//            }
//        }
        points.forEachIndexed { index, point ->
            if (index != points.lastIndex && index != 0) {
                addMakerWithPoint(point.point, index)
                viewAnnotations[point] = null
            }
        }
        val listPoint = points.map { it.point }
        MapboxNavigationApp.current()?.requestRoutes(
            routeOptions =
                RouteOptions
                    .builder()
                    .profile(DirectionsCriteria.PROFILE_DRIVING)
                    .applyDefaultNavigationOptions()
                    .coordinatesList(listPoint)
                    .alternatives(true)
                    .build(),
            this,
        )
    }

    private fun handleUiState(uiState: UiState) {
        when (uiState) {
            is UiState.Position -> {
                uiState.location?.let {
                    //     showLocationInMap(it)
                }
            }

            is UiState.Routers -> {
                requestRoutes(uiState.routers)
            }

            is UiState.DetailBill -> {
                uiState.data?.let { showDialogDetailBill(it) }
            }

            else -> {}
        }
    }

    private fun showDialogDetailBill(orderShipModel: OrderShipModel) {
        val dialog =
            DetailBillBottomSheetDialog.newInstance(
                orderShipModel,
                onClickCall = {
                    if (orderShipModel.customer?.phone != null) {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:${orderShipModel.customer.phone}")
                        startActivity(intent)
                    } else {
                        // TODO add zegoCloud call
                    }
                },
                onClickChat = {},
            )
        dialog.show(supportFragmentManager, DetailBillBottomSheetDialog.TAG)
    }

    private fun showLocationInMap(location: Location) {
        bitmapFromDrawableRes(
            this,
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
            pointAnnotationManager?.create(pointAnnotation)
        }
        mapView?.getMapboxMap()?.setCamera(
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
    }

    private fun addMakerWithPoint(
        point: Point,
        number: Int,
    ) {
        makerNumber(
            this,
            number = number,
        )?.let {
            val pointAnnotation =
                PointAnnotationOptions().withPoint(
                    point,
                )
                    .withIconImage(it)
            pointAnnotationManager?.create(pointAnnotation)
        }
    }

    override fun onDestroy() {
        binding.navigationView.unregisterMapObserver(mapViewObserver)
        binding.navigationView.api.routeReplayEnabled(false)
        MapboxNavigationApp.current()?.setNavigationRoutes(emptyList())
        MapboxNavigationApp.current()?.unregisterLocationObserver(locationObserver)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        MapboxNavigationApp.current()?.unregisterLocationObserver(locationObserver)
    }

    override fun onResume() {
        super.onResume()
        MapboxNavigationApp.current()?.registerLocationObserver(locationObserver)
    }

    override fun onCanceled(
        routeOptions: RouteOptions,
        routerOrigin: RouterOrigin,
    ) {
    }

    override fun onFailure(
        reasons: List<RouterFailure>,
        routeOptions: RouteOptions,
    ) {
    }

    @SuppressLint("MissingPermission")
    override fun onRoutesReady(
        routes: List<NavigationRoute>,
        routerOrigin: RouterOrigin,
    ) {
        binding.navigationView.api.routeReplayEnabled(true)
        binding.navigationView.api.startRoutePreview(routes)
        MapboxNavigationApp.current()?.startTripSession(true)
    }
}
