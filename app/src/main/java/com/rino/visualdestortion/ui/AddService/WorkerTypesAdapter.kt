package com.rino.visualdestortion.ui.AddService

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rino.visualdestortion.databinding.EquipmentItemBinding


class WorkerTypesAdapter(private var itemsList: ArrayList<EquipmentItem>, private var viewModel: AddServiceViewModel) :
    RecyclerView.Adapter<WorkerTypesAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int

    ): ItemViewHolder {
        return ItemViewHolder(
            EquipmentItemBinding.inflate(
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
        holder.binding.itemCount.text = itemsList[position].count.toString()
        holder.binding.nameTxt.text = itemsList[position].name
        holder.binding.plusImg.setOnClickListener({
            itemsList[position].count = itemsList[position].count?.plus(1)
            holder.binding.itemCount.text = itemsList[position].count.toString()
        })
        holder.binding.minImg.setOnClickListener({
            itemsList[position].count = itemsList[position].count?.minus(1)
            holder.binding.itemCount.text = itemsList[position].count.toString()
        })
        holder.binding.deleteItem.setOnClickListener({
            println("deleted item"+itemsList[position])
            println("listAfterDeleted"+itemsList.toString())
            viewModel.setWorkerTypeDeletedItem(itemsList[position])
            itemsList.remove(itemsList[position])
            notifyDataSetChanged()
        })
    }

    fun updateItems(newFavoriteList: List<EquipmentItem>) {
        itemsList.clear()
        itemsList.addAll(newFavoriteList)
        notifyDataSetChanged()
    }
    fun getWorkerTypesMap():Map<Long,Int>
    {
        var hashmap = HashMap<Long,Int>()
        for (item in itemsList)
        {
            hashmap[item.id] = item.count
        }
        return hashmap
    }

    inner class ItemViewHolder(val binding: EquipmentItemBinding) :
        RecyclerView.ViewHolder(binding.root)


}