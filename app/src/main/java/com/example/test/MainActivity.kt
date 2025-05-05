package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

data class Variable(val name: String, val value: Int = 0, val pos: IntOffset = IntOffset(0, 0))
data class VarError(val msg: String, val blockId: String = "")

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlock() {
    val vars = remember { mutableStateListOf<Variable>() }
    val errors = remember { mutableStateListOf<VarError>() }

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
                            label = { Text("Variable name") },
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
                                    when {
                                        newVarName.isBlank() -> {
                                            newVarError = "Variable name must not be empty"
                                        }
                                        vars.any { it.name == newVarName } -> {
                                            newVarError = "Variable \"${newVarName}\" already exists"
                                        }
                                        !newVarName[0].isLetter() && newVarName[0] != '_' -> {
                                            newVarError = "Variable name must start with a letter or an underscore"
                                        }
                                        newVarName.any { !it.isLetterOrDigit() && it != '_' } -> {
                                            newVarError = "Variable name must contain only digits, letters or underscores"
                                        }
                                        else -> {
                                            vars.add(
                                                Variable(
                                                    name = newVarName,
                                                    pos = if (vars.isEmpty()) IntOffset(10, 50) else IntOffset(vars.last().pos.x + 50, vars.last().pos.y + 50)
                                                )
                                            )
                                            showNewVarDialog = false
                                            newVarName = ""
                                            newVarError = ""
                                        }
                                    }
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
            Text(
                text = "Declare ${variable.name} = 0",
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestTheme {
        CodeBlock()
    }
}