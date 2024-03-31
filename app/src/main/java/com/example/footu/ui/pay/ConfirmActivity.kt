package com.example.footu.ui.pay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityPayConfirmBinding
import com.example.footu.model.DetailItemChoose
import com.example.footu.model.PromotionUser
import com.example.footu.utils.ITEMS_PICKED
import com.example.footu.utils.ORDER_TYPE
import com.example.footu.utils.formatToPrice
import com.example.footu.utils.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.mapbox.geojson.Point
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.gestures.addOnFlingListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import org.json.JSONObject
import vn.momo.momo_partner.AppMoMoLib

@AndroidEntryPoint
class ConfirmActivity :
    BaseActivity<ActivityPayConfirmBinding>() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latLong: Pair<Double, Double> = Pair(0.0, 0.0)

    private val viewModel: PayConfirmViewModel by viewModels()
    lateinit var adapter: ItemConfirmAdapter

    // lateinit var promotionAdapter: ItemPromotionAdapter
    var promotions: MutableList<PromotionUser> = mutableListOf()
    var promotionsPicked: PromotionUser? = null
    var items: MutableList<DetailItemChoose> = mutableListOf()

    var priceAfterDiscount = 0
    private val fee = "0"
    var environment = 0 // developer default

    private var type = -1

    private val merchantName = "The Coffe House"
    private val merchantCode = "123456"
    private val merchantNameLabel = "Nhà cung cấp"
    private val description = "Thanh toán đồ uống"
    var dialogForShip: AlertDialog? = null

    override fun getContentLayout(): Int {
        return R.layout.activity_pay_confirm
    }

    override fun initView() {
        AppMoMoLib.getInstance()
            .setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT); // AppMoMoLib.ENVIRONMENT.PRODUCTION
        dialogForShip = setupProgressDialog()
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(true)
        type = intent.getIntExtra(ORDER_TYPE, -1)
        if (type == 1) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            lifecycleScope.launch(Dispatchers.Main) {
                val location = startLocationUpdates()
                location?.let {
                    latLong = Pair(it.latitude, it.longitude)
                    binding.mapView.getMapboxMap().setCamera(
                        cameraOptions {
                            center(
                                Point.fromLngLat(
                                    location.longitude,
                                    location.latitude,
                                ),
                            )
                            zoom(14.0)
                        },
                    )
                    viewModel.getAddress(location.latitude, location.longitude)
                }
            }
        } else {
            binding.mapView.visibility = View.GONE
            binding.mapPoint.visibility = View.GONE
            binding.tvAddress.visibility = View.GONE
        }

        items = intent.getParcelableArrayListExtra(ITEMS_PICKED)!!
        val price = intent.getIntExtra("price", 0)
        binding.tvTotal.text = "Tổng cộng: ${price.formatToPrice()}"
        priceAfterDiscount = price

        var priceDiscount = 0
        adapter = ItemConfirmAdapter(items)
        binding.rcItem.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rcItem.adapter = adapter
