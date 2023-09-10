package com.example.footu.ui.Order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.footu.model.DetailItemChoose
import com.example.footu.R
import com.example.footu.databinding.ItemCatgoryBinding

class ItemChooseAdapter(val context: Context, var list: MutableList<DetailItemChoose>) :
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
        (holder as ViewHolder).bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemCatgoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailItemChoose) {
            if (item.imgUrl?.isNotEmpty() == true) {
                Glide.with(binding.root.context)
                    .load(item.imgUrl!![0]).into(binding.ivProduct)
            } else {
                binding.ivProduct.setImageResource(R.drawable.ic_picture_nodata)
            }
            binding.tvNameProduct.text = item.name
            binding.amount.text = item.count.toString()
            binding.tvPrice.text = item.price.toString()
            binding.ivUp.visibility = View.INVISIBLE
            binding.ivDown.visibility = View.INVISIBLE
            binding.ivCheck.visibility = View.INVISIBLE
        }
    }
}
