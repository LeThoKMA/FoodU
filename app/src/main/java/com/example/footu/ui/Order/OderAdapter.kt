package com.example.footu.ui.Order

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.databinding.ItemCatgoryBinding
import com.example.footu.model.DetailItemChoose
import com.example.footu.model.Item
import com.example.footu.utils.formatToPrice

class OderAdapter(var list: ArrayList<Item?>, val callback: OrderInterface) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listState: HashMap<String, Boolean> = hashMapOf()
    var listCount: HashMap<Int, Int> = hashMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_catgory,
            parent,
            false,
        ) as ItemCatgoryBinding
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        list[position]?.let {
            (holder as ViewHolder).bind(
                position,
                callback,
                it,
                listState,
                listCount,

            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: ItemCatgoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            position: Int,
            callback: OrderInterface,
            item: Item?,
            listState: HashMap<String, Boolean>,
            listCount: HashMap<Int, Int>,

        ) {
            if (item?.imgUrl?.isNotEmpty() == true) {
                Glide.with(binding.root.context)
                    .load(item.imgUrl?.get(0)).into(binding.ivProduct)
            }
            binding.tvNameProduct.text = item?.name
            binding.amount.text = item?.amount.toString()
            binding.amount.visibility = View.INVISIBLE
            binding.tvPrice.text = item?.price.formatToPrice()

            if (listCount.containsKey(position)) {
                binding.edtNumber.text = listCount[position].toString()
            } else {
                binding.edtNumber.text = "1"
            }
            if (listState.containsKey(item?.id.toString())) {
                binding.ivCheck.isChecked = listState[item?.id.toString()] == true
            } else {
                binding.ivCheck.isChecked = false
            }
            binding.ivCheck.setOnClickListener {
                if (binding.ivCheck.isChecked) {
                    listState.set(item?.id.toString(), true)
                } else {
                    listState.set(item?.id.toString(), false)
                }
                item?.let { it1 -> check(position, callback, it1, listState) }
            }

            binding.ivUp.setOnClickListener {
                val count = binding.edtNumber.text.toString().toInt()
                if (binding.edtNumber.text.toString().toInt() < item?.amount!!
                ) {
                    binding.edtNumber.setText(count.plus(1).toString())
                } else {
                    binding.edtNumber.setText(item.amount!!.toString())
                }
                listCount.set(position, binding.edtNumber.text.toString().toInt())
                check(position, callback, item, listState)
            }
            binding.edtNumber.setOnClickListener {
                val dialog = ConfirmDialog(object : ConfirmDialog.CallBack {
                    override fun accept(count: String) {
                        if (count.toInt() > item?.amount!!) {
                            binding.edtNumber.text = item.amount.toString()
                        } else {
                            if (count.toInt() == 0) {
                                binding.edtNumber.text = "1"
                            } else {
                                binding.edtNumber.text = count
                            }
                        }
                        listCount.put(position, binding.edtNumber.text.toString().toInt())
                        if (binding.ivCheck.isChecked) {
                            check(position, callback, item, listState)
                        }
                    }
                })
                dialog.show((binding.root.context as AppCompatActivity).supportFragmentManager, "")
            }

            binding.ivDown.setOnClickListener {
                val count = binding.edtNumber.text.toString().toInt()
                if (binding.edtNumber.text.toString().toInt() > 1) {
                    binding.edtNumber.text = count.minus(1).toString()
                } else {
                    binding.edtNumber.text = "1"
                }
                listCount.put(position, binding.edtNumber.text.toString().toInt())
                //   listCount.set(position, binding.edtNumber.text.toString().toInt())

                item?.let { it1 -> check(position, callback, it1, listState) }
            }
        }

        fun check(
            position: Int,
            callback: OrderInterface,
            item: Item,
            listState: HashMap<String, Boolean>,
        ) {
            // if (listState[item.id.toString()] == true) {
            val priceItem = binding.edtNumber.text.toString().toInt() * (item.price ?: 0)

            val detailBill = DetailItemChoose(
                item.id,
                item.name,
                binding.edtNumber.text.toString().toInt(),
                priceItem,
                item.price,
                item.imgUrl,
                flag = listState[item.id.toString()],
            )
            callback.addItemToBill(detailBill)
            // } else {
            //     callback.removeItem(item.id!!)
            // }
        }
    }

    fun resetData() {
        listState.clear()
        listCount.clear()
        notifyDataSetChanged()
    }
}
