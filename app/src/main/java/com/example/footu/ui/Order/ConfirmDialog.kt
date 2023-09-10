package com.example.footu.ui.Order

import android.os.Bundle
import android.view.View
import com.example.footu.base.BaseDialog
import com.example.footu.R
import com.example.footu.databinding.CountDialogBinding

open class ConfirmDialog(var callback: CallBack) :
    BaseDialog<CountDialogBinding>() {
    override fun getLayoutResource(): Int {
        return R.layout.count_dialog
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
    }

    override fun setUp(view: View?) {
        binding.tvAcp.setOnClickListener {
            callback.accept(binding.edtFilter.text.toString())
            dismiss()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    interface CallBack {
        fun accept(count: String)
    }
}
