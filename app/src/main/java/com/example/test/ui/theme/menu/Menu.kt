package com.example.test.ui.theme.menu

import com.example.test.R
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.res.stringResource
import com.example.test.CodeBlockState
import com.example.test.ContextMenuState
import com.example.test.utils.handleDelete
import com.example.test.utils.handleDeleteArrayBlock
import com.example.test.utils.handleDeleteIfBlock
import com.example.test.utils.handleEdit
import com.example.test.utils.handleEditArrayBlock
import com.example.test.utils.handleEditIfBlock
import com.example.test.utils.handleDeletePrintBlock

@Composable
fun Menu(state: CodeBlockState) {
    val edit = stringResource(R.string.edit_button_menu)
    val delete = stringResource(R.string.delete_button_menu)
    if (state.contextMenuState.shown) {
        val density = LocalDensity.current
        val menuOffset = with(density) {
            DpOffset(
                x = (state.contextMenuState.position.x + 100).toDp(),
                y = (state.contextMenuState.position.y + 100).toDp()
            )
        }
        DropdownMenu(
            expanded = true,
            onDismissRequest = { state.contextMenuState = ContextMenuState() },
            offset = menuOffset
        ) {
            if (state.contextMenuState.variableName != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleEdit(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleDelete(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
            }

            if (state.contextMenuState.whileBlockId != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleWhileEdit(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleWhileDelete(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
            } else if (state.contextMenuState.ifBlockId != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleEditIfBlock(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleDeleteIfBlock(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
            } else if (state.contextMenuState.arrayBlockId != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleEditArrayBlock(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleDeleteArrayBlock(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
            } else if (state.contextMenuState.printBlockId != null) {
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleDeletePrintBlock(state)
                        state.contextMenuState = ContextMenuState()
                    }
                )
            }
        }
    }
}

fun handleWhileEdit(state: CodeBlockState) {
    state.contextMenuState.whileBlockId?.let { blockId ->
        val block = state.whileBlocks.firstOrNull { it.id == blockId }
        block?.let{
            state.selectedWhileOperator = it.comparisonOperator
            state.leftWhileExpression = it.leftExpression
            state.rightWhileExpression = it.rightExpression
            state.curWhileCommands.clear()
            state.selectedWhileTargetId = blockId
            state.showNewWhileDialog = true
            state.curBlockCommands.addAll(block.commands)
            state.targetCommandsList = block.commands
        }
    }
}

fun handleWhileDelete(state: CodeBlockState) {
    state.contextMenuState.whileBlockId?.let { blockId ->
        state.whileBlocks.removeAll{it.id == blockId }
    }
}
