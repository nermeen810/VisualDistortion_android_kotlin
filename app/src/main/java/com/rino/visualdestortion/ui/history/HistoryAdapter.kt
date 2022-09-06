package com.rino.visualdestortion.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rino.visualdestortion.databinding.HistoryItemBinding
import com.rino.visualdestortion.model.pojo.history.Data
import com.rino.visualdestortion.model.pojo.history.ServiceTypeData
import com.rino.visualdestortion.utils.Constants

import kotlin.collections.ArrayList

class HistoryAdapter(
    private var historyList: ArrayList<ServiceTypeData>,
    private val historyViewModel: HistoryViewModel
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int

    ): HistoryViewHolder {
        return HistoryViewHolder(
            HistoryItemBinding.inflate(
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
        holder.binding.serviceName.text = historyList[position].serviceName
        holder.binding.tasksCountTxt.text =
            "${Constants.convertNumsToArabic(historyList[position].numberOfTasks.toString())} خدمة "
        holder.binding.dateFromTxt.text =
            Constants.convertNumsToArabic(historyList[position].dateFrom ?: "")
        holder.binding.dateToTxt.text =
            Constants.convertNumsToArabic(historyList[position].dateTo ?: "")
        holder.binding.daysCountTxt.text =
            "${Constants.convertNumsToArabic(historyList[position].numberOfDays.toString())} يوم "
        holder.binding.card.setOnClickListener {
            historyList[position].serviceId?.let { it1 -> historyViewModel.navToHistoryById(it1) }
        }

    }


    fun updateItems(newList: List<ServiceTypeData>) {
        historyList.clear()
        historyList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}