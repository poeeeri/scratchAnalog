package com.example.test.ui.theme

import com.example.test.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.test.ContextMenuState
import com.example.test.CodeBlockState
import com.example.test.ui.theme.blocks.IfBlockCard
import com.example.test.ui.theme.blocks.VarCard
import com.example.test.ui.theme.menu.Menu

// запоминает состояния при перерисовке компосэбл
@Composable
fun rememberCodeBlockState(): CodeBlockState = remember {CodeBlockState()}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlock() {
    val states = rememberCodeBlockState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = { FloatingActivationButtons(states, context) }
    ) { innerPadding ->
        Canvas(states, Modifier.padding(innerPadding).fillMaxSize())
        Menu(states)
    }

    // я для каждого диалогового окна написала свою функцию, чтобы кодблок не был большим и улучшить
    // читабельность, как и сказали сделать на прошлой сдаче прогресса.
    // если что в string.xml хранятся значения строк и их айдишники, все строки записываем туда
    if (states.showNewVarDialog) VarDialog(states, context)
    if (states.showNewIfDialog) IfDialog(states)
    if (states.showDeleteAllDialog) DeleteAllDialog(states)
    if (states.showNewAssignmentDialog) NewAssignmentDialog(states)
}

@Composable
fun Canvas(state: CodeBlockState, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(16.dp)
    ) {
        state.vars.forEach { x ->
            key(x.name) {
                VarCard(
                    variable = x,
                    vars = state.vars,
                    hasError = state.errors.any { it.blockId == x.name },

                    onInteraction = { position, varName ->
                        state.contextMenuState = ContextMenuState(
                            shown = true,
                            position = position,
                            variableName = varName
                        )
                    }
                )
            }
        }
        //контекстное меню для изменения или удаления переменной
        state.ifBlock.forEach { block ->
            key(block.id) {
                IfBlockCard(
                    ifBlock = block,
                    vars = state.vars,
                ) { position, blockId ->
                    state.contextMenuState = ContextMenuState(
                        shown = true,
                        position = position,
                        ifBlockId = blockId
                    )
                }
            }
        }
        if (state.vars.isEmpty()) {
            Text(
                text = stringResource(R.string.tap_plus_to_add),
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}




