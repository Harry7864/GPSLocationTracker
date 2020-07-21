package com.example.gpslocationtracker;


import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.gpslocationtracker.network.RestCall
import com.example.gpslocationtracker.network.RestClient
import com.example.gpslocationtracker.networkresponce.CommonResponce
import com.example.gpslocationtracker.utility.PreferenceManager
import com.example.gpslocationtracker.utility.VariableBag
import com.getlocationbackground.util.Util
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.material.snackbar.Snackbar
import com.patloew.rxlocation.RxLocation
import kotlinx.android.synthetic.main.activitys_main1.*
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity1 : AppCompatActivity(), MainView {
    var mLocationService: LocationService = LocationService()
    lateinit var mServiceIntent: Intent
    lateinit var mActivity: Activity
    var fullAddress: String = ""
    lateinit var locationText: TextView
    lateinit var tvTitle: TextView
    lateinit var addressText: TextView
    lateinit var lastUpdate: TextView
    lateinit var btnIn: Button
    lateinit var btnOut: Button


    lateinit var currentLongi: String
    lateinit var currentAddress: String
    var location: Location? = null
    var location1: Location? = null
    var currentLocation: Location? = null
    private lateinit var preferenceManager: PreferenceManager
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null

    var latitude: String = ""
    var longitude: String = ""
    internal var address: String? = null
    internal var area: String? = null
    internal var locality: String? = null

    var BASE_URL: String? = "https://gps.fincasys.in/api/"
    internal var restCall: RestCall? = null
    private var rxLocation: RxLocation? = null
    private var presenter: MainPresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitys_main1)

        preferenceManager = PreferenceManager(this)

        mActivity = this@MainActivity1

        rxLocation = RxLocation(this)
        rxLocation!!.setDefaultTimeout(25, TimeUnit.SECONDS)
        presenter = MainPresenter(rxLocation)
        presenter!!.attachView(this);
        lastUpdate = findViewById<TextView>(R.id.tv_last_update)
        btnIn = findViewById<Button>(R.id.btnIn)
        btnOut = findViewById<Button>(R.id.btnOut)
        tvTitle = findViewById<TextView>(R.id.tvTitle)
        locationText = findViewById<TextView>(R.id.tv_current_location)
        addressText = findViewById<TextView>(R.id.tv_current_address)

        tvTitle.text = "Welcome - " + preferenceManager.getKeyValueString("fullname")
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

        if (preferenceManager.getKeyValueString("btnStatus").equals("true")) {
            btnOut.visibility = View.VISIBLE
            btnIn.visibility = View.GONE
        } else if (preferenceManager.getKeyValueString("btnStatus").equals("false")) {

            btnOut.visibility = View.GONE
            btnIn.visibility = View.VISIBLE
        }

        requestPermissionsSafely(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), 200
        )

        btnIn.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this@MainActivity1, R.style.AlertDialogTheme)
            builder.setMessage("Do you want to In?")
                    .setCancelable(false)
                    .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>")) { dialog, id ->
                        btnOut.visibility = View.VISIBLE
                        btnIn.visibility = View.GONE
                        preferenceManager.setKeyValueString("btnStatus", "true")
                        mLocationService = LocationService()
                        mServiceIntent = Intent(this, mLocationService.javaClass)
                        preferenceManager.setKeyValueString("UserStatus", "USERIN")
                        UploadData();
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
                        preferenceManager.setKeyValueString("UserStatus", "USEROUT")
                        Toast.makeText(
                                mActivity,
                                getString(R.string.service_stop_successfully),
                                Toast.LENGTH_SHORT
                        ).show()
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
                        mLocationService = LocationService()
                        val intent = Intent(this@MainActivity1, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>")) { dialog, id -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun UploadData() {
        restCall = RestClient.createService(RestCall::class.java, VariableBag.BASE_URL)
        restCall!!.getLatLong(
                "user_location",
                preferenceManager.registredUSerID,
                fullAddress, area, locality, latitude, longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Subscriber<CommonResponce>() {
                    override fun onError(e: Throwable?) {
                        Toast.makeText(
                                this@MainActivity1,
                                e!!.message + "",
                                Toast.LENGTH_SHORT
                        ).show()

                    }

                    override fun onNext(t: CommonResponce?) {
                        Toast.makeText(
                                this@MainActivity1,
                                t!!.message + "",
                                Toast.LENGTH_SHORT
                        ).show()

                    }

                    override fun onCompleted() {
                    }

                });
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
//        MyApplication.getRefWatcher().watch(presenter);
        super.onDestroy()
    }

    override fun onLocationSettingsUnsuccessful() {
        TODO("Not yet implemented")
    }

    // View Interface

    override fun onLocationUpdate(location: Location) {
        val dateFormatter: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        dateFormatter.setLenient(false)
        val today = Date()
        val s: String = dateFormatter.format(today)
        lastUpdate.text = s
        locationText.text = location.latitude.toString() + ", " + location.longitude
        latitude = location.latitude.toString()
        longitude = location.longitude.toString()
    }

    override fun onAddressUpdate(address: Address) {
        addressText.setText(getAddressText(address))
        fullAddress = getAddressText(address).toString()
        area = address.subLocality
        locality = address.locality
        mLocationService = LocationService()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)) {
            startService(mServiceIntent)
        }
    }

    private fun getAddressText(address: Address): String? {
        var addressText: String? = ""
        val maxAddressLineIndex = address.maxAddressLineIndex
        for (i in 0..maxAddressLineIndex) {
            addressText += address.getAddressLine(i)
            if (i != maxAddressLineIndex) {
                addressText += "\n"
            }
        }
        return addressText
    }

    override fun onResume() {
        checkPlayServicesAvailable()
        mLocationService = LocationService()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (!Util.isMyServiceRunning(mLocationService.javaClass, mActivity)) {
            startService(mServiceIntent)
        }
        super.onResume()
    }

    private fun checkPlayServicesAvailable() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val status = apiAvailability.isGooglePlayServicesAvailable(this)
        if (status != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(status)) {
                apiAvailability.getErrorDialog(this, status, 1).show()
            } else {
                Snackbar.make(lastUpdate, "Google Play Services unavailable. This app will not work", Snackbar.LENGTH_INDEFINITE).show()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        presenter!!.detachView();

    }


}