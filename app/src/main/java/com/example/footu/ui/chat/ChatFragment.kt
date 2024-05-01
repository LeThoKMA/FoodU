package com.example.footu.ui.chat

import androidx.fragment.app.viewModels
import com.example.footu.R
import com.example.footu.base.BaseFragment
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ChatFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : BaseFragment<ChatFragmentBinding>() {
    private val viewModel: HintChatViewModel by viewModels()
    override fun getContentLayout(): Int {
        return R.layout.chat_fragment
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun initView() {
        TODO("Not yet implemented")
    }

    override fun initListener() {
        TODO("Not yet implemented")
    }

    override fun observerLiveData() {
        TODO("Not yet implemented")
    }
}
