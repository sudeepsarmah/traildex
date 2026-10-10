package com.example.traildex.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class TrailPoint(val latitude: Double, val longitude: Double, val time: Long)
data class TrailRecord(
    val id: String,
    val name: String,
    val distanceMeters: Float,
    val startedAt: Long,
    val endedAt: Long,
    val points: List<TrailPoint>
)

/** Device-only persistence for the current walk and completed trail routes. */
object TrailHistory {
    private const val PREFS = "traildex_routes"
    private const val ACTIVE_POINTS = "active_points"
    private const val SAVED_ROUTES = "saved_routes"

    fun begin(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString("active_name", name.ifBlank { "My trail" })
            .putLong("active_started", System.currentTimeMillis())
            .putString(ACTIVE_POINTS, "[]").apply()
    }

    fun append(context: Context, latitude: Double, longitude: Double) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val points = decodePoints(prefs.getString(ACTIVE_POINTS, "[]"))
        val last = points.lastOrNull()
        if (last != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(last.latitude, last.longitude, latitude, longitude, results)
            if (results[0] < 4f || results[0] > 100f) return
        }
        points.add(TrailPoint(latitude, longitude, System.currentTimeMillis()))
        if (points.size > 2_000) points.removeAt(1)
        prefs.edit().putString(ACTIVE_POINTS, encodePoints(points).toString()).apply()
    }

    fun activePoints(context: Context): List<TrailPoint> = decodePoints(
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(ACTIVE_POINTS, "[]")
    )

    fun finish(context: Context, distanceMeters: Float): List<TrailRecord> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val points = activePoints(context)
        if (points.isNotEmpty()) {
            val record = TrailRecord(
                id = System.currentTimeMillis().toString(),
                name = prefs.getString("active_name", "My trail") ?: "My trail",
                distanceMeters = distanceMeters,
                startedAt = prefs.getLong("active_started", points.first().time),
                endedAt = System.currentTimeMillis(),
                points = points
            )
            val history = saved(context).toMutableList().apply { add(0, record) }.take(100)
            prefs.edit().putString(SAVED_ROUTES, encodeRoutes(history).toString()).putString(ACTIVE_POINTS, "[]").apply()
        }
        return saved(context)
    }

    fun saved(context: Context): List<TrailRecord> = decodeRoutes(
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(SAVED_ROUTES, "[]")
    )

    private fun encodePoints(points: List<TrailPoint>) = JSONArray().apply {
        points.forEach { put(JSONObject().put("lat", it.latitude).put("lon", it.longitude).put("time", it.time)) }
    }

    private fun decodePoints(raw: String?): MutableList<TrailPoint> = runCatching {
        val array = JSONArray(raw ?: "[]")
        MutableList(array.length()) { i -> array.getJSONObject(i).let { TrailPoint(it.getDouble("lat"), it.getDouble("lon"), it.optLong("time")) } }
    }.getOrDefault(mutableListOf())

    private fun encodeRoutes(routes: List<TrailRecord>) = JSONArray().apply {
        routes.forEach { route -> put(JSONObject().put("id", route.id).put("name", route.name)
            .put("distance", route.distanceMeters).put("start", route.startedAt).put("end", route.endedAt)
            .put("points", encodePoints(route.points))) }
    }

    private fun decodeRoutes(raw: String?): List<TrailRecord> = runCatching {
        val array = JSONArray(raw ?: "[]")
        List(array.length()) { i -> array.getJSONObject(i).let { item ->
            TrailRecord(item.getString("id"), item.optString("name", "My trail"), item.optDouble("distance", 0.0).toFloat(),
                item.optLong("start"), item.optLong("end"), decodePoints(item.optJSONArray("points")?.toString()))
        } }
    }.getOrDefault(emptyList())
}
