package com.example.footu.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.model.BillItem

class ChildAdapter :
    ListAdapter<BillItem, ChildAdapter.ViewHolder>(object : DiffUtil.ItemCallback<BillItem>() {
        override fun areItemsTheSame(oldItem: BillItem, newItem: BillItem): Boolean {
            return oldItem.item?.id == newItem.item?.id
        }

        override fun areContentsTheSame(oldItem: BillItem, newItem: BillItem): Boolean {
            return oldItem.item?.id == newItem.item?.id
        }
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(getItem(position))
    }

    class ViewHolder(
        val view: View,
    ) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.img_item)
        private val tvName: TextView = view.findViewById(R.id.tv_name)

        fun bindView(item: BillItem) {
            Glide.with(view.context).load(item.item?.imgUrl?.get(0)).into(img)
            tvName.text = "${item.item?.name} x${item.quantity}"
        }
    }
}
