package com.example.footu.ui.detail

import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footu.R
import com.example.footu.base.BaseFragment
import com.example.footu.databinding.FragmentOrderDetailBinding
import com.example.footu.ui.map.TrackingLocationActivity
import com.example.footu.utils.SHIPPER_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderDetailFragment :
    BaseFragment<FragmentOrderDetailBinding>() {
    private lateinit var ordersDetailAdapter: OrdersDetailAdapter
    private val viewModel: OrderDetailViewModel by viewModels()

    override fun getContentLayout() = R.layout.fragment_order_detail

    override fun initViewModel() = viewModel

    override fun initView() {
        ordersDetailAdapter = OrdersDetailAdapter(onClick = {
            val intent = Intent(requireContext(), TrackingLocationActivity::class.java)
            intent.putExtra(SHIPPER_ID, it)
            startActivity(intent)
        })
        binding.rcItem.layoutManager = LinearLayoutManager(requireContext())
        binding.rcItem.adapter = ordersDetailAdapter
    }

    override fun initListener() {
    }

    override fun observerLiveData() {
        lifecycleScope.launch {
            viewModel.ordersDetail.collect {
                ordersDetailAdapter.submitList(it)
                // ordersDetailAdapter.notifyDataSetChanged()
            }
        }
    }
}
