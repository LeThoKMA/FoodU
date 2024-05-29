package com.example.footu.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.R
import com.example.footu.Response.HintMessageResponse
import com.example.footu.model.User
import com.example.footu.utils.displayImage
import com.example.footu.utils.nameToAvatar
import com.example.footu.utils.randomColor

class HintMessageAdapter(private val onClickDetail: (User) -> Unit) :
    ListAdapter<HintMessageResponse, HintMessageAdapter.ViewHolder>(
        object :
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
        },
    ) {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val rootView: ConstraintLayout = view.findViewById(R.id.root)
        private val avatar: ImageView = view.findViewById(R.id.avatar)
        private val name: TextView = view.findViewById(R.id.tvName)
        private val content: TextView = view.findViewById(R.id.tvContent)

        fun bindView(
            hintMessageResponse: HintMessageResponse,
            onClickDetail: (User) -> Unit,
        ) {
            avatar.setBackgroundColor(randomColor())
            avatar.displayImage(
                nameToAvatar(
                    hintMessageResponse.otherUser?.fullname.toString(),
                    60,
                    60,
                ),
            )
            name.text = hintMessageResponse.otherUser?.fullname
            val isYour =
                hintMessageResponse.otherUser != hintMessageResponse.lastMessage?.fromUser
            content.text =
                if (isYour && hintMessageResponse.lastMessage != null) "Bạn đã gửi tin nhắn" else "${hintMessageResponse.otherUser?.fullname} đã gửi tin nhắn"
            rootView.setOnClickListener {
                hintMessageResponse.otherUser?.let { it1 ->
                    onClickDetail.invoke(
                        it1,
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_hint_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bindView(getItem(position), onClickDetail)
    }
}
