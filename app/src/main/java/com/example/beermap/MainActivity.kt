package com.example.beermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.beermap.firebase.PubData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.util.zip.Inflater

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheet: LinearLayout
    private lateinit var btnPersistent: Button
    private lateinit var bottomSheetStateText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference


    private lateinit var name: TextView
    private lateinit var address: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("pubs")


        initView()
        setBtnExpandSheet()

        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var num = 0
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

        btnPersistent.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                setBtnCloseSheet()
            } else {
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                setBtnExpandSheet()
            }
        }


        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        bottomSheetStateText.text = "HIDDEN"
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        setBtnCloseSheet()
                        bottomSheetStateText.text = "EXPANDED"
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        setBtnExpandSheet()
                        bottomSheetStateText.text = "COLLAPSED"
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }
        })

    }
    private fun initView() {
        bottomSheet = findViewById(R.id.bottomSheet)
        btnPersistent = findViewById(R.id.btnPersistent)
        bottomSheetStateText = findViewById(R.id.bottomSheetStateText)
        recyclerView = findViewById(R.id.recyclerView)
        bottomSheetStateText.text = "COLLAPSED"



    }

    private fun setBtnExpandSheet() {
        btnPersistent.text = "Expand sheet"
    }

    private fun setBtnCloseSheet() {
        btnPersistent.text = "Close sheet"
    }
}