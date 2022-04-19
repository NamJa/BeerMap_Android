package com.example.beermap

import androidx.lifecycle.ViewModel


class MainViewModel() : ViewModel() {
    // for using Google Map
    var isInitializedMap = false
    var isMarkedUsrGPS = false
    var isNotEnabledGPS = true
}