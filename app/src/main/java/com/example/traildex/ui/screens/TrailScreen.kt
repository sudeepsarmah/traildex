package com.example.traildex.ui.screens

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.traildex.theme.*
import com.example.traildex.ui.components.*
import com.example.traildex.TrailLocationService
import com.example.traildex.data.NearbyTrail
import com.example.traildex.data.NearbyTrails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TrailScreen(
    onNavigateToDex: () -> Unit = {},
    onDistanceChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionPrefs = remember(context) { context.getSharedPreferences("trail_session", Context.MODE_PRIVATE) }
    var pinging by remember { mutableStateOf(false) }
    var snapResult by remember { mutableStateOf<String?>(null) }
    var tracking by rememberSaveable { mutableStateOf(sessionPrefs.getBoolean(TrailLocationService.ACTIVE_KEY, false)) }
    var walkedMeters by rememberSaveable { mutableFloatStateOf(sessionPrefs.getFloat("distance_m", 0f)) }
    var latestLocation by remember { mutableStateOf<Location?>(null) }
    var nearbyTrails by remember { mutableStateOf<List<NearbyTrail>>(emptyList()) }
    var trailSearchStatus by remember { mutableStateOf("Use GPS to find mapped walking routes nearby.") }
    var searchingTrails by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var locationStatus by rememberSaveable { mutableStateOf("LOCATION OFF") }
    var permissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (permissionGranted) {
            tracking = true
            locationStatus = "WAITING FOR GPS FIX"
        } else {
            tracking = false
            locationStatus = "LOCATION PERMISSION DENIED"
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                walkedMeters = intent.getFloatExtra(TrailLocationService.EXTRA_DISTANCE, walkedMeters)
                tracking = intent.getBooleanExtra(TrailLocationService.EXTRA_ACTIVE, tracking)
                locationStatus = intent.getStringExtra(TrailLocationService.EXTRA_STATUS) ?: locationStatus
                if (intent.hasExtra(TrailLocationService.EXTRA_LATITUDE)) {
                    latestLocation = Location("TrailDex").apply {
                        latitude = intent.getDoubleExtra(TrailLocationService.EXTRA_LATITUDE, 0.0)
                        longitude = intent.getDoubleExtra(TrailLocationService.EXTRA_LONGITUDE, 0.0)
                        accuracy = intent.getFloatExtra(TrailLocationService.EXTRA_ACCURACY, 0f)
                    }
                }
            }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(TrailLocationService.ACTION_UPDATE), ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }
    LaunchedEffect(tracking, permissionGranted) {
        if (tracking && permissionGranted) {
            ContextCompat.startForegroundService(context, Intent(context, TrailLocationService::class.java))
        }
    }
    val progressKm = (walkedMeters / 1000f).coerceAtMost(4f)
    val progressPercent = (progressKm / 4f * 100).toInt()
    LaunchedEffect(walkedMeters) {
        sessionPrefs.edit().putFloat("distance_m", walkedMeters).apply()
        onDistanceChanged(walkedMeters)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceCream)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // 1. Trail Location & Progress
        item {
            RetroCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(PistachioGreen, RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WHISPERING PINES LOOP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = CharcoalText
                            )
                        }
                        RetroBadge(text = "SEC-04 [MOSS]", backgroundColor = SurfaceContainer)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROGRESS: ${"%.2f".format(progressKm)} / 4.0 KM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CharcoalMuted
                        )
                        Text(
                            text = "$progressPercent%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = PistachioGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    RetroSegmentedBar(
                        totalSegments = 10,
                        filledSegments = (progressPercent / 10).coerceIn(0, 10),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Telemetry metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBox(
                            title = "GPS DIST",
                            value = "${"%.2f".format(progressKm)} KM",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = "GPS ACC",
                            value = latestLocation?.let { "±${it.accuracy.toInt()}M" } ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            title = "SESSION",
                            value = if (tracking) "ACTIVE" else "PAUSED",
                            valueColor = SoftTeal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Field Matrix 32x32 (Radar Map)
        item {
            RetroCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite,
                shadowOffset = 4.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🗺", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ROUTE SCHEMATIC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = CharcoalText
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(PistachioGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (tracking) locationStatus else "TRACK PAUSED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = PistachioGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tactical Map Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .border(1.5.dp, CharcoalOutline, RoundedCornerShape(4.dp))
                            .background(Color(0xFFF9FAF7), RoundedCornerShape(4.dp))
                    ) {
                        // Drawing Grid, River, and Trail Path
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Dot Grid
                            val spacing = 20.dp.toPx()
                            for (x in 0..(w / spacing).toInt()) {
                                for (y in 0..(h / spacing).toInt()) {
                                    drawCircle(
                                        color = Color(0xFFDDE3D6),
                                        radius = 1.2f,
                                        center = Offset(x * spacing, y * spacing)
                                    )
                                }
                            }

                            // River (Cyan wavy line)
                            val riverPath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(w * 0.45f, 0f)
                                quadraticBezierTo(w * 0.40f, h * 0.4f, w * 0.42f, h * 0.6f)
                                quadraticBezierTo(w * 0.46f, h * 0.8f, w * 0.50f, h)
                            }
                            drawPath(
                                path = riverPath,
                                color = Color(0xFF7DE5D4),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                            )

                            // Trail (Dashed Green line)
                            drawLine(
                                color = PistachioGreen,
                                start = Offset(w * 0.15f, h * 0.85f),
                                end = Offset(w * 0.82f, h * 0.18f),
                                strokeWidth = 5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                            )

                            // Waypoint circles
                            drawCircle(color = CharcoalOutline, radius = 6f, center = Offset(w * 0.20f, h * 0.80f))
                            drawCircle(color = SoftTeal, radius = 7f, center = Offset(w * 0.42f, h * 0.60f))
                            drawCircle(color = PistachioGreen, radius = 8f, center = Offset(w * 0.62f, h * 0.38f))
                            drawCircle(color = WarmBerry, radius = 8f, center = Offset(w * 0.82f, h * 0.18f))

                            // Pulse ping if active
                            if (pinging) {
                                drawCircle(
                                    color = PistachioGreen.copy(alpha = 0.35f),
                                    radius = 45f,
                                    center = Offset(w * 0.52f, h * 0.48f)
                                )
                            }
                        }

                        // Waypoint Labels
                        Text(
                            text = "A",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CharcoalMuted,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 28.dp, bottom = 18.dp)
                        )
                        Text(
                            text = "MUDDY CREEK",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SoftTeal,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 100.dp, top = 20.dp)
                        )
                        Text(
                            text = "CEDAR RD",
                            fontSize = 7.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CharcoalMuted,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(start = 60.dp, bottom = 30.dp)
                        )
                        Text(
                            text = "🚩 TOWER B",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = WarmBerry,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 20.dp, top = 16.dp)
                        )

                        // Coordinates Bar
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(SurfaceWhite.copy(alpha = 0.85f))
                                .border(width = 1.dp, color = CharcoalOutline.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = latestLocation?.let { "${"%.4f".format(it.latitude)}, ${"%.4f".format(it.longitude)}" } ?: "NO GPS FIX",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CharcoalText
                            )
                            Text(
                                text = if (tracking) "⊕ ${locationStatus.take(12)}" else "LOCATION OFF",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = PistachioGreen
                            )
                        }
                    }
                }
            }
        }

        item {
            RetroCard(modifier = Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("NEARBY MAPPED TRAILS", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText)
                    Text("Search OpenStreetMap hiking and walking route relations within 8 km. Results are cached on this phone for 24 hours.", fontSize = 9.sp, color = CharcoalMuted)
                    RetroButton(
                        text = if (searchingTrails) "SEARCHING OPENSTREETMAP…" else "FIND TRAILS NEAR ME",
                        onClick = {
                            val fix = latestLocation
                            if (fix == null) {
                                trailSearchStatus = "Start GPS tracking and wait for a location fix first."
                            } else if (!searchingTrails) {
                                searchingTrails = true
                                trailSearchStatus = "Checking nearby mapped routes…"
                                scope.launch {
                                    try {
                                        nearbyTrails = withContext(Dispatchers.IO) { NearbyTrails.find(context, fix) }
                                        trailSearchStatus = if (nearbyTrails.isEmpty()) "No named mapped routes found within 8 km. Try another area." else "${nearbyTrails.size} nearby route${if (nearbyTrails.size == 1) "" else "s"} • tap a route to view its OpenStreetMap page."
                                    } catch (error: Exception) {
                                        trailSearchStatus = "Route lookup failed. ${if (nearbyTrails.isNotEmpty()) "Showing saved results." else "Check internet and try again."}"
                                    } finally { searchingTrails = false }
                                }
                            }
                        },
                        backgroundColor = PistachioGreen,
                        contentColor = SurfaceWhite,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(trailSearchStatus, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                    nearbyTrails.forEach { trail ->
                        Row(
                            modifier = Modifier.fillMaxWidth().border(1.dp, CharcoalOutline, RoundedCornerShape(4.dp)).background(SurfaceCream, RoundedCornerShape(4.dp)).clickable {
                                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.openstreetmap.org/relation/${trail.id}"))
                                runCatching { context.startActivity(intent) }
                            }.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(trail.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CharcoalText)
                                Text("${trail.kind.uppercase()} • OSM RELATION #${trail.id}", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                            }
                            Text("OPEN ↗", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SoftTeal)
                        }
                    }
                    Text("Map data © OpenStreetMap contributors • opening the route page needs internet.", fontSize = 8.sp, color = CharcoalMuted)
                }
            }
        }

        // Keep the prototype honest: no detector is attached to this screen yet.
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, SoftTealContainer, RoundedCornerShape(4.dp))
                    .background(SoftTealLight.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .clickable { onNavigateToDex() }
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "◎", fontSize = 11.sp, fontWeight = FontWeight.Black, color = SoftTeal)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FIELD NOTE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = SoftTeal
                            )
                        }
                        RetroBadge(text = "USER LOGGED", backgroundColor = SurfaceWhite)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .border(1.5.dp, CharcoalOutline, RoundedCornerShape(6.dp))
                                .background(SurfaceWhite, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "＋", fontSize = 24.sp, color = PistachioGreen)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "See something?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CharcoalText
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ADD TO DEX",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = WarmBerry
                                )
                            }
                            Text(
                                text = "Tap here to add your own observation.",
                                fontSize = 9.sp,
                                color = CharcoalMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                RetroBadge(text = "NO AUTO IDENTIFICATION", backgroundColor = SurfaceWhite)
                            }
                        }
                    }
                }
            }
        }

        // 4. Physical Console Buttons (A / B / Biome)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RetroButton(
                    text = if (tracking) "STOP TRAIL" else "START TRAIL",
                    subtext = "GPS TRACKING",
                    onClick = {
                        if (tracking) {
                            tracking = false
                            context.startService(Intent(context, TrailLocationService::class.java).setAction(TrailLocationService.ACTION_STOP))
                            snapResult = "Trail paused at ${"%.2f".format(progressKm)} km. Add observed species in Cards."
                        } else if (permissionGranted) {
                            tracking = true
                            locationStatus = "WAITING FOR GPS FIX"
                        } else {
                            val permissions = if (Build.VERSION.SDK_INT >= 33) {
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
                            } else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            locationPermission.launch(permissions)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = PistachioGreen,
                    contentColor = SurfaceWhite
                )

                RetroButton(
                    text = if (pinging) "PULSE ON" else "(B) PING",
                    subtext = "PULSE (10s)",
                    onClick = {
                        pinging = !pinging
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = WarmBerryContainer,
                    contentColor = SurfaceWhite
                )

                RetroButton(
                    text = "⌂ BIOME",
                    subtext = "DATA LOG",
                    onClick = onNavigateToDex,
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceContainer,
                    contentColor = CharcoalText
                )
            }
        }

        // Snap result banner if active
        if (snapResult != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, PistachioGreen, RoundedCornerShape(4.dp))
                        .background(SurfaceWhite, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "📸 $snapResult",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CharcoalText
                    )
                }
            }
        }

        // 5. Trail session details
        item {
            RetroCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceWhite
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXPEDITION LOGBOOK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CharcoalText
                        )
                        Text(
                            text = if (tracking) "GPS SESSION ACTIVE" else "GPS SESSION PAUSED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = PistachioGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = latestLocation?.let { "LAST FIX: ${"%.4f".format(it.latitude)}, ${"%.4f".format(it.longitude)}" }
                            ?: "Start a trail to record your GPS route.",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CharcoalText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live location quality
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CharcoalOutline.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                            .background(SurfaceCream, RoundedCornerShape(3.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (tracking) locationStatus else "LOCATION OFF • TRACK PAUSED",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CharcoalText
                        )
                        Text(
                            text = latestLocation?.let { "ACCURACY ±${it.accuracy.toInt()}M" } ?: "ACCURACY UNKNOWN",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SoftTeal
                        )
                    }
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@Composable
private fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    valueColor: Color = CharcoalText
) {
    Box(
        modifier = modifier
            .border(1.dp, CharcoalOutline, RoundedCornerShape(4.dp))
            .background(SurfaceCream, RoundedCornerShape(4.dp))
            .padding(6.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CharcoalMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Text(text = icon, fontSize = 9.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = valueColor
                )
            }
        }
    }
}
