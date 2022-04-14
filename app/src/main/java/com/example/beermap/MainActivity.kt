package com.example.beermap

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beermap.firebase.PubData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"
private const val LOCATION_PERMISSIONS_REQUEST = 1000

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val sheetBehavior by lazy {
        BottomSheetBehavior.from(bottomSheet)
    }
    private val fadeIn by lazy {
        AnimationUtils.loadAnimation(this, R.anim.fade_in)
    }
    private val fadeOut by lazy {
        AnimationUtils.loadAnimation(this, R.anim.fade_out)
    }
    private lateinit var innerContainer: ConstraintLayout
    private lateinit var bottomSheet: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var floatingButton: ExtendedFloatingActionButton
    private lateinit var mapGPSButton: ExtendedFloatingActionButton
    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var pubDataList: MutableList<PubData> = mutableListOf()
    private var userCurLat: Double = 0.0
    private var userCurLng: Double = 0.0
    private var isNotEnabledGPS: Boolean = true
    private var isMapZoomed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // LIGHT MODE 설정
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("pubs")


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        initView()

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        // firebase data 수신
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pubDataList = mutableListOf()
                for (data in snapshot.children) {
                    val result = data.getValue<PubData>()
                    pubDataList.add(result!!)
                }
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
                recyclerView.adapter = PubDataRecyclerViewAdapter(pubDataList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadPost: onCancelled", error.toException())
            }
        })

        // bottom sheet 동작 지정
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        floatingButton.visibility = View.VISIBLE
                        mapGPSButton.visibility = View.GONE
                        floatingButton.startAnimation(fadeIn)
                        mapGPSButton.startAnimation(fadeOut)
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        floatingButton.startAnimation(fadeOut)
                        floatingButton.visibility = View.GONE
                        mapGPSButton.startAnimation(fadeIn)
                        mapGPSButton.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }
        })

        // floating 버튼 동작
        floatingButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.innerContainer, AddPubDataFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        //GPS 버튼 동작
        mapGPSButton.setOnClickListener{
            if(ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                onCheckPermission()
            } else {
                // 여기에 위치정보 받아오는 역할
                if (isNotEnabledGPS) { // GPS가 비활성화 되어있다면
                    requestCurrentLocation()
                    mapGPSButton.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.gray_400))
                    isNotEnabledGPS = false
                } else { // GPS가 활성화 되어있다면
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    mapGPSButton.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.teal_200))
                    isNotEnabledGPS = true
                }
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val seoul = LatLng(37.1, 128.0)
        // 카메라 이동
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 7f))
        map.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
    }


    override fun onDestroy() {
        super.onDestroy()
        // 위치 업데이트를 할 필요 없는 시점
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    private fun onCheckPermission() {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this@MainActivity, "현재 위치를 받기 위해선 권한을 허용해야 합니다.", Toast.LENGTH_SHORT).show()
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
            interval = 50
            fastestInterval = 20
        }
        // 다른 Map app과 같이 현재 위치를 표시한다.
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        // 기능 설정 및 ui 설정도 해야 화면에 파란 마커가 표시된다.

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()!!)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            userCurLat = lastLocation.latitude
            userCurLng = lastLocation.longitude

            // 최초 한 번만 gps 버튼을 눌렀을 때 확대되면서 현재 위치를 보여줌
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
        bottomSheet = findViewById(R.id.bottomSheet)
        recyclerView = findViewById(R.id.recyclerView)
        innerContainer = findViewById(R.id.innerContainer)
        floatingButton = findViewById(R.id.floatingButton)
        mapGPSButton = findViewById(R.id.MapGPSButton)
        // statusbar 투명화
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        if(Build.VERSION.SDK_INT >= 30) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        // 위의 작업을 수행하면 navigationBar와 statusBar가 rootview의 영역과 겹치게 된다.
        // statusBar높이, navigationBar높이 만큼 padding 값을 부여한다.
        innerContainer.setPadding(
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
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val pubName: TextView
            private val pubAddress: TextView
            private val item: ConstraintLayout

            init {
                pubName = itemView.findViewById(R.id.pubName)
                pubAddress = itemView.findViewById(R.id.pubAddress)
                item = itemView.findViewById(R.id.itemView)
            }
            fun bind(pubData: PubData) {
                pubName.text = pubData.name
                pubAddress.text = pubData.address
                item.setOnClickListener {
                    //test
                    Toast.makeText(this@MainActivity, "${pubData.menu}", Toast.LENGTH_SHORT).show()
                    val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(LatLng(pubData.Lat, pubData.Lng), 16f)
                    map.animateCamera(newLatLngZoom)
                    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pubdata_recyclerview, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(pubDataList[position])
        }

        override fun getItemCount(): Int {
            return pubDataList.size
        }
    }
}