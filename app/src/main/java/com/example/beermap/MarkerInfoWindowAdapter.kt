package com.example.beermap

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.beermap.firebase.PubData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        val pub = marker.tag as? PubData ?: return null
        val view = LayoutInflater.from(context).inflate(R.layout.marker_info_contents, null)
        view.findViewById<TextView>(R.id.marker_pub_name).text = pub!!.name
        view.findViewById<TextView>(R.id.marker_pub_address).text = pub!!.address
        view.findViewById<TextView>(R.id.marker_pub_menu).text = pub!!.menu
        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }
}