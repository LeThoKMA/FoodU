package com.example.footu.ui.login

import android.content.Intent
import androidx.activity.viewModels
import com.example.footu.MainActivity
import com.example.footu.MyPreference
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivitySplashScreenBinding
import com.example.footu.model.User
import com.example.footu.ui.Order.HomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreen : BaseActivity<ActivitySplashScreenBinding>() {
    private val viewModel: LoginViewModel by viewModels()

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

    override fun getContentLayout(): Int {
        return R.layout.activity_splash_screen
    }

    override fun initView() {
        val preference = MyPreference.getInstance(this)
        if (preference?.getUser() == User()) {
            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
        } else {
            viewModel.signIn(
                preference?.getUser()?.username.toString(),
                preference?.getPasswd().toString(),
            )
        }
    }

    override fun initListener() {
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }
}
