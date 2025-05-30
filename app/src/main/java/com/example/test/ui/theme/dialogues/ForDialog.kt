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
import com.example.test.CodeBlockState
import com.example.test.CommandBlock
import com.example.test.ForBlock
import com.example.test.ForBlockCommand
import com.example.test.R
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.utils.calculateArithmeticExpression
import com.example.test.utils.convertToReversePolishNotation
import com.example.test.utils.preprocessArrayExprForDisplay


private fun String.filterExpr() = filter {
    char -> char.isDigit() || char.isLetter() || char == '_' || char in listOf('+', '-', '*', '/', '(', ')', '[', ']', '%')
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForDialog(state: CodeBlockState,
              context: Context) {
    var textAreaColor =OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(ContextCompat.getColor(context, R.color.shadow)),
        unfocusedBorderColor = Color(ContextCompat.getColor(context, R.color.cycle_main_color)),
        errorBorderColor = Color(ContextCompat.getColor(context, R.color.error_color)),
        cursorColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    )
    val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    Dialog(
        onDismissRequest = {
            state.showNewForDialog = false
            state.newForStartExpr = "0"
            state.newForEndExpr = "10"
            state.selectedForTargetId = ""
            state.forBlockError = ""
            state.curForCommands.clear()
            state.newForVar = ""
            state.newForStepIter = "1"
            state.showChooseForDialog = false
        }
    ) {
        fun calculatedValue(v: String) : Double {
            val rpn = convertToReversePolishNotation(v, context)
            return calculateArithmeticExpression(
                rpn,
                state,
                context = context,
                arrays = state.arrays
            )
        }
        Surface (
            color = Color(ContextCompat.getColor(context, R.color.dialog)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(context, R.color.shadow)))
                .fillMaxWidth(),
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
                    text = stringResource(R.string.create_for_block),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                val varName = stringResource(R.string.var_name)
                val errvar_not_be_empty = stringResource(R.string.errvar_not_be_empty)
                val var_must_start = stringResource(R.string.var_must_start)
                val var_must_only_digit = stringResource(R.string.var_must_only_digit)
                val cannotConvert = stringResource(R.string.cannot_convert_float_to_int)

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    OutlinedTextField(
                        label = { Text(varName,
                            color = textColor) },
                        value = state.newForVar,
                        onValueChange = {state.newForVar = it },
                        modifier = Modifier.weight(1f),
                        colors =  textAreaColor
                    )
                    IconButton(
                        onClick = {

                            if (state.newForVar.isBlank()) {
                                state.forBlockError = errvar_not_be_empty
                                return@IconButton
                            }
                            else {
                                val varName = state.newForVar
                                var containsError = false
                                var isExist = false

                                when {
                                    !varName[0].isLetter() && varName[0] != '_' -> {
                                        state.forBlockError = var_must_start
                                        containsError = true
                                        return@IconButton
                                    }

                                    varName.any{!it.isLetterOrDigit() && it != '_'} -> {
                                        state.forBlockError = var_must_start
                                        containsError = true
                                        return@IconButton
                                    }

                                    varName.any { !it.isLetterOrDigit() && it != '_' } -> {
                                        state.newVarError = var_must_only_digit
                                        containsError = true
                                        return@IconButton
                                    }

                                    state.vars.any { it.name == varName } -> {
                                        isExist = true
                                    }
                                }
                                if (!containsError && !isExist) {
                                    state.vars.add(
                                        Variable(
                                            name = state.newForVar,
                                            expression = state.newForStartExpr,
                                            type = state.selectedVarType
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Command",
                            tint = textColor)
                    }

                }

                Spacer(modifier = Modifier.height(8.dp))

                Row{
                    OutlinedTextField(
                        value = state.newForStartExpr,
                        onValueChange = { state.newForStartExpr = it.filterExpr() },
                        label = { Text(start,
                            color = textColor) },
                        modifier = Modifier.weight(1f),
                        colors =  textAreaColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = state.newForEndExpr,
                        onValueChange = { state.newForEndExpr = it.filterExpr() },
                        label = { Text(end,
                            color = textColor) },
                        modifier = Modifier.weight(1f),
                        colors =  textAreaColor
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
                    var expanded by remember {mutableStateOf(false)}
                    OutlinedTextField(
                        value = state.selectedForOperator,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(compOp,
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
                        modifier = Modifier.background(Color(ContextCompat.getColor(context, R.color.dark_header))),
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        comparisonOpers.forEach { oper ->
                            DropdownMenuItem(
                                text = { Text(oper,
                                    color = textColor) },
                                onClick = {
                                    state.selectedForOperator = oper
                                    expanded = false
                                },
                                colors =  MenuDefaults.itemColors(
                                    textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val step = stringResource(R.string.step_iter)
                OutlinedTextField(
                    value = state.newForStepIter,
                    onValueChange = { state.newForStepIter = it.filterExpr() },
                    label = { Text(step,
                        color = textColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors =  textAreaColor
                )

                Spacer(modifier = Modifier.height(8.dp))


                state.curForCommands.forEachIndexed { i, com ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (com is VarBlockCommand) {
                            var istr = i + 1
                            var varName = com.variable.name
                            var varExpr = preprocessArrayExprForDisplay(com.variable.expression)
                            Text(
                                text = context.getString(R.string.expression, istr.toString(), varName, varExpr),
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )
                        } else {
                            var istr = i + 1
                            Text(
                                text = context.getString(R.string.remove_command, istr.toString()),
                                modifier = Modifier.weight(1f),
                                color = textColor
                            )
                        }
                        IconButton(
                            onClick = { state.curForCommands.removeAt(i) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Command",
                                tint = Color(ContextCompat.getColor(context, R.color.light_green_for_text)))
                        }
                    }
                    HorizontalDivider()
                }



                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    OutlinedTextField(
                        value = state.newForCommand,
                        onValueChange = {
                            state.newForCommand = it
                            state.forBlockError = ""
                        },
                        label = {
                            Text(
                                stringResource(R.string.new_var_expression),
                                color = textColor
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = textAreaColor
                    )
                    IconButton(
                        onClick = {
                            val newCommand = state.newForCommand.trim()
                            if (newCommand.isNotBlank()) {
                                val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                val usedVars = regex.findAll(newCommand).map {it.value }.toSet()

                                val declaredVars = state.vars.map{it.name}.toSet() + state.arrays.map{it.name}.toSet()
                                val notDeclared = usedVars-declaredVars

                                if (notDeclared.isNotEmpty()) {
                                    state.forBlockError = context.getString(R.string.err_undeclared_var, notDeclared.joinToString(", "))
                                    return@IconButton
                                }
                            }

                            if (state.newForCommand.isNotBlank()) {
                                val parts = state.newForCommand.split("=")
                                val name = parts.getOrNull(0)?.trim() ?: "var"
                                val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                state.curForCommands.add(
                                    VarBlockCommand(
                                        Variable(
                                            name = name,
                                            expression = expr,
                                            pos = IntOffset(0, state.curForCommands.size * 220)
                                        )
                                    )
                                )
                                state.newForCommand = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Command",
                            tint = Color(ContextCompat.getColor(context, R.color.light_green_for_text)))
                    }

                }

                if (state.forBlockError.isNotBlank()) {
                    Text (
                        text = state.forBlockError,
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val errvar_not_be_empty = stringResource(R.string.errvar_not_be_empty)
                    val var_must_start = stringResource(R.string.var_must_start)
                    val var_must_only_digit = stringResource(R.string.var_must_only_digit)
                    val cannotConvert = stringResource(R.string.cannot_convert_float_to_int)

                    val start_value_must_not_be_empty = stringResource(R.string.start_value_must_not_be_empty)
                    val end_value_must_not_be_empty = stringResource(R.string.end_value_must_not_be_empty)
                    val step_value_must_not_be_empty = stringResource(R.string.step_value_must_not_be_empty)





                    Button(
                        onClick = {





                            if (state.newForStartExpr.isBlank()) {
                                state.forBlockError = start_value_must_not_be_empty
                                return@Button
                            }
                            if (state.newForEndExpr.isBlank()) {
                                state.forBlockError = end_value_must_not_be_empty
                                return@Button
                            }
                            if (state.newForStepIter.isBlank()) {
                                state.forBlockError = step_value_must_not_be_empty
                                return@Button
                            }

                            val declaredVarsNames = state.vars.map { it.name }.toSet() + state.arrays.map{it.name}.toSet()
                            val regex = Regex("([a-zA-Z_]\\w*)(?:\\s*\\[.*?\\])?")
                            val startVars = regex.findAll(state.newForStartExpr).map {
                                if (it.value.contains("[")) it.value.substring(0, it.value.indexOf("[")).trim()
                                else it.value
                            }.toSet()
                            val endVars = regex.findAll(state.newForEndExpr).map {
                                if (it.value.contains("[")) it.value.substring(0, it.value.indexOf("[")).trim()
                                else it.value
                            }.toSet()
                            val varsName = regex.findAll(state.newVarName).map {
                                if (it.value.contains("[")) it.value.substring(0, it.value.indexOf("[")).trim()
                                else it.value
                            }.toSet()

                            val notDeclared = (startVars + endVars + varsName) - declaredVarsNames
                            if (notDeclared.isNotEmpty()) {
                                state.ifBlockError = "Undeclared variable(-s): ${notDeclared.joinToString(", ")}"
                                return@Button
                            }



                            if (state.selectedForTargetId.isNotEmpty()) {
                                val i = state.forBlocks.indexOfFirst { it.id == state.selectedForTargetId }
                                if (i >= 0) {
                                    val newCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curForCommands)
                                    }
                                    state.forBlocks[i] = state.forBlocks[i].copy(
                                        startExpression = state.newForStartExpr,
                                        endExpression = state.newForEndExpr,
                                        comparisonOperator = state.selectedForOperator,
                                        stepIter = state.newForStepIter.toInt(),
                                        commands = newCommands,
                                        pos = state.forBlocks[i].pos
                                    )
                                }
                                if (state.newForCommand.isNotBlank()) {
                                    val parts = state.newForCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                    state.curForCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curForCommands.size * 60)
                                            )
                                        )
                                    )
                                    state.newForCommand = ""
                                }
                            }
                            else {
                                if (state.newForCommand.isNotBlank()) {
                                    val parts = state.newForCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0.0"
                                    state.curForCommands.add(
                                        VarBlockCommand(
                                            Variable(
                                                name = name,
                                                expression = expr,
                                                pos = IntOffset(0, state.curForCommands.size * 60)
                                            )
                                        )
                                    )
                                    state.newForCommand = ""
                                }
                                val commandsCopy: SnapshotStateList<CommandBlock> = mutableStateListOf<CommandBlock>().apply {
                                    addAll(state.curForCommands)
                                }

                                val calculatedValueStart = calculatedValue(state.newForStartExpr)
                                val calculatedValueEnd = calculatedValue(state.newForEndExpr)
                                val calculatedValueStep = calculatedValue(state.newForStepIter)
                                // тут уже создаю саму карту
                                val newFor = ForBlock(
                                    variable = state.newForVar,
                                    startExpression = calculatedValueStart.toInt().toString(),
                                    endExpression = calculatedValueEnd.toInt().toString(),
                                    comparisonOperator = state.selectedForOperator,
                                    stepIter = calculatedValueStep.toInt(),
                                    commands = commandsCopy,
                                    pos = IntOffset(10, 10 + (state.forBlocks.size * 120))
                                )

                                // проерка куда вставлять блок, будет ли он являться независимым
                                // илли вложенным
                                if (state.targetCommandsList != null) {
                                    state.targetCommandsList?.add(ForBlockCommand(newFor))
                                }
                                else {
                                    state.forBlocks.add(newFor)
                                }
                            }
                            if (state.selectedForTargetId.isNotEmpty()) {
                                val i = state.forBlocks.indexOfFirst { it.id == state.selectedForTargetId }
                                if (i >= 0) {
                                    val newCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curForCommands)
                                    }
                                    state.forBlocks[i] = state.forBlocks[i].copy(
                                        startExpression = state.newForStartExpr,
                                        endExpression = state.newForEndExpr,
                                        comparisonOperator = state.selectedForOperator,
                                        commands = newCommands,
                                        stepIter = state.newForStepIter.toInt(),
                                        pos = state.forBlocks[i].pos
                                    )
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
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                            containerColor = Color(ContextCompat.getColor(context, R.color.header))
                        )
                    ) {
                        Text(stringResource(R.string.create),
                            color = textColor,)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            if (state.selectedForTargetId.isNotEmpty()) {
                                val i = state.forBlocks.indexOfFirst { it.id == state.selectedForTargetId }
                                if (i >= 0) {
                                    val newCommands = mutableStateListOf<CommandBlock>().apply {
                                        addAll(state.curForCommands)
                                    }
                                    state.forBlocks[i] = state.forBlocks[i].copy(
                                        startExpression = state.newForStartExpr,
                                        endExpression = state.newForEndExpr,
                                        comparisonOperator = state.selectedForOperator,
                                        stepIter = state.newForStepIter.toInt(),
                                        commands = newCommands,
                                        pos = state.forBlocks[i].pos
                                    )
                                }
                            }
                            state.showNewForDialog = false
                            state.newForStartExpr = "0"
                            state.newForEndExpr = "10"
                            state.selectedForOperator = "<"
                            state.forBlockError = ""
                            state.curForCommands.clear()
                            state.newForCommand = ""
                            state.newForVar = ""
                            state.newForStepIter = "1"
                            state.showChooseForDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                            containerColor = Color(ContextCompat.getColor(context, R.color.dark_header))
                        )
                    ) {
                        Text(stringResource(R.string.cancel),
                            color = textColor)
                    }
                }
            }
        }
    }
}