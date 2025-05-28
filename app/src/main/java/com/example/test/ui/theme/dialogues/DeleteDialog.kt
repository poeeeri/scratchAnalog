package com.example.test.ui.theme.dialogues

import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.test.CodeBlockState
import com.example.test.R


@Composable
fun DeleteAllDialog(state: CodeBlockState,
                    context: Context) {
    val textColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    AlertDialog(
        modifier = Modifier
            .shadow(10.dp, shape = RoundedCornerShape(8.dp),
                spotColor = Color(ContextCompat.getColor(context, R.color.shadow))),
        onDismissRequest = { state.showDeleteAllDialog = false },

        containerColor = Color(ContextCompat.getColor(context, R.color.dialog)),
        title = { Text(stringResource(R.string.delete_all),
            color = textColor ) },
        text = { Text(stringResource(R.string.are_you_sure),
            color = textColor ) },
        confirmButton = {
            Button(
                onClick = {
                    state.showDeleteAllDialog = false
                    state.whileBlocks.clear()
                    state.vars.clear()
                    state.arrays.clear()
                    state.ifBlock.clear()
                    state.forBlocks.clear()
                    state.printBlocks.clear()
                    state.targetCommandsList = null
                    state.selectedIfBlock = ""
                    state.errors.removeAll { true }
                    state.selectedArrayId = ""
                    state.selectedArrayName = ""
                    state.arrayIndexExpression = ""
                    state.arrayValueExpression = ""
                    state.newArrayName = ""
                    state.newArraySize = ""
                    state.arrayError = ""
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                    containerColor = Color(ContextCompat.getColor(context, R.color.header))
                )
            ) {
                Text(stringResource(R.string.delete_all),
                    color = textColor )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { state.showDeleteAllDialog = false },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text)),
                    containerColor = Color(ContextCompat.getColor(context, R.color.dark_header))
                )
            ) {
                Text(stringResource(R.string.cancel),
                    color = textColor )
            }
        }
    )
}