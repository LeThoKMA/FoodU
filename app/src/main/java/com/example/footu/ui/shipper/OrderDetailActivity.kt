package com.example.footu.ui.shipper

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.footu.ItemSize
import com.example.footu.model.OrderShipModel
import com.example.footu.ui.map.RouterActivity
import com.example.footu.ui.shipper.ui.theme.Ivory
import com.example.footu.ui.shipper.ui.theme.Primary
import com.example.footu.utils.formatToPrice
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import dagger.hilt.android.AndroidEntryPoint
import java.util.Collections

@AndroidEntryPoint
class OrderDetailActivity : ComponentActivity() {
    private val viewModel: OrderDetailViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val orderDetail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("item", OrderShipModel::class.java)
        } else {
            intent.getParcelableExtra("item")
        }

        val type = intent.getIntExtra("type", -1)
        setContent {
            LocalContext.provides(this)
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            Primary,
                        ),
                        title = {
                            Text(
                                text = "Chi tiết đơn hàng",
                                color = Color.White,
                                fontSize = 18.sp,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Filled.ArrowBack, "", tint = Color.White)
                            }
                        },
                    )
                },

                ) { paddingValues ->
                orderDetail?.let {
                    OrderDetailScreen(
                        it,
                        viewModel,
                        paddingValues,
                        onAccepted = {
                            setResult(RESULT_OK)
                            finish()
                        },
                        type,
                    )
                }
            }
        }
    }
}

@Composable
fun OrderDetailScreen(
    item: OrderShipModel,
    viewModel: OrderDetailViewModel,
    paddingValues: PaddingValues,
    onAccepted: () -> Unit,
    type: Int,
) {
    val phoneNumber = item.customer?.phone
    val annotatedString = buildAnnotatedString {
        append(phoneNumber)
        addStyle(
            style = SpanStyle(
                textDecoration = TextDecoration.Underline,
                fontSize = 20.sp,
                color = Color.Red,
                fontStyle = FontStyle.Italic,
            ),
            start = 0,
            end = phoneNumber?.length ?: 0,
        )
        addStringAnnotation(
            tag = "ClickablePhone",
            annotation = phoneNumber.toString(),
            start = 0,
            end = phoneNumber?.length ?: 0,
        )
    }

    val context = LocalContext.current

    LaunchedEffect(key1 = Unit, block = {
        viewModel.onSuccess.collect {
            if (it) {
                onAccepted.invoke()
            }
        }
    })

    val onAccept = remember {
        {
            viewModel.acceptOrder(item)
        }
    }

    val onDone = remember {
        {
            viewModel.eventDone(item.id)
        }
    }
    val billItems = item.billItemList

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
    ) {
        DetailClientText(title = "Người đặt:", content = item.customer?.fullname ?: "")

        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Số điện thoại:", modifier = Modifier.fillMaxWidth(0.35f), fontSize = 18.sp)
            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        "ClickablePhone",
                        start = offset,
                        end = offset,
                    )
                        .firstOrNull()?.let { annotation ->
                            val phoneNumberr = annotation.item
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:$phoneNumberr")
                            context.startActivity(intent)
                        }
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),

                )
        }

        DetailClientText(title = "Giá:", content = item.totalPrice.formatToPrice())

        DetailClientText(title = "Thời gian:", content = item.time)

        DetailLocationText(title = "Địa chỉ:", content = item.address) {
            val intent = Intent(context, RouterActivity::class.java)
            intent.putExtra("item", item)
            context.startActivity(intent)
        }

        Row(modifier = Modifier.align(End)) {
            AndroidView(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray)
                    .padding(8.dp),
                factory = { context ->
                    ZegoSendCallInvitationButton(context).apply {
                        setIsVideoCall(true)
                        resourceID = "zego_uikit_call"
                        setInvitees(
                            Collections.singletonList(
                                ZegoUIKitUser(
                                    item.customer?.id.toString(),
                                    item.customer?.fullname.toString(),
                                ),
                            ),
                        )
                    }
                },
            )

            AndroidView(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray)
                    .padding(8.dp),
                factory = { context ->
                    ZegoSendCallInvitationButton(context).apply {
                        setIsVideoCall(false)
                        resourceID = "zego_uikit_call"
                        setInvitees(
                            Collections.singletonList(
                                ZegoUIKitUser(
                                    item.customer?.id.toString(),
                                    item.customer?.fullname.toString(),
                                ),
                            ),
                        )
                    }
                },
            )
        }

        LazyColumn(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .weight(0.6f),
        ) {
            items(billItems, key = { it.item?.id!! }) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Ivory, // Card background color
                        contentColor = Color.Black, // Card content color,e.g.text
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                ) {
//
                    Row(modifier = Modifier.padding(8.dp)) {
                        AsyncImage(
                            model = it.item?.imgUrl?.get(0),
                            contentDescription = it.item?.id.toString(),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(80.dp)
                                .width(80.dp)
                                .clip(shape = RoundedCornerShape(16.dp))
                                .align(CenterVertically),
                        )

                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.align(CenterVertically),
                        ) {
                            Text(
                                text = it.item?.name ?: "",
                                modifier = Modifier.padding(3.dp),
                                fontSize = 16.sp,
                            )
                            Text(
                                text = "Đơn giá: ${it.item?.price.formatToPrice()}",
                                modifier = Modifier.padding(3.dp),
                                fontSize = 16.sp,
                            )
                            Text(text = it.description ?: "", maxLines = 3, fontSize = 16.sp)

                        }
                        Row {
                            Text(
                                text = "Số lượng: ${it.quantity}",
                                modifier = Modifier.padding(3.dp),
                                fontSize = 16.sp,
                            )

                            Text(
                                text = when (it.size) {
                                    ItemSize.S.ordinal -> ItemSize.S.name
                                    ItemSize.M.ordinal -> ItemSize.M.name
                                    ItemSize.L.ordinal -> ItemSize.L.name
                                    else -> ""
                                },
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.fillMaxHeight(0.2f))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Button(
                onClick = { if (type == 0) onAccept() else onDone() },
            ) {
                Text(text = if (type == 0) "Nhận đơn" else "Hoàn thành đơn hàng")
            }
        }
    }
}

@Composable
fun DetailClientText(title: String, content: String) {
    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text = title, modifier = Modifier.fillMaxWidth(0.35f), fontSize = 16.sp)
        Text(text = content, fontSize = 16.sp)
    }
}

@Composable
fun DetailLocationText(title: String, content: String, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text = title, modifier = Modifier.fillMaxWidth(0.35f), fontSize = 16.sp)
        Column {
            Text(
                text = content,
                fontSize = 16.sp,
            )
            Text(
                modifier = Modifier.clickable { onClick() },
                text = "Xem tuyến đường giao hàng",
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                color = Color.Red,
            )
        }
    }
}
