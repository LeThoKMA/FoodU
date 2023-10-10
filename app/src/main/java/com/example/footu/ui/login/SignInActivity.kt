package com.example.footu.ui.login

import android.content.Intent
import androidx.activity.viewModels
import com.example.footu.MainActivity
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivitySignInBinding
import com.example.footu.ui.Order.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : BaseActivity<ActivitySignInBinding>() {

    private val viewModel: LoginViewModel by viewModels()
    override fun initListener() {
        binding.btnSignIn.setOnClickListener {
            viewModel.signIn(
                binding.edtName.text.toString(),
                binding.edtPasswd.text.toString(),
            )
        }
        binding.tvRegister.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java,
                ),
            )
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_sign_in
    }

    override fun initView() {
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
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
    }
}
