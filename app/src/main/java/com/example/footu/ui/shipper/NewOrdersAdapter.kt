package com.example.footu.ui.shipper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.databinding.ItemOrderShipBinding
import com.example.footu.model.OrderShipModel
import com.example.footu.utils.formatToPrice

class NewOrdersAdapter(
    private val callBack: NewOnClickDetailCallBack,
) : ListAdapter<OrderShipModel, ViewHolder>(object : DiffUtil.ItemCallback<OrderShipModel>() {
    override fun areItemsTheSame(oldItem: OrderShipModel, newItem: OrderShipModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: OrderShipModel, newItem: OrderShipModel): Boolean {
        return oldItem.id == newItem.id
    }
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_order_ship,
            parent,
            false,
        ) as ItemOrderShipBinding
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindView(getItem(position), callBack)
    }

    class ViewHolder(val binding: ItemOrderShipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(item: OrderShipModel, callBack: NewOnClickDetailCallBack) {
            item.billItemList[0].item?.imgUrl?.let {
                Glide.with(binding.root.context)
                    .load(it[0]).into(binding.imgItem)
            }
            binding.tvName.text = item.customer.fullname
            binding.tvPhone.text = item.customer.phone
            binding.tvPrice.text = item.totalPrice.formatToPrice()
            binding.root.setOnClickListener {
                callBack.onClickDetail(item)
            }
        }
    }
}

interface NewOnClickDetailCallBack {
    fun onClickDetail(item: OrderShipModel)
}
