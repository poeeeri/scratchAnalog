package com.example.test.ui.theme

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.test.CodeBlockState
import com.example.test.utils.recCalAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.test.R

@Composable
fun FloatingActivationButtons(state: CodeBlockState, context: Context) {
    Row {

        // кнопка для пересчета значений переменных
        FloatingActionButton(
            containerColor = Color(ContextCompat.getColor(context, R.color.header)),
            onClick = {
                recCalAll(state, context)
            }
        ) {
            Icon(
                Icons.Default.PlayArrow, contentDescription = "Recalculate All",
                tint = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
            )
        }
        Spacer(modifier = Modifier.width(4.dp))

        FloatingActionButton(
            containerColor = Color(ContextCompat.getColor(context, R.color.header)),
            onClick = { state.showDeleteAllDialog = true }
        ) {
            Icon(
                Icons.Default.Delete, contentDescription = "Delete All",
                tint = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
            )
        }
    }
}
