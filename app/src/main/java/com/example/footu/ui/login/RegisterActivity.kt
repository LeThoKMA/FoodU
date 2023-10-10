package com.example.footu.ui.login

import android.content.Intent
import androidx.activity.viewModels
import com.example.footu.MainActivity
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityAddUserBinding
import com.example.footu.ui.Order.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity :
    BaseActivity<ActivityAddUserBinding>() {
    private val viewModel: LoginViewModel by viewModels()
    override fun getContentLayout(): Int {
        return R.layout.activity_add_user
    }

    override fun initView() {
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(false)
    }

    override fun initListener() {
        binding.tvRegister.setOnClickListener {
            viewModel.register(
                binding.edtPhone.text.toString(),
                binding.edtName.text.toString(),
                binding.edtPasswd.text.toString(),
                binding.edtConfirmPasswd.text.toString(),
            )
        }
    }

    override fun observerData() {
        viewModel.doLogin.observe(this) {
            if (it == 0) {
                startActivity(Intent(this, HomeActivity::class.java))
                finishAffinity()
            }
            if (it == 2) {
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
        }
        binding.imvBack.setOnClickListener { finish() }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }
}
