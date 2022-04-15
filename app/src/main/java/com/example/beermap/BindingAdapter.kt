package com.example.beermap

import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.example.beermap.firebase.PubData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior


@BindingAdapter(value= [ "app:pubMenu", "app:pubLat", "app:pubLng"], requireAll = true)
fun locateToMarker(itemView: ConstraintLayout, pubMenu: String, pubLat: Double, pubLng: Double) {
    Toast.makeText(itemView.context, pubMenu, Toast.LENGTH_SHORT).show()
//    val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(LatLng(pubLat, pubLng), 16f)
//    map.animateCamera(newLatLngZoom)
//    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
}