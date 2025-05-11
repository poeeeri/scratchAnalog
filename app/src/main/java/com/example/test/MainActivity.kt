package com.example.test

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import com.example.test.ui.theme.TestTheme
import kotlin.math.roundToInt
import java.util.Stack
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.util.UUID

data class Variable(
    val name: String,
    val expression: String,
    val pos: IntOffset = IntOffset(0, 0)
)

data class VarError(
    val msg: String,
    val blockId: String = ""
)

data class ContextMenuState(
    val shown: Boolean = false,
    val position: Offset = Offset.Zero,
    val variableName: String? = null,
    val ifBlockId: String? = null
)

data class IfBlock(
    val id: String = UUID.randomUUID().toString(),
    val condition: String = "",
    val leftExpression: String = "",
    val rightExpression: String = "",
    val comparisonOperator: String = "",
    val commands: MutableList<String> = mutableStateListOf(),
    val pos: IntOffset = IntOffset(0, 0)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                CodeBlock()
            }
        }
    }
}


// ребята как же андроид студио тормозит епрст
// (」＞＜)」
// как же андроид студио жрёт заряд батареи...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlock() {
    val vars = remember { mutableStateListOf<Variable>() }
    val ifBlocks = remember { mutableStateListOf<IfBlock>() }
    val errors = remember { mutableStateListOf<VarError>() }

    var showNewAssignmentDialog by remember { mutableStateOf(false) }
    var selectedTargetVar by remember { mutableStateOf("") }
    var assignmentArithmExpr by remember { mutableStateOf("") }
    var assignmentError by remember { mutableStateOf("") }

    var showNewIfDialog by remember { mutableStateOf(false) }
    var selectedIfBlock by remember { mutableStateOf("") }
    var leftIfExpression by remember { mutableStateOf("") }
    var rightIfExpression by remember { mutableStateOf("") }
    var selectedComparisonOperator by remember { mutableStateOf("==") }
    var ifBlockError by remember { mutableStateOf("") }
    var curBlockCommands = remember { mutableStateListOf<String>() }
    var newIfCommand by remember { mutableStateOf("") }

    var showNewVarDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var newVarName by remember { mutableStateOf("") }
    var newVarError by remember { mutableStateOf("") }

    var contextMenuState by remember { mutableStateOf(ContextMenuState()) }

    val context = LocalContext.current

    //обработчик для изменения переменной
    fun handleEdit() {
        contextMenuState.variableName?.let { varName ->
            val variable = vars.firstOrNull { it.name == varName }
            variable?.let {
                selectedTargetVar = varName
                assignmentArithmExpr = it.expression
                showNewAssignmentDialog = true
            }
        }
        contextMenuState = ContextMenuState()
    }
    //обработчик для удаления переменной
    fun handleDelete() {
        contextMenuState.variableName?.let { varName ->
            vars.removeAll { it.name == varName }
        }
        contextMenuState = ContextMenuState()
    }

    // Обработка изменения условия
    fun handleEditIfBlock() {
        contextMenuState.ifBlockId?.let { blockId ->
            val block = ifBlocks.firstOrNull { it.id == blockId }
            block?.let {
                selectedIfBlock = blockId
                leftIfExpression = it.leftExpression
                rightIfExpression = it.rightExpression
                selectedComparisonOperator = it.comparisonOperator
                curBlockCommands.clear()
                curBlockCommands.addAll(it.commands)
                showNewIfDialog = true
            }
        }
    }

    // Обработка удаления условия
    fun handleDeleteIfBlock() {
        contextMenuState.ifBlockId?.let { blockId ->
            ifBlocks.removeAll { it.id == blockId }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scratch Analog") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = { showNewVarDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Это для добавления условия
                FloatingActionButton(
                    onClick = { showNewIfDialog = true }
                ) {
                    Icon(Icons.Default.Code, contentDescription = "Add If Block")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // кнопка присваивания
                FloatingActionButton(
                    onClick = { showNewAssignmentDialog = true }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Assign")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // кнопка для пересчета значений переменных
                FloatingActionButton(
                    onClick = {
                        val result = recalculateAllVariables(vars, context)
                        result.onSuccess { updated ->
                            vars.clear()
                            vars.addAll(updated)
                            // Также с условиями!
                            ifBlocks.forEach { block ->
                                val conditionRes = evaluateIfCondition(
                                    block.leftExpression,
                                    block.rightExpression,
                                    block.comparisonOperator,
                                    vars,
                                    context
                                )
                                if (conditionRes)
                                    executeIfCommands(block.commands, vars, context)
                            }
                        }.onFailure { e ->
                            Toast.makeText(context, e.message ?: "Error", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Recalculate All")
                }
                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = { showDeleteAllDialog = true }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete All")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(16.dp)
            ) {
                vars.forEach { x ->
                    key(x.name) {
                        VarCard(
                            variable = x,
                            vars = vars,
                            hasError = errors.any { it.blockId == x.name },

                            onInteraction = { position, varName ->
                                contextMenuState = ContextMenuState(
                                    shown = true,
                                    position = position,
                                    variableName = varName
                                )
                            }
                        )
                    }
                }
                //контекстное меню для изменения или удаления переменной
                if (contextMenuState.shown){
                    val density = LocalDensity.current
                    val menuOffset = with(density){
                        DpOffset(
                            x = (contextMenuState.position.x + 100).toDp(),
                            y = (contextMenuState.position.y + 100).toDp()
                        )
                    }
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { contextMenuState = ContextMenuState()},
                        offset = menuOffset
                    ) {
                        if (contextMenuState.variableName != null) {
                            DropdownMenuItem(
                                text = { Text("Change") },
                                onClick = {
                                    handleEdit()
                                    contextMenuState = ContextMenuState()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    handleDelete()
                                    contextMenuState = ContextMenuState()
                                }
                            )
                        }
                        else if (contextMenuState.ifBlockId != null) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    handleEditIfBlock()
                                    contextMenuState = ContextMenuState()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    handleDeleteIfBlock()
                                    contextMenuState = ContextMenuState()
                                }
                            )
                        }
                    }
                }
                if (vars.isEmpty()) {
                    Text(
                        text = "Tap + to add a variable",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                ifBlocks.forEach { block ->
                    key(block.hashCode()) {
                        IfBlockCard(
                            ifBlock = block,
                            vars = vars,
                            onInteraction = { position, blockId ->
                                contextMenuState = ContextMenuState(
                                    shown = true,
                                    position = position,
                                    ifBlockId = blockId
                                )
                            }
                        )
                    }
                }
            }
        }
        if (showNewVarDialog) {
            Dialog(onDismissRequest = {
                showNewVarDialog = false
                newVarName = ""
                newVarError = ""
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
                            text = "Create New Variable",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newVarName,
                            onValueChange = { newVarName = it },
                            label = { Text("Variable name (or several, with \",\")") },
                            isError = newVarError != "",
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (newVarError != "") {
                            Text(
                                text = newVarError,
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
                                    if (newVarName.isNotBlank()) {
                                        val varsArray = newVarName.split(',').map { it.trim() }
                                        var containsError = false
                                        for (v in varsArray) {
                                            when {
                                                v.isBlank() -> {
                                                    newVarError = "Variable name must not be empty"
                                                    containsError = true
                                                    break
                                                }

                                                vars.any { it.name == v } -> {
                                                    newVarError =
                                                        "Variable \"${v}\" already exists"
                                                    containsError = true
                                                    break
                                                }

                                                !v[0].isLetter() && v[0] != '_' -> {
                                                    newVarError =
                                                        "Variable name must start with a letter or an underscore"
                                                    containsError = true
                                                    break
                                                }

                                                v.any { !it.isLetterOrDigit() && it != '_' } -> {
                                                    newVarError =
                                                        "Variable name must contain only digits, letters or underscores"
                                                    containsError = true
                                                    break
                                                }
                                            }
                                        }

                                        if (!containsError) {
                                            varsArray.forEach { v ->
                                                vars.add(
                                                    Variable(
                                                        name = v,
                                                        expression = "",
                                                        pos = IntOffset(10 + vars.size, vars.size*220)
                                                    )
                                                )
                                            }
                                            
                                            showNewVarDialog = false
                                            newVarName = ""
                                            newVarError = ""
                                        }
                                    }
                                    else newVarError = "Variable name must not be empty"
                                }
                            ) {
                                Text("Create")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    showNewVarDialog = false
                                    newVarName = ""
                                    newVarError = ""
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
        if (showNewIfDialog) {
            Dialog(onDismissRequest = {
                showNewIfDialog = false
                leftIfExpression = ""
                rightIfExpression = ""
                selectedComparisonOperator = "=="
                ifBlockError = ""
                curBlockCommands.clear()
                newIfCommand = ""
                selectedIfBlock = ""
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
                            value = leftIfExpression,
                            onValueChange = { leftIfExpression = it },
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
                                value = selectedComparisonOperator,
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
                                            selectedComparisonOperator = oper
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Right part
                        OutlinedTextField(
                            value = rightIfExpression,
                            onValueChange = { rightIfExpression = it },
                            label = { Text("Right Expression") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Commands (if condition is true):",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        curBlockCommands.forEachIndexed { i, com ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${i + 1}. ${com}",
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { curBlockCommands.removeAt(i) }
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
                                value = newIfCommand,
                                onValueChange = { newIfCommand = it },
                                label = { Text("New Command") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (newIfCommand.isNotBlank())
                                        curBlockCommands.add(newIfCommand)
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Command")
                            }
                        }
                        if (ifBlockError.isNotBlank()) {
                            Text(
                                text = ifBlockError,
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
                                    if (leftIfExpression.isBlank()) {
                                        ifBlockError = "Left part must not be empty"
                                        return@Button
                                    }
                                    if (rightIfExpression.isBlank()) {
                                        ifBlockError = "Right part must not be empty"
                                        return@Button
                                    }

                                    val declaredVarsNames = vars.map { it.name }.toSet()
                                    val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                    val leftPartVars = regex.findAll(leftIfExpression).map { it.value }.toSet()
                                    val rightPartVars = regex.findAll(rightIfExpression).map { it.value }.toSet()
                                    val notDeclared = (leftPartVars + rightPartVars) - declaredVarsNames
                                    if (notDeclared.isNotEmpty()) {
                                        ifBlockError = "Undeclared variable(-s): ${notDeclared.joinToString(", ")}"
                                        return@Button
                                    }
                                    if (selectedIfBlock.isNotEmpty()) {
                                        val i = ifBlocks.indexOfFirst { it.id == selectedIfBlock }
                                        if (i >= 0) {
                                            ifBlocks[i] = ifBlocks[i].copy(
                                                leftExpression = leftIfExpression,
                                                rightExpression = rightIfExpression,
                                                comparisonOperator = selectedComparisonOperator,
                                                commands = curBlockCommands.toMutableStateList(),
                                                pos = IntOffset(
                                                    ifBlocks[i].pos.x,
                                                    ifBlocks[i].pos.y
                                                )
                                            )
                                        }
                                    }
                                    else {
                                        ifBlocks.add(
                                            IfBlock(
                                                leftExpression = leftIfExpression,
                                                rightExpression = rightIfExpression,
                                                comparisonOperator = selectedComparisonOperator,
                                                commands = curBlockCommands.toMutableStateList(),
                                                pos = IntOffset(10, 10 + ifBlocks.size * 220)
                                            )
                                        )
                                    }
                                    showNewIfDialog = false
                                    leftIfExpression = ""
                                    rightIfExpression = ""
                                    selectedComparisonOperator = "=="
                                    ifBlockError = ""
                                    newIfCommand = ""
                                }
                            ) {
                                Text("Create")
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            TextButton(
                                onClick = {
                                    showNewIfDialog = false
                                    leftIfExpression = ""
                                    rightIfExpression = ""
                                    selectedComparisonOperator = "=="
                                    ifBlockError = ""
                                    curBlockCommands.clear()
                                    newIfCommand = ""
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Delete All") },
                text = { Text("Are you sure you want to remove all elements? This cannot be undone!") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAllDialog = false
                            vars.clear()
                            ifBlocks.clear()
                            errors.removeAll { true }
                        }
                    ) {
                        Text("Delete All")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteAllDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // диалог присваивания
        if (showNewAssignmentDialog) {
            Dialog(onDismissRequest = {
                showNewAssignmentDialog = false
                selectedTargetVar = ""
                assignmentArithmExpr = ""
                assignmentError = ""
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

                        MenuBoxForAssignmentsBlock(vars.map { it.name }, selectedTargetVar) { selected ->
                            selectedTargetVar = selected
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = assignmentArithmExpr,
                            onValueChange = { assignmentArithmExpr = it },
                            label = { Text("Expression (x + 5)") },
                            isError = assignmentError != "",
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (assignmentError.isNotBlank()) {
                            Text(
                                text = assignmentError,
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
                                    if (selectedTargetVar.isBlank()) {
                                        assignmentError = "Please select a variable"
                                        return@Button
                                    }
                                    else if (assignmentArithmExpr.isBlank()) {
                                        assignmentError = "Expression cannot be empty"
                                        return@Button
                                    }
                                    else {
                                        // здесь чекаем есть ли переменная вообще такая
                                        val declaredVarsName = vars.map {it.name}.toSet()
                                        val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                                        val findVars = regex.findAll(assignmentArithmExpr).map {it.value}.toSet()
                                        var notDeclared = findVars - declaredVarsName

                                        if (notDeclared.isNotEmpty()) {
                                            assignmentError = "Undeclared variable|variables: ${notDeclared.joinToString(", ")}"
                                            return@Button
                                        }
                                        else {
                                            // здесь происходит проверка валидности арифметического выражения
                                            if (!Regex("\\s*([a-zA-Z_]\\w*|\\d+)(\\s*[+\\-*\\/\\%]" +
                                                        "\\s*([a-zA-Z_]\\w*|\\d+))*\\s*\$").matches(assignmentArithmExpr)){
                                                assignmentError = "Invalid expression"
                                                return@Button
                                            }
                                            else {
                                                // здесь чекаем для изменения значения выбранной из менюшки переменной
                                                val index = vars.indexOfFirst { it.name == selectedTargetVar }
                                                if (index >= 0) {
                                                    vars[index] = vars[index].copy(expression = assignmentArithmExpr)
                                                }
                                                showNewAssignmentDialog= false
                                                selectedTargetVar = ""
                                                assignmentError= ""
                                                assignmentArithmExpr = ""
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
                                    showNewAssignmentDialog = false
                                    selectedTargetVar = ""
                                    assignmentError= ""
                                    assignmentArithmExpr = ""
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VarCard(variable: Variable, vars: List<Variable>, hasError: Boolean,  onInteraction: (Offset, String) -> Unit) {
    var x by remember { mutableFloatStateOf(variable.pos.x.toFloat()) }
    var y by remember { mutableFloatStateOf(variable.pos.y.toFloat()) }
    var blockPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x.roundToInt(),
                    y.roundToInt()
                )
            }
            .background(
                color = if (hasError) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (hasError) 2.dp else 0.dp,
                color = if (hasError) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)

            .onGloballyPositioned { coords ->
                blockPosition = coords.localToWindow(Offset.Zero)
            }

            .pointerInput(Unit){
                detectTapGestures (
                    onDoubleTap = {
                        onInteraction(blockPosition, variable.name)
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
                }
            }
    ) {
        Column {
            val value = variable.expression.ifBlank { "0" }
            Text(
                text = "Declare ${variable.name} = $value",
                fontWeight = FontWeight.Bold,
                color = if (hasError) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Integer",
                fontSize = 12.sp,
                color = if (hasError) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun IfBlockCard(ifBlock: IfBlock, vars: List<Variable>, onInteraction: (Offset, String) -> Unit) {
    var x by remember { mutableFloatStateOf(ifBlock.pos.x.toFloat()) }
    var y by remember { mutableFloatStateOf(ifBlock.pos.y.toFloat()) }
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
                        onInteraction(blockPosition, ifBlock.id)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "If ${ifBlock.leftExpression} " +
                            "${ifBlock.comparisonOperator} " +
                            "${ifBlock.rightExpression}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(12.dp)
            ) {
                Column (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Then:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (ifBlock.commands.isEmpty()) {
                        Text(
                            text = "No commands",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    else {
                        ifBlock.commands.forEachIndexed { i, com ->
                            Text(
                                text = "${i + 1}. ${com}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (i < ifBlock.commands.size - 1)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

fun evaluateIfCondition(
    leftExpression: String,
    rightExpression: String,
    comparisonOperator: String,
    vars: List<Variable>,
    context: Context
): Boolean {
    try {
        val leftRpn = convertToReversePolishNotation(leftExpression, context)
        val rightRpn = convertToReversePolishNotation(rightExpression, context)

        val leftValue = calculateArithmeticExpression(leftRpn, vars, context = context)
        val rightValue = calculateArithmeticExpression(rightRpn, vars, context = context)
        return when (comparisonOperator) {
            "==" -> leftValue == rightValue
            "!=" -> leftValue != rightValue
            ">" -> leftValue > rightValue
            "<" -> leftValue < rightValue
            ">=" -> leftValue >= rightValue
            "<=" -> leftValue <= rightValue
            else -> {
                Toast.makeText(
                    context,
                    "Invalid comparison operator: ${comparisonOperator}",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        }
    }
    catch (e: Exception) {
        Toast.makeText(
            context,
            "Error evaluating condition: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
        return false
    }
}

fun executeIfCommands(commands: List<String>, vars: MutableList<Variable>, context: Context) {
    commands.forEach { com ->
        try {
            val assignmentRegex = Regex("\\s*([a-zA-Z_]\\w*)\\s*=\\s*(.+)\\s*")
            val matchRes = assignmentRegex.matchEntire(com)

            if (matchRes != null) {
                val (varName, expr) = matchRes.destructured
                val i = vars.indexOfFirst { it.name == varName }
                if (i == -1) {
                    Toast.makeText(
                        context,
                        "Variable \"${varName}\" not found in command: ${com}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@forEach
                }

                val rpn = convertToReversePolishNotation(expr, context)
                if (rpn.isBlank()) {
                    Toast.makeText(
                        context,
                        "Invalid expression in command: ${com}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@forEach
                }

                val declaredVars = vars.map { it.name }.toSet()
                val regex = Regex("(?!_|\\d+)([a-zA-Z_]\\w*)")
                val exprVars = regex.findAll(expr).map { it.value }.toSet()
                val notDeclared = exprVars - declaredVars
                if (notDeclared.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        "Undeclared variable(-s) in command: ${notDeclared.joinToString(", ")}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@forEach
                }

                val newValue = calculateArithmeticExpression(rpn, vars, context = context)
                vars[i] = vars[i].copy(expression = newValue.toString())
            } else {
                Toast.makeText(
                    context,
                    "Unsupported command format: ${com}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        catch (e: Exception) {
            Toast.makeText(
                context,
                "Error executing command \"${com}\": ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

// меню выбора переменной для присвоения
@Composable
fun MenuBoxForAssignmentsBlock(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember {mutableStateOf(false)}

    Box (
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Var") },
            // создаю типа кликабельную иконку чтобы сворачивать менюшку
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded=false}) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {Text(option)},
                    leadingIcon = {Icon(Icons.Outlined.Star, contentDescription = null)},
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestTheme {
        CodeBlock()
    }
}

//приоритеты операций
fun getPriority(operator: Char): Int = when(operator){
    '+','-' -> 1
    '*','/', '%' -> 2
    else -> 0
}

//преобразуем выражение в обратную польскую запись
fun convertToReversePolishNotation(expression: String, context: Context) : String{
    val output = StringBuilder()
    val stack = Stack<Char>()
    var i = 0

    if (expression.isBlank()){
        Toast.makeText(context, "Expression cannot be blank", Toast.LENGTH_LONG).show()
    }

    while (i < expression.length) {
        val c = expression[i]

        when {
            c.isDigit()-> {
                while (i < expression.length && expression[i].isDigit()){
                    output.append(expression[i++])
                }
                output.append(' ')
                continue
            }

            c.isLetter()->{
                while (i <expression.length && (expression[i].isLetterOrDigit() || expression[i] == '_')){
                    output.append(expression[i++])
                }
                output.append(' ')
                continue
            }

            c == '(' -> {
                stack.push(c)
                i++
            }

            c == ')' ->{
                while (stack.isNotEmpty() && stack.peek() != '('){
                    output.append(stack.pop()).append(' ')
                }
                if (stack.isEmpty() || stack.peek() != '('){
                    Toast.makeText(context, "Extra closing parenthesis", Toast.LENGTH_LONG).show()
                }
                stack.pop()
                i++
            }

            c in "+-*/%" ->{
                while (stack.isNotEmpty() && stack.peek() != '(' && getPriority(stack.peek()) >= getPriority(c)){
                    output.append(stack.pop()).append(' ')
                }
                stack.push(c)
                i++
            }

            c.isWhitespace()-> {
                i++
            }

            else-> {
                Toast.makeText(context, "Invalid character '$c' in expression", Toast.LENGTH_LONG).show()
            }
        }
    }

    while(stack.isNotEmpty()){
        val operator = stack.pop()
        when (operator){
            '(' -> {
                Toast.makeText(context, "Extra opening parenthesis", Toast.LENGTH_LONG).show()
            }
            ')' -> {
                Toast.makeText(context, "Extra closing parenthesis", Toast.LENGTH_LONG).show()
            }
        }
        output.append(operator).append(' ')
    }
    return output.toString().trim().also{
        if (it.isEmpty()) {
            Toast.makeText(context, "Empty RPN expression", Toast.LENGTH_LONG).show()
        }
    }
}

// вычисляем значение арифметического выражения
fun calculateArithmeticExpression(expression: String, vars: List<Variable>,  callStack: Set<String> = emptySet(), computedCache: MutableMap<String, Int> = mutableMapOf(), context: Context):Int{
    if (vars.any {it.name.isEmpty()}){
        Toast.makeText(context, "Variable with empty name found", Toast.LENGTH_LONG).show()
    }

    val stack = mutableListOf<Int>()
    val tokens = expression.split(" ").filter { it.isNotBlank() }

    if (tokens.isEmpty()){
        Toast.makeText(context, "Empty expression", Toast.LENGTH_LONG).show()
    }

    for (token in tokens) {
        try {
            when {
                token.toIntOrNull() != null -> {
                    stack.add(token.toInt())
                }

                vars.any { it.name == token } -> {
                    val variable = vars.first {it.name == token}
                    val value = if (variable.expression.isBlank()){
                        0
                    } else{
                        calculateArithmeticExpression(variable.expression, vars, context=context)
                    }
                    stack.add(value)
                }

                token == "+" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a + b)
                }

                token == "-" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a - b)
                }

                token == "*" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a * b)
                }

                token == "/" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    if (b == 0) {
                        Toast.makeText(context, "Division by zero", Toast.LENGTH_LONG).show()
                    }
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a / b)
                }

                token == "%" -> {
                    val b = stack.removeAt(stack.lastIndex)
                    if (b == 0) {
                        Toast.makeText(context, "Division by zero", Toast.LENGTH_LONG).show()
                    }
                    val a = stack.removeAt(stack.lastIndex)
                    stack.add(a % b)
                }
            }
        } catch (e: NoSuchElementException){
            Toast.makeText(context, "Variable '$token' not found", Toast.LENGTH_LONG).show()
        }
    }

    val result = stack.singleOrNull()
    if (result == null){
        Toast.makeText(context, "Invalid expression format", Toast.LENGTH_LONG).show()
        return 0
    }
    return result
}

//находим зависимость переменной от других переменных
fun extractDependencies(expression: String): Set<String>{
    return Regex("[a-zA-Z_]\\w*")
        .findAll(expression)
        .map { it.value }
        .toSet()
}
// здесь мы пересчитываем все переменные
fun recalculateAllVariables(vars: List<Variable>, context: Context) : Result<List<Variable>> {
    if (vars.isEmpty()) return Result.success(emptyList())

    val graph = mutableMapOf<String, Set<String>>()
    for (variable in vars) {
        graph[variable.name] = extractDependencies(variable.expression)
    }

    return runCatching {
        val updatedVars = vars.map { it.copy() }.toMutableList()
        val sortedOrder = topologicalSort(graph)
        val computedValues = mutableMapOf<String, Int>()
        for (varName in sortedOrder) {
            val variable = updatedVars.first { it.name == varName }
            try {
                var processed = variable.expression
                computedValues.forEach { (name, value) ->
                    processed = processed.replace(name, value.toString())
                }
                val rpn = convertToReversePolishNotation(processed, context)
                val value = calculateArithmeticExpression(
                    rpn,
                    vars.filter { computedValues.containsKey(it.name) },
                    context = context
                )
                computedValues[varName] = value
                updatedVars.replaceAll {
                    if (it.name == varName)
                        it.copy(expression = value.toString())
                    else
                        it
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        updatedVars
    }
}

//используем сортировку переменных, чтобы считать их в правильном порядке, если они связаны между собой
fun dfs(graph: Map<String, Set<String>>, node: String, visited: MutableMap<String, Int>, order: MutableList<String>){
    when (visited[node]){
        1 -> throw IllegalArgumentException("Cyclic dependency")
        2 -> return
    }

    visited[node] = 1
    graph[node]?.forEach{
        neighbor -> dfs(graph, neighbor, visited, order)
    }

    visited[node] = 2
    order.add(node)
}

fun topologicalSort(graph: Map<String, Set<String>>) : List<String> {
    val visited = mutableMapOf<String, Int>()
    val order = mutableListOf<String>()

    for (node in graph.keys){
        if (visited[node] == null){
            dfs(graph, node, visited, order)
        }
    }
    return order
}