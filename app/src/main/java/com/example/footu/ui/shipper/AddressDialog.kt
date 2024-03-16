package com.example.footu.ui.shipper

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.example.footu.R
import com.example.footu.base.BaseDialog
import com.example.footu.databinding.AddressDialogBinding

class AddressDialog(val callback: AddressCallBack) : BaseDialog<AddressDialogBinding>() {
    override fun getLayoutResource(): Int {
        return R.layout.address_dialog
    }

    override fun init(saveInstanceState: Bundle?, view: View?) {
    }

    override fun setUp(view: View?) {
        binding.imgDelivery.setOnClickListener {
            callback.delivery(1)
        }
        binding.imgStore.setOnClickListener { callback.onStore(0) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        return dialog
    }
}

interface AddressCallBack {
    fun delivery(type: Int)
    fun onStore(type: Int)
}
