package com.example.beermap.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class PubData(var address: String, var name: String, var menu: String) {
    constructor() : this("", "", "")
}