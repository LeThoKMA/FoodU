package com.example.footu.ui.chat.activity

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.R
import com.example.footu.Response.MessageResponse
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityUserChatBinding
import com.example.footu.model.User
import com.example.footu.ui.chat.adapter.MessageAdapter
import com.example.footu.utils.OTHER_USER_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        adapter.submitList(newList)
    }

    private fun onNewMessage(messageResponse: MessageResponse?) {
        messageResponse?.let {
            val newList = adapter.currentList.toMutableList()
            newList.add(0, messageResponse)
            adapter.submitList(newList)
            if (messageResponse.fromUser.id != otherUserId?.id) {
                binding.recycleChat.scrollToPosition(0)
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
        binding.tvName.text = otherUserId?.fullname
        binding.recycleChat.adapter = adapter
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
        binding.recycleChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) { // Chỉ kiểm tra khi cuộn lên
                    val firstVisibleItemPosition =
                        ((binding.recycleChat.layoutManager) as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (firstVisibleItemPosition == 0) {
                        // Gọi API để tải thêm dữ liệu khi cuộn lên đầu danh sách
                        mPage++
                        viewModel.loadMoreDataMessage(mPage)
                    }
                }
            }
        })
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
}
