package com.example.traildex.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traildex.data.FieldCard
import com.example.traildex.theme.*
import com.example.traildex.ui.components.*

@Composable
fun ScoutScreen(
    cards: List<FieldCard> = emptyList(),
    trailDistanceKm: Float = 0f,
    duelWins: Int = 0,
    duelLosses: Int = 0,
    onNavigateToTrail: () -> Unit = {},
    onNavigateToCards: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val level = 1 + ((trailDistanceKm / 5f).toInt() + cards.size / 3).coerceAtMost(49)
    val duelCount = duelWins + duelLosses
    val winRate = if (duelCount == 0) 0 else duelWins * 100 / duelCount
    val nextGoal = 5f
    val progress = (trailDistanceKm / nextGoal).coerceIn(0f, 1f)
    val badges = listOf(
        Triple("MOSS WALKER", "Walk 5 km", "🍃") to (trailDistanceKm >= 5f),
        Triple("FIELD NATURALIST", "Save 3 cards", "📖") to (cards.size >= 3),
        Triple("FIRST VICTORY", "Win a practice duel", "⚔") to (duelWins > 0),
        Triple("TRAIL REGULAR", "Walk 20 km", "🥾") to (trailDistanceKm >= 20f),
        Triple("NATURE COLLECTOR", "Save 10 field cards", "🪶") to (cards.size >= 10),
        Triple("WILD PATHFINDER", "Walk 50 km", "🧭") to (trailDistanceKm >= 50f),
        Triple("DUELIST", "Win 10 practice battles", "🏆") to (duelWins >= 10),
        Triple("FIELD ARCHIVIST", "Save 25 field cards", "🗂") to (cards.size >= 25)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize().background(SurfaceCream).padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("WILDDEx SCOUT ID", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
                        Text("LOCAL PROFILE", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PistachioGreen)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(54.dp).border(2.dp, CharcoalOutline, RoundedCornerShape(6.dp)).background(Color(0xFFFFEEB2), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                            Text("🥾", fontSize = 28.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("TRAIL SCOUT", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CharcoalText)
                            Text("Level $level • Your field journal", fontSize = 10.sp, color = CharcoalMuted)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("TRAIL DIST.", "${"%.2f".format(trailDistanceKm)} km", "GPS logged", Modifier.weight(1f))
                        StatBox("FIELD CARDS", "${cards.size}", "Tap to view", Modifier.weight(1f).clickable(onClick = onNavigateToCards))
                        StatBox("PRACTICE", "${duelWins}W · ${duelLosses}L", "$winRate% wins", Modifier.weight(1f), WarmBerry)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Nothing is shared with a league server. Your profile stays on this device.", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                    Spacer(Modifier.height(8.dp))
                    RetroButton(
                        text = "SHARE SCOUT SUMMARY",
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SurfaceContainer,
                        onClick = {
                            val text = "TrailDex Scout • Level $level\n${"%.2f".format(trailDistanceKm)} km walked • ${cards.size} field cards • $duelWins wins / $duelLosses losses"
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }, "Share scout summary"))
                        }
                    )
                }
            }
        }

        item {
            RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("NEXT TRAIL MILESTONE", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
                    Text("Walk 5 km to earn Moss Walker", fontSize = 12.sp, color = CharcoalText)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${"%.2f".format(trailDistanceKm.coerceAtMost(nextGoal))} / 5.00 km", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = CharcoalText)
                        Text("${(progress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PistachioGreen)
                    }
                    RetroSegmentedBar(totalSegments = 10, filledSegments = (progress * 10).toInt(), modifier = Modifier.fillMaxWidth())
                    RetroButton(text = "OPEN TRAIL TRACKER", modifier = Modifier.fillMaxWidth(), onClick = onNavigateToTrail)
                }
            }
        }

        item {
            RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FIELD BADGES", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
                    badges.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (badge, unlocked) ->
                                Column(Modifier.weight(1f).border(1.dp, CharcoalOutline.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).background(if (unlocked) SoftTealLight else SurfaceCream, RoundedCornerShape(4.dp)).padding(8.dp)) {
                                    Text("${badge.third} ${badge.first}", fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
                                    Text(badge.second, fontSize = 9.sp, color = CharcoalMuted)
                                    Text(if (unlocked) "UNLOCKED" else "LOCKED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (unlocked) PistachioGreen else WarmBerry)
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Text("YOUR FIELD JOURNAL • ${cards.size} ENTRIES", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
        }
        if (cards.isEmpty()) {
            item {
                RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                    Text("No observations saved yet. Add a species or nature find in Cards; it will appear here.", fontSize = 11.sp, color = CharcoalMuted)
                }
            }
        } else {
            items(cards.reversed(), key = { it.id }) { card ->
                RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite, shadowOffset = 2.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (card.photoUri != null) FieldPhoto(card.photoUri, Modifier.size(48.dp)) else Text(card.icon, fontSize = 26.sp)
                        Column {
                            Text(card.name, fontSize = 11.sp, fontWeight = FontWeight.Black, color = CharcoalText)
                            Text(card.habitat, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                            Text(card.haiku, fontSize = 10.sp, color = CharcoalText, lineHeight = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, subtitle: String, modifier: Modifier = Modifier, valueColor: Color = CharcoalText) {
    Column(modifier.border(1.dp, CharcoalOutline, RoundedCornerShape(4.dp)).background(SurfaceCream, RoundedCornerShape(4.dp)).padding(6.dp)) {
        Text(title, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = valueColor)
        Text(subtitle, fontSize = 8.sp, color = CharcoalMuted)
    }
}
