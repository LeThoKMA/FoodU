package com.example.footu.ui.pay

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.footu.R
import com.example.footu.base.BaseActivity
import com.example.footu.base.BaseViewModel
import com.example.footu.databinding.ActivityPayConfirmBinding
import com.example.footu.model.DetailItemChoose
import com.example.footu.model.PromotionUser
import com.example.footu.ui.shipper.AddressCallBack
import com.example.footu.ui.shipper.AddressDialog
import com.example.footu.utils.ITEMS_PICKED
import com.example.footu.utils.formatToPrice
import com.example.footu.utils.toast
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import vn.momo.momo_partner.AppMoMoLib

@AndroidEntryPoint
class ConfirmActivity :
    BaseActivity<ActivityPayConfirmBinding>() {
    private val viewModel: PayConfirmViewModel by viewModels()
    lateinit var adapter: ItemConfirmAdapter
    lateinit var promotionAdapter: ItemPromotionAdapter
    var promotions: MutableList<PromotionUser> = mutableListOf()
    var promotionsPicked: PromotionUser? = null
    var items: MutableList<DetailItemChoose> = mutableListOf()

    var priceAfterDiscount = 0
    private val fee = "0"
    var environment = 0 // developer default

    var type = -1

    private val merchantName = "CGV Cinemas"
    private val merchantCode = "CGV19072017"
    private val merchantNameLabel = "Nhà cung cấp"
    private val description = "Thanh toán đồ uống"
    lateinit var dialogForShip: AlertDialog

    override fun getContentLayout(): Int {
        return R.layout.activity_pay_confirm
    }

    override fun initView() {
        AppMoMoLib.getInstance()
            .setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT); // AppMoMoLib.ENVIRONMENT.PRODUCTION
        setColorForStatusBar(R.color.colorPrimary)
        setLightIconStatusBar(true)
        dialogForShip = setupProgressDialog()

        items = intent.getParcelableArrayListExtra(ITEMS_PICKED)!!
        val price = intent.getIntExtra("price", 0)
        binding.tvPrice.text = price.formatToPrice()
        priceAfterDiscount = price

        var priceDiscount = 0
        adapter = ItemConfirmAdapter(items)
        binding.rcItem.layoutManager = LinearLayoutManager(binding.root.context)
        binding.rcItem.adapter = adapter
        binding.rcPromotion.layoutManager =
            LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
        promotionAdapter = ItemPromotionAdapter(
            promotions,
            object : PromotionCallBack {
                override fun pick(index: Int) {
                    val promotion = promotions[index]
                    if (promotion.isPicked) {
                        promotionsPicked = promotion
                        promotions.forEach { if (it != promotion) it.isPicked = false }
                        promotionAdapter.notifyDataSetChanged()
                    } else {
                        promotionsPicked = null
                    }

                    priceDiscount =
                        promotionsPicked?.percentage?.div(100f)?.times(price)?.toInt() ?: 0

                    binding.tvPromotionDiscount.text = priceDiscount.formatToPrice()
                    priceAfterDiscount = price.minus(priceDiscount)
                    binding.tvPrice.text = price.minus(priceDiscount).formatToPrice()
                    priceDiscount = 0
                }
            },
        )
        binding.rcPromotion.adapter = promotionAdapter
    }

    override fun initListener() {
        binding.tvCreate.setOnClickListener {
            type = 0
            requestPayment()
        }
        binding.imvBack.setOnClickListener {
            finish()
        }
        binding.tvShip.setOnClickListener {
            type = 1
            val dialog = AddressDialog(object : AddressCallBack {
                override fun accept(address: String) {
                    viewModel.listenResponseByShipper(
                        items,
                        promotionsPicked,
                        priceAfterDiscount,
                        address
                    ) { requestPayment() }
                }
            })
            dialog.show(supportFragmentManager, "address_dialog")
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
            promotionAdapter.notifyDataSetChanged()
        }
        viewModel.isShowDialog.observe(this) {
            if (it) {
                dialogForShip.show()
            } else {
                dialogForShip.dismiss()
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
                    viewModel.confirmBill(items, promotionsPicked, priceAfterDiscount, type)
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
}
