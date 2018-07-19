package com.shadow3x3x3.bustracker.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.google.android.gms.common.api.ResultTransform
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.shadow3x3x3.bustracker.R
import org.json.JSONObject

fun Context.toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

class LocationService : Service() {

    private var isRunning = false

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationRequest by lazy {
        LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    private lateinit var locationCallback: LocationCallback

    companion object {
        const val SERVICE_TAG = "LocationService"
        const val CHANNEL_ID = "LocationChannelID"
        const val CHANNEL_NAME= "LocationChannelName"
        const val NOTIFICATION_ID = 19

        const val NOTIFICATION_TITLE = "Location Updated !"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SERVICE_TAG, "onStartCommand")

        if (isRunning) {
            return START_STICKY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY
        }

        toast("Start Bus Tracker!")

        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location ->
            Log.d(SERVICE_TAG, "Location: ${location.longitude}, ${location.latitude}")
            createNotification("Location: ${location.longitude}, ${location.latitude}")
        }

        startLocationUpdates()

        isRunning = true

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(SERVICE_TAG, "onDestroy")
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("DO NOT onBind THIS SERVICE.")
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                Log.d(SERVICE_TAG, "Update Location!")
                for (location in locationResult.locations) {
                    Log.d(SERVICE_TAG, "Location: ${location.longitude}, ${location.latitude}")
                    createNotification("Location: ${location.longitude}, ${location.latitude}")
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun showNotification(notification: Notification) {
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(contextText: String) {
        val json = JSONObject()
        json.put("title", "foo")
        json.put("body", "bar")
        json.put("userId", "1")

        Fuel.post("https://jsonplaceholder.typicode.com/posts")
            .body(json.toString())
            .responseJson { _, _, result ->
            result.fold(
                success = { json -> Log.d(SERVICE_TAG, json.toString()) },
                failure = { error -> Log.d(SERVICE_TAG, error.toString()) }
            )
        }


        val notification = buildNotification(contextText)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN)

            notificationManager.createNotificationChannel(channel)
        }

        showNotification(notification)
    }

    private fun buildNotification(contextText: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(NOTIFICATION_TITLE)
            setContentText(contextText)
            setSmallIcon(R.mipmap.ic_launcher_round)
            setWhen(System.currentTimeMillis())
            setOngoing(true)
            setVibrate(longArrayOf(0))
        }.build()

}
