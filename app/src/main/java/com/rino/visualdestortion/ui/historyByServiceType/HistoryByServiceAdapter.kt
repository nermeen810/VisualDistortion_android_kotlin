package com.rino.visualdestortion.ui.historyByServiceType

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.ServiceHistoryItemBinding
import com.rino.visualdestortion.databinding.SubFilteredHistoryItemBinding
import com.rino.visualdestortion.model.pojo.history.ServiceData
import com.rino.visualdestortion.utils.Constants

import kotlin.collections.ArrayList

class HistoryByServiceAdapter (private var historyList: ArrayList<ServiceData>,
                               private val historyViewModel: HistoryByServiceViewModel, private val context: Context) : RecyclerView.Adapter<HistoryByServiceAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int

    ): HistoryViewHolder {
        return HistoryViewHolder(
            SubFilteredHistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.binding.serviceNumValue.text = Constants.convertNumsToArabic(historyList[position].serviceNumber.toString())
        holder.binding.dateFromTxt.text     = Constants.convertNumsToArabic(historyList[position].createdDate?:"")
  //      holder.binding.timeTxt.text         = historyList[position].createdDate?: "00/00/00 00:00".split(" ").toList()[1]
        holder.binding.card.setOnClickListener {
         historyViewModel.navToServiceDetails(historyList[position])
        }
        holder.binding.addressTxt.setOnClickListener {
         showDialog(historyList[position].fullLocation)
        }
        holder.binding.locationTxt.setOnClickListener {
            navToLocationInMap(historyList[position].latitude?:"0",historyList[position].longtitude?:"0")
        }
    }
    fun showDialog(address: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage("${context.getString(R.string.full_address)} \n ${address}")
        builder.setIcon(R.drawable.ic_baseline_location_on_24)
        builder.setNeutralButton(R.string.cancel) { dialogInterface, which ->

        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    fun navToLocationInMap(lat : String, long :String){
        val uri = "http://maps.google.com/maps?q=loc:${lat},${long}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        context.startActivity(intent)
    }

    fun updateItems(newList: List<ServiceData>) {
   //     historyList.clear()
        historyList.addAll(newList)
        notifyDataSetChanged()
    }
    fun clearList() {
        historyList.clear()
        notifyDataSetChanged()
    }
    inner class HistoryViewHolder(val binding:  SubFilteredHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}