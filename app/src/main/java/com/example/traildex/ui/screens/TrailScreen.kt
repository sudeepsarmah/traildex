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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.traildex.TrailLocationService
import com.example.traildex.data.TrailHistory
import com.example.traildex.data.TrailPoint
import com.example.traildex.data.TrailRecord
import com.example.traildex.theme.*
import com.example.traildex.ui.components.RetroBadge
import com.example.traildex.ui.components.RetroButton
import com.example.traildex.ui.components.RetroCard
import com.example.traildex.ui.components.RetroSegmentedBar
import com.example.traildex.ui.components.TrailMap

@Composable
fun TrailScreen(onNavigateToDex: () -> Unit = {}, onDistanceChanged: (Float) -> Unit = {}, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sessionPrefs = remember(context) { context.getSharedPreferences("trail_session", Context.MODE_PRIVATE) }
    var tracking by rememberSaveable { mutableStateOf(sessionPrefs.getBoolean(TrailLocationService.ACTIVE_KEY, false)) }
    var walkedMeters by rememberSaveable { mutableFloatStateOf(sessionPrefs.getFloat(TrailLocationService.DISTANCE_KEY, 0f)) }
    var latestLocation by remember { mutableStateOf<Location?>(null) }
    var locationStatus by rememberSaveable { mutableStateOf("Waiting for a GPS fix") }
    var permissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }
    var points by remember { mutableStateOf(TrailHistory.activePoints(context)) }
    var savedRoutes by remember { mutableStateOf(TrailHistory.saved(context)) }
    var selectedHistoryRoute by remember { mutableStateOf<TrailRecord?>(null) }
    var trailName by rememberSaveable { mutableStateOf("My nature walk") }
    var selectingPoint by rememberSaveable { mutableStateOf("") }
    var plannedStart by remember(context) { mutableStateOf(readPlannedPoint(context, "start")) }
    var plannedEnd by remember(context) { mutableStateOf(readPlannedPoint(context, "finish")) }
    var showRouteName by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permissionGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (permissionGranted) {
            selectedHistoryRoute = null
            sessionPrefs.edit().putFloat(TrailLocationService.DISTANCE_KEY, 0f).apply()
            walkedMeters = 0f
            tracking = true
        } else locationStatus = "Location permission is needed to record a walk"
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
                points = TrailHistory.activePoints(context)
                savedRoutes = TrailHistory.saved(context)
            }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(TrailLocationService.ACTION_UPDATE), ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }
    LaunchedEffect(tracking) {
        if (tracking) ContextCompat.startForegroundService(context, Intent(context, TrailLocationService::class.java).putExtra(TrailLocationService.EXTRA_TRAIL_NAME, trailName))
    }
    LaunchedEffect(walkedMeters) { onDistanceChanged(walkedMeters) }

    if (showRouteName) AlertDialog(
        onDismissRequest = { showRouteName = false },
        title = { Text("Name this walk") },
        text = { OutlinedTextField(value = trailName, onValueChange = { trailName = it }, singleLine = true, label = { Text("Trail name") }) },
        confirmButton = { TextButton(onClick = { showRouteName = false; startTracking(permissionGranted, permissionLauncher, sessionPrefs) { selectedHistoryRoute = null; walkedMeters = 0f; tracking = true } }) { Text("START WALK") } },
        dismissButton = { TextButton(onClick = { showRouteName = false }) { Text("CANCEL") } }
    )

    LazyColumn(modifier.fillMaxSize().background(SurfaceCream).padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
        item {
            RetroCard(Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Text(if (tracking) trailName.uppercase() else "READY FOR A WALK", fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText); Text(locationStatus.uppercase(), fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = if (tracking) PistachioGreen else CharcoalMuted) }
                        RetroBadge(text = if (tracking) "GPS ON" else "GPS OFF", backgroundColor = if (tracking) SoftTealLight else SurfaceContainer)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${"%.2f".format(walkedMeters / 1000f)} km", fontSize = 20.sp, fontWeight = FontWeight.Black, color = CharcoalText)
                        Text("${points.size} GPS points", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = CharcoalMuted)
                    }
                    RetroSegmentedBar(totalSegments = 10, filledSegments = ((walkedMeters / 1000f).toInt() % 10), modifier = Modifier.fillMaxWidth())
                    RetroButton(text = if (tracking) "FINISH & SAVE WALK" else "START A WALK", subtext = "GPS ROUTE SAVES ON THIS PHONE", onClick = {
                        if (tracking) { tracking = false; context.startService(Intent(context, TrailLocationService::class.java).setAction(TrailLocationService.ACTION_STOP)) }
                        else showRouteName = true
                    }, modifier = Modifier.fillMaxWidth(), backgroundColor = if (tracking) WarmBerry else PistachioGreen)
                }
            }
        }
        item {
            RetroCard(Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("LIVE TRAIL MAP", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText); Text("TAP MAP TO SET POINTS", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = SoftTeal) }
                    val displayedTrack = selectedHistoryRoute?.points ?: points
                    TrailMap(modifier = Modifier.fillMaxWidth().height(230.dp).border(1.dp, CharcoalOutline, RoundedCornerShape(4.dp)), center = selectedHistoryRoute?.points?.lastOrNull() ?: latestLocation?.let { TrailPoint(it.latitude, it.longitude, System.currentTimeMillis()) } ?: points.lastOrNull(), track = displayedTrack, plannedStart = plannedStart, plannedEnd = plannedEnd, focusRouteId = selectedHistoryRoute?.id, followLocation = tracking && selectingPoint.isBlank() && selectedHistoryRoute == null, onMapTap = { lat, lon ->
                        val point = TrailPoint(lat, lon, System.currentTimeMillis())
                        when (selectingPoint) {
                            "START" -> { plannedStart = point; savePlannedPoint(context, "start", point); selectingPoint = "" }
                            "FINISH" -> { plannedEnd = point; savePlannedPoint(context, "finish", point); selectingPoint = "" }
                        }
                    })
                    Text(if (selectedHistoryRoute != null) "Viewing saved walk • © OpenStreetMap contributors" else "Map tiles need internet. GPS tracks stay on this phone • © OpenStreetMap contributors", fontSize = 8.sp, color = CharcoalMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RetroButton(text = "PICK START", subtext = if (plannedStart == null) "TAP MAP" else coordLabel(plannedStart!!), onClick = { selectingPoint = "START" }, modifier = Modifier.weight(1f), backgroundColor = SoftTealLight)
                        RetroButton(text = "PICK FINISH", subtext = if (plannedEnd == null) "TAP MAP" else coordLabel(plannedEnd!!), onClick = { selectingPoint = "FINISH" }, modifier = Modifier.weight(1f), backgroundColor = AlertYellow)
                    }
                    if (selectingPoint.isNotBlank()) Text("Tap the map to set your ${selectingPoint.lowercase()} point.", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = WarmBerry)
                    RetroButton(text = "OPEN WALKING DIRECTIONS", subtext = "GOOGLE MAPS • NO API KEY", onClick = { openDirections(context, plannedStart, plannedEnd) }, modifier = Modifier.fillMaxWidth(), backgroundColor = if (plannedStart != null && plannedEnd != null) PistachioGreen else SurfaceContainer, enabled = plannedStart != null && plannedEnd != null)
                }
            }
        }
        item {
            RetroCard(Modifier.fillMaxWidth(), backgroundColor = SoftTealLight) {
                Row(Modifier.fillMaxWidth().clickable(onClick = onNavigateToDex), verticalAlignment = Alignment.CenterVertically) {
                    Text("＋", fontSize = 28.sp, color = SoftTeal); Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) { Text("NOTICE SOMETHING?", fontSize = 11.sp, fontWeight = FontWeight.Black, color = CharcoalText); Text("Take a photo or describe a bird, plant, or bug to make a field card.", fontSize = 9.sp, color = CharcoalMuted) }
                    Text("CARDS →", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SoftTeal)
                }
            }
        }
        item {
            RetroCard(Modifier.fillMaxWidth(), backgroundColor = SurfaceWhite) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("SAVED WALKS", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CharcoalText); Text("${savedRoutes.size} LOGGED", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = PistachioGreen) }
                    if (savedRoutes.isEmpty()) Text("Finish your first GPS walk and its route, distance, and time will appear here.", fontSize = 10.sp, color = CharcoalMuted)
                    else savedRoutes.take(6).forEach { route -> SavedWalkRow(route, onSelect = { selectedHistoryRoute = route }) }
                }
            }
        }
    }
}

