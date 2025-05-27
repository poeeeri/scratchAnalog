package com.example.test.ui.theme.menu

import android.content.Context
import androidx.compose.foundation.background
import com.example.test.R
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.test.CodeBlockState
import com.example.test.ContextMenuState
import com.example.test.utils.handleDelete
import com.example.test.utils.handleDeleteArrayBlock
import com.example.test.utils.handleDeleteForBlock
import com.example.test.utils.handleDeleteIfBlock
import com.example.test.utils.handleEdit
import com.example.test.utils.handleEditArrayBlock
import com.example.test.utils.handleEditForBlock
import com.example.test.utils.handleEditIfBlock
import com.example.test.utils.handleDeletePrintBlock
import com.example.test.utils.handleWhileDelete
import com.example.test.utils.handleWhileEdit

@Composable
fun Menu(state: CodeBlockState,
         context: Context) {
    val edit = stringResource(R.string.edit_button_menu)
    val delete = stringResource(R.string.delete_button_menu)

    val textColor = MenuDefaults.itemColors(
        textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    )
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
            offset = menuOffset,
            modifier = Modifier.background(Color(ContextCompat.getColor(context, R.color.dialog)))
        ) {
            if (state.contextMenuState.variableName != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleEdit(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleDelete(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
            }

            if (state.contextMenuState.whileBlockId != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleWhileEdit(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleWhileDelete(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
            } else if (state.contextMenuState.ifBlockId != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleEditIfBlock(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleDeleteIfBlock(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
            } else if (state.contextMenuState.arrayBlockId != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleEditArrayBlock(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleDeleteArrayBlock(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
            }

            else if (state.contextMenuState.forBlockId != null) {
                DropdownMenuItem(
                    text = { Text(edit) },
                    onClick = {
                        handleEditForBlock(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
                )
                DropdownMenuItem(
                    text = { Text(delete) },
                    onClick = {
                        handleDeleteForBlock(state)
                        state.contextMenuState = ContextMenuState()
                    },
                    colors = textColor
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