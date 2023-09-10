package com.example.footu

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityMainBinding
import com.example.footu.ui.Order.OrderViewModel
import com.example.footu.utils.hideSoftKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel : MainViewModel by viewModels()

    lateinit var currentFragment: Fragment
    private lateinit var targetFragment: Fragment

    override fun observerData() {
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(true)
    }

    override fun initListener() {
    }

    override fun initViewModel(): BaseViewModel {
      return viewModel
    }

    private fun initializeFragment(tag: String, createFragment: () -> Fragment): Fragment {
        return supportFragmentManager.findFragmentByTag(tag) ?: createFragment().also { fragment ->
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, fragment, tag)
//                .hide(fragment)
                .commit()
        }
    }

    private fun showFragment(currentFragment: Fragment, targetFragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, targetFragment)
            .commit()
        hideSoftKeyboard()
        loadingDialog?.hide()
    }
}
