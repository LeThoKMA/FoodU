package com.example.footu.ui.orderlist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.OrderStatus
import com.example.footu.R
import com.example.footu.databinding.ItemOrderBinding
import com.example.footu.model.OrderItem

class OrderListAdapter(var list: MutableList<OrderItem>, val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_order,
            parent,
            false,
        ) as ItemOrderBinding
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(list[position], onClick)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderItem, onClick: (Int) -> Unit) {
            binding.orderId.text = item.id.toString()
            binding.dateTime.text = item.time
            binding.orderPrice.text = item.totalPrice.toString()
            binding.orderStatus.text = when (item.status) {
                OrderStatus.PAID.value -> OrderStatus.PAID.name
                OrderStatus.CANCELLED.value -> OrderStatus.CANCELLED.name
                else -> OrderStatus.PENDING.name
            }
            binding.orderStatus.setTextColor(
                when (item.status) {
                    OrderStatus.PAID.value -> Color.GREEN
                    OrderStatus.CANCELLED.value -> Color.RED
                    else -> Color.YELLOW
                },
            )
            binding.root.setOnClickListener {
                item.id?.let { it1 -> onClick.invoke(it1) }
            }
        }
    }
}
