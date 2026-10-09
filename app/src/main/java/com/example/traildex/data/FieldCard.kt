package com.example.traildex.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import android.net.Uri

data class FieldCard(
    val id: String,
    val name: String,
    val habitat: String,
    val haiku: String,
    val hp: Int,
    val power: Int,
    val icon: String,
    val createdAt: Long,
    val category: String = "Nature",
    val photoUri: String? = null
)

object FieldCardStore {
    private const val preferences = "traildex_collection"
    private const val cardsKey = "cards"

    fun load(context: Context): List<FieldCard> {
        val raw = context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getString(cardsKey, "[]") ?: "[]"
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { index ->
                val item = json.getJSONObject(index)
                FieldCard(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    habitat = item.optString("habitat", "Unrecorded trail"),
                    haiku = item.optString("haiku", ""),
                    hp = item.optInt("hp", 80),
                    power = item.optInt("power", 25),
                    icon = item.optString("icon", "🌿"),
                    createdAt = item.optLong("createdAt", 0L),
                    category = item.optString("category", "Nature"),
                    photoUri = item.optString("photoUri").takeIf(String::isNotBlank)
                )
            }
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, card: FieldCard) {
        val cards = (load(context) + card).takeLast(100)
        val json = JSONArray()
        cards.forEach { item ->
            json.put(JSONObject()
                .put("id", item.id)
                .put("name", item.name)
                .put("habitat", item.habitat)
                .put("haiku", item.haiku)
                .put("hp", item.hp)
                .put("power", item.power)
                .put("icon", item.icon)
                .put("createdAt", item.createdAt)
                .put("category", item.category)
                .put("photoUri", item.photoUri ?: ""))
        }
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE)
            .edit().putString(cardsKey, json.toString()).apply()
    }
}
