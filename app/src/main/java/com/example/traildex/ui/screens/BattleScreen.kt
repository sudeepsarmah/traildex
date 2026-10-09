package com.example.traildex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traildex.theme.*
import com.example.traildex.ui.components.*
import com.example.traildex.data.FieldCard

@Composable
fun BattleScreen(
    cards: List<FieldCard> = emptyList(),
    onBattleComplete: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rivalMaxHp = 90
    var selectedCard by remember(cards) { mutableStateOf(cards.firstOrNull()) }
    val playerMaxHp = selectedCard?.hp ?: 110
    var turn by remember(selectedCard?.id) { mutableIntStateOf(1) }
    var rivalHp by remember(selectedCard?.id) { mutableIntStateOf(rivalMaxHp) }
    var playerHp by remember(selectedCard?.id) { mutableIntStateOf(playerMaxHp) }
    var sapCount by remember { mutableIntStateOf(2) }
    var battleFinished by remember(selectedCard?.id) { mutableStateOf(false) }
    var battleLog by remember {
        mutableStateOf("Choose a field card to begin a local practice duel.")
    }
    fun attack(name: String, damage: Int) {
        if (battleFinished) return
        val card = selectedCard
        if (card == null) {
            battleLog = "No field cards yet. Save an observation in Cards before battling."
            return
        }
        rivalHp = (rivalHp - damage).coerceAtLeast(0)
        turn++
        if (rivalHp == 0) {
            battleFinished = true
            battleLog = "${card.name} wins! $name dealt $damage damage."
            onBattleComplete(true)
        } else {
            playerHp = (playerHp - 12).coerceAtLeast(0)
            if (playerHp == 0) {
                battleFinished = true
                battleLog = "The Trailkeeper bot wins this round. Try another card."
                onBattleComplete(false)
            } else {
                battleLog = "${card.name} used $name for $damage damage. The bot counterattacked for 12."
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCream)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite, shadowOffset = 2.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("CHOOSE YOUR BATTLE CARD", fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
                        Text("${cards.size} AVAILABLE", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                    }
                    if (cards.isEmpty()) {
                        Text("Save a field card from Cards to build your deck.", fontSize = 10.sp, color = CharcoalMuted)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(cards, key = { it.id }) { card ->
                                val selected = card.id == selectedCard?.id
                                Row(
                                    modifier = Modifier
                                        .border(2.dp, if (selected) PistachioGreen else CharcoalOutline, RoundedCornerShape(4.dp))
                                        .background(if (selected) SoftTealLight else SurfaceCream, RoundedCornerShape(4.dp))
                                        .clickable {
                                            selectedCard = card
                                            battleLog = "${card.name} selected. A fresh practice duel is ready."
                                        }
                                        .padding(horizontal = 9.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (card.photoUri != null) FieldPhoto(card.photoUri, Modifier.size(34.dp)) else Text(card.icon, fontSize = 17.sp)
                                    Column {
                                        Text(card.name.take(14), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                                        Text("HP ${card.hp} · POW ${card.power}", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. Status Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CharcoalOutline, RoundedCornerShape(4.dp))
                    .background(SurfaceWhite, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(PistachioGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LOCAL PRACTICE BOT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OFFLINE",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CharcoalMuted
                    )
                }

                Text(
                    text = "TURN %02d".format(turn),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = CharcoalText
                )

                Text(
                    text = "PRACTICE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = PistachioGreen
                )
            }
        }

        // 2. Battle Arena Canvas
        item {
            RetroCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFF9FBF7),
                shadowOffset = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                ) {
                    // Central Field Clash Circle
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.Center)
                            .border(1.dp, Color(0xFFD0D7C9), CircleShape)
                            .background(Color(0xFFE8EFE3).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FIELD CLASH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFA5B29B)
                        )
                    }

                    // Rival Card & Info (Top Right)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(220.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            // Rival Info Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.5.dp, CharcoalOutline, RoundedCornerShape(4.dp))
                                    .background(SurfaceWhite, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "TRAILKEEPER BOT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = CharcoalMuted
                                        )
                                        Text(
                                            text = "Lv.18",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = CharcoalText
                                        )
                                    }
                                    Text(
                                        text = "MOSS BEETLE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CharcoalText
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        RetroBadge(text = "INSECT", backgroundColor = Color(0xFFE2E7DA))
                                        RetroBadge(text = "EARTH", backgroundColor = Color(0xFFE2D642).copy(alpha = 0.3f))
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "HP",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = CharcoalMuted
                                        )
                                        Text(
                                            text = "$rivalHp / $rivalMaxHp",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = CharcoalText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    RetroSegmentedBar(
                                        totalSegments = 10,
                                        filledSegments = ((rivalHp.toFloat() / rivalMaxHp) * 10).toInt(),
                                        fillColor = if (rivalHp < 30) WarmBerry else PistachioGreen,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Rival Card Thumbnail
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .background(WarmBerry, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "RIVAL",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = SurfaceWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(60.dp, 75.dp)
                                        .border(1.5.dp, CharcoalOutline, RoundedCornerShape(3.dp))
                                        .background(Color(0xFFE5EDE0), RoundedCornerShape(3.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "🪲", fontSize = 26.sp)
                                        Text(
                                            text = "BEETLE",
                                            fontSize = 6.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Player Card & Info (Bottom Left)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.95f),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Player Card Thumbnail
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .background(SoftTeal, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "YOURS",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = SurfaceWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(65.dp, 82.dp)
                                    .border(1.5.dp, CharcoalOutline, RoundedCornerShape(3.dp))
                                    .background(SoftTealLight, RoundedCornerShape(3.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (selectedCard?.photoUri != null) FieldPhoto(selectedCard!!.photoUri!!, Modifier.size(58.dp, 68.dp)) else Text(text = selectedCard?.icon ?: "＋", fontSize = 30.sp)
                                    Text(
                                        text = selectedCard?.name?.take(9) ?: "ADD CARD",
                                        fontSize = 6.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Player Info Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.5.dp, CharcoalOutline, RoundedCornerShape(4.dp))
                                .background(SurfaceWhite, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedCard?.name ?: "NO FIELD CARD",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CharcoalText
                                    )
                                    Text(
                                        text = if (selectedCard == null) "—" else "FIELD",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = CharcoalText
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    RetroBadge(text = if (selectedCard == null) "NO DECK" else selectedCard!!.habitat.take(12).uppercase(), backgroundColor = SoftTealLight)
                                    RetroBadge(
                                        text = if (selectedCard == null) "SAVE A CARD" else "POWER ${selectedCard!!.power}",
                                        backgroundColor = AlertYellow,
                                        contentColor = CharcoalText
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "HP",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = CharcoalMuted
                                    )
                                    Text(
                                        text = "$playerHp / $playerMaxHp",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = CharcoalText
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                RetroSegmentedBar(
                                    totalSegments = 10,
                                    filledSegments = ((playerHp.toFloat() / playerMaxHp) * 10).toInt(),
                                    fillColor = PistachioGreen,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Wild Duel Dialogue Box
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, CharcoalOutline, RoundedCornerShape(4.dp))
                    .background(LcdWellBg, RoundedCornerShape(4.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📟", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRACTICE DUEL LOG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = PistachioGreen
                            )
                        }
                        Text(
                            text = "▾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CharcoalMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = battleLog,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CharcoalText,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 4. Battle Action Grid (2x2 Buttons)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RetroButton(
                        text = "1. FIELD STRIKE",
                        subtext = "Power ${selectedCard?.power ?: 0}",
                        onClick = { attack("FIELD STRIKE", selectedCard?.power ?: 0) },
                        modifier = Modifier.weight(1f),
                        backgroundColor = SurfaceWhite,
                        contentColor = CharcoalText
                    )

                    RetroButton(
                        text = "2. DEEP STRIKE",
                        subtext = "Power ${(selectedCard?.power ?: 0) + 12}",
                        onClick = { attack("DEEP STRIKE", (selectedCard?.power ?: 0) + 12) },
                        modifier = Modifier.weight(1f),
                        backgroundColor = SurfaceWhite,
                        contentColor = CharcoalText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RetroButton(
                        text = "3. HAIKU ECHO",
                        subtext = "🎵 Spirit Verse • PP 10/10",
                        onClick = {
                            if (selectedCard == null) battleLog = "Save an observation in Cards first."
                            else if (!battleFinished) {
                                playerHp = (playerHp + 15).coerceAtMost(playerMaxHp)
                                turn++
                                battleLog = "${selectedCard!!.name} recalled its field haiku and recovered 15 HP."
                            }
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = SurfaceWhite,
                        contentColor = CharcoalText
                    )

                    RetroButton(
                        text = "4. SWAP CARD",
                        subtext = "⇄ ${cards.size} cards ready",
                        onClick = {
                            if (cards.isEmpty()) battleLog = "No field cards saved yet."
                            else if (cards.size == 1) {
                                rivalHp = rivalMaxHp
                                playerHp = playerMaxHp
                                turn = 1
                                battleFinished = false
                                battleLog = "Rematch started with ${selectedCard!!.name}."
                            }
                            else {
                                val currentIndex = cards.indexOfFirst { it.id == selectedCard?.id }.coerceAtLeast(0)
                                selectedCard = cards[(currentIndex + 1) % cards.size]
                                battleLog = "${selectedCard!!.name} joined the practice duel."
                            }
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = SurfaceWhite,
                        contentColor = CharcoalText
                    )
                }
            }
        }

        // 5. Field Pouch Tray
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CharcoalOutline, RoundedCornerShape(4.dp))
                    .background(SurfaceWhite, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎒", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Field Pouch: ${sapCount}x Trail Snack (+30HP)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CharcoalText
                    )
                }

                RetroButton(
                    text = "USE SAP",
                    onClick = {
                        if (sapCount > 0) {
                            sapCount--
                            playerHp = (playerHp + 30).coerceAtMost(playerMaxHp)
                            battleLog = "Used a trail snack. Restored 30 HP to ${selectedCard?.name ?: "your card"}."
                        }
                    },
                    backgroundColor = if (sapCount > 0) PistachioGreen else Color.Gray,
                    contentColor = SurfaceWhite
                )
            }
        }
    }
}
