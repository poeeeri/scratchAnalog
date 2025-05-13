package com.example.test.ui.theme

import com.example.test.R
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
//import com.example.test.CodeBlockState
import com.example.test.ContextMenuState
import com.example.test.IfBlock
import com.example.test.VarError
import com.example.test.Variable
import com.example.test.utils.evaluateIfCondition
import com.example.test.utils.executeIfCommands
import com.example.test.utils.recalculateAllVariables
import com.example.test.CodeBlockState

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






