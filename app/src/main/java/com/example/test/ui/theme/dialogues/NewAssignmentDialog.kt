package com.example.test.ui.theme.dialogues

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.test.CodeBlockState
import com.example.test.R
import com.example.test.VariableType
import com.example.test.ui.theme.menu.MenuBoxForAssignments
import com.example.test.utils.calculateArithmeticExpression
import com.example.test.utils.convertToReversePolishNotation
import com.example.test.utils.isValidArithmExpression
import com.example.test.utils.preprocessArrayExprForDisplay
import com.example.test.utils.setArrayElement

@SuppressLint("StringFormatInvalid")
@Composable
fun NewAssignmentDialog(state: CodeBlockState, ctx: Context) {
    val textColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    val textAreaColor = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
        unfocusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.cycle_main_color)),
        errorBorderColor = Color(ContextCompat.getColor(ctx, R.color.error_color)),
        cursorColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    )
    Dialog(onDismissRequest = {
        state.showNewAssignmentDialog = false
        state.selectedTargetVar = ""
        state.assignmentArithmExpr = ""
        state.assignmentError = ""
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
            ) {
                Text(
                    text = stringResource(R.string.create_assign),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.select_var),
                    color = textColor
                )

                MenuBoxForAssignments(
                    ctx,
                    state.vars.map { it.name },
                    state.selectedTargetVar,
                ) { selected ->
                    state.selectedTargetVar = selected
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = preprocessArrayExprForDisplay(state.assignmentArithmExpr),
                    onValueChange = { state.assignmentArithmExpr = it },
                    label = { Text("Expression 7(2x + 5)") },
                    isError = state.assignmentError != "",
                    modifier = Modifier.fillMaxWidth(),
                    colors =  textAreaColor
                )
                if (state.assignmentError.isNotBlank()) {
                    Text(
                        text = state.assignmentError,
                        color = textColor,
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
                            // ну тут все понятно надеюсь
                            if (state.selectedTargetVar.isBlank()) {
                                state.assignmentError = R.string.please_select_var.toString()
                                return@Button
                            }
                            else if (state.assignmentArithmExpr.isBlank()) {
                                state.assignmentError = R.string.exp_cannot_be_empty.toString()
                                return@Button
                            }
                            else {
                                val arrayAssignPattern = Regex("([a-zA-Z_]\\w*)\\s*\\[(.*?)\\]\\s*=\\s*(.*)")
                                val arrMatch = arrayAssignPattern.matchEntire(state.assignmentArithmExpr)

                                if (arrMatch != null) {
                                    val arrName = arrMatch.groupValues[1]
                                    val idExpr = arrMatch.groupValues[2]
                                    val valueExpr = arrMatch.groupValues[3]

                                    val success = setArrayElement(
                                        arrName,
                                        idExpr,
                                        valueExpr,
                                        state.arrays.toMutableList(),
                                        state,
                                        ctx
                                    )
                                    if (success) {
                                        state.showNewAssignmentDialog = false
                                        state.selectedTargetVar = ""
                                        state.assignmentError = ""
                                        state.assignmentArithmExpr = ""
                                    }
                                    return@Button
                                }
                                // здесь чекаем есть ли переменная вообще такая
                                val declaredVarsName = state.vars.map {it.name}.toSet() + state.arrays.map {it.name}.toSet()
                                val regex = Regex("([a-zA-Z_]\\w*)(?:\\s*\\[.*?\\])?")
                                val findVars = regex.findAll(state.assignmentArithmExpr).map {
                                    if (it.value.contains("[")) it.value.substring(0, it.value.indexOf("[")).trim()
                                    else it.value
                                }.toSet()
                                var notDeclared = findVars - declaredVarsName

                                if (notDeclared.isNotEmpty()) {
                                    state.assignmentError = "Undeclared variable|variables: ${notDeclared.joinToString(", ")}"
                                    return@Button
                                }
                                else {
                                    // здесь происходит проверка валидности арифметического выражения
                                    if (!isValidArithmExpression(state)){
                                        state.assignmentError = "Invalid expression"
                                        return@Button
                                    }
                                    else {
                                        val targetVar = state.vars.find { it.name == state.selectedTargetVar }
                                        if (targetVar != null) {
                                            try {
                                                val rpn = convertToReversePolishNotation(state.assignmentArithmExpr, ctx)
                                                val calculatedValue = calculateArithmeticExpression(
                                                    rpn,
                                                    state,
                                                    context = ctx,
                                                    arrays = state.arrays
                                                )
                                                if (targetVar.type == VariableType.INT &&
                                                    calculatedValue != calculatedValue.toInt().toDouble()) {
                                                    state.assignmentError = "Cannot convert float to int"
                                                    return@Button
                                                }

                                                // здесь чекаем для изменения значения выбранной из менюшки переменной
                                                val index =
                                                    state.vars.indexOfFirst { it.name == state.selectedTargetVar }
                                                if (index >= 0) {
                                                    state.vars[index] =
                                                        state.vars[index].copy(expression = state.assignmentArithmExpr)
                                                }
                                                state.showNewAssignmentDialog = false
                                                state.selectedTargetVar = ""
                                                state.assignmentError = ""
                                                state.assignmentArithmExpr = ""
                                            }
                                            catch (e: Exception) {
                                                state.assignmentError = "Error evaluating expression: ${e.message}"
                                                return@Button
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text)),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.header))
                        )
                    ) {
                        Text(stringResource(R.string.create),
                            color = textColor,)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            state.showNewAssignmentDialog = false
                            state.selectedTargetVar = ""
                            state.assignmentError= ""
                            state.assignmentArithmExpr = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text)),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.dark_header))
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