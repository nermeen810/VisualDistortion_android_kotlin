package com.rino.visualdestortion.ui.serviceDetails

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rino.visualdestortion.databinding.SliderViewBinding
import com.smarteist.autoimageslider.SliderViewAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


class SliderAdapter(
    private var context: Context,
    private var sliderDataArrayList: ArrayList<SliderData>
) :
    SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder>() {

    override fun getCount(): Int {
        return sliderDataArrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapter.SliderAdapterViewHolder {
        return SliderAdapterViewHolder(
            SliderViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        viewHolder: SliderAdapter.SliderAdapterViewHolder,
        position: Int
    ) {
        viewHolder.binding.titleTxt.text = sliderDataArrayList[position].title
        Picasso.with(context).load(sliderDataArrayList[position].imgURL)
            .into(viewHolder.binding.myimage)
    }

    inner class SliderAdapterViewHolder(val binding: SliderViewBinding) :
        SliderViewAdapter.ViewHolder(binding.root)
}

//        ( var itemView: View) :
//        ViewHolder(itemView) {
//
//        private var imageViewBackground: ImageView
//
//        init {
//            imageViewBackground = itemView.findViewById(R.id.myimage)
//
//        }
//    }
//}