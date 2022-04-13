package com.example.beermap

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "AddPubDataFragment"

class AddPubDataFragment : Fragment() {
    private lateinit var pubDataFragmentContainer: ConstraintLayout
    private lateinit var toolbar: Toolbar
    private lateinit var pubTitle: EditText
    private lateinit var pubAddress: EditText
    private lateinit var pubMenu: EditText
    private lateinit var searchAddressBtn: Button
    private lateinit var registerBtn: Button

    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var totalPubdataNum: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // places api는 이렇게 초기화를 해줘야 한다.
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), resources.getString(R.string.google_map_api_key), Locale.KOREA)
        }
        // firebase initialize
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("pubs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.Main).launch {
                    totalPubdataNum = snapshot.children.count()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "loadData: onCancelled", error.toException())
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_pub_data, container, false)
        initView(view)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            (activity as AppCompatActivity).onBackPressed()
        }
        toolbar.title = "ADD PUB PAGE"

        // AutoCompleteActivity에서 Places API를 정상적으로 입력받았을 경우
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val place: Place = Autocomplete.getPlaceFromIntent(result.data!!)

                pubAddress.setText(place.address)
                latitude = place.latLng!!.latitude
                longitude = place.latLng!!.longitude
                Log.d(TAG, "lat: $latitude")
                Log.d(TAG, "lat: $longitude")
            }
        }

        searchAddressBtn.setOnClickListener {
            val fields = listOf(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(requireActivity())

            startForResult.launch(intent)
        }

        registerBtn.setOnClickListener {
            Log.d(TAG, "firebase data nums: $totalPubdataNum")
            if (pubTitle.text.isEmpty() || pubAddress.text.isEmpty() || pubMenu.text.isEmpty()) {
                Toast.makeText(context, "각 입력 필드에 올바른 값을 채워주세요", Toast.LENGTH_SHORT).show()
            } else {
                updateData(pubAddress.text.toString(), pubMenu.text.toString(), pubTitle.text.toString(), latitude, longitude)
                pubTitle.setText("")
                pubAddress.setText("")
                pubMenu.setText("")
            }
        }

        return view
    }

    private fun updateData(address: String, menu: String, name: String, Lat: Double, Lng: Double){
        val pub: Map<String, Any> = mapOf("address" to address, "menu" to menu, "name" to name, "Lat" to Lat, "Lng" to Lng)
        val pubNo = "pubNo$totalPubdataNum"
        databaseReference.child(pubNo).updateChildren(pub)
    }

    private fun initView(view: View) {
        // 부모 Activity에서 상태바와 네비게이션바의 영역 설정을 했기 때문에, 자식 fragment에서도 똑같이 설정
        pubDataFragmentContainer = view.findViewById(R.id.pubDataFragmentContainer)
        pubDataFragmentContainer.setPadding(
            0,
            statusBarHeight(),
            0,
            navigationHeight()
        )

        toolbar = view.findViewById(R.id.addPubToolbar)
        pubTitle = view.findViewById(R.id.pubTitleEditText)
        pubAddress = view.findViewById(R.id.pubAddressEditText)
        pubMenu = view.findViewById(R.id.pubMenuEditText)
        searchAddressBtn = view.findViewById(R.id.addressSearchButton)
        registerBtn = view.findViewById(R.id.registerPubButton)
    }


    fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if(resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    fun navigationHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if(resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }


    companion object {
        @JvmStatic
        fun newInstance() = AddPubDataFragment()
    }
}