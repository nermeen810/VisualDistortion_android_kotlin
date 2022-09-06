package com.rino.visualdestortion.ui.filteredHistory

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.PeriodItemBinding


class PeriodAdapter(
    private var periodList: ArrayList<String>,
    private val filteredHistoryViewModel: FilteredHistoryViewModel
) : RecyclerView.Adapter<PeriodAdapter.PeriodViewHolder>() {

    var lastSelectedCard: CardView? = null
    var lastSelectedText: TextView? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int

    ): PeriodViewHolder {
        return PeriodViewHolder(
            PeriodItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: PeriodViewHolder, position: Int) {
        Log.e("lastPosition", FilteredHistoryViewModel.lastSelectedPos.toString())

        if (lastSelectedCard == null && position == FilteredHistoryViewModel.lastSelectedPos) {
            Log.e(
                "period",
                FilteredHistoryViewModel.periodTimeList_en[FilteredHistoryViewModel.lastSelectedPos]
            )

            lastSelectedCard = holder.binding.categoryCard
            lastSelectedText = holder.binding.categoryName

            lastSelectedCard?.setCardBackgroundColor(
                ContextCompat.getColor(
                    holder.binding.categoryCard.context,
                    R.color.teal
                )
            )
            lastSelectedText?.setTextColor(
                ContextCompat.getColor(
                    holder.binding.categoryCard.context,
                    R.color.white
                )
            )
        }
        val temp = periodList[position]
        holder.bind(temp, position)
    }

    inner class PeriodViewHolder(val binding: PeriodItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(period: String, position: Int) {

            binding.categoryName.text = period
            binding.categoryCard.setOnClickListener {
                FilteredHistoryViewModel.lastSelectedPos = position
                Log.e("lastPos", FilteredHistoryViewModel.lastSelectedPos.toString())
                filteredHistoryViewModel.getHistoryData(
                    filteredHistoryViewModel.serviceId,
                    FilteredHistoryViewModel.periodTimeList_en[position]
                )
                lastSelectedCard?.setCardBackgroundColor(
                    ContextCompat.getColor(
                        it.context,
                        R.color.white
                    )
                )
                lastSelectedText?.setTextColor(
                    ContextCompat.getColor(
                        it.context,
                        R.color.black
                    )
                )

                binding.categoryCard.setCardBackgroundColor(
                    ContextCompat.getColor(
                        it.context,
                        R.color.teal
                    )
                )
                binding.categoryName.setTextColor(
                    ContextCompat.getColor(
                        it.context,
                        R.color.white
                    )
                )
                lastSelectedCard = binding.categoryCard
                lastSelectedText = binding.categoryName
            }
        }
    }

    override fun getItemCount(): Int {
        return periodList.size
    }

}