@Composable
private fun SavedWalkRow(route: TrailRecord, onSelect: () -> Unit) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth().border(1.dp, CharcoalOutline.copy(alpha = .45f), RoundedCornerShape(4.dp)).background(SurfaceCream).clickable(onClick = onSelect).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(route.name.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = CharcoalText); Text("${route.points.size} GPS points • ${java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(route.endedAt)}", fontSize = 8.sp, color = CharcoalMuted) }
        Column(horizontalAlignment = Alignment.End) {
            Text("${"%.2f".format(route.distanceMeters / 1000f)} KM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SoftTeal)
            Text("DIRECTIONS ↗", Modifier.clickable {
        val first = route.points.firstOrNull(); val last = route.points.lastOrNull()
        if (first != null && last != null) openDirections(context, first, last)
            }, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SoftTeal)
        }
    }
}

private fun openDirections(context: Context, start: TrailPoint?, end: TrailPoint?) {
    if (start == null || end == null) return
    val uri = android.net.Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&travelmode=walking")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}
private fun coordLabel(point: TrailPoint) = "${"%.4f".format(point.latitude)}, ${"%.4f".format(point.longitude)}"
private fun savePlannedPoint(context: Context, key: String, point: TrailPoint) {
    context.getSharedPreferences("traildex_plan", Context.MODE_PRIVATE).edit().putString(key, "${point.latitude},${point.longitude}").apply()
}
private fun readPlannedPoint(context: Context, key: String): TrailPoint? = runCatching {
    val saved = context.getSharedPreferences("traildex_plan", Context.MODE_PRIVATE).getString(key, null) ?: return null
    val values = saved.split(",")
    TrailPoint(values[0].toDouble(), values[1].toDouble(), 0L)
}.getOrNull()
private fun startTracking(granted: Boolean, launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>, prefs: android.content.SharedPreferences, onStart: () -> Unit) {
    if (granted) { prefs.edit().putFloat(TrailLocationService.DISTANCE_KEY, 0f).apply(); onStart() }
    else { val permissions = if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.POST_NOTIFICATIONS) else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION); launcher.launch(permissions) }
}
private fun hasLocationPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
