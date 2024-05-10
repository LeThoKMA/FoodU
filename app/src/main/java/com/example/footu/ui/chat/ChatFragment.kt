package com.example.footu.ui.chat

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.footu.R
import com.example.footu.base.BaseFragment
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ChatFragmentBinding
import com.example.footu.ui.chat.activity.UserChatActivity
import com.example.footu.utils.OTHER_USER_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        paddingStatusBar(binding.root)
    }

    override fun initListener() {
        binding.tvEmpty.setOnClickListener {
            val intent = Intent(requireContext(), UserChatActivity::class.java)
            intent.putExtra(OTHER_USER_ID, 2)
            startActivity(intent)
        }
    }

    override fun observerLiveData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        println(it)
                    }
                }
            }
        }
    }
}
