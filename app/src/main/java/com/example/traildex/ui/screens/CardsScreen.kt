package com.example.traildex.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.traildex.R
import com.example.traildex.data.LocalAiHaiku
import com.example.traildex.data.FieldCard
import com.example.traildex.data.VisionNote
import com.example.traildex.data.OfflineHaiku
import kotlin.math.absoluteValue
import com.example.traildex.theme.*
import com.example.traildex.ui.components.*

data class Specimen(
    val id: String,
    val name: String,
    val type: String,
    val hp: Int,
    val level: Int,
    val biome: String,
    val weight: String,
    val length: String,
    val haiku: String,
    val move1: String,
    val move1Dmg: Int,
    val move1Desc: String,
    val move2: String,
    val move2Dmg: Int,
    val move2Desc: String,
    val passive: String,
    val rarity: String,
    val location: String,
    val iconEmoji: String,
    val color: Color
)

@Composable
fun CardsScreen(
    cards: List<FieldCard> = emptyList(),
    onSaveCard: (FieldCard) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val specimens = remember {
        listOf(
            Specimen(
                id = "#042",
                name = "AZURE JAY",
                type = "AERIAL",
                hp = 110,
                level = 21,
                biome = "MONTANE FOREST",
                weight = "85g",
                length = "29cm",
                haiku = "\"Morning frost takes flight.\nCrest of azure greets the sun.\nAcorn cache secure.\"",
                move1 = "CANOPY CALL",
                move1Dmg = 25,
                move1Desc = "Alerts nearby flock",
                move2 = "PINECONE BARRAGE",
                move2Dmg = 55,
                move2Desc = "30% chance to stun target",
                passive = "Forest Scout [+10% Speed on pine ...]",
                rarity = "★★★☆☆ UNCOMMON",
                location = "2.1 WHISPERING...",
                iconEmoji = "🐦",
                color = SoftTeal
            ),
            Specimen(
                id = "#019",
                name = "COMMON MOREL",
                type = "FLORA / FUNGI",
                hp = 80,
                level = 12,
                biome = "DAMP FOREST FLOOR",
                weight = "45g",
                length = "12cm",
                haiku = "\"Hidden in the moss.\nHoneycomb upon the earth.\nSpringtime golden prize.\"",
                move1 = "SPORE BURST",
                move1Dmg = 20,
                move1Desc = "Inflicts sleep condition",
                move2 = "MYCELIUM HEAL",
                move2Dmg = 0,
                move2Desc = "Restores 30 HP to self",
                passive = "Decomposer [+5 HP per turn]",
                rarity = "★★☆☆☆ COMMON",
                location = "1.4 CEDAR RIDGE",
                iconEmoji = "🍄",
                color = PistachioGreen
            ),
            Specimen(
                id = "#077",
                name = "VELVET ANT",
                type = "INSECTS / STING",
                hp = 65,
                level = 10,
                biome = "SUNNY TRAIL EDGES",
                weight = "2g",
                length = "2cm",
                haiku = "\"Scarlet velvet coat.\nProwling across dusty sands.\nPotent hidden strike.\"",
                move1 = "COW KILLER STING",
                move1Dmg = 45,
                move1Desc = "High critical hit ratio",
                move2 = "CHITIN ARMOR",
                move2Dmg = 0,
                move2Desc = "Reduces next damage by 50%",
                passive = "Hard Shell [Takes -5 damage]",
                rarity = "★★★☆☆ UNCOMMON",
                location = "0.8 MUDDY CREEK",
                iconEmoji = "🐜",
                color = WarmBerry
            ),
            Specimen(
                id = "#004",
                name = "DEWY FERN",
                type = "FLORA / SPORE",
                hp = 95,
                level = 26,
                biome = "SHADED GLEN",
                weight = "120g",
                length = "45cm",
                haiku = "\"Unfurling soft fronds.\nMorning dew drops catch the dawn.\nAncient canopy.\"",
                move1 = "FROND WHIP",
                move1Dmg = 35,
                move1Desc = "Strikes with coiled leaf",
                move2 = "DROPLET VEIL",
                move2Dmg = 0,
                move2Desc = "Evades next enemy attack",
                passive = "Photosynthesis [+10% Solar charging]",
                rarity = "★★★★☆ RARE",
                location = "3.2 FERN HOLLOW",
                iconEmoji = "🌿",
                color = PistachioGreen
            )
        )
    }

    var selectedSpecimen by remember { mutableStateOf(specimens[0]) }
    var foilEnabled by remember { mutableStateOf(true) }
    var inDeck by remember { mutableStateOf(false) }
    var speciesInput by remember { mutableStateOf("") }
    var placeInput by remember { mutableStateOf("Whispering Pines") }
    var aiMessage by remember { mutableStateOf("LOCAL MODEL: Ollama • gemma3:4b") }
    var generatedHaiku by remember { mutableStateOf("") }
    var generatedByModel by remember { mutableStateOf(false) }
    var categoryInput by remember { mutableStateOf("Nature observation") }
    var observationPhoto by remember { mutableStateOf<Uri?>(null) }
    var modelEndpoint by remember {
        mutableStateOf(context.getSharedPreferences("traildex_ai", android.content.Context.MODE_PRIVATE)
            .getString("endpoint", LocalAiHaiku.defaultEndpoint) ?: LocalAiHaiku.defaultEndpoint)
    }
    var generating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            observationPhoto = uri
            categoryInput = "Nature observation"
            generatedHaiku = ""
            generatedByModel = false
            aiMessage = "PHOTO READY • CHOOSE IDENTIFY & WRITE"
        }
    }
    val cameraCapture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            scope.launch {
                observationPhoto = withContext(Dispatchers.IO) {
                    val photoFile = java.io.File(context.filesDir, "field-${System.currentTimeMillis()}.jpg")
                    photoFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
                    Uri.fromFile(photoFile)
                }
                generatedHaiku = ""
                generatedByModel = false
                aiMessage = "PHOTO CAPTURED • CHOOSE IDENTIFY & WRITE"
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCream)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceContainer) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("FIELD NOTE → OPEN MODEL HAIKU", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
                    Text("Capture a nature photo or describe what you saw. Local Ollama can identify the image and write the haiku.", fontSize = 10.sp, color = CharcoalMuted)
                    OutlinedTextField(value = modelEndpoint, onValueChange = { modelEndpoint = it }, label = { Text("Ollama address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            aiMessage = "CHECKING LOCAL OLLAMA…"
                            scope.launch {
                                try {
                                    val status = withContext(Dispatchers.IO) { LocalAiHaiku.checkConnection(modelEndpoint.trim()) }
                                    context.getSharedPreferences("traildex_ai", android.content.Context.MODE_PRIVATE).edit().putString("endpoint", modelEndpoint.trim()).apply()
                                    aiMessage = status
                                } catch (error: Exception) {
                                    aiMessage = "OLLAMA NOT REACHABLE • ${error.message ?: "check address, server, and model"}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainer, contentColor = CharcoalText)
                    ) { Text("CHECK OLLAMA CONNECTION") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, colors = ButtonDefaults.buttonColors(containerColor = SoftTeal, contentColor = SurfaceWhite)) { Text("CHOOSE PHOTO") }
                        Button(onClick = { cameraCapture.launch(null) }, colors = ButtonDefaults.buttonColors(containerColor = PistachioGreen, contentColor = CharcoalText)) { Text("TAKE PHOTO") }
                    }
                    observationPhoto?.let { photo ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FieldPhoto(photo.toString(), Modifier.size(64.dp))
                            Text("PHOTO ATTACHED • REVIEW AI IDENTIFICATION BEFORE SAVING", fontSize = 9.sp, color = CharcoalMuted)
                        }
                    }
                    OutlinedTextField(value = speciesInput, onValueChange = { speciesInput = it; generatedHaiku = ""; generatedByModel = false }, label = { Text("Species / observation (editable)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = placeInput, onValueChange = { placeInput = it; generatedHaiku = ""; generatedByModel = false }, label = { Text("Trail / habitat") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (speciesInput.isBlank() || generating) return@Button
                            generating = true
                            aiMessage = if (observationPhoto == null) "ASKING LOCAL MODEL FOR HAIKU…" else "ANALYZING IMAGE WITH LOCAL MODEL…"
                            scope.launch {
                                try {
                                    val photo = observationPhoto
                                    val result = withContext(Dispatchers.IO) {
                                        if (photo != null) {
                                            val bytes = readPhotoForModel(context, photo)
                                            LocalAiHaiku.identifyImage(bytes, placeInput.trim(), modelEndpoint.trim())
                                        } else {
                                            VisionNote(speciesInput.trim(), categoryInput, LocalAiHaiku.generate(speciesInput.trim(), placeInput.trim(), modelEndpoint.trim()))
                                        }
                                    }
                                    if (photo != null) {
                                        speciesInput = result.species
                                        categoryInput = result.category
                                    }
                                    val poem = result.haiku
                                    generatedHaiku = poem
                                    generatedByModel = true
                                    aiMessage = "OLLAMA ${if (photo == null) "HAIKU" else "VISION"} RESULT • VERIFY BEFORE SAVING"
                                } catch (error: Exception) {
                                    generatedByModel = false
                                    generatedHaiku = OfflineHaiku.generate(speciesInput.trim(), placeInput.trim())
                                    aiMessage = "OLLAMA UNAVAILABLE • OFFLINE FIELD VERSE • EDIT OR SAVE"
                                } finally {
                                    generating = false
                                }
                            }
                        },
                        enabled = speciesInput.isNotBlank() && !generating,
                        colors = ButtonDefaults.buttonColors(containerColor = PistachioGreen, contentColor = CharcoalText)
                    ) { Text(if (generating) "GENERATING…" else "GENERATE FIELD HAIKU") }
                    Text(aiMessage, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                    if (generatedHaiku.isNotBlank()) {
                        Text(generatedHaiku, fontSize = 12.sp, color = CharcoalText, lineHeight = 17.sp)
                    }
                    Button(
                        onClick = {
                            val name = speciesInput.trim().uppercase()
                            if (name.isNotBlank()) {
                                val now = System.currentTimeMillis()
                                val seed = name.hashCode().absoluteValue
                                onSaveCard(
                                    FieldCard(
                                        id = now.toString(),
                                        name = name,
                                        habitat = placeInput.trim().ifBlank { "Trail observation" },
                                        haiku = generatedHaiku.ifBlank { OfflineHaiku.generate(name, placeInput) },
                                        hp = 60 + seed % 51,
                                        power = 15 + seed % 36,
                                        icon = natureIcon("$categoryInput $name"),
                                        createdAt = now,
                                        category = categoryInput,
                                        photoUri = observationPhoto?.toString()
                                    )
                                )
                                aiMessage = "$name SAVED • ${if (generatedByModel) "OLLAMA GENERATED" else "OFFLINE TEMPLATE • AI NOT USED"}"
                            }
                        },
                        enabled = speciesInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmBerry, contentColor = SurfaceWhite)
                    ) { Text("SAVE FIELD CARD") }
                }
            }
        }

        if (cards.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("YOUR FIELD CARDS • ${cards.size}", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
                    cards.asReversed().forEach { card ->
                        RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite, shadowOffset = 2.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (card.photoUri != null) FieldPhoto(card.photoUri, Modifier.size(52.dp)) else Text(card.icon, fontSize = 28.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(card.name, fontSize = 12.sp, fontWeight = FontWeight.Black, color = CharcoalText)
                                    Text("${card.category} • ${card.habitat} • HP ${card.hp} • POW ${card.power}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                                    Text(card.haiku, fontSize = 10.sp, color = CharcoalText, lineHeight = 14.sp)
                                    Text(
                                        "SHARE FIELD CARD ↗",
                                        modifier = Modifier.clickable {
                                            val text = "${card.name} • ${card.category}\nObserved at ${card.habitat}\n${card.haiku}\n\nMade with TrailDex"
                                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, text)
                                            }, "Share field card"))
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = SoftTeal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Badges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RetroBadge(
                    text = "SAMPLE SPECIMEN • DEMO DATA",
                    backgroundColor = PistachioGreen,
                    contentColor = SurfaceWhite
                )
                Box(
                    modifier = Modifier
                        .border(1.5.dp, CharcoalOutline, RoundedCornerShape(3.dp))
                        .background(if (foilEnabled) AlertYellow else SurfaceContainer, RoundedCornerShape(3.dp))
                        .clickable { foilEnabled = !foilEnabled }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (foilEnabled) "‹ FOIL: ON" else "‹ FOIL: OFF",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CharcoalText
                    )
                }
            }
        }

        // Main Specimen Showcase Card
        item {
            RetroCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite,
                borderColor = if (foilEnabled) SoftTealContainer else CharcoalOutline,
                shadowOffset = 4.dp
            ) {
                Column {
                    // Card Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedSpecimen.id,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = SurfaceWhite,
                                modifier = Modifier
                                    .background(CharcoalText, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedSpecimen.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = CharcoalText
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RetroBadge(
                                text = selectedSpecimen.type,
                                backgroundColor = selectedSpecimen.color.copy(alpha = 0.2f),
                                contentColor = selectedSpecimen.color
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HP ${selectedSpecimen.hp}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = WarmBerry
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Artwork Window
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(2.dp, CharcoalOutline, RoundedCornerShape(4.dp))
                            .background(Color(0xFFE5EDE0), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedSpecimen.iconEmoji,
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "LV. ${selectedSpecimen.level} • ${selectedSpecimen.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CharcoalText
                            )
                        }

                        // Biome / Spec tags
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🌲 ${selectedSpecimen.biome}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CharcoalText,
                                modifier = Modifier
                                    .background(SurfaceWhite.copy(alpha = 0.9f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            Text(
                                text = "WT: ${selectedSpecimen.weight} · LEN: ${selectedSpecimen.length}",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CharcoalText,
                                modifier = Modifier
                                    .background(SurfaceWhite.copy(alpha = 0.9f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Field Haiku Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CharcoalOutline.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .background(LcdWellBg, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "FIELD HAIKU",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CharcoalMuted
                                )
                                Text(text = "📜", fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedSpecimen.haiku,
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = CharcoalText,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Moves Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MoveItem(
                            name = selectedSpecimen.move1,
                            dmg = selectedSpecimen.move1Dmg,
                            desc = selectedSpecimen.move1Desc
                        )
                        MoveItem(
                            name = selectedSpecimen.move2,
                            dmg = selectedSpecimen.move2Dmg,
                            desc = selectedSpecimen.move2Desc
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Passive Ability
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceContainer, RoundedCornerShape(3.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡ PASSIVE: ${selectedSpecimen.passive}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Footer Specs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedSpecimen.rarity,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = WarmBerry
                        )
                        Text(
                            text = "📍 ${selectedSpecimen.location}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CharcoalMuted
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RetroButton(
                    text = if (inDeck) "✓ [SAMPLE ADDED TO DECK]" else "⛶ [ADD SAMPLE TO BATTLE DECK]",
                    onClick = {
                        if (!inDeck) {
                            val now = System.currentTimeMillis()
                            onSaveCard(
                                FieldCard(
                                    id = now.toString(),
                                    name = selectedSpecimen.name,
                                    habitat = selectedSpecimen.biome,
                                    haiku = selectedSpecimen.haiku.trim('"'),
                                    hp = selectedSpecimen.hp,
                                    power = selectedSpecimen.move1Dmg.coerceAtLeast(15),
                                    icon = selectedSpecimen.iconEmoji,
                                    createdAt = now
                                )
                            )
                            inDeck = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (inDeck) SoftTealLight else PistachioGreen,
                    contentColor = CharcoalText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RetroButton(
                        text = "➦ SHARE CARD",
                        onClick = {
                            val shareText = "${selectedSpecimen.name} • ${selectedSpecimen.type}\n${selectedSpecimen.biome}\n${selectedSpecimen.haiku}"
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }, "Share field card"))
                        },
                        modifier = Modifier.weight(1f),
                        backgroundColor = SurfaceWhite,
                        contentColor = CharcoalText
                    )
                    RetroButton(
                        text = "📖 NATUREDEX",
                        onClick = { aiMessage = "YOUR SAVED CARDS ARE LISTED ABOVE AND IN SCOUT LOG." },
                        modifier = Modifier.weight(1f),
                        backgroundColor = WarmBerry,
                        contentColor = SurfaceWhite
                    )
                }
            }
        }

        // Carousel of Trail Specimens Today
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SoftTeal, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SAMPLE SPECIMEN DECK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CharcoalText
                        )
                    }
                    Text(
                        text = "${specimens.size} SAMPLES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CharcoalMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(specimens) { item ->
                        val isSelected = item.id == selectedSpecimen.id
                        Box(
                            modifier = Modifier
                                .width(115.dp)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.5.dp,
                                    color = if (isSelected) PistachioGreen else CharcoalOutline,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(SurfaceWhite, RoundedCornerShape(4.dp))
                                .clickable { selectedSpecimen = item }
                                .padding(6.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.id,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = SurfaceWhite,
                                        modifier = Modifier
                                            .background(CharcoalText, RoundedCornerShape(2.dp))
                                            .padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                    Text(
                                        text = "HP ${item.hp}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = WarmBerry
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .background(Color(0xFFE5EDE0), RoundedCornerShape(2.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = item.iconEmoji, fontSize = 28.sp)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "LV. ${item.level}",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CharcoalMuted
                                )
                                Text(
                                    text = item.name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalText,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.type,
                                    fontSize = 7.sp,
                                    color = CharcoalMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun natureIcon(name: String): String = when {
    listOf("BIRD", "JAY", "HAWK", "FALCON", "ROBIN", "CROW", "CUCKOO", "RAVEN", "SPARROW", "EAGLE").any(name::contains) -> "🐦"
    listOf("FLOWER", "ORCHID", "FERN", "MOSS", "PLANT").any(name::contains) -> "🌿"
    listOf("MUSHROOM", "MOREL", "FUNGUS").any(name::contains) -> "🍄"
    listOf("ANT", "BEE", "BEETLE", "BUTTERFLY", "INSECT").any(name::contains) -> "🪲"
    listOf("FOX", "DEER", "RABBIT", "SQUIRREL").any(name::contains) -> "🐾"
    else -> "🌱"
}

private fun readPhotoForModel(context: android.content.Context, uri: Uri): ByteArray {
    val original = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        ?: error("Could not read the selected photo")
    val longest = maxOf(original.width, original.height)
    val scale = minOf(1f, 1024f / longest)
    val resized = if (scale < 1f) Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true) else original
    return java.io.ByteArrayOutputStream().use { output ->
        resized.compress(Bitmap.CompressFormat.JPEG, 82, output)
        output.toByteArray()
    }
}

@Composable
fun FieldPhoto(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Saved nature observation photo",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(4.dp))
        )
    } else {
        Box(modifier.background(SurfaceContainer, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
            Text("🌿", fontSize = 20.sp)
        }
    }
}

@Composable
private fun MoveItem(
    name: String,
    dmg: Int,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CharcoalOutline.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            .background(SurfaceCream, RoundedCornerShape(3.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(PistachioGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = CharcoalText
                )
            }
            Text(
                text = desc,
                fontSize = 8.sp,
                color = CharcoalMuted,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
        if (dmg > 0) {
            Text(
                text = "$dmg DMG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = SoftTeal
            )
        }
    }
}
