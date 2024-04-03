package com.example.footu.ui.Order

import android.content.Intent
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footu.R
import com.example.footu.TypeItem
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityOrderBinding
import com.example.footu.model.DetailItemChoose
import com.example.footu.ui.pay.ConfirmActivity
import com.example.footu.ui.shipper.AddressCallBack
import com.example.footu.ui.shipper.AddressDialog
import com.example.footu.utils.ITEMS_CHOOSE
import com.example.footu.utils.ITEMS_CHOOSE_ACTION
import com.example.footu.utils.ITEMS_PICKED
import com.example.footu.utils.ITEM_TYPE
import com.example.footu.utils.ORDER_TYPE
import com.example.footu.utils.ORDER_TYPE_DIALOG
import com.example.footu.utils.formatToPrice
import com.example.footu.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderActivity :
    BaseActivity<ActivityOrderBinding>(), OrderInterface {

    private val viewModel: OrderViewModel by viewModels()

    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    var listItem: MutableList<DetailItemChoose> = mutableListOf()
    lateinit var oderAdapter: OderAdapter

    override fun getContentLayout(): Int {
        return R.layout.activity_order
    }

    override fun initView() {
        val type = intent.getIntExtra(ITEM_TYPE, 0)
        viewModel.getProductByType(type)
        oderAdapter = OderAdapter(listItem, this)
        binding.tvTitle.text = when (type) {
            TypeItem.COFFEE.ordinal.plus(1) -> "Cà phê"
            TypeItem.CAKE.ordinal.plus(1) -> "Bánh ngọt"
            TypeItem.FREEZE.ordinal.plus(1) -> "Freeze"
            TypeItem.TEA.ordinal.plus(1) -> "Trà"
            else -> ""
        }
        binding.rvCategory.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rvCategory.adapter = oderAdapter

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    viewModel.resetData()
                }
            }
    }

    override fun initListener() {
        binding.tvAdd.setOnClickListener {
            val intent = Intent(ITEMS_CHOOSE_ACTION)
            intent.putParcelableArrayListExtra(ITEMS_CHOOSE, viewModel.getListItemChoose())
            sendBroadcast(intent)
            finish()
        }
        binding.tvCreateOrder.setOnClickListener {
            if (viewModel.price.value == 0) {
                toast("Hãy chọn sản phẩm")
                return@setOnClickListener
            }
            val dialog = AddressDialog(object : AddressCallBack {
                override fun delivery(type: Int) {
                    val intent = Intent(binding.root.context, ConfirmActivity::class.java)
                    intent.putParcelableArrayListExtra(
                        ITEMS_PICKED,
                        viewModel.getListItemChoose() as ArrayList<out Parcelable>,
                    )
                    intent.putExtra(ORDER_TYPE, type)
                    intent.putExtra("price", viewModel.price.value)
                    resultLauncher?.launch(intent)
                    (supportFragmentManager.findFragmentByTag(ORDER_TYPE_DIALOG) as? AddressDialog)?.dismiss()
                }

                override fun onStore(type: Int) {
                    val intent = Intent(binding.root.context, ConfirmActivity::class.java)
                    intent.putParcelableArrayListExtra(
                        ITEMS_PICKED,
                        viewModel.getListItemChoose() as ArrayList<out Parcelable>,
                    )
                    intent.putExtra(ORDER_TYPE, type)
                    intent.putExtra("price", viewModel.price.value)
                    resultLauncher?.launch(intent)
                    (supportFragmentManager.findFragmentByTag(ORDER_TYPE_DIALOG) as? AddressDialog)?.dismiss()
                }
            })
            dialog.show(supportFragmentManager, ORDER_TYPE_DIALOG)
        }
    }

    override fun detailItem(position: Int) {
        val dialog = DetailItemFragment.newInstance(listItem[position], onSelect = {
            listItem[position] = it
            oderAdapter.notifyItemChanged(position)
            viewModel.addItemToBill(it)
        })
        dialog.show(supportFragmentManager, DetailItemFragment.TAG)
    }

    override fun plusItem(position: Int) {
        listItem[position].count++
        oderAdapter.notifyItemChanged(position)
        viewModel.addItemToBill(listItem[position])
    }

    override fun subtractItem(position: Int) {
        if (listItem[position].count > 1) listItem[position].count--
        oderAdapter.notifyItemChanged(position)
        viewModel.addItemToBill(listItem[position])
    }

    override fun editCount(position: Int) {
        val dialog = ConfirmDialog(object : ConfirmDialog.CallBack {
            override fun accept(count: String) {
                val itemCount = count.toInt()
                listItem[position].count = itemCount
                oderAdapter.notifyItemChanged(position)
                viewModel.addItemToBill(listItem[position])
            }
        })
        dialog.show((binding.root.context as AppCompatActivity).supportFragmentManager, "")
    }

    override fun selectItem(flag: Boolean, position: Int) {
        listItem[position].flag = flag
        oderAdapter.notifyItemChanged(position)
        viewModel.addItemToBill(listItem[position])
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
                oderAdapter.notifyDataSetChanged()
            }
        }
        viewModel.price.observe(this) {
            binding.tvPrice.text = it.formatToPrice()
        }
        viewModel.message.observe(this) {
            Toast.makeText(binding.root.context, it, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }
}
