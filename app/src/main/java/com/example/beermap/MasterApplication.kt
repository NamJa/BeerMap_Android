package com.example.beermap

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.beermap.firebase.PubData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MasterApplication: Application() {

    private lateinit var database : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    private val _pubDataListLiveData: MutableLiveData<List<PubData>> = MutableLiveData()
    val pubDataListLiveData: LiveData<List<PubData>>
        get() = _pubDataListLiveData
    var totalPubNum = 0

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("pubs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.Main).launch {
                    totalPubNum = snapshot.children.count()
                    val pubDataList: MutableList<PubData> = mutableListOf()
                    for (data in snapshot.children) {
                        val result = data.getValue<PubData>()
                        pubDataList.add(result!!)
                    }
                    _pubDataListLiveData.value = pubDataList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MasterApplication", "loadData: onCancelled", error.toException())
            }
        })
    }
}