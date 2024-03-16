package com.example.footu

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityMainBinding
import com.example.footu.ui.account.AccountFragment
import com.example.footu.ui.detail.OrderDetailFragment
import com.example.footu.ui.home.HomeFragment
import com.example.footu.utils.hideSoftKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivityForUser : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainUserViewModel by viewModels()

    val homeFragment by lazy {
        HomeFragment()
    }
    val ordersDetailFragment by lazy { OrderDetailFragment() }

    val accountFragment by lazy { AccountFragment() }
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
        pagerAdapter.addFragment(homeFragment)
        pagerAdapter.addFragment(ordersDetailFragment)
        pagerAdapter.addFragment(accountFragment)
        binding.pagger2.offscreenPageLimit = pagerAdapter.itemCount
        binding.pagger2.adapter = pagerAdapter
        binding.pagger2.isUserInputEnabled = false // disable swiping
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_main
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView() {
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(true)
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false,
                ) -> {
                    // Precise location access granted.
                }

                permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    false,
                ) -> {
                    // Only approximate location access granted.
                }

                else -> {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            }
        }

// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            ),
        )

        if (!isLocationEnabled()) {
            requestLocationEnable(this)
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

                R.id.navigation_order_detail -> {
                    binding.pagger2.currentItem = 1
                    true
                }

                R.id.navigation_profile -> {
                    binding.pagger2.currentItem = 2
                    true
                }

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
