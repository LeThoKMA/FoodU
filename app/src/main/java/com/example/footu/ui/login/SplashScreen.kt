package com.example.footu.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.footu.MainActivity
import com.example.footu.MainActivityForUser
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivitySplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen : BaseActivity<ActivitySplashScreenBinding>() {
    private val viewModel: LoginViewModel by viewModels()

    override fun observerData() {
        viewModel.doLogin.observe(this) {
            if (it == 0) {
                startActivity(Intent(this, MainActivityForUser::class.java))
                finishAffinity()
            }
            if (it == 2) {
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
        }
        viewModel.loginBack.observe(this) {
            if (it) {
                startActivity(Intent(this, SignInActivity::class.java))
                finishAffinity()
            }
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_splash_screen
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView() {
        if (!viewModel.haveToken()) {
            startActivity(Intent(this, SignInActivity::class.java))
            finishAffinity()
        } else {
            viewModel.fetchUserInfo()
        }
    }

    override fun initListener() {
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }
}
