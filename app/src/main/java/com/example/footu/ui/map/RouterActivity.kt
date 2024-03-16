package com.example.footu.ui.map

import android.location.Location
import android.os.Build
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.MapboxActivityTurnByTurnExperienceBinding
import com.example.footu.model.OrderShipModel
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.dropin.map.MapViewObserver
import com.mapbox.navigation.ui.utils.internal.ifNonNull
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RouterActivity :
    BaseActivity<MapboxActivityTurnByTurnExperienceBinding>(),
    OnMapLongClickListener,
    NavigationRouterCallback {
    private val viewModel: RouterViewModel by viewModels()
    private var lastLocation: Location? = null
    private val destination by lazy {
        val orderDetail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("item", OrderShipModel::class.java)
        } else {
            intent.getParcelableExtra("item")
        }
        Point.fromLngLat(orderDetail?.longitude!!, orderDetail.lat!!)
    }

    private val locationObserver = object : LocationObserver {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            lastLocation = locationMatcherResult.enhancedLocation
            viewModel.handleEvent(RouterViewModel.Event.Position(lastLocation))
        }

        override fun onNewRawLocation(rawLocation: Location) {
// no impl
        }
    }

    private val mapViewObserver = object : MapViewObserver() {
        override fun onAttached(mapView: com.mapbox.maps.MapView) {
            mapView.gestures.addOnMapLongClickListener(
                this@RouterActivity,
            )
        }

        override fun onDetached(mapView: com.mapbox.maps.MapView) {
            mapView.gestures.removeOnMapLongClickListener(
                this@RouterActivity,
            )
        }
    }

    override fun observerData() {
        lifecycleScope.launch {
            viewModel.locationStateFlow.take(2).collect {
                it?.let {
                    val origin = Point.fromLngLat(it.longitude, it.latitude)
                    requestRoutes(
                        origin,
                        destination,
                    )
                }
            }
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.mapbox_activity_turn_by_turn_experience
    }

    override fun initView() {
        binding.navigationView.registerMapObserver(mapViewObserver)
    }

    override fun initListener() {
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onMapLongClick(point: Point): Boolean {
        ifNonNull(lastLocation) {
            requestRoutes(Point.fromLngLat(it.longitude, it.latitude), point)
        }
        return false
    }

    private fun requestRoutes(origin: Point, destination: Point) {
        MapboxNavigationApp.current()?.requestRoutes(
            routeOptions = RouteOptions
                .builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(listOf(origin, destination))
                .alternatives(true)
                .build(),
            this,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.navigationView.api.routeReplayEnabled(false)
        binding.navigationView.unregisterMapObserver(mapViewObserver)
        MapboxNavigationApp.current()?.setNavigationRoutes(emptyList())
        MapboxNavigationApp.current()?.unregisterLocationObserver(locationObserver)
    }

    override fun onPause() {
        super.onPause()
        MapboxNavigationApp.current()?.unregisterLocationObserver(locationObserver)
    }

    override fun onResume() {
        super.onResume()
        MapboxNavigationApp.current()?.registerLocationObserver(locationObserver)
    }

    override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
    }

    override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {
    }

    override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: RouterOrigin) {
        binding.navigationView.api.routeReplayEnabled(true)
        binding.navigationView.api.startRoutePreview(routes)
    }
}
