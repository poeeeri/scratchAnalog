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
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.PlayArrow

@Composable
fun FloatingActivationButtons(state: CodeBlockState, context: Context) {
    Row {

        // кнопка для пересчета значений переменных
        FloatingActionButton(
            onClick = {
                recCalAll(state, context)
            }
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Recalculate All")
        }
        Spacer(modifier = Modifier.width(4.dp))

        FloatingActionButton(
            onClick = { state.showDeleteAllDialog = true }
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete All")
        }
    }
}
