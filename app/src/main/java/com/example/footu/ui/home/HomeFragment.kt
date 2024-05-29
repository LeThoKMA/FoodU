package com.example.footu.ui.home

import android.content.Intent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.footu.R
import com.example.footu.Response.CategoryResponse
import com.example.footu.base.BaseFragment
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.HomeFragmentBinding
import com.example.footu.ui.Order.CategoryAdapter
import com.example.footu.ui.Order.DescriptionProductAdapter
import com.example.footu.ui.Order.OrderActivity
import com.example.footu.utils.ITEM_TYPE
import com.makeramen.roundedimageview.RoundedImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment :
    BaseFragment<HomeFragmentBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    private var categoryAdapter: CategoryAdapter? = null
    private lateinit var descriptionAdapter: DescriptionProductAdapter
    private var listCategory = mutableListOf<CategoryResponse>()

    override fun getContentLayout(): Int {
        return R.layout.home_fragment
    }

    override fun initView() {
        descriptionAdapter = DescriptionProductAdapter()
        binding.rvDescriptionProduct.layoutManager = GridLayoutManager(binding.root.context, 2)
        binding.rvDescriptionProduct.adapter = descriptionAdapter
        val inAnimation: Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right)
        val outAnimation: Animation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left)
        binding.viewFlipper.inAnimation = inAnimation
        binding.viewFlipper.outAnimation = outAnimation

        categoryAdapter =
            CategoryAdapter(listCategory, onClickItem = {
                val intent = Intent(requireContext(), OrderActivity::class.java)
                intent.putExtra(ITEM_TYPE, it)
                startActivity(intent)
            })
        binding.rvType.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvType.adapter = categoryAdapter
    }

    override fun initListener() {
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun observerLiveData() {
        viewModel.dataItems.observe(viewLifecycleOwner) {
            if (it != null) {
                descriptionAdapter.submitList(it)
            }
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(binding.root.context, it, Toast.LENGTH_LONG).show()
        }

        viewModel.banner.observe(viewLifecycleOwner) {
            it.forEach {
                val imgView = RoundedImageView(requireContext())
                imgView.scaleType = ImageView.ScaleType.CENTER_CROP
                imgView.cornerRadius = 32f
                Glide.with(this).load(it).into(imgView)
                binding.viewFlipper.addView(imgView)
            }
            lifecycleScope.launch {
                while (true) {
                    delay(3000L)
                    binding.viewFlipper.showPrevious()
                }
            }
        }

        viewModel.category.observe(viewLifecycleOwner) {
            listCategory.clear()
            it?.let { it1 -> listCategory.addAll(it1) }
            categoryAdapter?.notifyDataSetChanged()
        }
    }
}
