package com.speed.car.ui.main

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.speed.car.R
import com.speed.car.core.BaseFragment
import com.speed.car.databinding.FragmentMainBinding
import com.speed.car.interfaces.OnGpsServiceUpdate
import com.speed.car.model.Data
import com.speed.car.model.History
import com.speed.car.model.SOSPeople
import com.speed.car.notification.ChannelDetail
import com.speed.car.notification.NotificationChannelType
import com.speed.car.notification.NotificationContent
import com.speed.car.notification.NotificationRepository
import com.speed.car.services.GpsServices
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class MainFragment : BaseFragment<MainViewModel, FragmentMainBinding>(), LocationListener,
    OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,
    GoogleMap.OnMarkerClickListener {
    private val defaultZoom = 17.0f
    private var onGpsServiceUpdate: OnGpsServiceUpdate? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mLocationManager: LocationManager
    private lateinit var sharedPreferences: SharedPreferences
    private val defaultLocation = LatLng(16.0668632, 108.2112561)
    private var locationPermissionGranted = false
    private lateinit var mediaPlayer: MediaPlayer
    private var tts: TextToSpeech? = null

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    private lateinit var locationCallback: LocationCallback

    private val notificationRepository: NotificationRepository by inject()
    var fetchFirstFlag = true

    companion object {
        lateinit var data: Data
    }

    override val viewModel: MainViewModel by viewModel()

    override fun getViewBinding(): FragmentMainBinding = FragmentMainBinding.inflate(layoutInflater)
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    override fun viewBinding() {
        setHasOptionsMenu(true)
        binding.viewModel = viewModel
        initViews()
        observers()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        data = Data(onGpsServiceUpdate)
        mLocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                markCurrentLocation(locationResult)
            }
        }
        mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.police)
        onGpsServiceUpdate = object : OnGpsServiceUpdate {
            override fun update() {
                Log.d("xxx", "update: ")
                var maxSpeedTemp = data.maxSpeed
                var distanceTemp = data.distance
                var averageTemp: Double = if (sharedPreferences.getBoolean("auto_average", false)) {
                    data.averageSpeedMotion
                } else {
                    data.averageSpeed
                }

                val speedUnits: String
                val distanceUnits: String
                if (sharedPreferences.getBoolean("miles_per_hour", false)) {
                    maxSpeedTemp *= 0.62137119
                    distanceTemp = distanceTemp / 1000.0 * 0.62137119
                    averageTemp *= 0.62137119
                    speedUnits = "mi/h"
                    distanceUnits = "mi"
                } else {
                    speedUnits = "km/h"
                    if (distanceTemp <= 1000.0) {
                        distanceUnits = "m"
                    } else {
                        distanceTemp /= 1000.0
                        distanceUnits = "km"
                    }
                }

                val s = SpannableString(String.format("%.0f %s", maxSpeedTemp, speedUnits))
                s.setSpan(RelativeSizeSpan(0.5f), s.length - speedUnits.length - 1, s.length, 0)
                viewModel.maxSpeedView.postValue(s.toString())

                val sVa = SpannableString(String.format("%.0f %s", averageTemp, speedUnits))
                sVa.setSpan(RelativeSizeSpan(0.5f), s.length - speedUnits.length - 1, s.length, 0)
                viewModel.avaView.postValue(sVa.toString())

                val sDis = SpannableString(String.format("%.3f %s", distanceTemp, distanceUnits))
                sDis.setSpan(
                    RelativeSizeSpan(0.5f),
                    s.length - distanceUnits.length - 1,
                    s.length,
                    0
                )
                viewModel.distanceView.postValue(sDis.toString())
            }

        }
        data = Data(onGpsServiceUpdate)
    }

    override fun onStart() {
        super.onStart()
        onGrantPermissionNeeded()
        tts = TextToSpeech(
            requireActivity()
        ) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val textToSay = "Thiết bị đã được kết nối dữ liệu trên Ứng Dụng Team SPEED MOTOR"
                tts?.speak(textToSay, TextToSpeech.QUEUE_ADD, null)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mLocationManager.allProviders.indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, this)
        } else {
            Log.w(
                "MainActivity",
                "No GPS location provider found. GPS data display will not be available."
            )
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog()
        }
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val currentLocationRequest = LocationRequest()
        currentLocationRequest.setInterval(500)
            .setFastestInterval(0)
            .setMaxWaitTime(0)
            .setSmallestDisplacement(0F).priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationProviderClient.requestLocationUpdates(
            currentLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
        tts?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().stopService(Intent(activity, GpsServices::class.java))
    }

    override fun onLocationChanged(location: Location) {
        viewModel.onLocationChangeSpeed(location)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onGrantPermissionNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CALL_PHONE,
                    )
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CALL_PHONE,
                    )
                )
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it.values.any { it == false }) {
                Toast.makeText(activity, "Permission request failed", Toast.LENGTH_LONG).show()
            }
        }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
        getDeviceLocation()
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this)
    }

    private fun initViews() {
        setHasOptionsMenu(true)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        (requireActivity() as? AppCompatActivity)?.let {
            it.supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
            }
        }
        mDrawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            R.string.drawer_open,
            R.string.drawer_close
        )
        mDrawerToggle?.syncState()
        binding.drawerLayout.addDrawerListener(mDrawerToggle!!)
        binding.navigationDrawer.apply {
            setNavigationItemSelectedListener(this@MainFragment)
            bringToFront()
        }

        val menuItem = binding.navigationDrawer.menu.findItem(R.id.action_sos)
        val switchId = menuItem.actionView as SwitchCompat
        switchId.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnableSoS(isChecked)
        }
    }

    private fun observers() {
        viewModel.currentSpeed.observe(viewLifecycleOwner) {
            binding.viewSpeed.speedTo(speed = it.first)
        }
        viewModel.isOverSpeedLimit.observe(viewLifecycleOwner) {
            if (it.first) {
                val channelDetail = ChannelDetail(
                    NotificationChannelType.SPEED_CAR,
                    NotificationManager.IMPORTANCE_MAX
                )
                val notificationContent =
                    NotificationContent(
                        1234,
                        "Speed Motor",
                        "Tốc độ của bạn đã vượt quá giới hạn cho phép ${it.second}"
                    )
                notificationRepository.sendNotification(channelDetail, notificationContent)
                viewModel.insertHistoryLimit(
                    History(
                        id=Date().time.toInt(),
                        exceedSpeedTime = Date(),
                        exceedSpeedKilometers = "${it.second} km",
                        exceedSpeedKilometersAtArea = viewModel.currentWayName.toString()
                    )
                )
            }
        }

        viewModel.speedAddress.observe(viewLifecycleOwner) {
//            if (it?.address != viewModel.currentWayName) {
//                val speedMotor = if (viewModel.isMotorMode.value == true) 40 else 30
//                tts?.speak(
//                    "Bạn đang đi trên đường ${it?.address} với tốc độ cho phép xe của bạn là $speedMotor",
//                    TextToSpeech.QUEUE_ADD,
//                    null
//                )
//            }
        }

        viewModel.voiceRate.observe(viewLifecycleOwner) {
            voiceSpeed(it)
        }

        viewModel.combineSettingAndDataSOS.observe(viewLifecycleOwner) {
            val menuItem = binding.navigationDrawer.menu.findItem(R.id.action_sos)
            val switchId = menuItem.actionView as SwitchCompat
            switchId.isChecked = it.first
            if (it.first) {
                showSosMarker(it.second ?: listOf())
            } else {
                clearSosMarker()
            }
        }
    }

    private fun markCurrentLocation(locationResult: LocationResult) {
        val lastLocation = locationResult.lastLocation
        val addresses: List<Address>?
        val geocoder = Geocoder(requireActivity(), Locale.getDefault())
        addresses = kotlin.runCatching {
            geocoder.getFromLocation(lastLocation.latitude, lastLocation.longitude, 20)
        }.getOrNull()
        addresses?.map {
            it.thoroughfare ?: ""
        }?.filter { it.isNotEmpty() }?.let {
            viewModel.checkSpeedLimit(it.first())
        }
        for (location in locationResult.locations) {
            with(location) {
                val current = LatLng(this.latitude, this.longitude)
                Log.d("xxx", "$location")
                if (fetchFirstFlag) {
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            current, defaultZoom
                        )
                    )
                }
                fetchFirstFlag = false
            }
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        Log.d("xxx", "getDeviceLocation: ")
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), defaultZoom
                                )
                            )
                        }
                    } else {
                        Log.d("xxx", "Current location is null. Using defaults.")
                        Log.e("xxx", "Exception: %s", task.exception)
                        mMap.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, defaultZoom)
                        )
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun showGpsDisabledDialog() {
        startActivity(Intent("android.settings.LOCATION_SOURCE_SETTINGS"))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_history -> findNavController().navigate(R.id.historyFragment)
            R.id.action_sos -> {

            }
            R.id.action_vehicle_max_speed -> {
                val menuItem = binding.navigationDrawer.menu.findItem(R.id.action_vehicle_max_speed)
                val switchId = menuItem.actionView as SwitchCompat
                val isChecked = viewModel.isMotorMode.value ?: false
                switchId.isChecked = !isChecked
                viewModel.setTurnVehicleSpeed(!isChecked)
                switchId.setOnCheckedChangeListener { _, isCheck ->
                    viewModel.setTurnVehicleSpeed(isCheck)
                }
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun voiceSpeed(status: Boolean) {
        if (status) {
            val channelDetail = ChannelDetail(
                NotificationChannelType.SPEED_CAR,
                NotificationManager.IMPORTANCE_MAX
            )
            val speedMotor = if (viewModel.isMotorMode.value == true) 50 else 40
            val notificationContent =
                NotificationContent(
                    1234,
                    "Speed Motor",
                    "Tốc độ của bạn đã vượt quá giới hạn cho phép $speedMotor"
                )
            notificationRepository.sendNotification(channelDetail, notificationContent)

            tts?.speak("Bạn đã vượt quá tốc độ cho phép", TextToSpeech.ERROR_INVALID_REQUEST, null)
        } else
            tts?.stop()
    }

    private fun showSosMarker(sosPeople: List<SOSPeople>) {
        sosPeople.map {
            Log.d("TAG", "showSosMarker: $it")
            Log.d("TAG", "showSosMarker: ${it.phoneNumber}")
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.lng, it.lat))
                    .title(it.name)
            )
            marker?.tag = it
            marker?.tag = it.phoneNumber
            marker
        }.let {
            viewModel.markers = it
        }
    }

    private fun clearSosMarker() {
        viewModel.markers.forEach { mLocationMarker ->
            mLocationMarker?.remove()
        }
        viewModel.markers = listOf()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        marker.showInfoWindow()
        // Retrieve the data from the marker.
        // Retrieve the data from the marker.
        var clickCount = marker.tag
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$clickCount"))
        startActivity(intent)
        return false
    }
}