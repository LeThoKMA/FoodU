package com.example.footu.ui.Order

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityOrderBinding
import com.example.footu.model.DetailItemChoose
import com.example.footu.model.Item
import com.example.footu.ui.account.AccountFragment
import com.example.footu.ui.pay.ConfirmActivity
import com.example.footu.ui.shipper.AddressCallBack
import com.example.footu.ui.shipper.AddressDialog
import com.example.footu.utils.BILL_RESPONSE
import com.example.footu.utils.ITEMS_PICKED
import com.example.footu.utils.ORDER_TYPE
import com.example.footu.utils.ORDER_TYPE_DIALOG
import com.example.footu.utils.formatToPrice
import com.example.footu.utils.toast
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderActivity :
    BaseActivity<ActivityOrderBinding>(), OrderInterface {

    private val viewModel: OrderViewModel by viewModels()

    var listItem: ArrayList<Item?> = arrayListOf()
    var listItemChoose: MutableList<DetailItemChoose> = mutableListOf()
    lateinit var oderAdapter: OderAdapter

    override fun getContentLayout(): Int {
        return R.layout.activity_order
    }

    override fun initView() {
        val type = intent.getIntExtra("TYPE", 0)
        viewModel.getProductByType(type)
        oderAdapter = OderAdapter(listItem, this)
        binding.rvCategory.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rvCategory.adapter = oderAdapter
    }

    override fun initListener() {
        binding.tvCreate.setOnClickListener {
            if (viewModel.price.value == 0) {
                binding.root.context.toast("Hãy chọn sản phẩm")
                return@setOnClickListener
            }
            val dialog = AddressDialog(object : AddressCallBack {
                override fun delivery(type: Int) {
                    val intent = Intent(binding.root.context, ConfirmActivity::class.java)
                    intent.putParcelableArrayListExtra(
                        ITEMS_PICKED,
                        listItemChoose as java.util.ArrayList<out Parcelable>,
                    )
                    intent.putExtra(ORDER_TYPE, type)
                    intent.putExtra("price", viewModel.price.value)
                    startActivity(intent)
                    (supportFragmentManager.findFragmentByTag(ORDER_TYPE_DIALOG) as? AddressDialog)?.dismiss()
                }

                override fun onStore(type: Int) {
                    val intent = Intent(binding.root.context, ConfirmActivity::class.java)
                    intent.putParcelableArrayListExtra(
                        ITEMS_PICKED,
                        listItemChoose as java.util.ArrayList<out Parcelable>,
                    )
                    intent.putExtra(ORDER_TYPE, type)
                    intent.putExtra("price", viewModel.price.value)
                    startActivity(intent)
                    (supportFragmentManager.findFragmentByTag(ORDER_TYPE_DIALOG) as? AddressDialog)?.dismiss()
                }
            })
            dialog.show(supportFragmentManager, ORDER_TYPE_DIALOG)
        }
        binding.ivAccount.setOnClickListener {
            startActivity(Intent(binding.root.context, AccountFragment::class.java))
        }
    }

    override fun addItemToBill(item: DetailItemChoose) {
        viewModel.addItemToBill(item)
        if (item.flag == true) {
            if (listItemChoose.find { it.id == item.id } == null) {
                listItemChoose.add(item)
            } else {
                for (i in 0 until listItemChoose.size) {
                    if (listItemChoose[i].id == item.id) {
                        listItemChoose[i] = item
                        break
                    }
                }
            }
        } else {
            if (listItemChoose.find { it.id == item.id } == null) return
            val index = listItemChoose.indexOf(listItemChoose.find { it.id == item.id })
            listItemChoose.removeAt(index)
        }
    }

    override fun detailItem(item: Item) {
//        val dialog = DetailItemFragment.newInstance(item)
//        dialog.show(supportFragmentManager, DetailItemFragment.TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun observerData() {
        viewModel.dataItems.observe(this) {
            if (it != null) {
                listItem.clear()
                listItem.addAll(it)
                oderAdapter.resetData()
            }
        }
        viewModel.price.observe(this) {
            binding.tvPrice.text = it.formatToPrice()
        }
        viewModel.message.observe(this) {
            Toast.makeText(binding.root.context, it, Toast.LENGTH_LONG).show()
        }
        viewModel.confirm.observe(this) {
            if (it != null) {
                val bundle = Bundle()
                bundle.putString(BILL_RESPONSE, Gson().toJson(it))
                bundle.putParcelableArrayList(
                    ITEMS_PICKED,
                    listItemChoose as java.util.ArrayList<out Parcelable>,
                )
                val intent = Intent(binding.root.context, ConfirmActivity::class.java)
                intent.putExtra("data", bundle)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        oderAdapter.resetData()
        listItemChoose.clear()
        binding.tvPrice.text = ""
    }
}
