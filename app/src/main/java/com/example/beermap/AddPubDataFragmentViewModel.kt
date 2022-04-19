package com.example.beermap

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class AddPubDataFragmentViewModel: ViewModel() {
    val _isAddressSearchBtnClicked: MutableLiveData<Boolean> = MutableLiveData()
    val isAddressSearchBtnClicked: LiveData<Boolean>
        get() = _isAddressSearchBtnClicked
    var getReceiveIntent: Intent = Intent()

}