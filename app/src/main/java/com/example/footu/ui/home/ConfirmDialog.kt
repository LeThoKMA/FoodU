package com.example.footu.ui.home

import android.os.Bundle
import android.view.View
import com.example.footu.base.BaseDialog
import com.example.footu.R
import com.example.footu.databinding.LogoutDialogBinding

open class ConfirmDialog(var callback: CallBack) :
    BaseDialog<LogoutDialogBinding>() {
    override fun getLayoutResource(): Int {
        return R.layout.logout_dialog
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
    }

    override fun setUp(view: View?) {
        binding.tvAcp.setOnClickListener {
            callback.accept()
            dismiss()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    interface CallBack {
        fun accept()
    }
}
