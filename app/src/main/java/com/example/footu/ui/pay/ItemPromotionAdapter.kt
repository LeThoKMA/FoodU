package com.example.footu.ui.pay

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.databinding.ItemPromotionBinding
import com.example.footu.model.PromotionUser

class ItemPromotionAdapter(
    var list: MutableList<PromotionUser>,
    val onItemPicked: PromotionCallBack,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_promotion,
            parent,
            false,
        ) as ItemPromotionBinding
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(list[position], onItemPicked, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: ItemPromotionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PromotionUser, onItemPicked: PromotionCallBack, position: Int) {
            if (item.image.isNotEmpty() == true) {
                Glide.with(binding.root.context)
                    .load(item.image).into(binding.img)
            }
            binding.tvPromotionName.text = item.promotionDetail

            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (!item.isPicked) R.color.white else R.color.blue,
                ),
            )

            binding.root.setOnClickListener {
                if (item.isPicked) {
                    item.isPicked = false
                    binding.root.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.white,
                        ),
                    )
                } else {
                    item.isPicked = true
                    binding.root.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.blue,
                        ),
                    )
                }
                onItemPicked.pick(position)
            }
        }
    }
}

interface PromotionCallBack {
    fun pick(index: Int)
}
