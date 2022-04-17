package com.example.beermap

import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton


@BindingAdapter("app:setFgm")
fun callAddPubFragment(fab: ExtendedFloatingActionButton, fgm: FragmentManager) {
    fab.setOnClickListener {
        fgm.beginTransaction()
            .replace(R.id.innerContainer, AddPubDataFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }
}

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