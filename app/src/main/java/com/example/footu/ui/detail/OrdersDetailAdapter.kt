package com.example.footu.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.R
import com.example.footu.model.OrderShipModel

class OrdersDetailAdapter(val onClick: (Int) -> Unit) :
    ListAdapter<OrderShipModel, OrdersDetailAdapter.DetailViewHolder>(object :
        DiffUtil.ItemCallback<OrderShipModel>() {
        override fun areItemsTheSame(oldItem: OrderShipModel, newItem: OrderShipModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderShipModel, newItem: OrderShipModel): Boolean {
            return oldItem.id == newItem.id
        }
    }) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(R.layout.item_orders, parent, false)
        return DetailViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bindView(getItem(position), onClick)
    }

    class DetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val recyclerView: RecyclerView = view.findViewById(R.id.rc_order)
        private val tvOrderId: TextView = view.findViewById(R.id.tv_orderId)
        private val tvLocation: TextView = view.findViewById(R.id.tv_location)
        fun bindView(orderShipModel: OrderShipModel, onClick: (Int) -> Unit) {
            val adapter = ChildAdapter()
            recyclerView.adapter = adapter
            adapter.submitList(orderShipModel.billItemList)
            tvOrderId.text = "Mã đơn: ${orderShipModel.id}"
            tvLocation.setOnClickListener { onClick.invoke(orderShipModel.shipper?.id ?: 0) }
        }
    }
}
