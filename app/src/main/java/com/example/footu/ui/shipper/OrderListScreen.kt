package com.example.footu.ui.shipper

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.compose.AsyncImage
import com.example.footu.R
import com.example.footu.base.BaseFragment
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityShipBinding
import com.example.footu.model.OrderShipModel
import com.example.footu.utils.formatToPrice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderListScreen : BaseFragment<ActivityShipBinding>() {

    var launcher: ActivityResultLauncher<Intent>? = null

    private val viewModel: OrderSViewModel by viewModels()
    lateinit var adapter: OrdersAdapter
    var itemDelete: OrderShipModel? = null

    override fun observerLiveData() {
        viewModel.viewModelScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    if (it.orderList.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                    }
                    adapter.setData(it.orderList)
                }
            }
        }
    }

    override fun getContentLayout(): Int {
        return R.layout.activity_ship
    }

    override fun initView() {
        binding.rvOrders.layoutManager = LinearLayoutManager(binding.root.context)
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                itemDelete?.let { it1 -> viewModel.onDeleteItem(it1) }
                itemDelete = null
            }
        }
        adapter = OrdersAdapter(
            viewModel.state.value.orderList,
            binding.root.context,
            object : OnClickDetailCallBack {
                override fun onClickDetail(item: OrderShipModel) {
                    itemDelete = item
                    val intent = Intent(binding.root.context, OrderDetailActivity::class.java)
                    intent.putExtra("item", item)
                    intent.putExtra("type", 0)
                    launcher?.launch(intent)
                }
            },
        )
        binding.rvOrders.adapter = adapter
    }

    override fun initListener() {
        binding.refreshLayout.setOnRefreshListener {
            // viewModel.hideSnackbar()
            viewModel.getOrderShip()
            binding.refreshLayout.isRefreshing = false
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    @Composable
    fun OrderMainView(
        viewModel: OrderSViewModel = hiltViewModel(),
        paddingValues: PaddingValues,
    ) {
        val uiState = viewModel.state.collectAsState()
        var orderList by remember {
            mutableStateOf(uiState.value.orderList)
        }

        val onClickItem = remember<(OrderShipModel) -> Unit> {
            {
//              //  val intent = Intent(this.context, OrderDetailActivity::class.java)
//                intent.putExtra("item", it)
//                startActivity(intent)
            }
        }

        SideEffect {
            viewModel.viewModelScope.launch {
                viewModel.state.collect {
                    orderList = it.orderList
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(
                    orderList,
                    key = { index: Int, item: OrderShipModel -> item.id },
                ) { index, item ->

                    val url = remember {
                        mutableStateOf(item.billItemList[0].item?.imgUrl ?: "")
                    }

                    Row(
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .fillMaxWidth()
                            .fillMaxSize(0.2f)
                            .clip(RoundedCornerShape(8.dp))
                            .shadow(2.dp)
                            .clickable { onClickItem.invoke(item) },
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = index.toString(),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .fillMaxHeight()
                                .width(80.dp),
                        )
                        Column {
                            Text(
                                text = item.customer.fullname ?: "",
                                modifier = Modifier.padding(3.dp),
                            )
                            Text(
                                text = item.customer.phone ?: "",
                                modifier = Modifier.padding(3.dp),
                            )
                            Text(
                                text = item.totalPrice.formatToPrice(),
                                modifier = Modifier.padding(3.dp),
                            )
                            Text(text = item.time, modifier = Modifier.padding(3.dp))
                        }
                    }
                }
            }
        }
    }
}
