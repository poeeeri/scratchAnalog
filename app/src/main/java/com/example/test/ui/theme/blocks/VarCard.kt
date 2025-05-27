package com.example.test.ui.theme.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.test.Variable
import com.example.test.utils.preprocessArrayExprForDisplay
import kotlin.math.roundToInt
import android.content.Context
import com.example.test.R

@Composable
fun VarCard(
    variable: Variable,
    vars: List<Variable>,
    hasError: Boolean,
    onInteraction: (Offset, String) -> Unit,
    context: Context
) {
    var x by remember { mutableFloatStateOf(variable.pos.x.toFloat()) }
    var y by remember { mutableFloatStateOf(variable.pos.y.toFloat()) }
    var blockPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x.roundToInt(),
                    y.roundToInt()
                )
            }
            .background(
                color = if (hasError) Color(0xFFFFEBEE) else Color(ContextCompat.getColor(context, R.color.var_color)),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (hasError) 2.dp else 0.2.dp,
                color = if (hasError) Color.Red else Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)

            .onGloballyPositioned { coords ->
                blockPosition = coords.localToWindow(Offset.Zero)
            }

            .pointerInput(Unit){
                detectTapGestures (
                    onDoubleTap = {
                        onInteraction(blockPosition, variable.name)
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
                    variable.pos = IntOffset(x.roundToInt(), y.roundToInt())
                }
            }
    ) {
        Column {
            if (variable.expression == "") variable.expression = "0"
            val value = if (variable.expression != "0")
                preprocessArrayExprForDisplay(variable.expression)
            else "0"
            Text(
                text = "${variable.type.toString().capitalize()} ${variable.name} = $value",
                fontWeight = FontWeight.Bold,
                color = if (hasError) Color.Red else Color(ContextCompat.getColor(context, R.color.light_green_for_text))
            )
            Text(
                text = variable.type.toString(),
                fontSize = 12.sp,
                color = if (hasError) Color.Red else Color(ContextCompat.getColor(context, R.color.light_green_for_text))
            )
        }
    }
}
