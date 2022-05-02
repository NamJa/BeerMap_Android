package com.example.beermap.view_model

import androidx.lifecycle.ViewModel


class MainViewModel() : ViewModel() {
    // for using Google Map
    var isInitializedMap = false
    var isMarkedUsrGPS = false
    var isNotEnabledGPS = true
}