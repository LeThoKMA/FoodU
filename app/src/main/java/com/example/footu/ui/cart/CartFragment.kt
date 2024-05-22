package com.example.footu.ui.cart

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import coil.compose.AsyncImage
import com.example.footu.ItemSize
import com.example.footu.model.DetailItemChoose
import com.example.footu.ui.pay.ConfirmActivity
import com.example.footu.ui.shipper.ui.theme.Ivory
import com.example.footu.utils.ITEMS_PICKED
import com.example.footu.utils.ORDER_TYPE
import com.example.footu.utils.formatToPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment(val onChangeItem: (DetailItemChoose) -> Unit) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val items: ArrayList<DetailItemChoose>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.arguments?.getParcelableArrayList(
                    ITEMS,
                    DetailItemChoose::class.java,
                )
            } else {
                this.arguments?.getParcelableArrayList(ITEMS)
            }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CartView(items?.toList() ?: emptyList(), onChangeItem = { onChangeItem.invoke(it) })
            }
        }
    }

    override fun onStart() {
        val dialog: Dialog? = dialog
        val width = LayoutParams.MATCH_PARENT
        val height = LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)
        super.onStart()
    }

    companion object {
        private const val ITEMS = "ITEMS"
        private const val TOTAL_PRICE = "TOTAL_PRICE"
        val TAG = "CartFragment"

        fun newInstance(
            items: List<DetailItemChoose>,
            price: Int,
            onChangeItem: (DetailItemChoose) -> Unit,
        ) = CartFragment(onChangeItem).apply {
            arguments = bundleOf(
                ITEMS to items,
                TOTAL_PRICE to price,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartView(list: List<DetailItemChoose>, onChangeItem: (DetailItemChoose) -> Unit) {
    val context = LocalContext.current
    val stateList = remember {
        list.toMutableStateList()
    }
    val totalPrice by rememberUpdatedState(newValue = stateList.sumOf { it.price })
    val onPickMethod: (Int) -> Unit = {
        val intent = Intent(context, ConfirmActivity::class.java)
        intent.putParcelableArrayListExtra(
            ITEMS_PICKED,
            list as ArrayList<out Parcelable>,
        )
        intent.putExtra(ORDER_TYPE, it)
        intent.putExtra("price", totalPrice)
        context.startActivity(intent)
    }
    val onRemove = remember<(DetailItemChoose) -> Unit> {
        {
            stateList.remove(it)
            onChangeItem.invoke(it)
        }
    }

    Column() {
        LazyColumn(modifier = Modifier.weight(0.75f)) {
            items(stateList.toList(), key = { item -> "${item.id}:${item.size}" }) {
                ItemView(it, onRemove = { onRemove(it) })
            }
        }
        Spacer(modifier = Modifier.size(2.dp))

        Text(
            text = "Tổng giá: ${totalPrice.formatToPrice()}",
            modifier = Modifier
                .padding(end = 8.dp, top = 8.dp)
                .align(End),
            fontWeight = FontWeight.Black,
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.align(CenterHorizontally)
        ) {
            Button(onClick = { onPickMethod(1) }, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(text = "Đặt ship")
            }

            Button(onClick = { onPickMethod(0) }, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(text = "Nhận tại cửa hàng")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemView(item: DetailItemChoose, onRemove: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                onRemove()
                true
            } else {
                false
            }
        },
        positionalThreshold = { 150f },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { DismissBackground(dismissState = dismissState) }
    ) {
        ItemRow(
            imgUrl = item.imgUrl[0],
            name = item.name,
            count = item.count,
            unitPrice = item.priceForSize,
            size = item.size ?: ItemSize.M,
            description = item.textDescription,
        )
    }
}

@Composable
fun ItemRow(
    imgUrl: String? = "",
    name: String,
    count: Int,
    unitPrice: Int,
    size: ItemSize,
    description: String,
) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = imgUrl,
                contentDescription = "",
                modifier = Modifier
                    .height(80.dp)
                    .width(80.dp)
                    .weight(0.2f)
                    .padding(8.dp)
                    .clip(shape = RoundedCornerShape(16.dp))
                    .align(Alignment.CenterVertically),
            )
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(0.6f)
                    .padding(start = 16.dp),
            ) {
                Text(text = name)
                Text(
                    text = "Size: ${
                        when (size) {
                            ItemSize.M -> ItemSize.M.name
                            ItemSize.L -> ItemSize.L.name
                            ItemSize.S -> ItemSize.S.name
                        }
                    }",
                )
                if (description.isNotBlank()) {
                    Text(text = "Mô tả: $description", maxLines = 3)
                }
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = End,
                modifier = Modifier
                    .weight(0.2f)
                    .padding(end = 8.dp),
            ) {
                Text(text = "x$count")
                Text(text = unitPrice.formatToPrice())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFFFF1744)
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "delete",
        )
    }
}
