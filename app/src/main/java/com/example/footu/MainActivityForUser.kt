package com.example.footu

import android.content.pm.PackageManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityMainBinding
import com.example.footu.ui.Order.HomeActivity
import com.example.footu.utils.hideSoftKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivityForUser : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    lateinit var currentFragment: Fragment
    private lateinit var targetFragment: Fragment

    val homeFragment by lazy {
        HomeActivity()
    }
//
//    val orderListShipper by lazy {
//        initializeFragment(
//            "orders_shipper",
//        ) { OrderListScreen() }
//    }

    lateinit var pagerAdapter: FragmentNavigator

    override fun observerData() {
    }

    fun setupPager() {
        pagerAdapter = FragmentNavigator(this.supportFragmentManager, lifecycle)
        //pagerAdapter.addFragment(homeFragment)
        binding.pagger2.offscreenPageLimit = pagerAdapter.itemCount
        binding.pagger2.adapter = pagerAdapter
        binding.pagger2.isUserInputEnabled = false // disable swiping
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(true)
        val permissionNeeded = arrayOf(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
        )

        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.CAMERA",
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                "android.permission.RECORD_AUDIO",
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(permissionNeeded, 101)
        }
        setupPager()
    }

    override fun initListener() {
        binding.navigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    binding.pagger2.currentItem = 0
                    true
                }

//                R.id.navigation_profile -> {
//                    binding.pagger2.currentItem = 1
//                    true
//                }

                else -> {
                    false
                }
            }
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

//    private fun initializeFragment(tag: String, createFragment: () -> Fragment): Fragment {
//        return supportFragmentManager.findFragmentByTag(tag) ?: createFragment().also { fragment ->
//            supportFragmentManager
//                .beginTransaction()
//                .add(R.id.fragmentContainer, fragment, tag)
//                .hide(fragment)
//                .commit()
//        }
//    }

    private fun showFragment(currentFragment: Fragment, targetFragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .hide(currentFragment)
            .show(targetFragment)
            .commit()
        hideSoftKeyboard()
        loadingDialog?.hide()
    }
}
