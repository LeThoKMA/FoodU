//package com.example.footu.ui.orderlist
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.footu.R
//import com.example.footu.base.BaseFragment
//import com.example.footu.databinding.FragmentOrderListBinding
//import com.example.footu.model.OrderItem
//
///**
// * A simple [Fragment] subclass.
// * Use the [OrderListFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class OrderListFragment : BaseFragment<FragmentOrderListBinding, OrderListViewModel>() {
//    lateinit var orderListAdapter: OrderListAdapter
//    val list = mutableListOf<OrderItem>()
//    override fun getContentLayout(): Int {
//        return R.layout.fragment_order_list
//    }
//
//    override fun initViewModel() {
//    }
//
//    override fun initView() {
//        paddingStatusBar(binding.root)
//        orderListAdapter = OrderListAdapter(list, onClick = { viewModel.getOrderDetail(it) })
//        binding.rcOrders.layoutManager = LinearLayoutManager(binding.root.context)
//        binding.rcOrders.adapter = orderListAdapter
//    }
//
//    override fun initListener() {
//        binding.rcOrders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val linearLayoutManager: LinearLayoutManager =
//                    recyclerView.layoutManager as LinearLayoutManager
//                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == list.size - 1) {
//                    viewModel.fetchOrderList()
//                }
//            }
//
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//            }
//        })
//    }
//
//    override fun observerLiveData() {
//        viewModel.orderList.observe(this) {
//            list.addAll(it)
//            orderListAdapter.notifyDataSetChanged()
//        }
//        viewModel.orderDetail.observe(this) {
//            val bundle = Bundle()
//            bundle.putParcelable("order_detail", it)
//            val detailOrderDialog = DetailOrderDialog()
//            detailOrderDialog.arguments = bundle
//            detailOrderDialog.show((binding.root.context as AppCompatActivity).supportFragmentManager, "")
//        }
//    }
//}
