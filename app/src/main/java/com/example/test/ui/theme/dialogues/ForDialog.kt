package com.example.test.ui.theme.dialogues

import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.test.ForBlock
import com.example.test.ForBlockCommand
import com.example.test.R
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.VariableType
import com.example.test.utils.calculateArithmeticExpression
import com.example.test.utils.convertToReversePolishNotation
import com.example.test.utils.preprocessArrayExprForDisplay

private fun String.filterDigits() = this
private fun String.filterExpressionChars() = filter {
    it.isLetterOrDigit() || it in "+-*/%()[]._" || it.isWhitespace()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForDialog(
    state: CodeBlockState,
    context: Context
) {
    var textAreaColor = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(ContextCompat.getColor(context, R.color.shadow)),
        unfocusedBorderColor = Color(ContextCompat.getColor(context, R.color.cycle_main_color)),
        errorBorderColor = Color(ContextCompat.getColor(context, R.color.error_color)),
        cursorColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    )
    val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    val isEditing = state.isEditingForBlock
    Dialog(
        onDismissRequest = {
            state.showNewForDialog = false
            state.newForStartExpr = "0"
            state.newForEndExpr = "10"
            state.selectedForTargetId = ""
            state.forBlockError = ""
            state.selectedForOperator = "<"
            state.curForCommands.clear()
            state.newForVar = ""
            state.newForStepIter = "1"
            state.showChooseForDialog = false
            state.newForCommand = ""
            state.originalForVar = ""
            state.isEditingForBlock = false
        }
    ) {
        Surface(
            color = Color(ContextCompat.getColor(context, R.color.dialog)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .shadow(
                    10.dp, shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(context, R.color.shadow))
                )
        ) {
            val start = stringResource(R.string.start_expression)
            val end = stringResource(R.string.end_expression)

            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isEditing)
                        stringResource(R.string.edit_for_block)
                    else
                        stringResource(R.string.create_for_block),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                val varName = stringResource(R.string.var_name)
                OutlinedTextField(
                    label = {
                        Text(
                            varName,
                            color = textColor
                        )
                    },
                    value = state.newForVar,
                    onValueChange = { state.newForVar = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textAreaColor,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    OutlinedTextField(
                        value = state.newForStartExpr,
                        onValueChange = { state.newForStartExpr = it },
                        label = {
                            Text(
                                start,
                                color = textColor
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = textAreaColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.newForEndExpr,
                        onValueChange = { state.newForEndExpr = it },
                        label = {
                            Text(
                                end,
                                color = textColor
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = textAreaColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val comparisonOpers = listOf(">", "<", ">=", "<=")
                val compOp = stringResource(R.string.comparison_operator)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = state.selectedForOperator,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text(
                                compOp,
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
                                tint = textColor
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        colors = textAreaColor
                    )

                    DropdownMenu(
                        expanded = expanded,
                        modifier = Modifier.background(
                            Color(
                                ContextCompat.getColor(
                                    context,
                                    R.color.dark_header
                                )
                            )
                        ),
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        comparisonOpers.forEach { oper ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        oper,
                                        color = textColor
                                    )
                                },
                                onClick = {
                                    state.selectedForOperator = oper
                                    expanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.light_green_for_text
                                        )
                                    )
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val step = stringResource(R.string.step_iter)
                OutlinedTextField(
                    value = state.newForStepIter,
                    onValueChange = { state.newForStepIter = it },
                    label = {
                        Text(
                            step,
                            color = textColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textAreaColor
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.commands),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(8.dp))
                state.curForCommands.forEachIndexed { i, com ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (com is VarBlockCommand) {
                            val displayText =
                                if (com.variable.expression.contains(Regex("\\w+\\[.*?\\]\\s*="))) {
                                    "${i + 1}. ${preprocessArrayExprForDisplay(com.variable.expression)}"
                                } else {
                                    "${i + 1}. ${com.variable.name} = ${
                                        preprocessArrayExprForDisplay(
                                            com.variable.expression
                                        )
                                    }"
                                }

                            Text(
                                text = displayText,
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
                            onClick = { state.curForCommands.removeAt(i) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove Command",
                                tint = textColor
                            )
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
                        value = state.newForCommand,
                        onValueChange = { state.newForCommand = it },
                        label = {
                            Text(
                                text = stringResource(R.string.new_command),
                                color = textColor
                            )
                        },
                        placeholder = {
                            Text(
                                text = "x = 5, or m[0] = 7",
                                color = textColor.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = textAreaColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = {
                            if (state.newForCommand.isNotBlank()) {
                                val newCommand = state.newForCommand.trim()

                                val arraySetPattern =
                                    Regex("([a-zA-Z_]\\w*)\\[(.*?)\\]\\s*=\\s*(.*)")
                                val arrMatch = arraySetPattern.matchEntire(newCommand)
                                if (arrMatch != null) {
                                    val arrName = arrMatch.groupValues[1]
                                    val mas = state.arrays.firstOrNull { it.name == arrName }
                                    if (mas == null) {
                                        state.forBlockError = context.getString(
                                            R.string.err_array_not_found,
                                            arrName
                                        )
                                        return@IconButton
                                    }
                                    state.curForCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = "${arrName}_set_${System.currentTimeMillis()}",
                                                expression = newCommand,
                                                pos = IntOffset(0, state.curForCommands.size * 10)
                                            )
                                        )
                                    )
                                } else {
                                    val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                    val usedVars =
                                        regex.findAll(newCommand).map { it.value }.toSet()
                                    val declaredVars = state.vars.map { it.name }.toSet() +
                                            state.arrays.map { it.name }.toSet()
                                    val notDeclared = declaredVars - usedVars

                                    notDeclared.forEach { name ->
                                        val newVar = Variable(
                                            name = name,
                                            expression = "0",
                                            pos = IntOffset(0, state.curForCommands.size * 220)
                                        )
                                        state.vars.add(newVar)
                                    }

                                    val parts = newCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"
                                    val expr = parts.getOrNull(1)?.trim() ?: "0"
                                    state.curForCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curForCommands.size * 10)
                                            )
                                        )
                                    )
                                }
                                state.newForCommand = ""
                                state.forBlockError = ""
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_command),
                            tint = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val errvar_not_be_empty = stringResource(R.string.errvar_not_be_empty)
                    val var_must_start = stringResource(R.string.var_must_start)
                    val var_must_only_digit = stringResource(R.string.var_must_only_digit)
                    val cannotConvert = stringResource(R.string.cannot_convert_float_to_int)

                    fun calculatedValue(v: String): Double {
                        val rpn = convertToReversePolishNotation(v, context)
                        return calculateArithmeticExpression(
                            rpn,
                            state,
                            context = context,
                            arrays = state.arrays,
                            targetVarType = VariableType.INT
                        )
                    }

                    Button(
                        onClick = {
                            if (state.newForVar.isNotBlank()) {
                                val varName = state.newForVar
                                var containsError = false

                                when {
                                    !varName[0].isLetter() && varName[0] != '_' -> {
                                        state.forBlockError = var_must_start
                                        containsError = true
                                        return@Button
                                    }

                                    varName.any { !it.isLetterOrDigit() && it != '_' } -> {
                                        state.forBlockError = var_must_start
                                        containsError = true
                                        return@Button
                                    }

                                    varName.any { !it.isLetterOrDigit() && it != '_' } -> {
                                        state.newVarError = var_must_only_digit
                                        containsError = true
                                        return@Button
                                    }
                                }

                                if (isEditing) {
                                    val originalVarName = state.originalForVar
                                    val newVarName = state.newForVar
                                    if (originalVarName != newVarName) {
                                        val existingVar =
                                            state.vars.firstOrNull { it.name == newVarName }
                                        val existingArr =
                                            state.arrays.firstOrNull { it.name == newVarName }
                                        if ((existingVar != null && existingVar.name != originalVarName) || existingArr != null) {
                                            state.forBlockError = context.getString(
                                                R.string.err_var_already_exist,
                                                newVarName
                                            )
                                            return@Button
                                        }
                                        val varToRename =
                                            state.vars.firstOrNull { it.name == originalVarName }
                                        if (varToRename != null) {
                                            val i = state.vars.indexOf(varToRename)
                                            state.vars[i] = varToRename.copy(name = newVarName)
                                        }
                                    }
                                } else {
                                    val existingVar = state.vars.firstOrNull { it.name == varName }
                                    val existingArr =
                                        state.arrays.firstOrNull { it.name == varName }
                                    if (existingVar == null && existingArr == null) {
                                        state.vars.add(
                                            Variable(
                                                name = varName,
                                                expression = "0",
                                                type = state.selectedVarType
                                            )
                                        )
                                    }
                                }
                            } else {
                                state.forBlockError = errvar_not_be_empty
                                return@Button
                            }

                            val calculatedValueStart =
                                calculatedValue(state.newForStartExpr)
                            val calculatedValueEnd = calculatedValue(state.newForEndExpr)
                            val calculatedValueStep = calculatedValue(state.newForStepIter)

                            if (calculatedValueStart != calculatedValueStart.toInt()
                                    .toDouble() ||
                                calculatedValueEnd != calculatedValueEnd.toInt()
                                    .toDouble() ||
                                calculatedValueStep != calculatedValueStep.toInt()
                                    .toDouble()
                            ) {
                                state.forBlockError = cannotConvert
                                return@Button
                            }

                            if (calculatedValueStep == 0.0) {
                                state.forBlockError = context.getString(
                                    R.string.err_step_cannot_be_zero
                                )
                                return@Button
                            }

                            if (state.newForCommand.isNotBlank()) {
                                val newCommand = state.newForCommand.trim()

                                val arraySetPattern =
                                    Regex("([a-zA-Z_]\\w*)\\[(.*?)\\]\\s*=\\s*(.*)")
                                val arrMatch = arraySetPattern.matchEntire(newCommand)
                                if (arrMatch != null) {
                                    val arrName = arrMatch.groupValues[1]
                                    val mas = state.arrays.firstOrNull { it.name == arrName }
                                    if (mas == null) {
                                        state.forBlockError = context.getString(
                                            R.string.err_array_not_found,
                                            arrName
                                        )
                                        return@Button
                                    }

                                    state.curForCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = "${arrName}_set_${System.currentTimeMillis()}",
                                                expression = newCommand,
                                                pos = IntOffset(0, state.curForCommands.size * 10)
                                            )
                                        )
                                    )
                                } else {
                                    val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                    val usedVars =
                                        regex.findAll(newCommand).map { it.value }.toSet()
                                    val declaredVars = state.vars.map { it.name }
                                        .toSet() + state.arrays.map { it.name }.toSet()
                                    val notDeclared = usedVars - declaredVars

                                    notDeclared.forEach { name ->
                                        val newVar = Variable(
                                            name = name,
                                            expression = "0",
                                            pos = IntOffset(0, state.curForCommands.size * 220)
                                        )
                                        state.vars.add(newVar)
                                    }

                                    val parts = newCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"
                                    val expr = parts.getOrNull(1)?.trim() ?: "0"
                                    state.curForCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curForCommands.size * 10)
                                            )
                                        )
                                    )
                                }
                                state.newForCommand = ""
                                state.forBlockError = ""
                            }

                            val commandsCopy: SnapshotStateList<CommandBlock> =
                                mutableStateListOf<CommandBlock>().apply {
                                    addAll(state.curForCommands)
                                }

                            if (isEditing) {
                                val i =
                                    state.forBlocks.indexOfFirst { it.id == state.selectedForTargetId }
                                if (i >= 0) {
                                    val newCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curForCommands)
                                    }
                                    state.forBlocks[i] = state.forBlocks[i].copy(
                                        variable = state.newForVar,
                                        startExpression = state.newForStartExpr,
                                        endExpression = state.newForEndExpr,
                                        comparisonOperator = state.selectedForOperator,
                                        commands = newCommands,
                                        stepIter = state.newForStepIter.toInt(),
                                        pos = state.forBlocks[i].pos
                                    )
                                }
                            } else {
                                val newFor = ForBlock(
                                    variable = state.newForVar,
                                    startExpression = state.newForStartExpr,
                                    endExpression = state.newForEndExpr,
                                    comparisonOperator = state.selectedForOperator,
                                    stepIter = calculatedValueStep.toInt(),
                                    commands = commandsCopy,
                                    pos = IntOffset(10, 10 + (state.forBlocks.size * 120))
                                )
                                if (state.targetCommandsList != null) {
                                    state.targetCommandsList?.add(ForBlockCommand(newFor))
                                } else {
                                    state.forBlocks.add(newFor)
                                }
                            }
                            state.showNewForDialog = false
                            state.newForStartExpr = "0"
                            state.newForEndExpr = "10"
                            state.selectedForOperator = "<"
                            state.forBlockError = ""
                            state.curForCommands.clear()
                            state.newForVar = ""
                            state.newForCommand = ""
                            state.newForStepIter = "1"
                            state.showChooseForDialog = false
                            state.originalForVar = ""
                            state.isEditingForBlock = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(
                                ContextCompat.getColor(
                                    context,
                                    R.color.light_green_for_text
                                )
                            ),
                            containerColor = Color(ContextCompat.getColor(context, R.color.header))
                        )
                    ) {
                        Text(
                            if (isEditing) stringResource(R.string.update) else stringResource(R.string.create),
                            color = textColor,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            state.showNewForDialog = false
                            state.newForStartExpr = "0"
                            state.newForEndExpr = "10"
                            state.selectedForOperator = "<"
                            state.forBlockError = ""
                            state.curForCommands.clear()
                            state.newForVar = ""
                            state.newForCommand = ""
                            state.newForStepIter = "1"
                            state.selectedForTargetId = ""
                            state.showChooseForDialog = false
                            state.isEditingForBlock = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(
                                ContextCompat.getColor(
                                    context,
                                    R.color.light_green_for_text
                                )
                            ),
                            containerColor = Color(
                                ContextCompat.getColor(
                                    context,
                                    R.color.dark_header
                                )
                            )
                        )
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = textColor
                        )
                    }
                }

                if (state.forBlockError.isNotBlank()) {
                    Text(
                        text = state.forBlockError,
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}