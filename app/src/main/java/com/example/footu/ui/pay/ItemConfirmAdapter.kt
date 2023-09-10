package com.example.footu.ui.pay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.databinding.ItemChooseBinding
import com.example.footu.model.DetailItemChoose

class ItemConfirmAdapter(val list: MutableList<DetailItemChoose>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_choose,
            parent,
            false,
        ) as ItemChooseBinding
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(position, list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: ItemChooseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, item: DetailItemChoose?) {
            if (item?.imgUrl?.isNotEmpty() == true) {
                Glide.with(binding.root.context)
                    .load(item.imgUrl?.get(0)).into(binding.ivProduct)
            }
            binding.tvNameProduct.text = item?.name
            binding.amount.text = item?.totalPrice.toString()
            binding.tvPrice.text = item?.price.toString()
            binding.edtNumber.text = "x" + item?.count.toString()
            binding.ivUp.visibility = View.GONE
            binding.ivDown.visibility = View.GONE
            binding.ivCheck.visibility = View.GONE
        }
    }
}
