package com.example.test.ui.theme

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test.CodeBlockState
import com.example.test.utils.recCalAll

@Composable
fun FloatingActivationButtons(state: CodeBlockState, context: Context) {
    Row {
        FloatingActionButton(
            onClick = { state.showNewVarDialog = true }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Это для добавления условия
        FloatingActionButton(
            onClick = { state.showNewIfDialog = true }
        ) {
            Icon(Icons.Default.Code, contentDescription = "Add If Block")
        }

        Spacer(modifier = Modifier.width(8.dp))

        // кнопка присваивания
        FloatingActionButton(
            onClick = { state.showNewAssignmentDialog = true }
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Assign")
        }

        Spacer(modifier = Modifier.width(8.dp))

        // кнопка для пересчета значений переменных
        FloatingActionButton(
            onClick = {
                recCalAll(state, context)
            }
        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Recalculate All")
        }
        Spacer(modifier = Modifier.width(8.dp))

        FloatingActionButton(
            onClick = { state.showDeleteAllDialog = true }
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete All")
        }
    }
}
