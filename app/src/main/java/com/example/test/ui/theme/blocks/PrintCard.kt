package com.example.test.ui.theme.blocks


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.example.test.*
import com.example.test.R
import com.example.test.utils.formatNumber
import kotlin.math.roundToInt


@Composable
fun PrintCard(
    state: CodeBlockState,
    blockId: String,
    vars: List<Variable>,
    arrays: List<ArrayBlock>,
    onInteraction: (Offset, String) -> Unit,
    hasError: Boolean = false,
    context: Context
) {
    val block = state.printBlocks.firstOrNull { it.id == blockId } ?: return
    val color = Color(ContextCompat.getColor(context, R.color.cycle_main_color))
    val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))

    var x by remember { mutableFloatStateOf(block.pos.x.toFloat()) }
    var y by remember { mutableFloatStateOf(block.pos.y.toFloat()) }
    var expanded by remember { mutableStateOf(true) }
    var blockPos by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .offset {
                IntOffset(
                    x.roundToInt(),
                    y.roundToInt()
                )
            }
            .onGloballyPositioned { coords ->
                blockPos = coords.localToWindow(Offset.Zero)
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onInteraction(blockPos, blockId)
                    },
                    onPress = {
                        tryAwaitRelease()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    x += drag.x
                    y += drag.y
                }
            }
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (hasError) Color.Red else color,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (hasError) Color.Red else Color(
                        ContextCompat.getColor(
                            context,
                            R.color.light_green_for_text
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Print",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded)
                            Icons.Default.ExpandLess
                        else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
                    )
                }
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .background(
                        color = color,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (hasError) Color.Red else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(12.dp)
                    .heightIn(max = 170.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Variables:",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    vars.forEach { variable ->
                        val res = variable.value.toString().toDouble()
                        val value1 = if (variable.type == VariableType.INT)
                                res.toInt()
                            else res

                        val displayValue = when (val value = value1) {
                            is List<*> -> value.joinToString(prefix = "[", postfix = "]")
                            is String -> value
                            else -> value.toString()
                        }
                        Text(
                            text = "${variable.name} = $displayValue",
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = textColor
                        )
                    }
                    if (arrays.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Arrays:",
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        arrays.forEach { array ->
                            Text(
                                text = "${array.name}[${array.size}] = ${
                                    array.elems.joinToString(
                                        prefix = "[",
                                        postfix = "]"
                                    )
                                }",
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}