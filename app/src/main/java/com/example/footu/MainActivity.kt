package com.example.footu

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityMainBinding
import com.example.footu.model.RegisterFirebaseModel
import com.example.footu.ui.shipper.OrderListScreen
import com.example.footu.ui.shipper.OrderShipPickedFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    lateinit var currentFragment: Fragment
    private lateinit var targetFragment: Fragment

    val orderShipPickedFragment by lazy { OrderShipPickedFragment() }

    val orderListShipper by lazy {
        OrderListScreen()
    }

    lateinit var pagerAdapter: FragmentNavigator
    val id = MyPreference.getInstance(this)?.getUser()?.id

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale("android.permission.POST_NOTIFICATIONS")) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        }
    }

    override fun observerData() {
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

        askNotificationPermission()

        initFirebase()

        FirebaseMessaging.getInstance().subscribeToTopic("shipperTopic")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.e(">>>>>>>>>>>>>>>", msg)
            }
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
    }

    fun setupPager() {
        pagerAdapter = FragmentNavigator(this.supportFragmentManager, lifecycle)
        pagerAdapter.addFragment(orderListShipper)
        pagerAdapter.addFragment(orderShipPickedFragment)
        binding.pagger2.offscreenPageLimit = pagerAdapter.itemCount
        binding.pagger2.adapter = pagerAdapter
        binding.pagger2.isUserInputEnabled = true // disable swiping
        if(intent.hasExtra("type")){
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

                R.id.navigation_profile -> {
                    binding.pagger2.currentItem = 1
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
