package com.example.footu.ui.shipper

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.databinding.ItemOrderShipBinding
import com.example.footu.model.OrderShipModel
import com.example.footu.utils.formatToPrice

class OrdersAdapter(
    var list: List<OrderShipModel>,
    val context: Context,
    val callBack: OnClickDetailCallBack,
) :
    RecyclerView.Adapter<ViewHolder>() {

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
        (holder as ViewHolder).bindView(list[position], callBack)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemOrderShipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(item: OrderShipModel, callBack: OnClickDetailCallBack) {
            item.billItemList.get(0).item?.imgUrl?.let {
                Glide.with(binding.root.context)
                    .load(it[0]).into(binding.imgItem)
            }
            binding.tvName.text = item.customer?.fullname
            //binding.tvPhone.text = item.customer?.phone
            binding.tvPrice.text = item.totalPrice.formatToPrice()
            binding.root.setOnClickListener {
                callBack.onClickDetail(item)
            }
        }
    }

    fun setData(list: List<OrderShipModel>) {
        this.list = list
        notifyDataSetChanged()
    }
}

interface OnClickDetailCallBack {
    fun onClickDetail(item: OrderShipModel)
}
