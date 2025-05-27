package com.example.test.ui.theme.blocks

import android.content.Context
import com.example.test.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.test.CodeBlockState
import com.example.test.ForBlockCommand
import com.example.test.IfBlockCommand
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.WhileBlock
import com.example.test.WhileBlockCommand
import com.example.test.utils.preprocessArrayExprForDisplay
import kotlin.math.roundToInt

@Composable
fun WhileBlockCard(
    state: CodeBlockState,
    whileBlock: WhileBlock,
    onInteraction: (Offset, String) -> Unit,
    vars: List<Variable>,
    context: Context
) {
    var x by remember { mutableFloatStateOf(whileBlock.pos.x.toFloat()) }
    var y by remember { mutableFloatStateOf(whileBlock.pos.y.toFloat()) }
    var expanded by remember { mutableStateOf(true) }
    var blockPosition by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .offset {
                IntOffset(
                    x.roundToInt(),
                    y.roundToInt()
                )
            }
            .onGloballyPositioned { coords ->
                blockPosition = coords.localToWindow(Offset.Zero)
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onInteraction(blockPosition, whileBlock.id)
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
                    whileBlock.pos = IntOffset(x.roundToInt(), y.roundToInt())
                }
            }
    ) {
        val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
        Box(
            modifier = Modifier
                .background(
                    color = Color(ContextCompat.getColor(context, R.color.cycle_main_color)),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .border(
                    width = 0.2.dp,
                    color = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(12.dp)
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = textColor
                    )
                }
                Text (
                    modifier = Modifier.fillMaxWidth(),
                    text = "while (${preprocessArrayExprForDisplay(whileBlock.leftExpression)}" +
                            " ${whileBlock.comparisonOperator} "+
                            "${preprocessArrayExprForDisplay(whileBlock.rightExpression)})",
                    fontWeight = FontWeight.Bold,
                    color = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
                )

            }
        }

        if (expanded) {
            Box (
                modifier = Modifier
                    .background(
                        color = Color(ContextCompat.getColor(context, R.color.cycle_body_color)),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .border(
                        width = 0.2.dp,
                        color = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .padding(12.dp)
            ) {

                Column (
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text(
                        text = "Do:",
                        fontWeight = FontWeight.Bold,
                        color = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
                    )
                    IconButton( onClick = {
                        state.targetCommandsList = whileBlock.commands
                        state.showChooseWhileDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add inner Block",
                            tint = textColor)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (whileBlock.commands.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_commands),
                            color = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                            fontSize = 12.sp
                        )
                    }
                    else {
                        // для отрисовки карточек внутри блока
                        whileBlock.commands.forEach { cmd ->
                            when(cmd) {
                                is WhileBlockCommand -> WhileBlockCard(
                                    state = state,
                                    whileBlock = cmd.whileBlock,
                                    onInteraction = onInteraction,
                                    vars = vars,
                                    context = context
                                )
                                is IfBlockCommand -> IfBlockCard(
                                    state = state,
                                    ifBlock = cmd.ifBlock,
                                    onInteraction = onInteraction,
                                    vars = vars,
                                    context = context
                                )
                                is VarBlockCommand -> VarCard(
                                    variable = cmd.variable,
                                    vars = vars,
                                    hasError = false,
//<<<<<<< HEAD
//                                    onInteraction = onInteraction
//=======
                                    onInteraction = onInteraction,
                                    context = context

//>>>>>>> origin/develop
                                )
                                is ForBlockCommand -> ForBlockCard(
                                    state = state,
                                    forBlock = cmd.forBlock,
                                    onInteraction = onInteraction,
                                    vars = vars,
                                    context = context
                                )
                                else -> Text(stringResource(R.string.unknown_block))
                            }
                        }
                    }
                    if (state.showChooseWhileDialog) {
                        ChooseWhileBlockDialog(state, context)
                    }
                }
            }
        }
    }
}

@Composable
fun ChooseWhileBlockDialog(state: CodeBlockState,
                           context: Context) {
    Dialog(
        onDismissRequest = {
            state.showNewIfDialog = false
            state.showNewVarDialog = false
            state.showNewWhileDialog = false
            state.leftWhileExpression = ""
            state.rightWhileExpression = ""
            state.selectedWhileOperator = "=="
            state.curBlockCommands.clear()
            state.newWhileCommand = ""
            state.selectedWhileTargetId = ""
            state.showChooseWhileDialog = false
        }
    ) {
        val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
        Surface(
            color = Color(ContextCompat.getColor(context, R.color.dialog)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.choose_command),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    FloatingActionButton(
                        containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                        onClick = { state.showNewIfDialog = true
                            state.showChooseWhileDialog = false}
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "Add If Block",
                            tint = textColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                        onClick = { state.showNewWhileDialog = true
                            state.showChooseWhileDialog = false}
                    ) {
                        Icon(Icons.Default.Loop, contentDescription = "Add While Block",
                            tint = textColor)
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                        onClick = { state.showNewVarDialog = true
                            state.showChooseWhileDialog = false}
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Var Block",
                            tint = textColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                        onClick = { state.showNewForDialog = true
                            state.showChooseWhileDialog = false}
                    ) {
                        Icon(Icons.Default.Replay5, contentDescription = "Add For Block",
                            tint = textColor)
                    }
                }
            }
        }
    }
}
