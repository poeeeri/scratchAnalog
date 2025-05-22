package com.example.test.ui.theme

import com.example.test.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.test.ContextMenuState
import com.example.test.CodeBlockState
import com.example.test.ui.theme.blocks.ArrayCard
import com.example.test.ui.theme.blocks.IfBlockCard
import com.example.test.ui.theme.blocks.VarCard
import com.example.test.ui.theme.blocks.WhileBlockCard
import com.example.test.ui.theme.menu.Menu

// запоминает состояния при перерисовке компосэбл
@Composable
fun rememberCodeBlockState(): CodeBlockState = remember {CodeBlockState()}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlock() {
    val states = rememberCodeBlockState()
    val context = LocalContext.current
    var showTopMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showTopMenu = !showTopMenu }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu"
                            )
                        }
                        TopBarDropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false },
                            states = states
                        )
                    }
                }
            )
        },
        floatingActionButton = { FloatingActivationButtons(states, context) }
    ) { innerPadding ->
        Canvas(states, Modifier.padding(innerPadding).fillMaxSize())
        Menu(states)
    }

    if (states.showNewVarDialog) VarDialog(states, context)
    if (states.showNewIfDialog) IfDialog(states, context)
    if (states.showDeleteAllDialog) DeleteAllDialog(states)
    if (states.showNewAssignmentDialog) NewAssignmentDialog(states)
    if (states.showNewWhileDialog) WhileDialog(states, context)
    if (states.showNewArrayDialog) NewArrayDialog(states, context)
    if (states.showEditArrayDialog) EditArrayDialog(states, context)
    if (states.showArrayAccessDialog) ArrayAccessDialog(states, context)
    if (states.showArraySetDialog) ArraySetDialog(states, context)
}

@Composable
fun TopBarDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    states: CodeBlockState
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text("Create Variable") },
            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
            onClick = {
                states.showNewVarDialog = true
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text("Create If Block") },
            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
            onClick = {
                states.showNewIfDialog = true
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text("Create While Block") },
            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            onClick = {
                states.showNewWhileDialog = true
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text("Create Array") },
            leadingIcon = { Icon(Icons.Default.List, contentDescription = null) },
            onClick = {
                states.showNewArrayDialog = true
                onDismissRequest()
            }
        )

        DropdownMenuItem(
            text = { Text("Create Assignment") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            onClick = {
                states.showNewAssignmentDialog = true
                onDismissRequest()
            }
        )
    }
}

@Composable
fun Canvas(state: CodeBlockState, modifier: Modifier) {
    val onInteraction: (Offset, String) -> Unit = { position, id ->
        state.contextMenuState = when {
            state.vars.any { it.name == id } -> ContextMenuState(
                shown = true,
                position = position,
                variableName = id
            )

            state.ifBlock.any { it.id == id } -> ContextMenuState(
                shown = true,
                position = position,
                ifBlockId = id
            )

            state.whileBlocks.any { it.id == id } -> ContextMenuState(
                shown = true,
                position = position,
                whileBlockId = id
            )

            state.arrays.any { it.id == id} -> ContextMenuState(
                shown = true,
                position = position,
                arrayBlockId = id
            )

            else -> ContextMenuState(
                shown = true,
                position = position
            )
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(16.dp)
    ) {


        // рисуем блоки
        state.vars.forEach { x ->
            key(x.name) {
                VarCard(
                    variable = x,
                    vars = state.vars,
                    hasError = state.errors.any { it.blockId == x.name },
                    onInteraction = onInteraction
                )
            }
        }

        state.ifBlock.forEach { block ->
            key(block.id) {
                IfBlockCard(
                    state = state,
                    ifBlock = block,
                    vars = state.vars,
                    onInteraction = onInteraction
                )
            }
        }

        state.whileBlocks.forEach { block ->
            key(block.id) {
                WhileBlockCard(
                    whileBlock = block,
                    state = state,
                    onInteraction = onInteraction,
                    vars = state.vars
                )
            }
        }

        state.arrays.forEach { block ->
            key(block.id) {
                ArrayCard(
                    arrayBlock = block,
                    state = state,
                    onInteraction = onInteraction,
                    vars = state.vars
                )
            }
        }

        if (state.vars.isEmpty() && state.ifBlock.isEmpty() &&
            state.whileBlocks.isEmpty() && state.arrays.isEmpty()) {
            Text(
                text = stringResource(R.string.tap_plus_to_add),
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}