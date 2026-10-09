package com.example.traildex

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class TrailLocationService : Service() {
    private var locationManager: LocationManager? = null
    private var lastLocation: Location? = null
    private var distanceMeters = 0f

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (!location.hasAccuracy() || location.accuracy > 35f) {
                publish(location, "GPS WEAK")
                return
            }
            lastLocation?.let { old ->
                val delta = old.distanceTo(location)
                if (delta in 2f..60f) distanceMeters += delta
            }
            lastLocation = location
            publish(location, "GPS OK")
            updateNotification()
        }

        override fun onProviderEnabled(provider: String) = publish(lastLocation, "GPS ACTIVE")
        override fun onProviderDisabled(provider: String) = publish(lastLocation, "GPS DISABLED")
    }

    override fun onCreate() {
        super.onCreate()
        distanceMeters = getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(DISTANCE_KEY, 0f)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            return START_NOT_STICKY
        }
        if (locationManager != null) return START_STICKY
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(ACTIVE_KEY, true).apply()
        if (!hasLocationPermission()) {
            publish(null, "LOCATION PERMISSION NEEDED")
            stopTracking()
            return START_NOT_STICKY
        }
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = manager
        try {
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .filter { manager.isProviderEnabled(it) }
                .forEach { manager.requestLocationUpdates(it, 5_000L, 3f, listener) }
            publish(null, "WAITING FOR GPS FIX")
        } catch (_: SecurityException) {
            publish(null, "LOCATION PERMISSION NEEDED")
            stopTracking()
        }
        return START_STICKY
    }

    private fun publish(location: Location?, status: String) {
        if (location != null) lastLocation = location
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putFloat(DISTANCE_KEY, distanceMeters)
            .putBoolean(ACTIVE_KEY, locationManager != null)
            .apply()
        sendBroadcast(Intent(ACTION_UPDATE).setPackage(packageName).apply {
            putExtra(EXTRA_DISTANCE, distanceMeters)
            putExtra(EXTRA_STATUS, status)
            putExtra(EXTRA_ACTIVE, locationManager != null)
            location?.let {
                putExtra(EXTRA_LATITUDE, it.latitude)
                putExtra(EXTRA_LONGITUDE, it.longitude)
                putExtra(EXTRA_ACCURACY, it.accuracy)
            }
        })
    }

    private fun stopTracking() {
        locationManager?.removeUpdates(listener)
        locationManager = null
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(ACTIVE_KEY, false).apply()
        publish(null, "TRACKING PAUSED")
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopIntent = PendingIntent.getService(this, 2, Intent(this, TrailLocationService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("TrailDex is tracking your walk")
            .setContentText("${"%.2f".format(distanceMeters / 1000f)} km recorded • tap to open")
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, "Stop trail", stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Trail tracking", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        locationManager?.removeUpdates(listener)
        locationManager = null
        super.onDestroy()
    }

    companion object {
        const val ACTION_UPDATE = "com.example.traildex.TRAIL_UPDATE"
        const val ACTION_STOP = "com.example.traildex.TRAIL_STOP"
        const val EXTRA_DISTANCE = "distance"
        const val EXTRA_STATUS = "status"
        const val EXTRA_ACTIVE = "active"
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_ACCURACY = "accuracy"
        const val PREFS = "trail_session"
        const val DISTANCE_KEY = "distance_m"
        const val ACTIVE_KEY = "active"
        private const val CHANNEL_ID = "trail_tracking"
        private const val NOTIFICATION_ID = 401
    }
}
