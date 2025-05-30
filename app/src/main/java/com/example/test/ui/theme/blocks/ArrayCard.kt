package com.example.test.ui.theme.blocks

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.test.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.example.test.*
import com.example.test.utils.preprocessArrayExprForDisplay
import kotlin.math.roundToInt

@Composable
fun ArrayCard(
    state: CodeBlockState,
    arrayBlock: ArrayBlock,
    vars: List<Variable>,
    onInteraction: (Offset, String) -> Unit,
    hasError: Boolean = false,
    context: Context
) {
    var x by remember { mutableFloatStateOf(arrayBlock.pos.x.toFloat()) }
    var y by remember { mutableFloatStateOf(arrayBlock.pos.y.toFloat()) }
    var expanded by remember { mutableStateOf(true) }
    var blockPos by remember { mutableStateOf(Offset.Zero) }
    val bodyTextColor = Color(ContextCompat.getColor(context, R.color.canvas))
    val bodyColor = Color(ContextCompat.getColor(context, R.color.if_body_color))
    val mainColor = Color(ContextCompat.getColor(context, R.color.if_main_color))
    val mainTextColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    val textAreaColor = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(ContextCompat.getColor(context, R.color.shadow)),
        unfocusedBorderColor = Color(ContextCompat.getColor(context, R.color.cycle_main_color)),
        errorBorderColor = Color(ContextCompat.getColor(context, R.color.error_color)),
        cursorColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    )
    val buttonColors = ButtonDefaults.buttonColors(
        contentColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
        containerColor = Color(ContextCompat.getColor(context, R.color.if_main_color))
    )

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
                        onInteraction(blockPos, arrayBlock.id)
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
                    arrayBlock.pos = IntOffset(x.roundToInt(), y.roundToInt())
                }
            }
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (hasError) Color.Red else mainColor,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (hasError) Color.Red else mainTextColor,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Array ${arrayBlock.name}[${arrayBlock.size}]",
                    fontWeight = FontWeight.Bold,
                    color = mainTextColor
                )
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded)
                            Icons.Default.ExpandLess
                        else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .background(
                        color = bodyColor,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (hasError) Color.Red else mainTextColor,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Elements:",
                        fontWeight = FontWeight.Bold,
                        color = bodyTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(arrayBlock.size) { i ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$i:",
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.width(32.dp),
                                    color = bodyTextColor
                                )
                                var elemValue by remember { mutableStateOf(arrayBlock.elems[i]) }
                                val displayValue = preprocessArrayExprForDisplay(elemValue)
                                OutlinedTextField(
                                    value = displayValue,
                                    onValueChange = {
                                        elemValue = it
                                        arrayBlock.elems[i] = it
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = textAreaColor
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Operations:",
                        fontWeight = FontWeight.Bold,
                        color = bodyTextColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                state.selectedArrayName = arrayBlock.name
                                state.showArrayAccessDialog = true
                            },
                            colors = buttonColors
                        ) {
                            Text("Get element",
                                color = mainTextColor)
                        }
                        Button(
                            onClick = {
                                state.selectedArrayName = arrayBlock.name
                                state.showArraySetDialog = true
                            },
                            colors = buttonColors
                        ) {
                            Text("Set element",
                                color = mainTextColor)
                        }
                    }
                }
            }
        }
    }
}