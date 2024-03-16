package com.example.footu.Response

import com.google.gson.annotations.SerializedName

data class DirectionsResponse(
    @SerializedName("routes")
    var routes: List<Route>? = null,
)

class Route {
    @SerializedName("legs")
    var legs: List<Leg>? = null
}

class Leg {
    @SerializedName("steps")
    var steps: List<Step>? = null
}

class Step {
    @SerializedName("start_location")
    var startLocation: Location? = null

    @SerializedName("end_location")
    var endLocation: Location? = null

    @SerializedName("polyline")
    var polyline: Polyline? = null
}

class Location {
    @SerializedName("lat")
    var lat = 0.0

    @SerializedName("lng")
    var lng = 0.0
}

class Polyline {
    @SerializedName("points")
    var points: String? = null
}
