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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.test.CodeBlockState
import com.example.test.CommandBlock
import com.example.test.IfBlock
import com.example.test.IfBlockCommand
import com.example.test.VarBlockCommand
import com.example.test.Variable
import com.example.test.WhileBlock
import com.example.test.WhileBlockCommand
import com.example.test.ui.theme.menu.MenuBoxForAssignments
import com.example.test.utils.isValidArithmExpression

@SuppressLint("StringFormatInvalid")
@Composable
fun IfDialog(state: CodeBlockState, ctx: Context) {
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
                    text = stringResource(R.string.commands_if_true),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.curBlockCommands.forEachIndexed { i, com ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (com is VarBlockCommand) {
                            Text(
                                text = "${i + 1}. ${com.variable.name} = ${com.variable.expression}",
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                text = "${i + 1}. Command block",
                                modifier = Modifier.weight(1f)
                            )
                        }
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
                            val newCommand = state.newWhileCommand.trim()
                            if (newCommand.isNotBlank()) {
                                val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                val usedVars = regex.findAll(newCommand).map {it.value }.toSet()

                                val declaredVars = state.vars.map{it.name}.toSet()
                                val notDeclared = usedVars-declaredVars

                                if (notDeclared.isNotEmpty()) {
                                    state.whileBlockError = ctx.getString(R.string.err_undeclared_var, notDeclared.joinToString(", "))
                                    return@IconButton
                                }
                            }

                            if (state.newIfCommand.isNotBlank()) {
                                val parts = state.newIfCommand.split("=")
                                val name = parts.getOrNull(0)?.trim() ?: "var"

                                val expr = parts.getOrNull(1)?.trim() ?: "0"
                                state.curBlockCommands.add(
                                    VarBlockCommand(
                                        Variable(
                                            name = name,
                                            expression = expr,
                                            pos = IntOffset(0, state.curBlockCommands.size * 220)
                                        )
                                    )
                                )
                                state.newIfCommand = ""
                            }
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
                                        commands = state.curBlockCommands,
                                        pos = IntOffset(
                                            state.ifBlock[i].pos.x,
                                            state.ifBlock[i].pos.y
                                        )
                                    )
                                }
                                if (state.newIfCommand.isNotBlank()) {
                                    val parts = state.newIfCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0"
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
                                if (state.newIfCommand.isNotBlank()) {
                                    val parts = state.newIfCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0"
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
                                    addAll(state.curWhileCommands)
                                }

                                // тут уже создаю саму карту с командой для иф-блока
                                val newIf = IfBlock(
                                    leftExpression = state.leftIfExpression,
                                    rightExpression = state.rightIfExpression,
                                    comparisonOperator = state.selectedComparisonOperator,
                                    commands = commandsCopy,
                                    pos = IntOffset(10, 10 + state.ifBlock.size * 220)
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
                                    state.ifBlock[i] = state.ifBlock[i].copy(
                                        leftExpression = state.leftIfExpression,
                                        rightExpression = state.rightIfExpression,
                                        comparisonOperator = state.selectedComparisonOperator,
                                        commands = newCommands,
                                        pos = state.ifBlock[i].pos
                                    )
                                }
                            }
                            state.showNewIfDialog = false
                            state.leftIfExpression = ""
                            state.rightIfExpression = ""
                            state.selectedComparisonOperator = "=="
                            state.curBlockCommands.clear()
                            state.newIfCommand = ""
                            state.selectedIfBlock = ""
                            state.targetCommandsList = null
                        }
                    ) {
                        Text(stringResource(R.string.create))
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
                                    state.ifBlock[i] = state.ifBlock[i].copy(
                                        leftExpression = state.leftIfExpression,
                                        rightExpression = state.rightIfExpression,
                                        comparisonOperator = state.selectedComparisonOperator,
                                        commands = newCommands,
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
                            state.newIfCommand = ""
                            state.selectedIfBlock = ""
                            state.targetCommandsList = null
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@SuppressLint("StringFormatInvalid")
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
                    text = stringResource(R.string.create_assign),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.select_var)
                )

                MenuBoxForAssignments(
                    state.vars.map { it.name },
                    state.selectedTargetVar
                ) { selected ->
                    state.selectedTargetVar = selected
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.assignmentArithmExpr,
                    onValueChange = { state.assignmentArithmExpr = it },
                    label = { Text("Expression 7(2x + 5)") },
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
                                state.assignmentError = R.string.please_select_var.toString()
                                return@Button
                            }
                            else if (state.assignmentArithmExpr.isBlank()) {
                                state.assignmentError = R.string.exp_cannot_be_empty.toString()
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
fun DeleteAllDialog(state: CodeBlockState) {
    AlertDialog(
        onDismissRequest = { state.showDeleteAllDialog = false },
        title = { Text(stringResource(R.string.delete_all)) },
        text = { Text(stringResource(R.string.are_you_sure)) },
        confirmButton = {
            Button(
                onClick = {
                    state.showDeleteAllDialog = false
                    state.whileBlocks.clear()
                    state.vars.clear()
                    state.ifBlock.clear()
                    state.targetCommandsList = null
                    state.selectedIfBlock = ""
                    state.errors.removeAll { true }

                }
            ) {
                Text(stringResource(R.string.delete_all))
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

@SuppressLint("StringFormatInvalid")
@Composable
fun WhileDialog(state: CodeBlockState, ctx: Context) {
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
        Surface (
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.create_while_block),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.leftWhileExpression,
                    onValueChange = { state.leftWhileExpression = it },
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
                        value = state.selectedWhileOperator,
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
                                    state.selectedWhileOperator = oper
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
                    value = state.rightWhileExpression,
                    onValueChange = { state.rightWhileExpression = it },
                    label = { Text("Right Expression") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(text = stringResource(R.string.commands_while),
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                state.curWhileCommands.forEachIndexed { i, com ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (com is VarBlockCommand) {
                            var istr = i + 1
                            var varName = com.variable.name
                            var varExpr = com.variable.expression
                            Text(
                                text = ctx.getString(R.string.expression, istr.toString(), varName, varExpr),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            var istr = i + 1
                            Text(
                                text = ctx.getString(R.string.remove_command, istr.toString()),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        IconButton(
                            onClick = { state.curWhileCommands.removeAt(i) }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Command")
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
                        onValueChange = { state.newWhileCommand = it
                            state.whileBlockError = "" },
                        label = { Text("New Var Expression") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val newCommand = state.newWhileCommand.trim()
                            if (newCommand.isNotBlank()) {
                                val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                val usedVars = regex.findAll(newCommand).map {it.value }.toSet()

                                val declaredVars = state.vars.map{it.name}.toSet()
                                val notDeclared = usedVars-declaredVars

                                if (notDeclared.isNotEmpty()) {
                                    state.whileBlockError = ctx.getString(R.string.err_undeclared_var, notDeclared.joinToString(", "))
                                    return@IconButton
                                }
                            }

                            if (state.newWhileCommand.isNotBlank()) {
                                val parts = state.newWhileCommand.split("=")
                                val name = parts.getOrNull(0)?.trim() ?: "var"
                                val expr = parts.getOrNull(1)?.trim() ?: "0"
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
                        Icon(Icons.Default.Add, contentDescription = "Add Command")
                    }
                }
                if (state.whileBlockError.isNotBlank()) {
                    Text (
                        text = state.whileBlockError,
                        color = MaterialTheme.colorScheme.error,
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

                            //=====================
                            if (state.leftWhileExpression.isBlank()) {
                                state.whileBlockError = "Left part must not be empty"
                                return@Button
                            }
                            if (state.rightWhileExpression.isBlank()) {
                                state.whileBlockError = "Right part must not be empty"
                                return@Button
                            }

                            val declaredVarsNames = state.vars.map { it.name }.toSet()
                            val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                            val leftPartVars = regex.findAll(state.leftWhileExpression).map { it.value }.toSet()
                            val rightPartVars = regex.findAll(state.rightWhileExpression).map { it.value }.toSet()
                            val notDeclared = (leftPartVars + rightPartVars) - declaredVarsNames
                            if (notDeclared.isNotEmpty()) {
                                state.whileBlockError = "Undeclared variable(-s): ${notDeclared.joinToString(", ")}"
                                return@Button
                            }
                            if (state.selectedWhileTargetId.isNotEmpty()) {
                                val i = state.whileBlocks.indexOfFirst { it.id == state.selectedWhileTargetId }
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

                                    val expr = parts.getOrNull(1)?.trim() ?: "0"
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
                            }
                            else {
                                if (state.newWhileCommand.isNotBlank()) {
                                    val parts = state.newWhileCommand.split("=")
                                    val name = parts.getOrNull(0)?.trim() ?: "var"

                                    val expr = parts.getOrNull(1)?.trim() ?: "0"
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
                                val commandsCopy: SnapshotStateList<CommandBlock> = mutableStateListOf<CommandBlock>().apply {
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
                                }
                                else {
                                    state.whileBlocks.add(newIf)
                                }
                            }
                            if (state.selectedWhileTargetId.isNotEmpty()) {
                                val i = state.whileBlocks.indexOfFirst { it.id == state.selectedWhileTargetId }
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
                            if (state.selectedWhileTargetId.isNotEmpty()) {
                                val i = state.whileBlocks.indexOfFirst { it.id == state.selectedWhileTargetId }
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
                        }

                    ) {
                        Text(stringResource(R.string.create))
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            if (state.selectedWhileTargetId.isNotEmpty()) {
                                val i = state.whileBlocks.indexOfFirst { it.id == state.selectedWhileTargetId }
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
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}