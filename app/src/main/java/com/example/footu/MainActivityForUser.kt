package com.example.footu

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityMainBinding
import com.example.footu.model.DetailItemChoose
import com.example.footu.ui.account.AccountFragment
import com.example.footu.ui.cart.CartFragment
import com.example.footu.ui.chat.ChatFragment
import com.example.footu.ui.detail.OrderDetailFragment
import com.example.footu.ui.home.HomeFragment
import com.example.footu.utils.ITEMS_CHOOSE
import com.example.footu.utils.ITEMS_CHOOSE_ACTION
import com.example.footu.utils.hideSoftKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivityForUser : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainUserViewModel by viewModels()

    private val homeFragment by lazy {
        HomeFragment()
    }
    private val ordersDetailFragment by lazy { OrderDetailFragment() }

    private val accountFragment by lazy { AccountFragment() }

    private val chatFragment by lazy { ChatFragment() }

    private lateinit var pagerAdapter: FragmentNavigator

    private val scaleUp: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.scale_up) }
    private val scaleDown: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.scale_down,
        )
    }
    private var job: Job? = null

    private val broadcastItems = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.hasExtra(ITEMS_CHOOSE) == true) {
                val receivedList: ArrayList<DetailItemChoose> =
                    p1.getParcelableArrayListExtra(ITEMS_CHOOSE)!!
                viewModel.handleItemsCart(receivedList)
            }
        }
    }

    override fun observerData() {
        viewModel.totalPrice.observe(this) {
            if (it > 0) {
                binding.fab.alpha = 0f
                binding.fab.visibility = View.VISIBLE
                binding.fab.animate()
                    .alpha(1f).duration = 1000

                val animation =
                    ObjectAnimator.ofFloat(binding.fab, "translationZ", 0f, 100f, 0f)
                animation.duration = 2000 // Độ trễ mỗi chu kỳ
                animation.repeatCount = ObjectAnimator.INFINITE // Lặp vô hạn
                animation.interpolator = LinearInterpolator()
                animation.start()

                job = lifecycleScope.launch {
                    while (this.isActive) {
                        binding.fab.startAnimation(scaleUp)
                        delay(2500L)
                        binding.fab.startAnimation(scaleDown)
                    }
                }
            } else {
                job?.cancel()
                binding.fab.visibility = View.GONE
            }
        }
    }

    private fun setupPager() {
        pagerAdapter = FragmentNavigator(this.supportFragmentManager, lifecycle)
        pagerAdapter.addFragment(homeFragment)
        pagerAdapter.addFragment(ordersDetailFragment)
        pagerAdapter.addFragment(chatFragment)
        pagerAdapter.addFragment(accountFragment)
        binding.pagger2.offscreenPageLimit = pagerAdapter.itemCount
        binding.pagger2.adapter = pagerAdapter
        binding.pagger2.isUserInputEnabled = false // disable swiping
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_main
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun initView() {
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(true)
        val requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false,
                ) || permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    false,
                )
            ) {
                if (!isLocationEnabled()) {
                    requestLocationEnable(this)
                }
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }

// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                ),
            )
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                ),
            )
        }
        val intentFilter = IntentFilter(ITEMS_CHOOSE_ACTION)
        registerReceiver(broadcastItems, intentFilter)
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

                R.id.nav_chat -> {
                    binding.pagger2.currentItem = 2
                    true
                }

                R.id.navigation_profile -> {
                    binding.pagger2.currentItem = 3
                    true
                }

                else -> {
                    false
                }
            }
        }
        binding.fab.setOnClickListener {
            val dialog =
                CartFragment.newInstance(
                    viewModel.itemsChoose.values.toMutableList(),
                    viewModel.totalPrice.value ?: 0,
                    onChangeItem = { viewModel.onChangeItem(it) },
                )
            dialog.show(supportFragmentManager, CartFragment.TAG)
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    private fun showFragment(currentFragment: Fragment, targetFragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .hide(currentFragment)
            .show(targetFragment)
            .commit()
        hideSoftKeyboard()
        loadingDialog?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastItems)
    }
}
