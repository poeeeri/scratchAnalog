package com.example.test.ui.theme

import android.content.Context
import com.example.test.R
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
//>>>>>>> origin/develop
import com.example.test.ContextMenuState
import com.example.test.CodeBlockState
import com.example.test.PrintBlock
import com.example.test.Variable
import com.example.test.ui.theme.blocks.ArrayCard
import com.example.test.ui.theme.blocks.ForBlockCard
import com.example.test.ui.theme.blocks.IfBlockCard
import com.example.test.ui.theme.blocks.VarCard
import com.example.test.ui.theme.blocks.WhileBlockCard
import com.example.test.ui.theme.dialogues.ArrayAccessDialog
import com.example.test.ui.theme.dialogues.ArraySetDialog
import com.example.test.ui.theme.dialogues.DeleteAllDialog
import com.example.test.ui.theme.dialogues.EditArrayDialog
import com.example.test.ui.theme.dialogues.ForDialog
import com.example.test.ui.theme.dialogues.IfDialog
import com.example.test.ui.theme.dialogues.NewArrayDialog
import com.example.test.ui.theme.dialogues.NewAssignmentDialog
import com.example.test.ui.theme.dialogues.VarDialog
import com.example.test.ui.theme.dialogues.WhileDialog
import com.example.test.ui.theme.menu.Menu
import com.example.test.ui.theme.blocks.PrintCard
import java.util.UUID


class CodeBlockViewModel : ViewModel() {
    val codeBlockState = CodeBlockState()
}

// запоминает состояния при перерисовке компосэбл
@Composable
fun rememberCodeBlockState(): CodeBlockState {
    val viewModel: CodeBlockViewModel = viewModel()
    return viewModel.codeBlockState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlock() {
    val states = rememberCodeBlockState()
    val context = LocalContext.current
    var showTopMenu by remember { mutableStateOf(false) }
    val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(ContextCompat.getColor(context, R.color.header)),
                    titleContentColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showTopMenu = !showTopMenu }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = textColor
                            )
                        }
                        TopBarDropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false },
                            states = states,
                            context = context
                        )
                    }
                }
            )
        },
        floatingActionButton = { FloatingActivationButtons(states, context) }
    ) { innerPadding ->
        Canvas(states, Modifier
            .padding(innerPadding)
            .fillMaxSize(),
            context)
        Menu(states, context)
    }

    if (states.showNewVarDialog) VarDialog(states, context)
    if (states.showNewIfDialog) IfDialog(states, context)
    if (states.showDeleteAllDialog) DeleteAllDialog(states, context)
    if (states.showNewAssignmentDialog) NewAssignmentDialog(states, context)
    if (states.showNewWhileDialog) WhileDialog(states, context)
    if (states.showNewArrayDialog) NewArrayDialog(states, context)
    if (states.showEditArrayDialog) EditArrayDialog(states, context)
    if (states.showArrayAccessDialog) ArrayAccessDialog(states, context)
    if (states.showArraySetDialog) ArraySetDialog(states, context)
    if (states.showNewForDialog) ForDialog(states, context)
}

@Composable
fun TopBarDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    states: CodeBlockState,
    context: Context
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.background(Color(ContextCompat.getColor(context, R.color.dialog)))
    ) {
        var create = stringResource(R.string.create_var)
        val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
        DropdownMenuItem(
            text = { Text("Create Variable") },
            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null,
                tint = textColor) },
            onClick = {
                states.showNewVarDialog = true
                onDismissRequest()
            }
        )

        create = stringResource(R.string.create_if_block)
        DropdownMenuItem(
            text = { Text("Create If Block") },
            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null,
                tint = textColor )},
            onClick = {
                states.showNewIfDialog = true
                onDismissRequest()
            }
        )

        create = stringResource(R.string.create_while_block)
        DropdownMenuItem(
            text = { Text("Create While Block") },
            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null,
                tint = textColor )},
            onClick = {
                states.showNewWhileDialog = true
                onDismissRequest()
            }
        )

        create = stringResource(R.string.create_array)
        DropdownMenuItem(
            text = { Text("Create Array") },
            leadingIcon = { Icon(Icons.Default.List, contentDescription = null,
                tint = textColor) },
            onClick = {
                states.showNewArrayDialog = true
                onDismissRequest()
            }
        )

        create = stringResource(R.string.crate_assign_top_bar)
        DropdownMenuItem(
            text = { Text("Create Assignment") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null,
                tint =textColor) },
            onClick = {
                states.showNewAssignmentDialog = true
                onDismissRequest()
            }
        )

        create = stringResource(R.string.create_for_block)
        DropdownMenuItem(
            text = { Text(create) },
            leadingIcon = { Icon(Icons.Default.Replay5, contentDescription = null,
                tint = textColor) },
            onClick = {
                states.showNewForDialog = true
                onDismissRequest()
            }
        )
    }
}

@Composable
fun Canvas(state: CodeBlockState, modifier: Modifier, context: Context) {
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

            state.printBlocks.any { it.id == id} -> ContextMenuState(
                shown = true,
                position = position,
                printBlockId = id
            )

            state.forBlocks.any { it.id == id} -> ContextMenuState(
                shown = true,
                position = position,
                forBlockId = id
            )

            else -> ContextMenuState(
                shown = true,
                position = position
            )
        }
    }

    val hasContent = state.vars.isNotEmpty() ||
            state.ifBlock.isNotEmpty() ||
            state.whileBlocks.isNotEmpty() ||
            state.arrays.isNotEmpty() ||
            state.forBlocks.isNotEmpty() ||
            state.printBlocks.isNotEmpty()

    if (hasContent) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(ContextCompat.getColor(context, R.color.canvas)))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            // рисуем блоки
            state.vars.forEach { x ->
                VarCard(
                    variable = x,
                    vars = state.vars,
                    hasError = state.errors.any { it.blockId == x.name },
                    onInteraction = onInteraction,
                    context = context
                )
            }
            state.ifBlock.forEach { x ->
                IfBlockCard(
                    state = state,
                    ifBlock = x,
                    vars = state.vars,
                    onInteraction = onInteraction,
                    context = context
                )
            }
            state.whileBlocks.forEach { x ->
                WhileBlockCard(
                    state = state,
                    whileBlock = x,
                    onInteraction = onInteraction,
                    vars = state.vars,
                    context = context
                )
            }
            state.arrays.forEach { x ->
                ArrayCard(
                    state = state,
                    arrayBlock = x,
                    vars = state.vars,
                    onInteraction = onInteraction
                )
            }
            state.forBlocks.forEach { x ->
                ForBlockCard(
                    state = state,
                    forBlock = x,
                    vars = state.vars,
                    onInteraction = onInteraction,
                    context = context
                )
            }
            state.printBlocks.forEach { block ->
                PrintCard(
                    state = state,
                    blockId = block.id,
                    vars = state.vars,
                    arrays = state.arrays,
                    onInteraction = onInteraction
                )
            }
        }
    }
    else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(ContextCompat.getColor(context, R.color.canvas))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.tap_plus_to_add),
                color = Color.Gray,
            )
        }
    }
}
