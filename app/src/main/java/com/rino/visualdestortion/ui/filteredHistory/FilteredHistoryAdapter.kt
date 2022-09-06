package com.rino.visualdestortion.ui.filteredHistory

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.FilteredItemBinding
import com.rino.visualdestortion.model.pojo.history.Data
import com.rino.visualdestortion.model.pojo.history.Items
import com.rino.visualdestortion.model.pojo.history.ServiceData
import com.rino.visualdestortion.utils.Constants


class FilteredHistoryAdapter(
    private var filteredHistoryList: ArrayList<Data>,
    private val historyViewModel: FilteredHistoryViewModel, private val context: Context
) : RecyclerView.Adapter<FilteredHistoryAdapter.FilteredHistoryViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int

    ): FilteredHistoryViewHolder {
        return FilteredHistoryViewHolder(
            FilteredItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return filteredHistoryList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FilteredHistoryViewHolder, position: Int) {
        val temp = filteredHistoryList[position]
        holder.binding.periodTxt.text = Constants.convertNumsToArabic(temp.title ?: "")
        holder.binding.periodValue.text = Constants.convertNumsToArabic(temp.period)
        holder.binding.taskNumTxt.text =
            Constants.convertNumsToArabic(temp.count.toString()) + " " + context.getString(
                R.string.task
            )

        holder.binding.historyRecycle.visibility = View.VISIBLE
        var historyAdapter = SubItemFilteredHistoryAdapter(arrayListOf(), historyViewModel, context)
        val linearLayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        linearLayoutManager.stackFromEnd = true
        holder.binding.historyRecycle.apply {
            layoutManager = linearLayoutManager
            adapter = historyAdapter
        }
        historyAdapter.updateItems(temp.items)
        holder.binding.showAllTxt.setOnClickListener {
            historyViewModel.navToSeeAll(temp.period )
        }
        holder.binding.showAllBtn.setOnClickListener {
            historyViewModel.navToSeeAll(temp.period )
        }
        holder.binding.card.setOnClickListener {
            historyViewModel.navToSeeAll(temp.period )
        }

    }

    fun updateItems(newList: List<Data>) {
        filteredHistoryList.clear()
        filteredHistoryList.addAll(newList)
        notifyDataSetChanged()
    }

    fun clearList() {
        filteredHistoryList.clear()
        notifyDataSetChanged()
    }

    inner class FilteredHistoryViewHolder(val binding: FilteredItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}