package com.example.beermap

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beermap.databinding.ActivityMainBinding
import com.example.beermap.databinding.ItemPubdataRecyclerviewBinding
import com.example.beermap.firebase.PubData
import com.example.beermap.view_model.MainViewModel
import com.example.beermap.view_model.PubDataViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
private const val LOCATION_PERMISSIONS_REQUEST = 1000
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewModel: MainViewModel

    val sheetBehavior by lazy {
        BottomSheetBehavior.from(binding.bottomSheet)
    }
    lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userCurLat: Double = 0.0
    private var userCurLng: Double = 0.0
    private var isMapZoomed: Boolean = false

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            MainViewModel::class.java)
        binding.mainViewModel = viewModel
        binding.fragmentManager = supportFragmentManager


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        initView()


        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        // bottom Sheet RecyclerView ??????
        val pubData = (application as MasterApplication).pubDataListLiveData
        pubData.observe(
            this,
            Observer { pubDataList ->
                CoroutineScope(Dispatchers.Main).launch {
                    pubDataList.forEach { pubData ->
                        val pubLoc = LatLng(pubData.Lat, pubData.Lng)
                        val marker = map.addMarker(MarkerOptions()
                            .position(pubLoc)
                            .title(pubData.name)
                        )
                        marker!!.tag = pubData
                    }
                }
                binding.recyclerView.adapter = PubDataRecyclerViewAdapter(pubDataList)
            }
        )

        //GPS ?????? ??????
        binding.mapGPSButton.setOnClickListener{
            if(ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                onCheckPermission()
            } else {
                // ????????? ???????????? ???????????? ??????
                if (viewModel.isNotEnabledGPS) { // GPS??? ???????????? ???????????????
                    requestCurrentLocation()
                    viewModel.isMarkedUsrGPS = true
                    binding.mapGPSButton.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.gray_400))
                    viewModel.isNotEnabledGPS = false
                } else { // GPS??? ????????? ???????????????
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    binding.mapGPSButton.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.teal_200))
                    viewModel.isNotEnabledGPS = true
                    viewModel.isMarkedUsrGPS = false
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
        // ?????? ?????? ????????? ????????????
        if (!viewModel.isInitializedMap) {
            val seoul = LatLng(37.1, 128.0)
            // ????????? ??????
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 7f))
            viewModel.isInitializedMap = true
        }
        // GPS ?????? ??? ??????&??????????????? UI?????? ????????? ????????????
        if (!viewModel.isNotEnabledGPS) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false
            binding.mapGPSButton.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.gray_400))
            requestCurrentLocation()
        }
        // ?????? ?????????????????? ????????????????????? ???????????? ??? ?????? ??????
        val nightModeFlags = this@MainActivity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when(nightModeFlags) {
            Configuration.UI_MODE_NIGHT_NO -> {
                setMapStyle(R.raw.standard_map_stytle_json)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                setMapStyle(R.raw.night_map_style_json)
            }
        }
    }


    private fun setMapStyle(mapStyleJsonID: Int) {
        try {
            val isStyleParseSuccess = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MainActivity, mapStyleJsonID))
            if (!isStyleParseSuccess) {
                Log.e(TAG, "Style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ?????? ??????????????? ??? ?????? ?????? ??????
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun onCheckPermission() {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this@MainActivity, "?????? ????????? ?????? ????????? ????????? ???????????? ?????????.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSIONS_REQUEST)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSIONS_REQUEST)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 100
            fastestInterval = 50
        }
        // ?????? Map app??? ?????? ?????? ????????? ????????????.
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false
        // ?????? ?????? ??? ui ????????? ?????? ????????? ?????? ????????? ????????????.

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            userCurLat = lastLocation.latitude
            userCurLng = lastLocation.longitude

            // ?????? ??? ?????? gps ????????? ????????? ??? ??????????????? ?????? ????????? ?????????
            val newLatLng = if (isMapZoomed) {
                CameraUpdateFactory.newLatLng(LatLng(userCurLat, userCurLng))
            } else {
                isMapZoomed = true
                CameraUpdateFactory.newLatLngZoom(LatLng(userCurLat, userCurLng), 16f)
            }
            map.animateCamera(newLatLng)
            Log.d(TAG, "latitude: $userCurLat")
            Log.d(TAG, "longitude: $userCurLng")
        }
    }


    private fun initView() {
        // statusbar ?????????
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        if(Build.VERSION.SDK_INT >= 30) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        // ?????? ????????? ???????????? navigationBar??? statusBar??? rootview??? ????????? ????????? ??????.
        // statusBar??????, navigationBar?????? ?????? padding ?????? ????????????.
        binding.innerContainer.setPadding(
            0,
            0,
            0,
            this@MainActivity.navigationHeight()
        )

    }

    fun Context.navigationHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if(resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }



    inner class PubDataRecyclerViewAdapter(val pubDataList: List<PubData>) :
        RecyclerView.Adapter<PubDataRecyclerViewAdapter.ViewHolder>() {
        inner class ViewHolder(private val binding: ItemPubdataRecyclerviewBinding) : RecyclerView.ViewHolder(binding.root) {

            init {
                binding.viewModel = PubDataViewModel()
            }

            fun bind(pubData: PubData) {
                binding.viewModel?.pub = pubData
                binding.itemView.setOnClickListener {
                    val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(LatLng(pubData.Lat, pubData.Lng), 16f)
                    map.animateCamera(newLatLngZoom)
                    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                binding.executePendingBindings()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = DataBindingUtil.inflate<ItemPubdataRecyclerviewBinding>(layoutInflater, R.layout.item_pubdata_recyclerview, parent, false)
            binding.lifecycleOwner = this@MainActivity
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(pubDataList[position])
        }

        override fun getItemCount(): Int {
            return pubDataList.size
        }
    }
}