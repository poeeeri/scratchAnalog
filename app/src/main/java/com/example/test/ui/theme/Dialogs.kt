package com.example.test.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import com.example.test.R
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.getString
import com.example.test.CodeBlockState
import com.example.test.IfBlock
import com.example.test.Variable
import com.example.test.utils.isValidArithmExpression
import kotlin.math.exp

@Composable
fun NewAssignmentDialog(state: CodeBlockState) {
    Dialog(onDismissRequest = {
        state.showNewAssignmentDialog = false
        state.selectedTargetVar = ""
        state.assignmentArithmExpr = ""
        state.assignmentError = ""
    }) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create assignments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select variable:"
                )

                MenuBoxForAssignments(state.vars.map { it.name }, state.selectedTargetVar) { selected ->
                    state.selectedTargetVar = selected
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.assignmentArithmExpr,
                    onValueChange = { state.assignmentArithmExpr = it },
                    label = { Text("Expression (x + 5)") },
                    isError = state.assignmentError != "",
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.assignmentError.isNotBlank()) {
                    Text(
                        text = state.assignmentError,
                        color = MaterialTheme.colorScheme.error,
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
                                state.assignmentError = "Please select a variable"
                                return@Button
                            }
                            else if (state.assignmentArithmExpr.isBlank()) {
                                state.assignmentError = "Expression cannot be empty"
                                return@Button
                            }
                            else {
                                // здесь чекаем есть ли переменная вообще такая
                                val declaredVarsName = state.vars.map {it.name}.toSet()
                                val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                val findVars = regex.findAll(state.assignmentArithmExpr).map {it.value}.toSet()
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
                                        // здесь чекаем для изменения значения выбранной из менюшки переменной
                                        val index = state.vars.indexOfFirst { it.name == state.selectedTargetVar }
                                        if (index >= 0) {
                                            state.vars[index] = state.vars[index].copy(expression = state.assignmentArithmExpr)
                                        }
                                        state.showNewAssignmentDialog= false
                                        state.selectedTargetVar = ""
                                        state.assignmentError= ""
                                        state.assignmentArithmExpr = ""
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Create")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            state.showNewAssignmentDialog = false
                            state.selectedTargetVar = ""
                            state.assignmentError= ""
                            state.assignmentArithmExpr = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@SuppressLint("StringFormatInvalid")
@Composable
fun VarDialog(state: CodeBlockState, ctx: Context) {
    val create = stringResource(R.string.create_new_var)
    val errvar_not_be_empty = stringResource(R.string.errvar_not_be_empty)
    val var_must_start = stringResource(R.string.var_must_start)
    Dialog(onDismissRequest = {
        state.showNewVarDialog = false
        state.newVarName = ""
        state.newVarError = ""
    }) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = create,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.newVarName,
                    onValueChange = { state.newVarName = it },
                    label = { Text(stringResource(R.string.var_nam_or_several)) },
                    isError = state.newVarError != "",
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.newVarError != "") {
                    Text(
                        text = state.newVarError,
                        color = MaterialTheme.colorScheme.error,
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
                            if (state.newVarName.isNotBlank()) {
                                val varsArray = state.newVarName.split(',').map { it.trim() }
                                var containsError = false
                                for (v in varsArray) {
                                    when {
                                        v.isBlank() -> {
                                            state.newVarError = errvar_not_be_empty
                                            containsError = true
                                            break
                                        }

                                        state.vars.any { it.name == v } -> {
                                            val var_already_exist = ctx.getString(R.string.var_already_exist, v)
                                            state.newVarError =
                                                var_already_exist
                                            containsError = true
                                            break
                                        }

                                        !v[0].isLetter() && v[0] != '_' -> {
                                            state.newVarError =
                                                var_must_start
                                            containsError = true
                                            break
                                        }

                                        v.any { !it.isLetterOrDigit() && it != '_' } -> {
                                            state.newVarError =
                                                "Variable name must contain only digits, letters or underscores"
                                            containsError = true
                                            break
                                        }
                                    }
                                }

                                if (!containsError) {
                                    varsArray.forEach { v ->
                                        state.vars.add(
                                            Variable(
                                                name = v,
                                                expression = "",
                                                pos = IntOffset(10 + state.vars.size, state.vars.size*220)
                                            )
                                        )
                                    }

                                    state.showNewVarDialog = false
                                    state.newVarName = ""
                                    state.newVarError = ""
                                }
                            }
                            else state.newVarError = "Variable name must not be empty"
                        }
                    ) {
                        Text("Create")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            state.showNewVarDialog = false
                            state.newVarName = ""
                            state.newVarError = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun IfDialog(state: CodeBlockState) {
    Dialog(onDismissRequest = {
        state.showNewIfDialog = false
        state.leftIfExpression = ""
        state.rightIfExpression = ""
        state.selectedComparisonOperator = "=="
        state.ifBlockError = ""
        state.curBlockCommands.clear()
        state.newIfCommand = ""
        state.selectedIfBlock = ""
    }) {
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
                    text = "Create If Block",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Left part
                OutlinedTextField(
                    value = state.leftIfExpression,
                    onValueChange = { state.leftIfExpression = it },
                    label = { Text("Left Expression") },
                    modifier = Modifier.fillMaxWidth()
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
                        label = { Text("Comparison Operator") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (expanded)
                                    Icons.Filled.ArrowDropUp
                                else Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    expanded = !expanded
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        comparisonOpers.forEach { oper ->
                            DropdownMenuItem(
                                text = { Text(oper) },
                                onClick = {
                                    state.selectedComparisonOperator = oper
                                    expanded = false
                                }
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
                    label = { Text("Right Expression") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Commands (if condition is true):",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.curBlockCommands.forEachIndexed { i, com ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${i + 1}. ${com}",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { state.curBlockCommands.removeAt(i) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Command")
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
                        label = { Text("New Command") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (state.newIfCommand.isNotBlank())
                                state.curBlockCommands.add(state.newIfCommand)
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Command")
                    }
                }
                if (state.ifBlockError.isNotBlank()) {
                    Text(
                        text = state.ifBlockError,
                        color = MaterialTheme.colorScheme.error,
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

                            val declaredVarsNames = state.vars.map { it.name }.toSet()
                            val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                            val leftPartVars = regex.findAll(state.leftIfExpression).map { it.value }.toSet()
                            val rightPartVars = regex.findAll(state.rightIfExpression).map { it.value }.toSet()
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
                                        commands = state.curBlockCommands.toMutableStateList(),
                                        pos = IntOffset(
                                            state.ifBlock[i].pos.x,
                                            state.ifBlock[i].pos.y
                                        )
                                    )
                                }
                            }
                            else {
                                state.ifBlock.add(
                                    IfBlock(
                                        leftExpression = state.leftIfExpression,
                                        rightExpression = state.rightIfExpression,
                                        comparisonOperator = state.selectedComparisonOperator,
                                        commands = state.curBlockCommands.toMutableStateList(),
                                        pos = IntOffset(10, 10 + state.ifBlock.size * 220)
                                    )
                                )
                            }
                            state.showNewIfDialog = false
                            state.leftIfExpression = ""
                            state.rightIfExpression = ""
                            state.selectedComparisonOperator = "=="
                            state.ifBlockError = ""
                            state.newIfCommand = ""
                        }
                    ) {
                        Text("Create")
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            state.showNewIfDialog = false
                            state.leftIfExpression = ""
                            state.rightIfExpression = ""
                            state.selectedComparisonOperator = "=="
                            state.ifBlockError = ""
                            state.curBlockCommands.clear()
                            state.newIfCommand = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAllDialog(state: CodeBlockState) {
    AlertDialog(
        onDismissRequest = { state.showDeleteAllDialog = false },
        title = { Text("Delete All") },
        text = { Text("Are you sure you want to remove all elements? This cannot be undone!") },
        confirmButton = {
            Button(
                onClick = {
                    state.showDeleteAllDialog = false
                    state.vars.clear()
                    state.ifBlock.clear()
                    state.errors.removeAll { true }
                }
            ) {
                Text("Delete All")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { state.showDeleteAllDialog = false }
            ) {
                Text("Cancel")
            }
        }
    )
}

