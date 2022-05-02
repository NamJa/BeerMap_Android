package com.example.beermap.view_model

import androidx.databinding.BaseObservable
import com.example.beermap.firebase.PubData

class PubDataViewModel() : BaseObservable() {
    var pub: PubData? = null
    set(pub) {
        field = pub
        // RecyclerView.ViewHolder bind() 함수에서
        // 이 모델의 pub객체를 변경했음을 알 수 있는 방법이 없기 때문에
        notifyChange()
        // 이 함수를 사용하여 데이터 객체인 PubData의 모든 binding 속성값이 변경되었음을 바인딩 클래스에 알린다.
        // 특정 바인딩 속성값만 변경이 필요하다면
        /**
         *
        @get:Bindable
        var firstName: String = ""
            set(value) {
                field = value
                notifyPropertyChanged(BR.firstName)
        }
        * */
        // 이런식으로 사용하면 된다.
    }
    val pubTitle: String?
        get() = pub?.name

    val pubAddress: String?
        get() = pub?.address

    val pubMenu: String?
        get() = pub?.menu

    val pubLat: Double?
        get() = pub?.Lat

    val pubLng: Double?
        get() = pub?.Lng
}