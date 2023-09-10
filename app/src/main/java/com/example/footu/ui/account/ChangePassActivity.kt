package com.example.footu.ui.account

import androidx.activity.viewModels
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityChangePassBinding
import com.example.footu.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePassActivity :
    BaseActivity<ActivityChangePassBinding>() {
    private val viewModel: AccountViewModel by viewModels()
    override fun getContentLayout(): Int {
        return R.layout.activity_change_pass
    }

    override fun initView() {
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(false)
    }

    override fun initListener() {
        binding.tvSave.setOnClickListener {
            viewModel.changePass(
                binding.edtPassOld.text.toString(),
                binding.editPassNew.text.toString(),
                binding.edtPassRepeat.text.toString(),
            )
        }
        binding.imvBack.setOnClickListener { finish() }
    }

    override fun observerData() {
        viewModel.message.observe(this) {
            toast(it)
            finish()
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }
}
