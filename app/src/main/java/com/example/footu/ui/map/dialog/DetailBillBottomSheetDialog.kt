package com.example.footu.ui.map.dialog

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import com.example.footu.R
import com.example.footu.databinding.ItemViewPointBinding
import com.example.footu.model.OrderShipModel
import com.example.footu.ui.chat.activity.UserChatActivity
import com.example.footu.ui.map.adapter.ItemAdapter
import com.example.footu.utils.OTHER_USER_ID
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DetailBillBottomSheetDialog : BottomSheetDialogFragment(R.layout.item_view_point) {
    private var binding: ItemViewPointBinding? = null
    private var onClickCall: () -> Unit = {}
    private var onClickChat: () -> Unit = {}
    private var showChat: Boolean = false
        set(value) {
            field = value
            binding?.chatFragment?.visibility = if (value) View.VISIBLE else View.GONE
            binding?.fabChat?.visibility = if (value) View.VISIBLE else View.GONE
        }
    private var chatFragment: FloatingChatFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = ItemViewPointBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val data =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable(DATA, OrderShipModel::class.java)
            } else {
                arguments?.getParcelable(DATA)
            }
        val adapter = ItemAdapter()
        binding?.tvName?.text = "Người đặt: ${data?.customer?.fullname}"
        binding?.tvId?.text = "Đơn số: ${data?.id}"
        binding?.tvAddress?.text = "Địa chỉ: ${data?.address}"
        binding?.rcItems?.adapter = adapter
        adapter.submitList(data?.billItemList)

        binding?.tvCall?.setOnClickListener {
            onClickCall.invoke()
        }
        binding?.tvMessage?.setOnClickListener {
            showChat = !showChat
            if (showChat) {
                val existingFragment = childFragmentManager.findFragmentByTag(FloatingChatFragment.TAG)
                if (existingFragment == null) {
                    chatFragment =
                        data?.customer?.let { customer ->
                            FloatingChatFragment.newInstance(customer)
                        }
                    chatFragment?.let { fragment ->
                        childFragmentManager.commit {
                            setReorderingAllowed(true)
                            add(R.id.chatFragment, fragment, FloatingChatFragment.TAG)
                        }
                    }
                }
            }
        }

        binding?.fabChat?.setOnClickListener {
            val intent = Intent(requireActivity(), UserChatActivity::class.java)
            intent.putExtra(OTHER_USER_ID, data?.customer)
            startActivity(intent)
            this.dismiss()
        }
    }

    companion object {
        const val TAG = "DetailBillBottomSheetDialog"
        const val DATA = "DATA"

        @JvmStatic
        fun newInstance(
            data: OrderShipModel,
            onClickCall: () -> Unit,
            onClickChat: () -> Unit,
        ) = DetailBillBottomSheetDialog().apply {
            this.onClickCall = onClickCall
            this.onClickChat = onClickChat
            arguments =
                bundleOf(
                    DATA to data,
                )
        }
    }
}
