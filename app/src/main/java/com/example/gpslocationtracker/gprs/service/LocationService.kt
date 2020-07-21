package com.example.gpslocationtracker;

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.gpslocationtracker.network.RestCall
import com.example.gpslocationtracker.network.RestClient
import com.example.gpslocationtracker.networkresponce.CommonResponce
import com.example.gpslocationtracker.utility.PreferenceManager
import com.example.gpslocationtracker.utility.VariableBag
import com.google.android.gms.location.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException
import java.util.*

class LocationService : Service() {
    var counter = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    internal var address: String? = null
    internal var area: String? = null
    internal var locality: String? = null


    internal var restCall: RestCall? = null
    var duration: Long = 0
    private val TAG = "LocationService"
    lateinit var preferenceManager: PreferenceManager


    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
                1,
                Notification()
        )

        preferenceManager = PreferenceManager(applicationContext)
        val durations = preferenceManager!!.getKeyValueString("duration")
        if (durations != "") {
            duration = durations!!.toLong()
        } else {
            duration = 1
        }

        requestLocationUpdates()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val NOTIFICATION_CHANNEL_ID = "com.getlocationbackground"
        val channelName = "Background Service"
        val chan = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running count::" + counter)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimer()
        return START_STICKY
    }

    private fun uploadData() {
        var userStatus = preferenceManager.getKeyValueString("UserStatus")
        if (userStatus.equals("USERIN")) {
            restCall = RestClient.createService(RestCall::class.java, VariableBag.BASE_URL)
            restCall!!.getLatLong(
                    "user_location",
                    preferenceManager.registredUSerID,
                    address, area, locality, latitude.toString(), longitude.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Subscriber<CommonResponce>() {
                        override fun onError(e: Throwable?) {
                            Toast.makeText(
                                    this@LocationService,
                                    e!!.message + "",
                                    Toast.LENGTH_SHORT
                            ).show()

                        }

                        override fun onNext(t: CommonResponce?) {
                            Toast.makeText(
                                    this@LocationService,
                                    t!!.message + "",
                                    Toast.LENGTH_SHORT
                            ).show()

                        }

                        override fun onCompleted() {
                        }

                    });

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stoptimertask()

        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, RestartBackgroundService::class.java)
        this.sendBroadcast(broadcastIntent)

    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                var count = counter++
                if (latitude != 0.0 && longitude != 0.0) {
                    Log.d(
                            "Location::",
                            latitude.toString() + ":::" + longitude.toString() + "Count" +
                                    count.toString())
                    address = getAddress(latitude, longitude)
                    uploadData()
                }
            }
        }
        timer!!.schedule(
                timerTask,
                0,
                duration * 1000 * 60
        ) //1 * 60 * 1000 1 minute
    }

    fun stoptimertask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest()
        request.setInterval(10000)
        request.setFastestInterval(5000)
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val client: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) { // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location = locationResult.getLastLocation()
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude

                        Log.d("Location Service", "location update $location")

                    }
                }
            }, null)
        }
    }

    private fun getAddress(latitude: Double, longitude: Double): String? {
        val result = StringBuilder()
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            area = addresses.get(0).subAdminArea
            locality = addresses.get(0).locality
            if (addresses.size > 0) {
                val address = addresses[0]
                result.append(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName())
//                result.append(address.countryName)
            }
        } catch (e: IOException) {
            Log.e("tag", e.message)
        }
        return result.toString()
    }
}