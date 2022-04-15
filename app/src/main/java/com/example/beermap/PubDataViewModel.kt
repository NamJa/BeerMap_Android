package com.example.beermap

import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BaseObservable
import androidx.databinding.BindingAdapter
import com.example.beermap.firebase.PubData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PubDataViewModel() : BaseObservable() {
    var pub: PubData? = null
    set(pub) {
        field = pub
    }
    val pubTitle: String?
        get() = pub?.name

    val pubAddress: String?
        get() = pub?.address

    val pubMenu: String?
        get() = pub?.menu

    val pubLat: Double?
        get() = pub?.Lat

    val pubLng: Double?
        get() = pub?.Lng
}