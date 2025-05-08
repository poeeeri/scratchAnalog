package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.test.ui.theme.TestTheme
import kotlin.math.roundToInt

data class Variable(val name: String, val expression: String, val pos: IntOffset = IntOffset(0, 0))
data class VarError(val msg: String, val blockId: String = "")
data class AssignmentParam(val targetVar: String, val arithmeticExpression: String, val position:
IntOffset = IntOffset(0, 0))

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlock() {
    val vars = remember { mutableStateListOf<Variable>() }
    val errors = remember { mutableStateListOf<VarError>() }

    var showNewAssignmentDialog by remember {mutableStateOf(false)}
    var selectedTargetVar by remember {mutableStateOf("")}
    var assignmentArithmExpr by remember {mutableStateOf("")}
    var assignmentError by remember {mutableStateOf("")}

    var showNewVarDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var newVarName by remember { mutableStateOf("") }
    var newVarError by remember { mutableStateOf("") }

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
                // кнопка присваивания
                FloatingActionButton(
                    onClick = { showNewAssignmentDialog = true }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Assign")
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
                    VarCard(
                        variable = x,
                        hasError = errors.any { it.blockId == x.name }
                    )
                }
                if (vars.isEmpty()) {
                    Text(
                        text = "Tap + to add a variable",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                                                        pos = IntOffset(10 + vars.size * 60, 50)
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
                                            // десь происходит проверка валидности арифметического выражения
                                            if (!Regex("\\s*([a-zA-Z_]\\w*|\\d+)(\\s*[+\\-*\\/]" +
                                                        "\\s*([a-zA-Z_]\\w*|\\d+))*\\s*\$").matches(assignmentArithmExpr)){
                                                assignmentError = "Invalid expression"
                                                return@Button
                                            }
                                            else {
                                                val exp = assignmentArithmExpr.ifBlank { "0" }
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
fun VarCard(variable: Variable, hasError: Boolean) {
    var x by remember { mutableFloatStateOf(variable.pos.x.toFloat()) }
    var y by remember { mutableFloatStateOf(variable.pos.y.toFloat()) }
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


// меню выбора переменной для присвоения
@OptIn(ExperimentalMaterial3Api::class)
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