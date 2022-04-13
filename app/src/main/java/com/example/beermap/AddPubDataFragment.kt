package com.example.beermap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout


class AddPubDataFragment : Fragment() {
    private lateinit var pubDataFragmentContainer: ConstraintLayout
    private lateinit var toolbar: Toolbar
    private lateinit var pubTitle: EditText
    private lateinit var pubAddress: EditText
    private lateinit var pubMenu: EditText
    private lateinit var searchAddressBtn: Button
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        return view
    }

    private fun initView(view: View) {
        // 부모 Activity에서 상태바와 네비게이션바의 영역 설정을 했기 때문에, 자식 fragment에서도 똑같이 설정
        pubDataFragmentContainer = view.findViewById(R.id.pubDataFragmentContainer)
        pubDataFragmentContainer.setPadding(
            0,
            statusBarHeight(),
            0,
            0
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