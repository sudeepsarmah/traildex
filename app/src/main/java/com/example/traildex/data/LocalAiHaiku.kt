package com.example.traildex.data

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class VisionNote(val species: String, val category: String, val haiku: String)

/** Connects to a user-owned Ollama server on the local network. */
object LocalAiHaiku {
    val defaultEndpoint: String
        get() = if (com.example.traildex.BuildConfig.DEBUG) "http://10.0.2.2:11434" else ""
    const val defaultModel = "gemma3:4b"

    fun checkConnection(endpoint: String, model: String = defaultModel): String {
        val response = request("${endpoint.trimEnd('/')}/api/tags", "GET")
        val models = JSONObject(response).optJSONArray("models") ?: JSONArray()
        val available = (0 until models.length()).mapNotNull { models.optJSONObject(it)?.optString("name") }
        return if (available.any { it == model || it.startsWith("$model:") }) {
            "CONNECTED • $model READY"
        } else {
            "OLLAMA CONNECTED • MODEL MISSING. RUN: ollama pull $model"
        }
    }

    fun generate(species: String, place: String, endpoint: String): String {
        val prompt = "Write one original nature haiku about $species observed at $place. " +
            "Use exactly three lines, no title, and avoid factual claims not given by the user."
        val result = generateRequest(endpoint, prompt)
        return result.trim().trim('"')
    }

    fun identifyImage(image: ByteArray, place: String, endpoint: String): VisionNote {
        val prompt = "Look at this nature observation photo. Identify the most likely visible organism or object, " +
            "but say Uncertain observation if you cannot tell. Do not invent a precise species. " +
            "Respond only as JSON with keys species, category, haiku. The haiku must be three short original lines " +
            "inspired by the visible image and habitat $place."
        val connection = openConnection("${endpoint.trimEnd('/')}/api/generate", "POST").apply {
            readTimeout = 180_000
        }
        try {
            val body = JSONObject()
                .put("model", defaultModel)
                .put("prompt", prompt)
                .put("images", JSONArray().put(Base64.encodeToString(image, Base64.NO_WRAP)))
                .put("stream", false)
                .put("format", "json")
                .toString()
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val text = readResponse(connection, status)
            check(status in 200..299) { "Ollama returned HTTP $status: $text" }
            val response = JSONObject(text).getString("response")
            val first = response.indexOf('{')
            val last = response.lastIndexOf('}')
            check(first >= 0 && last > first) { "The vision model returned an unreadable result. Try again or enter the observation manually." }
            val result = JSONObject(response.substring(first, last + 1))
            return VisionNote(
                species = result.optString("species").ifBlank { "Uncertain observation" }.take(48),
                category = result.optString("category").ifBlank { "Nature" }.take(32),
                haiku = result.optString("haiku").ifBlank { OfflineHaiku.generate(result.optString("species"), place) }
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun generateRequest(endpoint: String, prompt: String): String {
        val connection = openConnection("${endpoint.trimEnd('/')}/api/generate", "POST").apply {
            readTimeout = 120_000
        }
        try {
            val body = JSONObject()
                .put("model", defaultModel)
                .put("prompt", prompt)
                .put("stream", false)
                .put("options", JSONObject().put("temperature", 0.7))
                .toString()
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val response = readResponse(connection, status)
            check(status in 200..299) { "Ollama returned HTTP $status: $response" }
            return JSONObject(response).getString("response")
        } finally {
            connection.disconnect()
        }
    }

    private fun request(url: String, method: String): String {
        val connection = openConnection(url, method)
        try {
            val status = connection.responseCode
            val response = readResponse(connection, status)
            check(status in 200..299) { "HTTP $status: $response" }
            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(url: String, method: String) =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5_000
            readTimeout = 30_000
            if (method == "POST") {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }

    private fun readResponse(connection: HttpURLConnection, status: Int): String {
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }
}