//        binding.rcPromotion.layoutManager =
//            LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
//        promotionAdapter = ItemPromotionAdapter(
//            promotions,
//            object : PromotionCallBack {
//                override fun pick(index: Int) {
//                    val promotion = promotions[index]
//                    if (promotion.isPicked) {
//                        promotionsPicked = promotion
//                        promotions.forEach { if (it != promotion) it.isPicked = false }
//                        promotionAdapter.notifyDataSetChanged()
//                    } else {
//                        promotionsPicked = null
//                    }
//
//                    priceDiscount =
//                        promotionsPicked?.percentage?.div(100f)?.times(price)?.toInt() ?: 0
//
//                    binding.tvPromotionDiscount.text = priceDiscount.formatToPrice()
//                    priceAfterDiscount = price.minus(priceDiscount)
//                    binding.tvPrice.text = price.minus(priceDiscount).formatToPrice()
//                    priceDiscount = 0
//                }
//            },
//        )
//        binding.rcPromotion.adapter = promotionAdapter
    }

    override fun initListener() {
        binding.tvCreate.setOnClickListener {
            if (type == 1) {
                viewModel.listenResponseByShipper(
                    items,
                    promotionsPicked,
                    priceAfterDiscount,
                    binding.tvAddress.text.toString(),
                    latLong = latLong,
                ) { requestPayment() }
            } else {
                requestPayment()
            }
        }
        binding.imvBack.setOnClickListener {
            finish()
        }

        binding.mapView.getMapboxMap().addOnFlingListener {
            latLong = binding.mapView.getMapboxMap().cameraState.center.let {
                Pair(it.latitude(), it.longitude())
            }
            viewModel.getAddress(latLong.first, latLong.second)
        }
    }

    override fun initViewModel(): BaseViewModel {
        return viewModel
    }

    override fun observerData() {
        viewModel.message.observe(this) {
            this.toast(it)
            finish()
        }
        viewModel.promotions.observe(this) {
            promotions.addAll(it.toMutableList())
            //    promotionAdapter.notifyDataSetChanged()
        }
        viewModel.isShowDialog.observe(this) {
            if (it) {
                dialogForShip?.show()
            } else {
                dialogForShip?.dismiss()
            }
        }
        lifecycleScope.launch {
            viewModel.address.collect {
                binding.tvAddress.text = it
            }
        }
    }

    private fun requestPayment() {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT)
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN)

        val eventValue: MutableMap<String, Any> = HashMap()
        // client Required
        eventValue["merchantname"] =
            merchantName // Tên đối tác. được đăng ký tại https://business.momo.vn. VD: Google, Apple, Tiki , CGV Cinemas
        eventValue["merchantcode"] =
            merchantCode // Mã đối tác, được cung cấp bởi MoMo tại https://business.momo.vn
        eventValue["amount"] = priceAfterDiscount // Kiểu integer
        eventValue["orderId"] =
            "orderId123456789" // uniqueue id cho BillId, giá trị duy nhất cho mỗi BILL
        eventValue["orderLabel"] = "Mã đơn hàng" // gán nhãn

        // client Optional - bill info
        eventValue["merchantnamelabel"] = "Dịch vụ" // gán nhãn
        eventValue["fee"] = 0 // Kiểu integer
        eventValue["description"] = description // mô tả đơn hàng - short description

        // client extra data
        eventValue["requestId"] = merchantCode + "merchant_billId_" + System.currentTimeMillis()
        eventValue["partnerCode"] = merchantCode
        // Example extra data
        val objExtraData = JSONObject()
        try {
            objExtraData.put("site_code", "008")
            objExtraData.put("site_name", "CGV Cresent Mall")
            objExtraData.put("screen_code", 0)
            objExtraData.put("screen_name", "Special")
            objExtraData.put("movie_name", "Kẻ Trộm Mặt Trăng 3")
            objExtraData.put("movie_format", "2D")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        eventValue["extraData"] = objExtraData.toString()
        eventValue["extra"] = ""
        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode === -1) {
            if (data != null) {
                Log.e(">>>>>>>>>>", data.getIntExtra("status", -1).toString())
                if (data.getIntExtra("status", -1) === 0) {
                    // TOKEN IS AVAILABLE
                    viewModel.confirmBill(
                        items,
                        promotionsPicked,
                        priceAfterDiscount,
                        type,
                        latLong,
                    )
                    // this.toast("message: " + "Get token " + data.getStringExtra("message"))
                    val token = data.getStringExtra("data") // Token response
                    val phoneNumber = data.getStringExtra("phonenumber")

                    var env: String? = data.getStringExtra("env")
                    if (env == null) {
                        env = "app"
                    }
                    if (token != null && token != "") {
                        // TODO: send phoneNumber & token to your server side to process payment with MoMo server
                        // IF Momo topup success, continue to process your order
                    } else {
                        //      this.toast("message: " + this.getString(R.string.not_receive_info))
                    }
                } else if (data.getIntExtra("status", -1) === 1) {
                    // TOKEN FAIL
                    val message =
                        if (data.getStringExtra("message") != null) data.getStringExtra("message") else "Thất bại"
                    //  this.toast("message: $message")
                } else if (data.getIntExtra("status", -1) === 2) {
                    // TOKEN FAIL
                    //   this.toast("message: " + this.getString(R.string.not_receive_info))
                } else {
                    // TOKEN FAIL
                    //  this.toast("message: " + this.getString(R.string.not_receive_info))
                }
            } else {
                //   this.toast("message: " + this.getString(R.string.not_receive_info))
            }
        } else {
            // this.toast("message: " + this.getString(R.string.not_receive_info_err))
        }
    }

    private fun setupProgressDialog(): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setCancelable(false)

        val myLayout = LayoutInflater.from(this)
        val dialogView: View =
            myLayout.inflate(R.layout.fragment_progress_dialog_waiting_shipper, null)

        builder.setView(dialogView)

        val dialog: AlertDialog = builder.create()
        val window: Window? = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    private suspend fun startLocationUpdates(): Location? {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null
        }
        val task = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
        }.await()

        return task
    }
}
