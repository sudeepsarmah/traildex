package com.example.traildex.data

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale

data class WalkingRoute(
    val points: List<TrailPoint>,
    val distanceMeters: Double,
    val durationSeconds: Double
)

/** Requests one walk route from the public FOSSGIS OSRM foot profile. */
fun findWalkingRoute(start: TrailPoint, finish: TrailPoint): WalkingRoute {
    val coordinates = String.format(
        Locale.US,
        "%.6f,%.6f;%.6f,%.6f",
        start.longitude, start.latitude, finish.longitude, finish.latitude
    )
    val url = URL("https://routing.openstreetmap.de/routed-foot/route/v1/foot/$coordinates?overview=full&geometries=geojson&steps=false")
    val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15_000
        readTimeout = 25_000
        setRequestProperty("User-Agent", "TrailDexAndroid/1.2 (https://github.com/sudeepsarmah/traildex)")
        setRequestProperty("Accept", "application/json")
    }

    try {
        val status = connection.responseCode
        val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
            ?.bufferedReader(StandardCharsets.UTF_8)
            ?.use { it.readText() }
            .orEmpty()
        if (status !in 200..299) {
            throw IOException("Walking route service returned HTTP $status")
        }

        val response = JSONObject(body)
        if (response.optString("code") != "Ok") {
            throw IOException(response.optString("message").ifBlank { "No walkable route found between those points." })
        }
        val route = response.optJSONArray("routes")?.optJSONObject(0)
            ?: throw IOException("No walkable route found between those points.")
        val coordinatesArray = route.optJSONObject("geometry")?.optJSONArray("coordinates")
            ?: throw IOException("The routing service returned no route geometry.")
        val points = buildList {
            for (index in 0 until coordinatesArray.length()) {
                val coordinate = coordinatesArray.optJSONArray(index) ?: continue
                if (coordinate.length() < 2) continue
                add(TrailPoint(coordinate.getDouble(1), coordinate.getDouble(0), 0L))
            }
        }
        if (points.size < 2) throw IOException("The routing service returned an incomplete route.")

        return WalkingRoute(
            points = points,
            distanceMeters = route.optDouble("distance", 0.0),
            durationSeconds = route.optDouble("duration", 0.0)
        )
    } finally {
        connection.disconnect()
    }
}
