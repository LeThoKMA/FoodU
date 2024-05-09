package com.example.footu.ui.chat.activity

import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.footu.R
import com.example.footu.Response.MessageResponse
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityUserChatBinding
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
    private val otherUserId by lazy { intent.getIntExtra(OTHER_USER_ID, 0) }

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
        adapter.submitList(list)
    }

    private fun onNewMessage(messageResponse: MessageResponse?) {
        messageResponse?.let {
            val newList = adapter.currentList.toMutableList()
            newList.add(messageResponse)
            adapter.submitList(newList)
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_user_chat
    }

    override fun initView() {
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(true)
        viewModel.getHintIdAndMessageData(otherUserId)
        binding.recycleChat.adapter = adapter
    }

    override fun initListener() {
        binding.imageBtnChat.setOnClickListener {
            if (binding.edtChat.text.isNotEmpty()) {
                Log.e(">>>>>>>", binding.edtChat.text.toString())
                viewModel.sendMessage(binding.edtChat.text.toString())
            }
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }
}
