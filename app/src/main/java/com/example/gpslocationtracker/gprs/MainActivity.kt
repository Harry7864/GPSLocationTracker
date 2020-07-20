package com.example.gpslocationtracker;


import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.gpslocationtracker.utility.PreferenceManager
import com.getlocationbackground.util.Util
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.android.synthetic.main.activitys_main1.*
import java.io.IOException
import java.text.DateFormat
import java.util.*

class MainActivity1 : AppCompatActivity() {
    var mLocationService: LocationService = LocationService()
    lateinit var mServiceIntent: Intent
    lateinit var mActivity: Activity
    lateinit var currentLat: String
    lateinit var locationText: TextView
    lateinit var addressText: TextView
    lateinit var lastUpdate: TextView
    lateinit var btnIn: Button
    lateinit var btnOut: Button
    lateinit var userStatus: String
    lateinit var btnStatus: String
    lateinit var currentDateTimeString: String

    lateinit var currentLongi: String
    lateinit var currentAddress: String
    var location: Location? = null
    var location1: Location? = null
    var currentLocation: Location? = null
    private lateinit var preferenceManager: PreferenceManager
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitys_main1)
        preferenceManager = PreferenceManager(this)
        currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        mActivity = this@MainActivity1
        lastUpdate = findViewById<TextView>(R.id.tv_last_update)
        btnIn = findViewById<Button>(R.id.btnIn)
        btnOut = findViewById<Button>(R.id.btnOut)
        locationText = findViewById<TextView>(R.id.tv_current_location)
        addressText = findViewById<TextView>(R.id.tv_current_address)
        lastUpdate.text = currentDateTimeString
        if (!Util.isLocationEnabledOrNot(mActivity)) {
            Util.showAlertLocation(
                    mActivity,
                    getString(R.string.gps_enable),
                    getString(R.string.please_turn_on_gps),
                    getString(
                            R.string.ok
                    )
            )

        }
        else
        {
            SendLocation()
        }

        SendLocation()

        if (preferenceManager.getKeyValueString("btnStatus").equals("true")) {
            mLocationService = LocationService()
            mServiceIntent = Intent(this, mLocationService.javaClass)
            userStatus = "USERIN";
            preferenceManager.setKeyValueString("UserStatus", userStatus)
            if (!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)) {
                startService(mServiceIntent)
                Toast.makeText(
                        mActivity,
                        getString(R.string.service_start_successfully),
                        Toast.LENGTH_SHORT
                ).show()
            }
            btnOut.visibility = View.VISIBLE
            btnIn.visibility = View.GONE
        } else if (preferenceManager.getKeyValueString("btnStatus").equals("false")) {

            btnOut.visibility = View.GONE
            btnIn.visibility = View.VISIBLE
        }

        requestPermissionsSafely(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), 200
        )

