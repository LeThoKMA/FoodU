package com.example.footu

import android.Manifest
import android.Manifest.permission.SYSTEM_ALERT_WINDOW
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityMainBinding
import com.example.footu.model.RegisterFirebaseModel
import com.example.footu.ui.account.AccountFragment
import com.example.footu.ui.chat.ChatFragment
import com.example.footu.ui.shipper.home.OrderListScreen
import com.example.footu.ui.shipper.picked.OrderShipPickedFragment
import com.example.footu.utils.APP_ID
import com.example.footu.utils.APP_SIGN
import com.example.footu.utils.ID_CHANNEL_LOCATION_SOCKET
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val viewModel: MainViewModel by viewModels()

    private val orderShipPickedFragment by lazy { OrderShipPickedFragment() }

    private val orderListShipper by lazy {
        OrderListScreen()
    }
    private val accountFragment by lazy { AccountFragment() }

    private val chatFragment by lazy { ChatFragment() }

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
        requestPermission()
        initFirebase()
        setupPager()
        startLocationService()
        configCallService()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun requestPermission() {
        val requestPermission =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(),
            ) { permissions ->
                if (!permissions.getOrDefault(
                        Manifest.permission.POST_NOTIFICATIONS,
                        false,
                    ) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(intent)
                }
                if (permissions.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        false,
                    ) ||
                    permissions.getOrDefault(
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

        requestPermission.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    SYSTEM_ALERT_WINDOW,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                )
            } else {
                arrayOf(
                    SYSTEM_ALERT_WINDOW,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                )
            },
        )
    }

    // Function to request the user to enable location services

    private fun startLocationService() {
        if (isServiceRunning(SocketService::class.java)) {
            return
        }
        val intent = Intent(this, SocketService::class.java)
        intent.putExtra(ID_CHANNEL_LOCATION_SOCKET, viewModel.user.id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
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
        pagerAdapter.addFragment(chatFragment)
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
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onDestroy() {
        ZegoUIKitPrebuiltCallService.endCall()
        super.onDestroy()
    }

    private fun configCallService() {
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        // callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit = true
        //      callInvitationConfig.provider =
//            ZegoUIKitPrebuiltCallConfigProvider { invitationData ->
//                var config: ZegoUIKitPrebuiltCallConfig? = null
//                val isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.value
//                val isGroupCall = invitationData.invitees.size > 1
//                config = if (isVideoCall && isGroupCall) {
//                    ZegoUIKitPrebuiltCallConfig.groupVideoCall()
//                } else if (!isVideoCall && isGroupCall) {
//                    ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
//                } else if (!isVideoCall) {
//                    ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
//                } else {
//                    ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
//                }
//                config
//            }
//        val notificationConfig = ZegoNotificationConfig()
//        notificationConfig.sound = "zego_uikit_sound_call"
//        notificationConfig.channelID = "CallInvitation"
//        notificationConfig.channelName = "CallInvitation"

        ZegoUIKitPrebuiltCallService.init(
            this.application,
            APP_ID,
            APP_SIGN,
            viewModel.user.id.toString(),
            viewModel.user.fullname,
            callInvitationConfig,
        )
    }

    @SuppressLint("HardwareIds")
    fun getID(context: Context): String? {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        )
    }
}
