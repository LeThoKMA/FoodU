package com.example.footu.ui.Order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.databinding.ItemCatgoryBinding
import com.example.footu.model.DetailItemChoose
import com.example.footu.utils.formatToPrice

class OderAdapter(var list: MutableList<DetailItemChoose>, val callback: OrderInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_catgory,
            parent,
            false,
        ) as ItemCatgoryBinding
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        list[position].let {
            (holder as ViewHolder).bind(
                position,
                callback,
                it,
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: ItemCatgoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            position: Int,
            callback: OrderInterface,
            item: DetailItemChoose,
        ) {
            if (item.imgUrl.isNotEmpty() == true) {
                Glide.with(binding.root.context)
                    .load(item.imgUrl[0]).into(binding.ivProduct)
            }
            binding.tvNameProduct.text = item.name
            binding.tvPrice.text = item.priceForSize.formatToPrice()
            binding.root.rootView.setOnClickListener { callback.detailItem(position) }

            binding.edtNumber.text = item.count.toString()
            binding.ivCheck.isChecked = item.flag

            binding.ivCheck.setOnClickListener {
                callback.selectItem(binding.ivCheck.isChecked, position)
            }

            binding.ivUp.setOnClickListener {
                callback.plusItem(position)
            }
            binding.edtNumber.setOnClickListener {
                callback.editCount(position)
            }

            binding.ivDown.setOnClickListener {
                callback.subtractItem(position)
            }
        }
    }

    fun resetData() {
        notifyDataSetChanged()
    }
}
