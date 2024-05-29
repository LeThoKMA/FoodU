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
            val chatFragment =
                data?.customer?.let { it1 ->
                    FloatingChatFragment.newInstance(it1)
                }
            chatFragment?.let {
                childFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.chatFragment, chatFragment, FloatingChatFragment.TAG)
                }
            }
            binding?.chatFragment?.visibility = View.VISIBLE
            binding?.fabChat?.visibility = View.VISIBLE
        }

        binding?.fabChat?.setOnClickListener {
            val intent = Intent(requireActivity(), UserChatActivity::class.java)
            intent.putExtra(OTHER_USER_ID, data?.customer)
            startActivity(intent)
            this.dismiss()
        }
//        binding?.chatFragment?.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    bottomSheetBehavior.isDraggable = true
//                }
//
//                else -> {
//                    bottomSheetBehavior.isDraggable = false
//                }
//            }
//            view.performClick()
//            true
//        }
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
