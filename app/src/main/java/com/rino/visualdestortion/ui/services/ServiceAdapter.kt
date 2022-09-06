package com.rino.visualdestortion.ui.services

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.rino.visualdestortion.databinding.ServiceItemBinding
import com.rino.visualdestortion.model.pojo.home.ServiceTypes
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class ServiceAdapter(
    private var servicesList: ArrayList<ServiceTypes>,
    private val serviceViewModel: ServiceViewModel,
    private val viewlifecyclerOwner: LifecycleOwner
) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int

    ): ServiceViewHolder {
        return ServiceViewHolder(
            ServiceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return servicesList.size
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.binding.serviceName.text = servicesList[position].name
        holder.binding.card.setOnClickListener {
            observeDailyPreparation(position)

        }

    }


    private fun observeDailyPreparation(position: Int) {
//        val date = DateFormat.getDateInstance().format(Calendar.getInstance().time).toString()
//        serviceViewModel.getDailyPreparationByServiceID(servicesList[position].id.toString(), date)
//        serviceViewModel.getDailyPreparation.observe(viewlifecyclerOwner) {
//            if (it != null) {
//                servicesList[position].let { it1 -> serviceViewModel.navToAddService(it1) }
//            } else {
//                servicesList[position].let { it1 ->
//                    serviceViewModel.navToDailyPreparation(it1)
//                }
//            }
//        }
        servicesList[position].let { it1 -> serviceViewModel.navToAddService(it1) }
    }

    fun updateServices(newFavoriteList: List<ServiceTypes>) {
        servicesList.clear()
        servicesList.addAll(newFavoriteList)
        notifyDataSetChanged()
    }

    inner class ServiceViewHolder(val binding: ServiceItemBinding) :
        RecyclerView.ViewHolder(binding.root)


}