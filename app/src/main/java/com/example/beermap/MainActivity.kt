package com.example.beermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beermap.firebase.PubData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var bottomSheet: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var map: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("pubs")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initView()

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pubDataList: MutableList<PubData> = mutableListOf()
                for (data in snapshot.children) {
                    val result = data.getValue<PubData>()
                    pubDataList.add(result!!)
                    Log.d(TAG, "${result?.name}")
                }
                recyclerView.adapter = PubdataRecyclerViewAdapter(this@MainActivity, pubDataList)
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
    }

    private fun initView() {
        bottomSheet = findViewById(R.id.bottomSheet)
        recyclerView = findViewById(R.id.recyclerView)
    }
}