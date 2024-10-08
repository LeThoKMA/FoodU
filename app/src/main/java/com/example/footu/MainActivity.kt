package com.example.footu

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityMainBinding
import com.example.footu.model.RegisterFirebaseModel
import com.example.footu.ui.account.AccountFragment
import com.example.footu.ui.shipper.home.OrderListScreen
import com.example.footu.ui.shipper.picked.OrderShipPickedFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    private val orderShipPickedFragment by lazy { OrderShipPickedFragment() }

    private val orderListShipper by lazy {
        OrderListScreen()
    }
    private val accountFragment by lazy { AccountFragment() }

    private lateinit var pagerAdapter: FragmentNavigator


    override fun observerData() {
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_main
    }

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

            if (!permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false)) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent)
            }

        }

// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.

        requestPermission.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            ) else arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
            )
        )

        initFirebase()
        LocationEmitter.emitLocation()
        setupPager()
    }

    private fun initFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("initFirebase", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                task.result?.let {
                    Log.d("initFirebase", it)
                    viewModel.registerFirebase(
                        RegisterFirebaseModel(
                            it,
                            getID(this),
                        ),
                    )
                }
            },
        )
        FirebaseMessaging.getInstance().subscribeToTopic("shipperTopic")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.e(">>>>>>>>>>>>>>>", msg)
            }
    }

    private fun setupPager() {
        pagerAdapter = FragmentNavigator(this.supportFragmentManager, lifecycle)
        pagerAdapter.addFragment(orderListShipper)
        pagerAdapter.addFragment(orderShipPickedFragment)
        pagerAdapter.addFragment(accountFragment)
        binding.pagger2.offscreenPageLimit = pagerAdapter.itemCount
        binding.pagger2.adapter = pagerAdapter
        binding.pagger2.isUserInputEnabled = false // disable swiping
        if (intent.hasExtra("type")) {
            binding.pagger2.currentItem = 1
            binding.navigation.selectedItemId = R.id.navigation_profile
        }
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

    override fun onDestroy() {
        super.onDestroy()
    }

//    private fun initializeFragment(tag: String, createFragment: () -> Fragment): Fragment {
//        return supportFragmentManager.findFragmentByTag(tag) ?: createFragment().also { fragment ->
//            supportFragmentManager
//                .beginTransaction()
//                .add(R.id.fragmentContainer, fragment, tag)
// //                .hide(fragment)
//                .commit()
//        }
//    }
//
//    private fun showFragment(currentFragment: Fragment, targetFragment: Fragment) {
//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.fragmentContainer, targetFragment)
//            .commit()
//        hideSoftKeyboard()
//        loadingDialog?.hide()
//    }

    @SuppressLint("HardwareIds")
    fun getID(context: Context): String? {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        )
    }
}
