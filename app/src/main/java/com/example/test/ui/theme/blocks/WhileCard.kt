package com.example.test.ui.theme.blocks

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.test.CodeBlockState
import com.example.test.IfBlockCommand
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.WhileBlock
import com.example.test.WhileBlockCommand
import kotlin.math.roundToInt

@Composable
fun WhileBlockCard(
    state: CodeBlockState,
    whileBlock: WhileBlock,
    onInteraction: (Offset, String) -> Unit,
    vars: List<Variable>
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
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(12.dp)
        ) {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                IconButton(onClick = {expanded = !expanded}) {
                    Icon (
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand"
                    )
                }
                Text (
                    modifier = Modifier.fillMaxWidth(),
                    text = "while (${whileBlock.leftExpression}" +
                            " ${whileBlock.comparisonOperator} "+
                            "${whileBlock.rightExpression})",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (expanded) {
            Box (
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
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
                    )
                    IconButton( onClick = {
                        state.targetCommandsList = whileBlock.commands
                        state.showChooseWhileDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add inner Block")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (whileBlock.commands.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_commands),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    else {
                        whileBlock.commands.forEach { cmd ->
                            when(cmd) {
                                is WhileBlockCommand -> WhileBlockCard(
                                    state = state,
                                    whileBlock = cmd.whileBlock,
                                    onInteraction = onInteraction,
                                    vars = vars
                                )
                                is IfBlockCommand -> IfBlockCard(
                                    state = state,
                                    ifBlock = cmd.ifBlock,
                                    onInteraction = onInteraction,
                                    vars = vars
                                )
                                is VarBlockCommand -> VarCard(
                                    variable = cmd.variable,
                                    vars = vars,
                                    hasError = false,
                                    onInteraction = onInteraction

                                )
                                else -> Text(stringResource(R.string.unknown_block))
                            }
                        }
                    }
                    if (state.showChooseWhileDialog) {
                        ChooseWhileBlockDialog(state)
                    }
                }
            }
        }
    }
}

@Composable
fun ChooseWhileBlockDialog(state: CodeBlockState) {
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
        Surface(
            color = MaterialTheme.colorScheme.surface,
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
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.padding(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically

                ) {

                    FloatingActionButton(
                        onClick = { state.showNewIfDialog = true
                            state.showChooseWhileDialog = false}
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "Add If Block")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = { state.showNewWhileDialog = true
                            state.showChooseWhileDialog = false}
                    ) {
                        Icon(Icons.Default.Loop, contentDescription = "Add While Block")
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    FloatingActionButton(
                        onClick = { state.showNewVarDialog = true
                            state.showChooseWhileDialog = false}
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Var Block")
                    }
                }
            }
        }
    }
}
