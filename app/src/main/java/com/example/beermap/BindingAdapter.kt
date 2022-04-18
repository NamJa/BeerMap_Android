package com.example.beermap

import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.DatabaseReference

/**
 * ADD PUB Button 클릭
 */
@BindingAdapter("app:setFgm")
fun callAddPubFragment(fab: ExtendedFloatingActionButton, fgm: FragmentManager) {
    fab.setOnClickListener {
        fgm.beginTransaction()
            .replace(R.id.innerContainer, AddPubDataFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }
}

/**
 * MainActivity의 BottomSheet 관련 동작 코드
 */
@BindingAdapter(value = ["app:addPubFAB", "app:mapGPSFAB"], requireAll = true)
fun SheetBehavior(
    sheet: ConstraintLayout,
    addPubFAButton: ExtendedFloatingActionButton,
    mapGPSButton: ExtendedFloatingActionButton
) {
    val sheetBehavior = BottomSheetBehavior.from(sheet)
    val fadeIn = AnimationUtils.loadAnimation(sheet.context, R.anim.fade_in)
    val fadeOut = AnimationUtils.loadAnimation(sheet.context, R.anim.fade_out)

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
                    addPubFAButton.visibility = View.VISIBLE
                    mapGPSButton.visibility = View.GONE
                    addPubFAButton.startAnimation(fadeIn)
                    mapGPSButton.startAnimation(fadeOut)
                }
                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    addPubFAButton.startAnimation(fadeOut)
                    addPubFAButton.visibility = View.GONE
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
}


/**
 * AddPubDataFragment의 registerPubButton 동작 코드, updateData()까지 포함
 */
@BindingAdapter(value=["app:pubTitle", "app:pubAddress", "app:pubMenu"], requireAll = true)
fun pubRegister(registerPubButton: Button, pubTitle: EditText, pubAddress: EditText, pubMenu: EditText) {
    registerPubButton.setOnClickListener {

        val dbReference = (registerPubButton.context.applicationContext as MasterApplication).databaseReference
        val totalPubNum = (registerPubButton.context.applicationContext as MasterApplication).totalPubNum
        if (pubTitle.text!!.isEmpty() || pubAddress.text!!.isEmpty() || pubMenu.text!!.isEmpty()) {
            Toast.makeText(registerPubButton.context, "각 입력 필드에 올바른 값을 채워주세요", Toast.LENGTH_SHORT).show()
        } else {
            updateData(
                dbReference,
                totalPubNum,
                pubAddress.text.toString(),
                pubMenu.text.toString(),
                pubTitle.text.toString(),
                AddPubDataFragment.addPubLatitude,
                AddPubDataFragment.addPubLongitude
            )
            pubTitle.setText("")
            pubAddress.setText("")
            pubMenu.setText("")
        }
    }
}
private fun updateData (
    dbReference: DatabaseReference,
    totalPubNum: Int,
    address: String, menu: String, name: String, Lat: Double, Lng: Double
){
    val pub: Map<String, Any> = mapOf("address" to address, "menu" to menu, "name" to name, "Lat" to Lat, "Lng" to Lng)
    val pubNo = "pubNo$totalPubNum"
    Log.d("BindingAdapter", pubNo)
    dbReference.child(pubNo).updateChildren(pub)
}