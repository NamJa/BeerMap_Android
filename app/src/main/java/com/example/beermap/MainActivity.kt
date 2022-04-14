package com.example.beermap

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.ThemeUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beermap.firebase.PubData
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
import java.util.*

private const val TAG = "MainActivity"

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
    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private var pubDataList: MutableList<PubData> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // LIGHT MODE 설정
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("pubs")


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geocoder = Geocoder(this@MainActivity, Locale.KOREA)
        initView()

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        // firebase data 수신
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pubDataList = mutableListOf()
                for (data in snapshot.children) {
                    val result = data.getValue<PubData>()
                    pubDataList.add(result!!)
                    Log.d(TAG, "${data.key}")
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
                        floatingButton.startAnimation(fadeIn)
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        floatingButton.startAnimation(fadeOut)
                        floatingButton.visibility = View.GONE
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
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val seoul = LatLng(37.1, 128.0)
        // 카메라 이동
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 7f))
        map.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
    }


    private fun updateData(pubNo: String, address: String, menu: String, name: String, Lat: Double, Lng: Double){
        val pub: Map<String, Any> = mapOf("address" to address, "menu" to menu, "name" to name, "Lat" to Lat, "Lng" to Lng)
        databaseReference.child(pubNo).updateChildren(pub)
    }


    private fun initView() {
        bottomSheet = findViewById(R.id.bottomSheet)
        recyclerView = findViewById(R.id.recyclerView)
        innerContainer = findViewById(R.id.innerContainer)
        floatingButton = findViewById(R.id.floatingButton)
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
                    val newLatingZoom = CameraUpdateFactory.newLatLngZoom(LatLng(pubData.Lat, pubData.Lng), 16f)
                    map.animateCamera(newLatingZoom)
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