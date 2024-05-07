package com.example.footu.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.R
import com.example.footu.Response.HintMessageResponse

class HintMessageAdapter() :
    ListAdapter<HintMessageResponse, HintMessageAdapter.ViewHolder>(object :
        DiffUtil.ItemCallback<HintMessageResponse>() {
        override fun areItemsTheSame(
            oldItem: HintMessageResponse,
            newItem: HintMessageResponse,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: HintMessageResponse,
            newItem: HintMessageResponse,
        ): Boolean {
            return oldItem == newItem
        }
    }) {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: ImageView = view.findViewById(R.id.avatar)
        private val name: TextView = view.findViewById(R.id.tvName)
        private val content: TextView = view.findViewById(R.id.tvContent)

        fun bindView(hintMessageResponse: HintMessageResponse) {
            name.text = hintMessageResponse.messageResponse.fromUser.username
            content.text = hintMessageResponse.messageResponse.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_hint_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }
}
