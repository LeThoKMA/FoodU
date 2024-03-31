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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import coil.compose.AsyncImage
import com.example.footu.ItemSize
import com.example.footu.R
import com.example.footu.model.DetailItemChoose
import com.example.footu.ui.shipper.ui.theme.Ivory
import com.example.footu.ui.shipper.ui.theme.Pink80
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailItemFragment(private val onSelect: (DetailItemChoose) -> Unit) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val item: DetailItemChoose? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.arguments?.getParcelable(ITEM, DetailItemChoose::class.java)
        } else {
            this.arguments?.getParcelable(ITEM)
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DetailItemView(
                    item = item ?: DetailItemChoose(),
                    onSelect = {
                        onSelect(it)
                        dismiss()
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    override fun onStart() {
        val dialog: Dialog? = dialog
        val width =
            (resources.displayMetrics.widthPixels * 0.8).toInt() // 80% chiều rộng của màn hình
        val height = (resources.displayMetrics.heightPixels * 0.6).toInt()
        dialog?.window?.setLayout(width, height)
        super.onStart()
    }

    companion object {
        private const val ITEM = "ITEM"
        val TAG = "DetailItemFragment"

        fun newInstance(
            item: DetailItemChoose,
            onSelect: (DetailItemChoose) -> Unit,
        ) = DetailItemFragment(onSelect).apply {
            arguments = bundleOf(
                ITEM to item,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailItemView(item: DetailItemChoose, onSelect: (DetailItemChoose) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { item.imgUrl.size ?: 0 })
    var count by remember {
        mutableIntStateOf(1)
    }
    var selectedSize by remember {
        mutableStateOf(
            ItemSize.M,
        )
    }
    val onCounter = remember<(Boolean) -> Unit> {
        {
            if (!it && count > 1) count-- else if (it) count++
        }
    }

    val onSelectSize = remember<(ItemSize) -> Unit> {
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
                .weight(0.6f)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                )
                .background(
                    color = Color.White,
                ),
        ) { page ->
            AsyncImage(
                model = item.imgUrl[page],
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        Text(
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            text = item.name.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Text(text = "Số lượng: ", fontSize = 16.sp)
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
                onClick = { onSelectSize.invoke(ItemSize.S) },
                colors = ButtonDefaults.buttonColors(if (selectedSize == ItemSize.S) Pink80 else Color.Gray),
            ) {
                Text(
                    text = ItemSize.S.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Button(
                onClick = { onSelectSize.invoke(ItemSize.M) },
                colors = ButtonDefaults.buttonColors(if (selectedSize == ItemSize.M) Pink80 else Color.Gray),
            ) {
                Text(
                    text = ItemSize.M.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Button(
                onClick = { onSelectSize.invoke(ItemSize.L) },
                colors = ButtonDefaults.buttonColors(if (selectedSize == ItemSize.L) Pink80 else Color.Gray),
            ) {
                Text(
                    text = ItemSize.L.name,
                    fontSize = 16.sp,
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
            minLines = 3,
        )

        Button(onClick = {
            val detailItemChoose = DetailItemChoose(
                id = item.id,
                count = count,
                name = item.name,
                imgUrl = item.imgUrl,
                price = item.price,
                size = selectedSize,
                flag = true,
            )
            onSelect(detailItemChoose)
        }, modifier = Modifier.align(CenterHorizontally)) {
            Text(text = "Thêm")
        }
    }
}
