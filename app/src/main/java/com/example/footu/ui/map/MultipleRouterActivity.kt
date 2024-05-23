package com.example.footu.ui.map

import android.location.Location
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.MapboxActivityTurnByTurnExperienceBinding
import com.example.footu.ui.map.MultipleRouterViewModel.UiState
import com.example.footu.utils.bitmapFromDrawableRes
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.annotation.annotations
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MultipleRouterActivity :
    BaseActivity<MapboxActivityTurnByTurnExperienceBinding>(),
    NavigationRouterCallback {
    private val viewModel: MultipleRouterViewModel by viewModels()
    private var mapView: MapView? = null
    private val pointAnnotationManager by lazy {
        mapView?.annotations?.createPointAnnotationManager()
    }
    private lateinit var pointAnnotationOptions: PointAnnotationOptions
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

    override fun observerData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::handleUiState)
            }
        }
    }

    private fun handleUiState(uiState: UiState) {
        when (uiState) {
            is UiState.Position -> {
                uiState.location?.let {
                    showLocationInMap(it)
                }
            }

            is UiState.Routers -> {
                requestRoutes(uiState.routers.values.toList())
            }
        }
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

    override fun getContentLayout(): Int {
        return R.layout.mapbox_activity_turn_by_turn_experience
    }

    override fun initView() {
        binding.navigationView.customizeViewBinders {
            this.mapViewBinder?.getMapView(this@MultipleRouterActivity)?.apply {
                mapView = this
                this.getMapboxMap().apply {
                    loadStyleUri(
                        Style.MAPBOX_STREETS,
                    )
                }
            }
        }
    }

    override fun initListener() {
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    private fun requestRoutes(points: List<Point>) {
        MapboxNavigationApp.current()?.requestRoutes(
            routeOptions =
                RouteOptions
                    .builder()
                    .applyDefaultNavigationOptions()
                    .coordinatesList(points)
                    .build(),
            this,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.navigationView.api.routeReplayEnabled(false)
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

    override fun onRoutesReady(
        routes: List<NavigationRoute>,
        routerOrigin: RouterOrigin,
    ) {
        binding.navigationView.api.routeReplayEnabled(true)
        binding.navigationView.api.startRoutePreview(routes)
    }
}
