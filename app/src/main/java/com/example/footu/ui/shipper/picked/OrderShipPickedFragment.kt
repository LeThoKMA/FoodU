package com.example.footu.ui.shipper.picked

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footu.R
import com.example.footu.base.BaseFragment
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.OrderShipPickedFragmentBinding
import com.example.footu.model.OrderShipModel
import com.example.footu.ui.map.MultipleRouterActivity
import com.example.footu.ui.shipper.OrderDetailActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderShipPickedFragment :
    BaseFragment<OrderShipPickedFragmentBinding>(),
    OrderPickedCallback {
    private val viewModel: OrderShipPickedViewModel by viewModels()
    lateinit var adapter: OrdersPickedAdapter
    private var launcher: ActivityResultLauncher<Intent>? = null
    private var itemDelete: OrderShipModel? = null

    override fun getContentLayout(): Int {
        return R.layout.order_ship_picked_fragment
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun initView() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    itemDelete?.let { it1 -> viewModel.onDeleteItem(it1) }
                    itemDelete = null
                }
            }
        binding.rvOrders.layoutManager = LinearLayoutManager(binding.root.context)
        adapter = OrdersPickedAdapter(this)
        binding.rvOrders.adapter = adapter
    }

    override fun initListener() {
        binding.ivRouters.setOnClickListener {
            startActivity(Intent(binding.root.context, MultipleRouterActivity::class.java))
        }
        binding.refreshLayout.setOnRefreshListener {
            viewModel.hideSnackbar()
            viewModel.loadData()
            binding.refreshLayout.isRefreshing = false
        }
    }

    override fun observerLiveData() {
        viewModel.viewModelScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter.submitList(it.orders)
                }
            }
        }
    }

    override fun onClickDetail(item: OrderShipModel) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())
        val intent = Intent(requireContext(), OrderDetailActivity::class.java)
        intent.putExtra("item", item)
        launcher?.launch(intent, options)
    }

    override fun onResume() {
        viewModel.loadData()
        super.onResume()
    }
}