//        CheckLocation()
        btnIn.setOnClickListener {

            val builder = android.app.AlertDialog.Builder(this@MainActivity1, R.style.AlertDialogTheme)
            builder.setMessage("Do you want to In?")
                    .setCancelable(false)
                    .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>")) { dialog, id ->
                        btnIn.visibility = View.GONE
                        btnOut.visibility = View.VISIBLE
                        preferenceManager.setKeyValueString("btnStatus", "true")
                        mLocationService = LocationService()
                        mServiceIntent = Intent(this, mLocationService.javaClass)
                        userStatus = "USERIN";
                        preferenceManager.setKeyValueString("UserStatus", userStatus)
                        if (!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)) {
                            startService(mServiceIntent)
                            Toast.makeText(
                                    mActivity,
                                    getString(R.string.service_start_successfully),
                                    Toast.LENGTH_SHORT
                            ).show()
                        } else {
//                            Toast.makeText(
//                                    mActivity,
//                                    getString(R.string.service_already_running),
//                                    Toast.LENGTH_SHORT
//                            ).show()
                        }
                    }
                    .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>")) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()


        }
        btnOut.setOnClickListener {

            val builder = android.app.AlertDialog.Builder(this@MainActivity1, R.style.AlertDialogTheme)
            builder.setMessage("Are you sure you want to Out?")
                    .setCancelable(false)
                    .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>")) { dialog, id ->
                        btnOut.visibility = View.GONE
                        btnIn.visibility = View.VISIBLE
                        preferenceManager.setKeyValueString("btnStatus", "false")
                        userStatus = "USEROUT"
                        preferenceManager.setKeyValueString("UserStatus", userStatus)
                        Toast.makeText(
                                mActivity,
                                getString(R.string.service_stop_successfully),
                                Toast.LENGTH_SHORT
                        ).show()
                        mLocationService = LocationService()
                        mServiceIntent = Intent(this, mLocationService.javaClass)

                        if (!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)) {
                            startService(mServiceIntent)
                            Toast.makeText(
                                    mActivity,
                                    getString(R.string.service_start_successfully),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>")) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()

        }
        ivLogout.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this@MainActivity1, R.style.AlertDialogTheme)
            builder.setMessage("Are you sure you want to logout?")
                    .setCancelable(false)
                    .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>")) { dialog, id ->
                        preferenceManager.clearPrefrence()
                        preferenceManager.setLoginSessionFalse()
                        userStatus = "USERLOGOUT"
                        preferenceManager.setKeyValueString("UserStatus", userStatus)

                        mLocationService = LocationService()
                        mServiceIntent = Intent(this, mLocationService.javaClass)

                        if (!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)) {
                            startService(mServiceIntent)
                        }
                        val intent = Intent(this@MainActivity1, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>")) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        }
    }

    fun CheckLocation() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = Objects.requireNonNull(lm).isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        try {
            network_enabled = Objects.requireNonNull(lm).isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder(this)
                    .setMessage("Location is Off1..")
                    .setPositiveButton("Open Location Setting") { paramDialogInterface, paramInt ->
                        try {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.setCancelable(false)
                    .show()
        }
    }

    private fun SendLocation() {
        getCurrentLocation(this@MainActivity1)
    }

    private fun getCurrentLocation(context: Context) {
        fusedLocationProviderClient = FusedLocationProviderClient(context)
        locationRequest = LocationRequest()
        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest!!.setFastestInterval(10000)
        locationRequest!!.setInterval(1000)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                location1 = locationResult.lastLocation
                if (location1 != null) {
                    currentLocation = location1
                    if (currentLocation != null) {
                        Handler().postDelayed({ updateLocation(currentLocation!!) }, 1000)
                    }
                }
                Log.e("##", "Location is set")
            }
        }, Looper.getMainLooper())
        try {
            if (currentLocation != null) {
                Handler().postDelayed({ updateLocation(this!!.currentLocation!!) }, 1000)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Location is not Getting...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocation(llocation: Location) {
        try {
            currentLat = "" + llocation.latitude
            currentLongi = "" + llocation.longitude
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        locationText.setText("Latitude:\t$currentLat  Longitude:\t$currentLongi")
        try {
            try {
                currentAddress = this!!.getAddress(llocation)!!
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            addressText.setText("Address :\t $currentAddress")
        } catch (e: java.lang.Exception) {
        }
    }

    fun getAddress(llocation: Location): String? {
        val geocoder: Geocoder
        val addresses: List<Address>
        var temp = ""
        geocoder = Geocoder(this@MainActivity1, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(llocation.latitude, llocation.longitude, 1)
//            locality = addresses[0].subLocality
//            area = addresses[0].subAdminArea
            return if (addresses[0].getAddressLine(1) != null) {
                addresses[0].getAddressLine(0)
            } else {
                if (addresses[0].subLocality != null) {
                    temp = temp + " " + addresses[0].subLocality
                    if (addresses[0].locality != null) {
                        temp = temp + " ," + addresses[0].locality
                        if (addresses[0].postalCode != null) {
                            temp = temp + " ," + addresses[0].postalCode
                        }
                    }
                } else if (addresses[0].locality != null) {
                    temp = temp + " " + addresses[0].locality
                    if (addresses[0].postalCode != null) {
                        temp = temp + " ," + addresses[0].postalCode
                    }
                } else {
                    temp = temp + " " + addresses[0].postalCode
                }
                temp
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissionsSafely(
            permissions: Array<String>,
            requestCode: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions!!, requestCode)

        }
    }

    override fun onDestroy() {
        if (::mServiceIntent.isInitialized) {
            stopService(mServiceIntent)
        }
        super.onDestroy()
    }


}