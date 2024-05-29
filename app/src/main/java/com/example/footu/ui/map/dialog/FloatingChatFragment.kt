package com.example.footu.ui.map.dialog

import android.os.Build
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.R
import com.example.footu.Response.MessageResponse
import com.example.footu.base.BaseFragment
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.FloatingFragmentUserChatBinding
import com.example.footu.model.User
import com.example.footu.ui.chat.activity.UserChatViewModel
import com.example.footu.ui.chat.adapter.MessageSmallAdapter
import com.example.footu.utils.convertUriToBitmap
import com.example.footu.utils.getVideoFileSize
import com.example.footu.utils.isVideoFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FloatingChatFragment : BaseFragment<FloatingFragmentUserChatBinding>() {
    private val viewModel: UserChatViewModel by viewModels()
    private val adapter by lazy {
        MessageSmallAdapter()
    }
    private val otherUser by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(DATA, User::class.java)
        } else {
            arguments?.getParcelable(DATA)
        }
    }
    private var mPage = 0

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri?.let { isVideoFile(requireContext(), it) } == true) {
                CoroutineScope(Dispatchers.IO).launch {
                    val size = getVideoFileSize(uri, requireContext())?.div(1024 * 1024) ?: 0
                    withContext(Dispatchers.Main) {
                        if (size <= 10L) {
                            uri.let { viewModel.sendVideo(it) }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Video có kích thước tối đa 10MB",
                                Toast.LENGTH_SHORT,
                            )
                                .show()
                        }
                    }
                }
            } else {
                val bitmap = convertUriToBitmap(requireContext(), uri)
                bitmap?.let {
                    viewModel.sendImage(
                        it,
                    )
                }
            }
        }

    override fun getContentLayout(): Int {
        return R.layout.floating_fragment_user_chat
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun initView() {
        otherUser?.let { viewModel.getHintIdAndMessageData(it.id) }
        binding.recycleChat.adapter = adapter
        binding.recycleChat.isNestedScrollingEnabled = true
    }

    override fun initListener() {
        binding.imageBtnChat.setOnClickListener {
            if (binding.edtChat.text.trim().isNotEmpty()) {
                viewModel.sendMessage(binding.edtChat.text.toString().trim())
                binding.edtChat.setText("")
            }
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
                            viewModel.loadMoreDataMessage(mPage)
                        }
                    }
                }
            },
        )
        binding.imgPick.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
    }

    override fun observerLiveData() {
        viewLifecycleOwner.lifecycleScope.launch {
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
                if (messageResponse.fromUser.id != otherUser?.id) {
                    binding.recycleChat.scrollToPosition(0)
                }
            }
        }
    }

    companion object {
        const val TAG = "FloatingChatFragment"
        const val DATA = "DATA"

        @JvmStatic
        fun newInstance(user: User) =
            FloatingChatFragment().apply {
                arguments =
                    bundleOf(
                        DATA to user,
                    )
            }
    }
}
