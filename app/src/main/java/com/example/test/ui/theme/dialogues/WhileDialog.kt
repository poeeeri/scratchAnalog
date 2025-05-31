package com.example.test.ui.theme.dialogues

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.test.CodeBlockState
import com.example.test.CommandBlock
import com.example.test.R
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.VariableType
import com.example.test.WhileBlock
import com.example.test.WhileBlockCommand
import com.example.test.utils.preprocessArrayExprForDisplay

@SuppressLint("StringFormatInvalid")
@Composable
fun WhileDialog(state: CodeBlockState, ctx: Context) {
    val textAreaColor = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
        unfocusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.cycle_main_color)),
        errorBorderColor = Color(ContextCompat.getColor(ctx, R.color.error_color)),
        cursorColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    )
    val textColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    Dialog(onDismissRequest = {
        state.showNewWhileDialog = false
        state.leftWhileExpression = ""
        state.rightWhileExpression = ""
        state.selectedWhileTargetId = ""
        state.newWhileCommand = ""
        state.curWhileCommands.clear()
        state.selectedWhileOperator = "=="
        state.whileBlockError = ""
    }) {
        Surface(
            color = Color(ContextCompat.getColor(ctx, R.color.dialog)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .shadow(
                    10.dp, shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(ctx, R.color.shadow))
                )
        ) {
            val leftExpr = stringResource(R.string.left_expression)
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.create_while_block),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.leftWhileExpression,
                    onValueChange = { state.leftWhileExpression = it },
                    label = {
                        Text(
                            leftExpr,
                            color = textColor,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textAreaColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                val comparisonOpers = listOf("==", "!=", ">", "<", ">=", "<=")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = state.selectedWhileOperator,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                "Comparison Operator",
                                color = textColor
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = if (expanded)
                                    Icons.Filled.ArrowDropUp
                                else Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    expanded = !expanded
                                },
                                tint = Color(
                                    ContextCompat.getColor(
                                        ctx,
                                        R.color.light_green_for_text
                                    )
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        colors = textAreaColor
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(
                            Color(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.dark_header
                                )
                            )
                        )
                    ) {
                        comparisonOpers.forEach { oper ->
                            DropdownMenuItem(
                                text = { Text(oper) },
                                onClick = {
                                    state.selectedWhileOperator = oper
                                    expanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color(
                                        ContextCompat.getColor(
                                            ctx,
                                            R.color.light_green_for_text
                                        )
                                    )
                                ),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.rightWhileExpression,
                    onValueChange = { state.rightWhileExpression = it },
                    label = {
                        Text(
                            "Right Expression",
                            color = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textAreaColor
                )
                Text(
                    text = stringResource(R.string.commands_while),
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(8.dp))

                state.curWhileCommands.forEachIndexed { i, com ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (com is VarBlockCommand) {
                            var istr = i + 1
                            var varName = com.variable.name
                            var varExpr = preprocessArrayExprForDisplay(com.variable.expression)
                            Text(
                                text = ctx.getString(
                                    R.string.expression,
                                    istr.toString(),
                                    varName,
                                    varExpr
                                ),
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )
                        } else {
                            var istr = i + 1
                            Text(
                                text = ctx.getString(R.string.remove_command, istr.toString()),
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )
                        }
                        IconButton(
                            onClick = { state.curWhileCommands.removeAt(i) }
                        ) {
                            Icon(
                                Icons.Default.Delete, contentDescription = "Remove Command",
                                tint = Color(
                                    ContextCompat.getColor(
                                        ctx,
                                        R.color.light_green_for_text
                                    )
                                )
                            )
                        }
                    }
                    HorizontalDivider()
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.newWhileCommand,
                        onValueChange = {
                            state.newWhileCommand = it
                            state.whileBlockError = ""
                        },
                        label = {
                            Text(
                                "New Var Expression",
                                color = textColor,
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = textAreaColor
                    )
                    IconButton(
                        onClick = {
                            val newCommand = state.newWhileCommand.trim()
                            if (newCommand.isNotBlank()) {
                                val arrayAssignPattern =
                                    Regex("([a-zA-Z_]\\w*)\\[(.*?)\\]\\s*=\\s*(.*)")
                                val arrMatch = arrayAssignPattern.matchEntire(newCommand)
                                if (arrMatch != null) {
                                    val arrName = arrMatch.groupValues[1]
                                    val indexExpr = arrMatch.groupValues[2]
                                    val valueExpr = arrMatch.groupValues[3]

                                    val isExist = state.arrays.any { it.name == arrName }
                                    if (!isExist) {
                                        state.whileBlockError = ctx.getString(
                                            R.string.err_array_not_found,
                                            arrName
                                        )
                                        return@IconButton
                                    }
                                    val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                    val indexVars =
                                        regex.findAll(indexExpr).map { it.value }.toSet()
                                    val valueVars =
                                        regex.findAll(valueExpr).map { it.value }.toSet()

                                    val usedVars = indexVars + valueVars
                                    val declaredVars = state.vars.map { it.name }
                                        .toSet() + state.arrays.map { it.name }.toSet()
                                    val notDeclared = declaredVars - usedVars
                                    if (notDeclared.isNotEmpty()) {
                                        state.whileBlockError = ctx.getString(
                                            R.string.err_undeclared_var,
                                            notDeclared.joinToString(", ")
                                        )
                                        return@IconButton
                                    }
                                    val tmpVar = Variable(
                                        name = "",
                                        expression = newCommand,
                                        type = VariableType.INT,
                                        pos = IntOffset(0, state.curWhileCommands.size * 220)
                                    )
                                    state.curWhileCommands.add(VarBlockCommand(tmpVar))
                                    state.newWhileCommand = ""
                                    state.whileBlockError = ""
                                    return@IconButton
                                }
                                val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                val usedVars = regex.findAll(newCommand).map { it.value }.toSet()

                                val declaredVars = state.vars.map { it.name }
                                    .toSet() + state.arrays.map { it.name }.toSet()
                                val notDeclared = usedVars - declaredVars

                                if (notDeclared.isNotEmpty()) {
                                    state.whileBlockError = ctx.getString(
                                        R.string.err_undeclared_var,
                                        notDeclared.joinToString(", ")
                                    )
                                    return@IconButton
                                }
                            }

                            if (state.newWhileCommand.isNotBlank()) {
                                val parts = state.newWhileCommand.split("=")
                                val name = parts.getOrNull(0)?.trim() ?: "var"
                                val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                state.curWhileCommands.add(
                                    VarBlockCommand(
                                        Variable(
                                            name = name,
                                            expression = expr,
                                            pos = IntOffset(0, state.curWhileCommands.size * 220)
                                        )
                                    )
                                )
                                state.newWhileCommand = ""
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Add Command",
                            tint = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
                        )
                    }
                }
                if (state.whileBlockError.isNotBlank()) {
                    Text(
                        text = state.whileBlockError,
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (state.leftWhileExpression.isBlank()) {
                                state.whileBlockError = "Left part must not be empty"
                                return@Button
                            }
                            if (state.rightWhileExpression.isBlank()) {
                                state.whileBlockError = "Right part must not be empty"
                                return@Button
                            }

                            val declaredVarsNames =
                                state.vars.map { it.name }.toSet() + state.arrays.map { it.name }
                                    .toSet()
                            val regex = Regex("(?!\\d)([a-zA-Z_]\\w*)(?:\\s*\\[.*?\\])?")
                            val leftPartVars = regex.findAll(state.leftWhileExpression).map {
                                if (it.value.contains("[")) it.value.substring(
                                    0,
                                    it.value.indexOf("[")
                                ).trim()
                                else it.value
                            }.toSet()
                            val rightPartVars = regex.findAll(state.rightWhileExpression).map {
                                if (it.value.contains("[")) it.value.substring(
                                    0,
                                    it.value.indexOf("[")
                                ).trim()
                                else it.value
                            }.toSet()
                            val notDeclared = (leftPartVars + rightPartVars) - declaredVarsNames
                            if (notDeclared.isNotEmpty()) {
                                state.whileBlockError =
                                    "Undeclared variable(-s): ${notDeclared.joinToString(", ")}"
                                return@Button
                            }
                            if (state.selectedWhileTargetId.isNotEmpty()) {
                                val i =
                                    state.whileBlocks.indexOfFirst { it.id == state.selectedWhileTargetId }
                                if (i >= 0) {
                                    state.whileBlocks[i] = state.whileBlocks[i].copy(
                                        leftExpression = state.leftWhileExpression,
                                        rightExpression = state.rightWhileExpression,
                                        comparisonOperator = state.selectedWhileOperator,
                                        commands = state.curWhileCommands,
                                        pos = IntOffset(
                                            state.whileBlocks[i].pos.x,
                                            state.whileBlocks[i].pos.y
                                        )
                                    )
                                }
                                if (state.newWhileCommand.isNotBlank()) {
                                    val parts = state.newWhileCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                    state.curWhileCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curWhileCommands.size * 60)
                                            )
                                        )
                                    )
                                    state.newWhileCommand = ""
                                }
                            } else {
                                if (state.newWhileCommand.isNotBlank()) {
                                    val parts = state.newWhileCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                    state.curWhileCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curWhileCommands.size * 60)
                                            )
                                        )
                                    )
                                    state.newWhileCommand = ""
                                }
                                val commandsCopy: SnapshotStateList<CommandBlock> =
                                    mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curWhileCommands)
                                    }

                                // тут уже создаю саму карту
                                val newIf = WhileBlock(
                                    leftExpression = state.leftWhileExpression,
                                    rightExpression = state.rightWhileExpression,
                                    comparisonOperator = state.selectedWhileOperator,
                                    commands = commandsCopy,
                                    pos = IntOffset(10, 10 + state.whileBlocks.size * 120)
                                )

                                // проерка куда вставлять блок, будет ли он являться независимым
                                // илли вложенным
                                if (state.targetCommandsList != null) {
                                    state.targetCommandsList?.add(WhileBlockCommand(newIf))
                                } else {
                                    state.whileBlocks.add(newIf)
                                }
                            }
                            // здесь мы просто копируем блок с командами чтобы сохранялись
                            // корректные списки а не обнулялись
                            if (state.selectedWhileTargetId.isNotEmpty()) {
                                val i =
                                    state.whileBlocks.indexOfFirst { it.id == state.selectedWhileTargetId }
                                if (i >= 0) {
                                    val newCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curWhileCommands)
                                    }
                                    state.whileBlocks[i] = state.whileBlocks[i].copy(
                                        leftExpression = state.leftWhileExpression,
                                        rightExpression = state.rightWhileExpression,
                                        comparisonOperator = state.selectedWhileOperator,
                                        commands = newCommands,
                                        pos = state.whileBlocks[i].pos
                                    )
                                }
                            }
                            state.showNewWhileDialog = false
                            state.leftWhileExpression = ""
                            state.rightWhileExpression = ""
                            state.selectedWhileOperator = "=="
                            state.whileBlockError = ""
                            state.curWhileCommands.clear()
                            state.newWhileCommand = ""
                            state.targetCommandsList = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.light_green_for_text
                                )
                            ),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.header))
                        )
                    ) {
                        Text(
                            stringResource(R.string.create),
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            if (state.selectedWhileTargetId.isNotEmpty()) {
                                val i =
                                    state.whileBlocks.indexOfFirst { it.id == state.selectedWhileTargetId }
                                if (i >= 0) {
                                    val newCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curWhileCommands)
                                    }
                                    state.whileBlocks[i] = state.whileBlocks[i].copy(
                                        leftExpression = state.leftWhileExpression,
                                        rightExpression = state.rightWhileExpression,
                                        comparisonOperator = state.selectedWhileOperator,
                                        commands = newCommands,
                                        pos = state.whileBlocks[i].pos
                                    )
                                }
                            }
                            state.showNewWhileDialog = false
                            state.leftWhileExpression = ""
                            state.rightWhileExpression = ""
                            state.selectedWhileOperator = "=="
                            state.whileBlockError = ""
                            state.curWhileCommands.clear()
                            state.newWhileCommand = ""
                            state.targetCommandsList = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.light_green_for_text
                                )
                            ),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.dark_header))
                        )
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}