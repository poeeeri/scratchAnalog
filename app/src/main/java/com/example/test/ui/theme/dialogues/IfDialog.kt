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
import com.example.test.BlockItem
import com.example.test.CodeBlockState
import com.example.test.CommandBlock
import com.example.test.IfBlock
import com.example.test.IfBlockCommand
import com.example.test.R
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.VariableType
import com.example.test.utils.calculateArithmeticExpression
import com.example.test.utils.convertToReversePolishNotation
import com.example.test.utils.preprocessArrayExprForDisplay

@SuppressLint("StringFormatInvalid")
@Composable
fun IfDialog(state: CodeBlockState, ctx: Context) {
    val textColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    val textAreaColor = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
        unfocusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.cycle_main_color)),
        errorBorderColor = Color(ContextCompat.getColor(ctx, R.color.error_color)),
        cursorColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    )
    Dialog(onDismissRequest = {
        state.showNewIfDialog = false
        state.leftIfExpression = ""
        state.rightIfExpression = ""
        state.selectedComparisonOperator = "=="
        state.ifBlockError = ""
        state.curBlockCommands.clear()
        state.curElseCommands.clear()
        state.newIfCommand = ""
        state.selectedIfBlock = ""
    }) {
        Surface(
            color = Color(ContextCompat.getColor(ctx, R.color.dialog)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .shadow(10.dp, shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(ctx, R.color.shadow)))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create If Block",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Left part
                OutlinedTextField(
                    value = state.leftIfExpression,
                    onValueChange = { state.leftIfExpression = it },
                    label = { Text("Left Expression",
                        color = textColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors =  textAreaColor
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
                        value = state.selectedComparisonOperator,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Comparison Operator",
                            color = textColor) },
                        trailingIcon = {
                            Icon(
                                imageVector = if (expanded)
                                    Icons.Filled.ArrowDropUp
                                else Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    expanded = !expanded
                                },
                                tint = textColor
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        colors =  textAreaColor

                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(ContextCompat.getColor(ctx, R.color.dark_header)))
                    ) {
                        comparisonOpers.forEach { oper ->
                            DropdownMenuItem(
                                text = { Text(oper,
                                    color = textColor) },
                                onClick = {
                                    state.selectedComparisonOperator = oper
                                    expanded = false
                                },
                                colors =  MenuDefaults.itemColors(
                                    textColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
                                ),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Right part
                // Я русский я иду до конца!!!!
                OutlinedTextField(
                    value = state.rightIfExpression,
                    onValueChange = { state.rightIfExpression = it },
                    label = { Text("Right Expression",
                        color = textColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors =  textAreaColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.commands_if_true),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.curBlockCommands.forEachIndexed { i, com ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (com is VarBlockCommand) {
                            Text(
                                text = "${i + 1}. ${com.variable.name} = ${
                                    preprocessArrayExprForDisplay(com.variable.expression)}",
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )
                        } else {
                            Text(
                                text = "${i + 1}. Command block",
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )
                        }
                        IconButton(
                            onClick = { state.curBlockCommands.removeAt(i) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Command",
                                tint = textColor)
                        }
                    }
                    HorizontalDivider()
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.newIfCommand,
                        onValueChange = { state.newIfCommand = it },
                        label = { Text("New Command",
                            color = textColor) },
                        modifier = Modifier.weight(1f),
                        colors =  textAreaColor
                    )
                    IconButton(
                        onClick = {
                            val newCommand = state.newIfCommand.trim()
                            if (newCommand.isNotBlank()) {
                                val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                val usedVars = regex.findAll(newCommand).map {it.value }.toSet()

                                val declaredVars = state.vars.map{it.name}
                                    .toSet() + state.arrays.map{it.name}.toSet()
                                val notDeclared = usedVars-declaredVars

//                                if (notDeclared.isNotEmpty()) {
//                                    state.ifBlockError = ctx.getString(R.string.err_undeclared_var, notDeclared.joinToString(", "))
//                                    return@IconButton
//                                }
                                notDeclared.forEach { name ->
                                    val newVar = Variable(name = name, expression = "0", pos = IntOffset(0, state.curBlockCommands.size * 220))
                                    state.vars.add(newVar)
                                    state.blockItems.add(BlockItem.VarBlock(newVar))
                                }
                            }

//                            if (state.newIfCommand.isNotBlank()) {
                            val parts = state.newIfCommand.split("=")
                            val name = parts.getOrNull(0)?.trim() ?: "var"

                            val expr = parts.getOrNull(1)?.trim() ?: "0"
                            val existingVar = state.vars.find { it.name == name }
                            if (existingVar != null) {
                                try {
                                    val rpn = convertToReversePolishNotation(
                                        expr,
                                        ctx
                                    )
                                    val calculatedValue = calculateArithmeticExpression(
                                        rpn,
                                        state,
                                        context = ctx,
                                        arrays = state.arrays
                                    )
                                    if (existingVar.type == VariableType.INT &&
                                        calculatedValue != calculatedValue.toInt().toDouble()) {
                                        state.ifBlockError = "Cannot convert float to int"
                                        return@IconButton
                                    }
                                }
                                catch (e: Exception) {
                                    return@IconButton
                                }
                            }

                            state.curBlockCommands.add(
                                VarBlockCommand(
                                    Variable(
                                        name = name,
                                        expression = expr,
                                        value = expr.toDoubleOrNull() ?: 0.0,
                                        pos = IntOffset(0, state.curBlockCommands.size * 10)
                                    )
                                )
                            )
                            state.newIfCommand = ""
                            state.ifBlockError = ""
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Command",
                            tint = textColor)
                    }
                }



                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Commands (if condition is false):",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                state.curElseCommands.forEachIndexed { i, com ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (com is VarBlockCommand) {
                            Text(
                                text = "${i + 1}. ${com.variable.name} = ${
                                    preprocessArrayExprForDisplay(
                                        com.variable.expression
                                    )
                                }",
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                text = "${i + 1}. Command block",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        IconButton(
                            onClick = { state.curElseCommands.removeAt(i) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Command",
                                tint = textColor)
                        }
                    }
                    HorizontalDivider()
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.newElseCommand,
                        onValueChange = { state.newElseCommand = it },
                        label = { Text("New Command (Else)") },
                        modifier = Modifier.weight(1f),
                        colors =  textAreaColor
                    )
                    IconButton(
                        onClick = {
                            val newCommand = state.newElseCommand.trim()
                            if (newCommand.isNotBlank()) {
                                val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                val usedVars = regex.findAll(newCommand).map { it.value }.toSet()

                                val declaredVars = state.vars.map { it.name }
                                    .toSet() + state.arrays.map { it.name }.toSet()
                                val notDeclared = usedVars - declaredVars

                                notDeclared.forEach{ name ->
                                    val newVar = Variable(name = name, expression = "0", value = 0, pos = IntOffset(0, state.curBlockCommands.size * 220))
                                    state.vars.add(newVar)
                                    state.blockItems.add(BlockItem.VarBlock(newVar))
                                }
                            }

                            val parts = state.newElseCommand.split("=")
                            val name = parts.getOrNull(0)?.trim() ?: "var"
                            val expr = parts.getOrNull(1)?.trim() ?: "0"


                            val existingVar = state.vars.find { it.name == name }
                            if (existingVar != null) {
                                try {
                                    val rpn = convertToReversePolishNotation(
                                        expr,
                                        ctx
                                    )
                                    val calculatedValue = calculateArithmeticExpression(
                                        rpn,
                                        state,
                                        context = ctx,
                                        arrays = state.arrays
                                    )
                                    if (existingVar.type == VariableType.INT &&
                                        calculatedValue != calculatedValue.toInt().toDouble()) {
                                        state.ifBlockError = "Cannot convert float to int"
                                        return@IconButton
                                    }
                                }
                                catch (e: Exception) {
                                    return@IconButton
                                }
                            }



                            state.curElseCommands.add(
                                VarBlockCommand(
                                    Variable(
                                        name = name,
                                        expression = expr,
                                        value = expr.toIntOrNull() ?: 0,
                                        pos = IntOffset(0, state.curElseCommands.size * 220)
                                    )
                                )
                            )
                            state.newElseCommand = ""
                            state.ifBlockError = ""
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Command",
                            tint = textColor)
                    }
                }




                if (state.ifBlockError.isNotBlank()) {
                    Text(
                        text = state.ifBlockError,
                        color =Color(ContextCompat.getColor(ctx, R.color.light_green_for_text)),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (state.leftIfExpression.isBlank()) {
                                state.ifBlockError = "Left part must not be empty"
                                return@Button
                            }
                            if (state.rightIfExpression.isBlank()) {
                                state.ifBlockError = "Right part must not be empty"
                                return@Button
                            }

                            val declaredVarsNames = state.vars.map { it.name }.toSet() + state.arrays.map{it.name}.toSet()
                            val regex = Regex("([a-zA-Z_]\\w*)(?:\\s*\\[.*?\\])?")
                            val leftPartVars = regex.findAll(state.leftIfExpression).map {
                                if (it.value.contains("[")) it.value.substring(0, it.value.indexOf("[")).trim()
                                else it.value
                            }.toSet()
                            val rightPartVars = regex.findAll(state.rightIfExpression).map {
                                if (it.value.contains("[")) it.value.substring(0, it.value.indexOf("[")).trim()
                                else it.value
                            }.toSet()
                            val notDeclared = (leftPartVars + rightPartVars) - declaredVarsNames
                            if (notDeclared.isNotEmpty()) {
                                state.ifBlockError = "Undeclared variable(-s): ${notDeclared.joinToString(", ")}"
                                return@Button
                            }
                            if (state.selectedIfBlock.isNotEmpty()) {
                                val i = state.ifBlock.indexOfFirst { it.id == state.selectedIfBlock }
                                if (i >= 0) {
                                    state.ifBlock[i] = state.ifBlock[i].copy(
                                        leftExpression = state.leftIfExpression,
                                        rightExpression = state.rightIfExpression,
                                        comparisonOperator = state.selectedComparisonOperator,
                                        commands = state.curBlockCommands,
                                        elseCommands = state.curElseCommands,
                                        pos = IntOffset(
                                            state.ifBlock[i].pos.x,
                                            state.ifBlock[i].pos.y
                                        )
                                    )
                                }
                                if (state.newIfCommand.isNotBlank()) {
                                    val parts = state.newIfCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                    state.curBlockCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curBlockCommands.size * 60)
                                            )
                                        )
                                    )
                                    state.newIfCommand = ""
                                }
                            }
                            else {
                                if (state.newElseCommand.isNotBlank()) {
                                    val parts = state.newElseCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                    state.curElseCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curElseCommands.size * 60)
                                            )
                                        )
                                    )
                                    state.newElseCommand = ""
                                }

                                if (state.newIfCommand.isNotBlank()) {
                                    val parts = state.newIfCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                    state.curBlockCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curBlockCommands.size * 60)
                                            )
                                        )
                                    )
                                    state.newIfCommand = ""
                                }

                                // делаю копию команд чтобы при нажатии на креэйт не сохранялся пустой список
                                val commandsCopy: SnapshotStateList<CommandBlock> = mutableStateListOf<CommandBlock>().apply {
                                    addAll(state.curBlockCommands)
                                }

                                val elseCommandCopy: SnapshotStateList<CommandBlock> = mutableStateListOf<CommandBlock>().apply {
                                    addAll(state.curElseCommands)
                                }

                                // тут уже создаю саму карту с командой для иф-блока
                                val newIf = IfBlock(
                                    leftExpression = state.leftIfExpression,
                                    rightExpression = state.rightIfExpression,
                                    comparisonOperator = state.selectedComparisonOperator,
                                    commands = commandsCopy,
                                    elseCommands = elseCommandCopy,
                                    pos = IntOffset(10, 10 + state.ifBlock.size * 10)
                                )

                                // проерка куда вставлять блок, будет ли он являться независимым
                                // илли вложенным
                                if (state.targetCommandsList != null) {
                                    state.targetCommandsList?.add(IfBlockCommand(newIf))
                                }
                                else {
                                    state.ifBlock.add(newIf)
                                }
                            }
                            // здесь просто создаем новый список команд и его копию чтобы он не обнулялся при редактировании блока
                            if (state.selectedIfBlock.isNotEmpty()) {
                                val i = state.ifBlock.indexOfFirst { it.id == state.selectedIfBlock }
                                if (i >= 0) {
                                    val newCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curBlockCommands)
                                    }
                                    val newElseCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curElseCommands)
                                    }
                                    state.ifBlock[i] = state.ifBlock[i].copy(
                                        leftExpression = state.leftIfExpression,
                                        rightExpression = state.rightIfExpression,
                                        comparisonOperator = state.selectedComparisonOperator,
                                        commands = newCommands,
                                        elseCommands = newElseCommands,
                                        pos = state.ifBlock[i].pos
                                    )
                                }
                            }
                            state.showNewIfDialog = false
                            state.leftIfExpression = ""
                            state.rightIfExpression = ""
                            state.selectedComparisonOperator = "=="
                            state.curBlockCommands.clear()
                            state.curElseCommands.clear()
                            state.newElseCommand = ""
                            state.newIfCommand = ""
                            state.selectedIfBlock = ""
                            state.targetCommandsList = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text)),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.header))
                        )
                    ) {
                        Text(stringResource(R.string.create),
                            color = textColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            if (state.selectedIfBlock.isNotEmpty()) {
                                val i = state.ifBlock.indexOfFirst { it.id == state.selectedIfBlock }
                                if (i >= 0) {
                                    val newCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curBlockCommands)
                                    }
                                    val newElseCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curElseCommands)
                                    }
                                    state.ifBlock[i] = state.ifBlock[i].copy(
                                        leftExpression = state.leftIfExpression,
                                        rightExpression = state.rightIfExpression,
                                        comparisonOperator = state.selectedComparisonOperator,
                                        commands = newCommands,
                                        elseCommands = newElseCommands,
                                        pos = state.ifBlock[i].pos
                                    )
                                }
                            }
                            state.showNewIfDialog = false
                            state.leftIfExpression = ""
                            state.rightIfExpression = ""
                            state.selectedComparisonOperator = "=="
                            state.ifBlockError = ""
                            state.curBlockCommands.clear()
                            state.curElseCommands.clear()
                            state.newIfCommand = ""
                            state.newElseCommand = ""
                            state.selectedIfBlock = ""
                            state.targetCommandsList = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text)),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.dark_header))
                        )
                    ) {
                        Text(stringResource(R.string.cancel),
                            color = textColor,)
                    }
                }
            }
        }
    }
}