package com.example.footu.ui.orderlist

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footu.Response.BillDetailResponse
import com.example.footu.base.BaseDialog
import com.example.footu.OrderStatus
import com.example.footu.R
import com.example.footu.databinding.DetailOrderDialogBinding

class DetailOrderDialog() : BaseDialog<DetailOrderDialogBinding>() {
    lateinit var adapter: ItemAdapter
    override fun getLayoutResource(): Int {
        return R.layout.detail_order_dialog
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
        val orderDetail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("order_detail", BillDetailResponse::class.java)
        } else {
            arguments?.getParcelable("order_detail")
        }
        binding.tvTime.text = orderDetail?.time ?: ""
        adapter = ItemAdapter(
            binding.root.context,
            orderDetail?.billItemList?.toMutableList() ?: mutableListOf(),
        )
        binding.rcItem.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rcItem.adapter = adapter
        binding.tvPrice.text = orderDetail?.totalPrice.toString() + " đ"
        val priceDiscount = (orderDetail?.usedPromotion?.percentage?.div(100f))?.times(
            orderDetail.totalPrice
                ?: 0,
        )?.toInt()
        binding.tvPromotionDiscount.text = "-$priceDiscount đ"
        if (orderDetail?.status == OrderStatus.PAID.value) {
            binding.tvAccept.visibility = View.GONE
        }
    }

    override fun setUp(view: View?) {
    }

    override fun onStart() {
        super.onStart()
        if (dialog?.window != null) {
            val width = resources.displayMetrics.widthPixels // Chiều rộng của màn hình
            val height = resources.displayMetrics.heightPixels // Chiều cao của màn hình
            val desiredWidth =
                (width * 0.8).toInt() // Kích thước mong muốn, ở đây là 80% chiều rộng màn hình
            val desiredHeight =
                (height * 0.6).toInt() // Kích thước mong muốn, ở đây là 60% chiều cao màn hình
            activity?.window?.decorView?.width?.let {
                dialog?.window?.setLayout(
                    desiredWidth,
                    desiredHeight,
                )
            }
        }
    }
}
