package com.example.footu.ui.chat.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.R
import com.example.footu.Response.MessageResponse
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityUserChatBinding
import com.example.footu.model.User
import com.example.footu.ui.chat.adapter.MessageAdapter
import com.example.footu.utils.APP_ID
import com.example.footu.utils.APP_SIGN
import com.example.footu.utils.OTHER_USER_ID
import com.example.footu.utils.REQUEST_CAMERA_PERMISSION
import com.example.footu.utils.REQUEST_IMAGE_CAPTURE
import com.example.footu.utils.convertUriToBitmap
import com.example.footu.utils.getVideoFileSize
import com.example.footu.utils.isVideoFile
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections

@AndroidEntryPoint
class UserChatActivity : BaseActivity<ActivityUserChatBinding>() {
    private val viewModel: UserChatViewModel by viewModels()
    private val adapter by lazy {
        MessageAdapter()
    }
    private val otherUserId by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(OTHER_USER_ID, User::class.java)
        } else {
            intent.getParcelableExtra(OTHER_USER_ID)
        }
    }
    private var mPage = 0

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri?.let { isVideoFile(this, it) } == true) {
                CoroutineScope(IO).launch {
                    val size = getVideoFileSize(uri, this@UserChatActivity)?.div(1024 * 1024) ?: 0
                    withContext(Main) {
                        if (size <= 10L) {
                            uri.let { viewModel.sendVideo(it) }
                        } else {
                            Toast.makeText(
                                this@UserChatActivity,
                                "Video có kích thước tối đa 10MB",
                                Toast.LENGTH_SHORT,
                            )
                                .show()
                        }
                    }
                }
            } else {
                val bitmap = convertUriToBitmap(this, uri)
                bitmap?.let {
                    viewModel.sendImage(
                        it,
                    )
                }
            }
        }

    override fun observerData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect(::handleState)
            }
        }
    }

    private fun handleState(state: UserChatViewModel.StateUi) {
        when (state) {
            is UserChatViewModel.StateUi.TotalMessage -> {
                setupData(state.messageList)
                viewModel.setupSocket()
            }

            is UserChatViewModel.StateUi.Message -> {
                onNewMessage(state.messageResponse)
            }
        }
    }

    private fun setupData(list: List<MessageResponse>?) {
        val newList = adapter.currentList.toMutableList()
        list?.let { newList.addAll(it) }
        adapter.submitList(newList) {
            if (mPage == 0) {
                binding.recycleChat.scrollToPosition(0)
            }
        }
    }

    private fun onNewMessage(messageResponse: MessageResponse?) {
        messageResponse?.let {
            val newList = adapter.currentList.toMutableList()
            newList.add(0, messageResponse)
            adapter.submitList(newList) {
                if (messageResponse.fromUser.id != otherUserId?.id) {
                    binding.recycleChat.scrollToPosition(0)
                }
            }
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_user_chat
    }

    override fun initView() {
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(true)
        otherUserId?.let { viewModel.getHintIdAndMessageData(it.id) }
        initCallService()
        binding.tvName.text = otherUserId?.fullname
        binding.recycleChat.adapter = adapter
        binding.composeView.setContent {
            Row {
                AndroidView(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                            .padding(8.dp),
                    factory = { context ->
                        ZegoSendCallInvitationButton(context).apply {
                            setIsVideoCall(true)
                            resourceID = "zego_uikit_call"
                            setInvitees(
                                Collections.singletonList(
                                    ZegoUIKitUser(
                                        otherUserId?.id.toString(),
                                        otherUserId?.fullname.toString(),
                                    ),
                                ),
                            )
                        }
                    },
                )

                AndroidView(
                    modifier =
                        Modifier
                            .padding(start = 16.dp, end = 8.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Transparent)
                            .padding(8.dp),
                    factory = { context ->
                        ZegoSendCallInvitationButton(context).apply {
                            setIsVideoCall(false)
                            resourceID = "zego_uikit_call"
                            setInvitees(
                                Collections.singletonList(
                                    ZegoUIKitUser(
                                        otherUserId?.id.toString(),
                                        otherUserId?.fullname.toString(),
                                    ),
                                ),
                            )
                        }
                    },
                )
            }
        }
    }

    override fun initListener() {
        binding.imageBtnChat.setOnClickListener {
            if (binding.edtChat.text.trim().isNotEmpty()) {
                viewModel.sendMessage(binding.edtChat.text.toString().trim())
                binding.edtChat.setText("")
            }
        }
        binding.imvBack.setOnClickListener {
            finish()
        }
        binding.recycleChat.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy < 0) { // Chỉ kiểm tra khi cuộn lên
                        if (!recyclerView.canScrollVertically(-1)) {
                            // Gọi API để tải thêm dữ liệu khi cuộn lên đầu danh sách
                            mPage++
                            val lastMessageId = adapter.currentList.last().messageId
                            viewModel.loadMoreDataMessage(mPage, lastMessageId)
                        }
                    }
                }
            },
        )
        binding.imgPick.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
        binding.imgNew.setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    private fun hideSoftKeyboard(activity: Activity?) {
        if (activity == null) {
            return
        }
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (activity.currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
            binding.edtChat.clearFocus()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null && !ev.isButtonPressed(binding.imageBtnChat.id)) {
            hideSoftKeyboard(this)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun initCallService() {
        val appID: Long = APP_ID
        val appSign = APP_SIGN

        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        val notificationConfig = ZegoNotificationConfig()
        notificationConfig.sound = "zego_uikit_sound_call"
        notificationConfig.channelID = "CallInvitation"
        notificationConfig.channelName = "CallInvitation"
        ZegoUIKitPrebuiltCallService.init(
            this.application,
            appID,
            appSign,
            viewModel.user.id.toString(),
            viewModel.user.fullname,
            callInvitationConfig,
        )
    }

    private fun dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                listOf(Manifest.permission.CAMERA).toTypedArray(),
                REQUEST_CAMERA_PERMISSION,
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } else {
            // Thông báo cho người dùng rằng    không có ứng dụng camera nào được cài đặt
            Toast.makeText(this, "Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val extras = data?.extras
            val bitmap = extras?.get("data") as Bitmap
            viewModel.sendImage(
                bitmap,
            )
        }
    }

    override fun onPause() {
        viewModel.onPauseSocket()
        super.onPause()
    }

    override fun onResume() {
        viewModel.setupSocket()
        super.onResume()
    }

    override fun onStop() {
        adapter.submitList(null)
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp, thực hiện các thao tác cần thiết ở đây
                openCamera()
            } else {
                // Người dùng từ chối cấp quyền, bạn có thể hiển thị thông báo thông báo tùy ý
                Toast.makeText(
                    this,
                    "Không thể truy cập camera mà không có quyền",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallService.endCall()
    }
}
