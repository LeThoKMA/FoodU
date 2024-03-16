package com.example.footu.ui.Order

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import coil.compose.AsyncImage
import com.example.footu.ItemSize
import com.example.footu.model.Item
import com.example.footu.ui.shipper.ui.theme.Ivory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailItemFragment() : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val item: Item? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.arguments?.getParcelable(ITEM, Item::class.java)
        } else {
            this.arguments?.getParcelable(ITEM)
        }
        return ComposeView(requireContext()).apply {
            setContent {
                DetailItemFragment(item = item ?: Item())
            }
        }
    }

    override fun onStart() {
        val dialog: Dialog? = dialog
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog?.window?.setLayout(width, height)
        super.onStart()
    }

    companion object {
        private const val ITEM = "ITEM"
        val TAG = "DetailItemFragment"

        fun newInstance(
            onSelect: () -> Unit,
            item: Item,
        ) = DetailItemFragment().apply {
            arguments = bundleOf(
                ITEM to item,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailItemFragment(item: Item) {
    val pagerState = rememberPagerState(pageCount = { item.imgUrl?.size ?: 0 })
    var count by remember {
        mutableIntStateOf(0)
    }
    var selectedSize by remember {
        mutableIntStateOf(
            ItemSize.M.ordinal,
        )
    }
    val onCounter = remember<(Boolean) -> Unit> {
        {
            if (!it && count > 0) count-- else if (it) count++
        }
    }

    val onSelectSize = remember<(Int) -> Unit> {
        {
            selectedSize = it
        }
    }
    var textDescription by remember {
        mutableStateOf("")
    }

    val brush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color.Red,
                Color(0xFFFF7F00), // Orange
                Color.Yellow,
                Color.Green,
                Color(0xFF00FFFF), // Cyan
                Color.Blue,
                Color(0xFF8B00FF), // Purple
            ),
        )
    }

    Column(modifier = Modifier.background(Ivory)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(16.dp)
                .weight(0.2f)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                )
                .background(
                    color = Color.White,
                ),
        ) { page ->
            AsyncImage(
                model = item.imgUrl?.get(page),
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
            text = item.name.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = "Số lượng: ", fontSize = 18.sp)
            IconButton(onClick = { onCounter.invoke(false) }) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(50.dp),
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            IconButton(onClick = { onCounter.invoke(true) }) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Increase",
                    modifier = Modifier.size(50.dp),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 16.dp),
        ) {
            Button(
                onClick = { onSelectSize.invoke(ItemSize.S.ordinal) },
                colors = ButtonDefaults.buttonColors(if (selectedSize == ItemSize.S.ordinal) Color.Yellow else Color.Gray),
            ) {
                Text(
                    text = ItemSize.S.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Button(
                onClick = { onSelectSize.invoke(ItemSize.M.ordinal) },
                colors = ButtonDefaults.buttonColors(if (selectedSize == ItemSize.M.ordinal) Color.Yellow else Color.Gray),
            ) {
                Text(
                    text = ItemSize.M.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Button(
                onClick = { onSelectSize.invoke(ItemSize.L.ordinal) },
                colors = ButtonDefaults.buttonColors(if (selectedSize == ItemSize.L.ordinal) Color.Yellow else Color.Gray),
            ) {
                Text(
                    text = ItemSize.L.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        OutlinedTextField(
            value = textDescription,
            onValueChange = { textDescription = it },
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            label = { Text(text = "Mô tả") },
            textStyle = TextStyle(brush = brush),
            minLines = 5,
        )

        Button(onClick = { /*TODO*/ }, modifier = Modifier.align(CenterHorizontally)) {
            Text(text = "Thêm vào đơn")
        }
    }
}

@Preview
@Composable
fun MyCompose() {
    val item = Item(
        id = 1,
        name = "Cà phê",
        amount = 4,
        price = 40000,
        imgUrl = listOf(
            "https://www.highlandscoffee.com.vn/vnt_upload/product/06_2023/thumbs/270_crop_HLC_New_logo_5.1_Products__PHINDI_KEM_SUA.jpg",
            "https://www.highlandscoffee.com.vn/vnt_upload/product/04_2023/New_product/thumbs/270_crop_HLC_New_logo_5.1_Products__BAC_XIU.jpg",
            "https://www.highlandscoffee.com.vn/vnt_upload/product/04_2023/New_product/thumbs/270_crop_HLC_New_logo_5.1_Products__CARAMEL_MACCHIATTO.jpg",
        ),
        description = "Theo một truyền thuyết đã được ghi lại trên giấy vào năm 1671, những người chăn dê ở Kaffa (thuộc Ethiopia ngày nay) phát hiện ra một số con dê trong đàn sau khi ăn một cành cây có hoa trắng và quả màu đỏ đã chạy nhảy không mệt mỏi cho đến tận đêm khuya. Họ bèn đem chuyện này kể với các thầy tu tại một tu viện gần đó. Khi một người chăn dê trong số đó ăn thử loại quả màu đỏ đó anh ta đã xác nhận công hiệu của nó. Sau đó các thầy tu đã đi xem xét lại khu vực ăn cỏ của bầy dê và phát hiện ra một loại cây có lá xanh thẫm và quả giống như quả anh đào. Họ uống nước ép ra từ loại quả đó và tỉnh táo cầu nguyện chuyện trò cho đến tận đêm khuya. Như vậy có thể coi rằng nhờ chính đàn dê này con người đã biết được cây cà phê.",
    )
    DetailItemFragment(item)
}
