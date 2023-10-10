package com.example.footu.ui.shipper

import android.os.Bundle
import android.view.View
import com.example.footu.R
import com.example.footu.base.BaseDialog
import com.example.footu.databinding.AddressDialogBinding
import com.example.footu.utils.toast

class AddressDialog(val callback: AddressCallBack) : BaseDialog<AddressDialogBinding>() {
    override fun getLayoutResource(): Int {
        return R.layout.address_dialog
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
    }

    override fun setUp(view: View?) {
        binding.tvAcp.setOnClickListener {
            if (binding.edtFilter.text.isBlank()) {
                binding.root.context.toast("Vui lòng nhập địa chỉ giao hàng")
            } else {
                callback.accept(binding.edtFilter.text.toString())
                dismiss()
            }
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }
}

interface AddressCallBack {
    fun accept(address: String)
}
