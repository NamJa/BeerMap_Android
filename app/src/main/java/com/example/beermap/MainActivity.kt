package com.example.beermap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity() {

    private lateinit var bottomSheet: LinearLayout
    private lateinit var btnPersistent: Button
    private lateinit var bottomSheetStateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        setBtnExpandSheet()

        val sheetBehavior = BottomSheetBehavior.from(bottomSheet)

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
        bottomSheetStateText.text = "COLLAPSED"

    }

    private fun setBtnExpandSheet() {
        btnPersistent.text = "Expand sheet"
    }

    private fun setBtnCloseSheet() {
        btnPersistent.text = "Close sheet"
    }
}