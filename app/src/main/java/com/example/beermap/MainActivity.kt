package com.example.beermap

import android.content.Context
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var bottomSheet: ConstraintLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private val pubDataList: MutableList<PubData> = mutableListOf()
    private val pubMap: MutableMap<String, PubData> = mutableMapOf()

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("pubs")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        geocoder = Geocoder(this@MainActivity, Locale.KOREA)
        initView()

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        button.setOnClickListener {
            for (i in pubMap) {
                Log.d(TAG, "${i.key} : ${(i.value)}")
                val geo = geocoder.getFromLocationName(i.value.address, 1)
                updateData(i.key, i.value.address, i.value.menu, i.value.name, geo[0].latitude, geo[0].longitude)
            }
        }

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val result = data.getValue<PubData>()
                    pubDataList.add(result!!)
                    Log.d(TAG, "${data.key}")
                    pubMap.put(data.key!!, result)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    pubDataList.forEach { pubData ->
                        val pubLoc = LatLng(pubData.Lat, pubData.Lng)
                        map.addMarker(MarkerOptions()
                            .position(pubLoc)
                            .title(pubData.name))
                    }
                }
                recyclerView.adapter = PubDataRecyclerViewAdapter(pubDataList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadPost: onCancelled", error.toException())
            }
        })

        val sheetBehavior = BottomSheetBehavior.from(bottomSheet)
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
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }
        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val seoul = LatLng(37.1, 128.0)
        // 카메라 이동
        map.moveCamera(CameraUpdateFactory.newLatLng(seoul))

    }
    private fun updateData(pubNo: String, address: String, menu: String, name: String, Lat: Double, Lng: Double){
        val pub: Map<String, Any> = mapOf("address" to address, "menu" to menu, "name" to name, "Lat" to Lat, "Lng" to Lng)
        databaseReference.child(pubNo).updateChildren(pub)
    }

    private fun initView() {
        bottomSheet = findViewById(R.id.bottomSheet)
        recyclerView = findViewById(R.id.recyclerView)
        button = findViewById(R.id.button)
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