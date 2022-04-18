package com.example.beermap

import android.app.Application
import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MasterApplication: Application() {

    private lateinit var database : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    var totalPubNum = 0

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("pubs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.Main).launch {
                    totalPubNum = snapshot.children.count()
                    Log.d("Master", "${totalPubNum}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddPubDataFragmentViewModel", "loadData: onCancelled", error.toException())
            }
        })
    }
}