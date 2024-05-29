package com.example.footu.ui.map.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.R
import com.example.footu.model.BillItem
import com.example.footu.utils.displayImage

class ItemAdapter() : androidx.recyclerview.widget.ListAdapter<BillItem, ItemAdapter.ViewHolder>(
    object :
        DiffUtil.ItemCallback<BillItem>() {
        override fun areItemsTheSame(
            oldItem: BillItem,
            newItem: BillItem,
        ): Boolean {
            return oldItem.item?.id == newItem.item?.id
        }

        override fun areContentsTheSame(
            oldItem: BillItem,
            newItem: BillItem,
        ): Boolean {
            return oldItem == newItem
        }
    },
) {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val img = view.findViewById<ImageView>(R.id.imgItem)

        fun bind(url: String) {
            img.displayImage(url)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        if (getItem(position).item?.imgUrl?.isNotEmpty() == true) {
            holder.bind(getItem(position).item?.imgUrl?.get(0)!!)
        }
    }
}
