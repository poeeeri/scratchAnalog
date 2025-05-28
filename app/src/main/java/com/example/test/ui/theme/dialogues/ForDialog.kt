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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.example.test.Variable
import com.example.test.utils.calculateArithmeticExpression
import com.example.test.utils.convertToReversePolishNotation

private fun String.filterDigits() = filter { it.isDigit() } // изменить на обработчик еше и массивов

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
        Surface (
            color = Color(ContextCompat.getColor(context, R.color.dialog)),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .shadow(10.dp, shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(context, R.color.shadow)))
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
                OutlinedTextField(
                    label = { Text(varName,
                        color = textColor) },
                    value = state.newForVar,
                    onValueChange = {state.newForVar = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors =  textAreaColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row{
                    OutlinedTextField(
                        value = state.newForStartExpr,
                        onValueChange = { state.newForStartExpr = it.filterDigits() },
                        label = { Text(start,
                            color = textColor) },
                        modifier = Modifier.weight(1f),
                        colors =  textAreaColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.newForEndExpr,
                        onValueChange = { state.newForEndExpr = it },
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
                    onValueChange = { state.newForStepIter = it.filterDigits() },
                    label = { Text(step,
                        color = textColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors =  textAreaColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val errvar_not_be_empty = stringResource(R.string.errvar_not_be_empty)
                    val var_must_start = stringResource(R.string.var_must_start)
                    val var_must_only_digit = stringResource(R.string.var_must_only_digit)
                    val cannotConvert = stringResource(R.string.cannot_convert_float_to_int)

                    fun calculatedValue(v: String) : Double {
                        val rpn = convertToReversePolishNotation(v, context)
                        return calculateArithmeticExpression(
                            rpn,
                            state,
                            context = context,
                            arrays = state.arrays
                        )
                    }

                    Button(
                        onClick = {
                            if (state.newForVar.isNotBlank()) {
                                val varName = state.newForVar
                                var containsError = false
                                var isExist = false


                                when {
                                    !varName[0].isLetter() && varName[0] != '_' -> {
                                        state.forBlockError = var_must_start
                                        containsError = true
                                        return@Button
                                    }

                                    varName.any{!it.isLetterOrDigit() && it != '_'} -> {
                                        state.forBlockError = var_must_start
                                        containsError = true
                                        return@Button
                                    }

                                    varName.any { !it.isLetterOrDigit() && it != '_' } -> {
                                        state.newVarError = var_must_only_digit
                                        containsError = true
                                        return@Button
                                    }

                                    state.vars.any { it.name == varName } -> {
                                        isExist = true
                                    }
                                }

                                val calculatedValueStart = calculatedValue(state.newForStartExpr)
                                val calculatedValueEnd = calculatedValue(state.newForEndExpr)
                                val calculatedValueStep = calculatedValue(state.newForStepIter)

                                if (calculatedValueStart != calculatedValueStart.toInt().toDouble() ||
                                    calculatedValueEnd != calculatedValueEnd.toInt().toDouble() ||
                                    calculatedValueStep != calculatedValueStep.toInt().toDouble()) {
                                    state.forBlockError = cannotConvert
                                    return@Button
                                }

                                if (!containsError && !isExist) {
                                    state.vars.add(
                                        Variable(
                                            name = state.newForVar,
                                            expression = "0",
                                            type = state.selectedVarType
                                        )
                                    )
                                }
                            }
                            else {
                                state.forBlockError = errvar_not_be_empty
                                return@Button
                            }

                            val commandsCopy: SnapshotStateList<CommandBlock> = mutableStateListOf<CommandBlock>().apply {
                                addAll(state.curForCommands)
                            }

                            val calculatedValueStart = calculatedValue(state.newForStartExpr)
                            val calculatedValueEnd = calculatedValue(state.newForEndExpr)
                            val calculatedValueStep = calculatedValue(state.newForStepIter)

                            val newFor = ForBlock(
                                variable = state.newForVar,
                                startExpression = calculatedValueStart.toInt().toString(),
                                endExpression = calculatedValueEnd.toInt().toString(),
                                comparisonOperator = state.selectedForOperator,
                                stepIter = calculatedValueStep.toInt(),
                                commands = commandsCopy,
                                pos = IntOffset(10, 10 + (state.forBlocks.size * 120))
                            )

                            if (state.targetCommandsList != null) {
                                state.targetCommandsList?.add(ForBlockCommand(newFor))
                            }
                            else {
                                state.forBlocks.add(newFor)
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

                if (state.forBlockError.isNotBlank()) {
                    Text (
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