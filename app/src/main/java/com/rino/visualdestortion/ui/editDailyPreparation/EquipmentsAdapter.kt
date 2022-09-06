package com.rino.visualdestortion.ui.editDailyPreparation

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rino.visualdestortion.R
import com.rino.visualdestortion.databinding.EquipmentItemBinding
import com.rino.visualdestortion.model.pojo.dailyPraperation.PrepEquipments
import com.rino.visualdestortion.utils.Constants


class EquipmentsAdapter(private var itemsList: ArrayList<PrepEquipments>, private var viewModel: EditDailyPViewModel, private var context: Context) :
        RecyclerView.Adapter<EquipmentsAdapter.ItemViewHolder>() {

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
            holder.binding.itemCount.text = Constants.convertNumsToArabic(itemsList[position].count.toString())
            holder.binding.nameTxt.text = itemsList[position].name
            holder.binding.plusImg.setOnClickListener {
                itemsList[position].count = itemsList[position].count?.plus(1)
                holder.binding.itemCount.text = Constants.convertNumsToArabic(itemsList[position].count.toString())
            }
            holder.binding.minImg.setOnClickListener {
                if (itemsList[position].count!! > 1) {
                    itemsList[position].count = itemsList[position].count?.minus(1)
                    holder.binding.itemCount.text = Constants.convertNumsToArabic(itemsList[position].count.toString())
                }
            }
            holder.binding.deleteItem.setOnClickListener {
             customTwoButtonsDialog(itemsList[position],position)
            }
        }
        private fun customTwoButtonsDialog(equipmentItem: PrepEquipments, position: Int) {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(R.string.app_name)

            builder.setMessage(R.string.dialogMessage)
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            builder.setNegativeButton(R.string.NoMessage) { dialogInterface, which ->

            }

            builder.setPositiveButton(R.string.yesMessage) { dialogInterface, which ->
                viewModel.setEquipmentDeletedItem(itemsList[position])
                itemsList.remove(itemsList[position])
                notifyDataSetChanged()

            }
            // Create the AlertDialog
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        fun updateItems(newList: List<PrepEquipments>) {
            itemsList.clear()
            itemsList.addAll(newList)
            notifyDataSetChanged()
        }

    fun getEquipmentMap():Map<Long,Int>
    {
        var hashmap = HashMap<Long,Int>()
        for (item in itemsList)
        {
           hashmap[item.id.toLong()] = item.count
        }
        return hashmap
    }

        inner class ItemViewHolder(val binding: EquipmentItemBinding) :
            RecyclerView.ViewHolder(binding.root)



    }