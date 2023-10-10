package com.example.footu.ui.call

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.footu.MyPreference
import com.example.footu.R
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment

class CallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        addCallFragment()
    }

    fun addCallFragment() {
        val appID: Long = 1910313105
        val appSign: String = "bf1eca2875e22f3d054be951695123ff82bbadff77e78fe03168b11238e76e81"
        val callID: String = MyPreference.getInstance(this)?.getUser()?.id.toString()
        val userID: String = MyPreference.getInstance(this)?.getUser()?.id.toString()
        val userName: String = MyPreference.getInstance(this)?.getUser()?.fullname.toString()

        // You can also use GroupVideo/GroupVoice/OneOnOneVoice to make more types of calls.
        val config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
        val fragment = ZegoUIKitPrebuiltCallFragment.newInstance(
            appID,
            appSign,
            callID,
            userID,
            userName,
            config,
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commitNow()
    }
}
