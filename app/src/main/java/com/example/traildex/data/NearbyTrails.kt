package com.example.traildex.data

import android.content.Context
import android.location.Location
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class NearbyTrail(val id: Long, val name: String, val kind: String, val latitude: Double, val longitude: Double)

/** Small, user-triggered OpenStreetMap lookup with a one-day local cache. */
object NearbyTrails {
    private val preferences = "traildex_nearby_trails"
    private const val cacheLifetimeMs = 24 * 60 * 60 * 1000L

    fun find(context: Context, location: Location): List<NearbyTrail> {
        val key = "${"%.2f".format(java.util.Locale.US, location.latitude)}_${"%.2f".format(java.util.Locale.US, location.longitude)}"
        val prefs = context.getSharedPreferences(preferences, Context.MODE_PRIVATE)
        val cache = prefs.getString("data_$key", null)
        val cachedAt = prefs.getLong("time_$key", 0L)
        if (cache != null && System.currentTimeMillis() - cachedAt < cacheLifetimeMs) return decode(JSONArray(cache))

        val query = "[out:json][timeout:20];relation(around:8000,${location.latitude},${location.longitude})[\"route\"~\"hiking|foot\"][\"name\"];out tags center;"
        try {
            val connection = (URL("https://overpass-api.de/api/interpreter").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8_000
                readTimeout = 25_000
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                setRequestProperty("User-Agent", "TrailDexAndroid/1.0 (OpenStreetMap route discovery)")
            }
            try {
                val payload = "data=" + URLEncoder.encode(query, Charsets.UTF_8.name())
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
                val status = connection.responseCode
                val response = (if (status in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()
                check(status in 200..299) { "OpenStreetMap lookup returned HTTP $status" }
                val routes = decode(JSONObject(response).optJSONArray("elements") ?: JSONArray())
                    .sortedBy { route -> distanceMeters(location.latitude, location.longitude, route.latitude, route.longitude) }
                    .take(12)
                val encoded = JSONArray().also { array -> routes.forEach { array.put(JSONObject().put("id", it.id).put("name", it.name).put("kind", it.kind).put("lat", it.latitude).put("lon", it.longitude)) } }
                prefs.edit().putString("data_$key", encoded.toString()).putLong("time_$key", System.currentTimeMillis()).apply()
                return routes
            } finally {
                connection.disconnect()
            }
        } catch (error: Exception) {
            if (cache != null) return decode(JSONArray(cache))
            throw error
        }
    }

    private fun decode(elements: JSONArray): List<NearbyTrail> = (0 until elements.length()).mapNotNull { index ->
        val item = elements.optJSONObject(index) ?: return@mapNotNull null
        val tags = item.optJSONObject("tags") ?: return@mapNotNull null
        val center = item.optJSONObject("center") ?: item
        val id = item.optLong("id", 0L)
        val lat = center.optDouble("lat", Double.NaN)
        val lon = center.optDouble("lon", Double.NaN)
        val name = tags.optString("name").trim()
        if (id == 0L || name.isBlank() || !lat.isFinite() || !lon.isFinite()) null
        else NearbyTrail(id, name, tags.optString("route", "walking route"), lat, lon)
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0]
    }
}
