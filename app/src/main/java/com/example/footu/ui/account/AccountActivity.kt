package com.example.footu.ui.account

import android.content.DialogInterface
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.example.footu.MyPreference
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityManageBinding
import com.example.footu.ui.login.SignInActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountActivity : BaseActivity<ActivityManageBinding>() {
    private val viewModel: AccountViewModel by viewModels()
    fun showDialog() {
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setMessage("Bạn có muốn đăng xuất")
                setPositiveButton(
                    "Đồng ý",
                    DialogInterface.OnClickListener { dialog, id ->
                        viewModel.logout()
                        dialog.dismiss()
                    },
                )
                setNegativeButton(
                    "Hủy",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    },
                )
            }
            builder.create()
        }
        alertDialog.show()
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_manage
    }

    override fun initView() {
        val user = MyPreference().getInstance(binding.root.context)?.getUser()
    }

    override fun initListener() {
        binding.tvLogout.setOnClickListener {
            showDialog()
        }
        binding.tvChangePass.setOnClickListener {
            startActivity(
                Intent(
                    binding.root.context,
                    ChangePassActivity::class.java,
                ),
            )
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun observerData() {
        viewModel.logout.observe(this) {
            if (it) {
                val intent = Intent(this, SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
    }
}
