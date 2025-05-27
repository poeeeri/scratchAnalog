package com.example.test.ui.theme.blocks

import android.content.Context
import com.example.test.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test.CodeBlockState
import com.example.test.IfBlock
import com.example.test.Variable
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material3.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.test.*

@Composable
fun ForBlockCard(state: CodeBlockState,
                 context: Context,
                 forBlock: ForBlock,
                 onInteraction: (Offset, String) -> Unit,
                 vars: List<Variable>)
{
    var x by remember { mutableFloatStateOf(forBlock.pos.x.toFloat()) }
    var y by remember { mutableFloatStateOf(forBlock.pos.y.toFloat()) }
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
                        onInteraction(blockPosition, forBlock.id)
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
                    forBlock.pos = IntOffset(x.roundToInt(), y.roundToInt())
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "for (${forBlock.variable} = ${forBlock.startExpression}; " +
                            "${forBlock.variable} ${forBlock.comparisonOperator} ${forBlock.endExpression};" +
                            " ${forBlock.variable} += ${forBlock.stepIter})",
                    fontWeight = FontWeight.Bold,
                    color = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
                )
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = textColor
                    )
                }
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color(ContextCompat.getColor(context, R.color.cycle_body_color)),
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .border(
                        width = 0.2.dp,
                        color = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.do_commands),
                        fontWeight = FontWeight.Bold,
                        color = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
                    )
                    IconButton(
                        onClick = {
                            state.targetCommandsList = forBlock.doCommands
                            state.showChooseForDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add inner Block",
                            tint = textColor)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (forBlock.doCommands.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_commands),
                            color = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                            fontSize = 12.sp
                        )
                    }
                    else {
                        forBlock.doCommands.forEach {cmd ->
                            when(cmd) {
                                is VarBlockCommand -> VarCard(
                                    variable = cmd.variable,
                                    vars = vars,
                                    hasError = false,
                                    onInteraction = onInteraction,
                                    context = context
                                )

                                is IfBlockCommand -> IfBlockCard(
                                    state = state,
                                    ifBlock = cmd.ifBlock,
                                    vars = vars,
                                    onInteraction = onInteraction,
                                    context = context
                                )

                                is WhileBlockCommand -> WhileBlockCard(
                                    state = state,
                                    whileBlock = cmd.whileBlock,
                                    onInteraction = onInteraction,
                                    vars = vars,
                                    context = context
                                )

                                is ForBlockCommand -> ForBlockCard(
                                    state = state,
                                    forBlock = cmd.forBlock,
                                    onInteraction = onInteraction,
                                    vars = vars,
                                    context = context
                                )
                                else -> stringResource(R.string.unknown_block)
                            }
                        }
                    }


                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.commands),
                        fontWeight = FontWeight.Bold,
                        color = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
                    )
                    IconButton(
                        onClick = {
                            state.targetCommandsList = forBlock.commands
                            state.showChooseForDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add inner Block",
                            tint = textColor)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (forBlock.commands.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_commands),
                            color = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                            fontSize = 12.sp
                        )
                    }
                    else {
                        forBlock.commands.forEach {cmd ->
                            when(cmd) {
                                is VarBlockCommand -> VarCard(
                                    variable = cmd.variable,
                                    vars = vars,
                                    hasError = false,
                                    onInteraction = onInteraction,
                                    context = context
                                )

                                is IfBlockCommand -> IfBlockCard(
                                    state = state,
                                    ifBlock = cmd.ifBlock,
                                    vars = vars,
                                    onInteraction = onInteraction,
                                    context = context
                                )

                                is WhileBlockCommand -> WhileBlockCard(
                                    state = state,
                                    whileBlock = cmd.whileBlock,
                                    onInteraction = onInteraction,
                                    vars = vars,
                                    context = context
                                )
                                is ForBlockCommand -> ForBlockCard(
                                    state = state,
                                    forBlock = cmd.forBlock,
                                    onInteraction = onInteraction,
                                    vars = vars,
                                    context = context
                                )
                                else -> stringResource(R.string.unknown_block)
                            }
                        }
                    }
                    if (state.showChooseForDialog)
                        ChooseForBlockDialog(state, context)
                }
            }
        }
    }
}


@Composable
fun ChooseForBlockDialog(state: CodeBlockState, context: Context) {
    Dialog(
        onDismissRequest = {
            state.showNewForDialog = false
            state.newForStartExpr = "0"
            state.newForEndExpr = "10"
            state.selectedForOperator = "<"
            state.forBlockError = ""
            state.selectedForTargetId = ""
            state.curForCommands.clear()
            state.newForVar = ""
            state.newForStepIter = "1"
            state.showChooseForDialog = false
        }
    ) {
        val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))

        Surface (
            color = Color(ContextCompat.getColor(context, R.color.dialog)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .shadow(10.dp, shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(context, R.color.shadow))),
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text (
                    text = stringResource(R.string.choose_command),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                        onClick = {
                            state.showNewIfDialog = true
                            state.showChooseForDialog = false
                        }
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "Add If Block",
                            tint = textColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                        onClick = { state.showNewVarDialog = true
                            state.showChooseForDialog = false}
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Var Block",
                            tint = textColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                        onClick = { state.showNewWhileDialog = true
                            state.showChooseForDialog = false}
                    ) {
                        Icon(Icons.Default.Loop, contentDescription = "Add While Block",
                            tint = textColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                        onClick = { state.showNewForDialog = true
                            state.showChooseForDialog = false}
                    ) {
                        Icon(Icons.Default.Replay5, contentDescription = "Add For Block",
                            tint = textColor)
                    }
                    // надо будет потом добавить списки когда сделаю
                    // глобальные и локальные типы данных
                }
            }
        }
    }
}