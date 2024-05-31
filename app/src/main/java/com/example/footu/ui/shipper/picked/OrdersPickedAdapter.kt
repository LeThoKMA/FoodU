package com.example.footu.ui.shipper.picked

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.footu.R
import com.example.footu.databinding.ItemOrdersPickedBinding
import com.example.footu.model.OrderShipModel
import com.example.footu.utils.formatToPrice

class OrdersPickedAdapter(
    private val callBack: OrderPickedCallback,
) : ListAdapter<OrderShipModel, ViewHolder>(
        object : DiffUtil.ItemCallback<OrderShipModel>() {
            override fun areItemsTheSame(
                oldItem: OrderShipModel,
                newItem: OrderShipModel,
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: OrderShipModel,
                newItem: OrderShipModel,
            ): Boolean {
                return oldItem.id == newItem.id
            }
        },
    ) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.item_orders_picked,
                parent,
                false,
            ) as ItemOrdersPickedBinding
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        (holder as ViewHolder).bindView(getItem(position), callBack)
    }

    class ViewHolder(val binding: ItemOrdersPickedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(
            item: OrderShipModel,
            callBack: OrderPickedCallback,
        ) {
            val adapter = ChildPickedAdapter()
            binding.rcOrder.adapter = adapter
            adapter.submitList(item.billItemList)
            binding.tvOrderId.text = "Mã đơn: ${item.id}"
            binding.tvName.text = item.customer?.fullname
            binding.tvAddress.text = item.address
            binding.tvPrice.text = item.totalPrice.formatToPrice()
            binding.cardView.setOnClickListener {
                callBack.onClickDetail(item)
            }
        }
    }
}

interface OrderPickedCallback {
    fun onClickDetail(item: OrderShipModel)
}
