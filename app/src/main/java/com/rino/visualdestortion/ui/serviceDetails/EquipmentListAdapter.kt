package com.rino.visualdestortion.ui.serviceDetails

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rino.visualdestortion.databinding.ItemDetailsBinding
import com.rino.visualdestortion.model.pojo.history.EquipmentList
import com.rino.visualdestortion.utils.Constants

class EquipmentListAdapter(
    private var itemsList: ArrayList<EquipmentList>,
    private var context: Context
) :
    RecyclerView.Adapter<EquipmentListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int

    ): ItemViewHolder {
        return ItemViewHolder(
            ItemDetailsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }


    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.binding.itemCount.text =
            Constants.convertNumsToArabic(itemsList[position].count.toString())
        holder.binding.nameTxt.text = itemsList[position].name

    }


    fun updateItems(newList: List<EquipmentList>) {
        itemsList.clear()
        itemsList.addAll(newList)
        notifyDataSetChanged()
    }


    inner class ItemViewHolder(val binding: ItemDetailsBinding) :
        RecyclerView.ViewHolder(binding.root)


}