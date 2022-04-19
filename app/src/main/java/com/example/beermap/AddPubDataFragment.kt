package com.example.beermap

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.beermap.databinding.FragmentAddPubDataBinding
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import java.util.*

private const val TAG = "AddPubDataFragment"
private const val LOCATION_PERMISSIONS_REQUEST = 1000

class AddPubDataFragment : Fragment() {

    private lateinit var binding: FragmentAddPubDataBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    private lateinit var viewModel: AddPubDataFragmentViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // places api는 이렇게 초기화를 해줘야 한다.
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity(), resources.getString(R.string.google_map_api_key), Locale.KOREA)
        }
        // GeoCoder initialize
        geocoder = Geocoder(context, Locale.KOREA)
        viewModel = ViewModelProvider(this).get(AddPubDataFragmentViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_pub_data, container, false)
        binding.requireActivity = requireActivity()
        binding.viewModel = viewModel
        initView(binding.root)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            (activity as AppCompatActivity).onBackPressed()
        }
        binding.toolbar.title = "ADD PUB PAGE"

        // AutoCompleteActivity에서 Places API를 정상적으로 입력받았을 경우
        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val place: Place = Autocomplete.getPlaceFromIntent(result.data!!)

                binding.pubAddress.setText(place.address)
                addPubLatitude = place.latLng!!.latitude
                addPubLongitude = place.latLng!!.longitude
                Log.d(TAG, "lat: $addPubLatitude")
                Log.d(TAG, "lat: $addPubLongitude")
            }
        }
        // addressSearchButton 동작시 변경되는 Boolean 값을 관찰
        viewModel.isAddressSearchBtnClicked.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { isBtnClicked ->
                if (isBtnClicked) {
                    startForResult.launch(viewModel.getReceiveIntent)
                }
            }
        )

        binding.useCurLocationButton.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                onCheckPermission()
            } else {
                Toast.makeText(context, "위치를 가져오는 중입니다.", Toast.LENGTH_SHORT).show()
                requestNewLocationData()
            }
        }

        return binding.root
    }


    private fun initView(view: View) {
        // 부모 Activity에서 상태바와 네비게이션바의 영역 설정을 했기 때문에, 자식 fragment에서도 똑같이 설정
        binding.pubDataFragmentContainer.setPadding(
            0,
            statusBarHeight(),
            0,
            navigationHeight()
        )
    }


    private fun onCheckPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(requireContext(), "현재 위치를 받기 위해선 권한을 허용해야 합니다.", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSIONS_REQUEST)
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSIONS_REQUEST)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            location?.let {
                addPubLatitude = location.latitude
                addPubLongitude = location.longitude
                Log.d(TAG, "latitude: $addPubLatitude, longitude: $addPubLongitude")
                val geoCodeAddress = geocoder.getFromLocation(addPubLatitude, addPubLongitude, 1)
                val address = geoCodeAddress[0].getAddressLine(0)
                binding.pubAddress.setText(address)
            }
        }
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
        var addPubLatitude: Double = 0.0
        var addPubLongitude: Double = 0.0
        @JvmStatic
        fun newInstance() = AddPubDataFragment()
    }
}