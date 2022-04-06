package com.example.beermap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.beermap.firebase.PubData

class PubdataRecyclerViewAdapter(val context: Context, val pubDataList: List<PubData>) :
    RecyclerView.Adapter<PubdataRecyclerViewAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pubName: TextView
        private val pubAddress: TextView
        private val item: ConstraintLayout

        init {
            pubName = itemView.findViewById(R.id.pubName)
            pubAddress = itemView.findViewById(R.id.pubAddress)
            item = itemView.findViewById(R.id.itemView)
        }
        fun bind(pubData: PubData) {
            pubName.text = pubData.name
            pubAddress.text = pubData.address
            item.setOnClickListener {
                //test
                Toast.makeText(context, "${pubData.menu}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pubdata_recyclerview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pubDataList[position])
    }

    override fun getItemCount(): Int {
        return pubDataList.size
    }
}