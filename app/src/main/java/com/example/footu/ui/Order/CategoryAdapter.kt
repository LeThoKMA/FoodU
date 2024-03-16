package com.example.footu.ui.Order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.Response.CategoryResponse

class CategoryAdapter(
    val list: List<CategoryResponse>,
    val onClickItem: (Int) -> Unit,
) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_type, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(list[position], onClickItem)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tv_type)
        private val img: ImageView = view.findViewById(R.id.img)
        fun bind(item: CategoryResponse, onClickItem: (Int) -> Unit) {
            tvName.text = item.name
            Glide.with(img.context).load(item.url).into(img)
            itemView.setOnClickListener { onClickItem.invoke(item.id) }
        }
    }
}
