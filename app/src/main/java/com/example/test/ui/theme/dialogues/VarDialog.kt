package com.example.test.ui.theme.dialogues

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.test.CodeBlockState
import com.example.test.R
import com.example.test.Variable
import com.example.test.VariableType

@SuppressLint("StringFormatInvalid")
@Composable
fun VarDialog(state: CodeBlockState, ctx: Context) {
    val textAreaColor = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
        unfocusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.cycle_main_color)),
        errorBorderColor = Color(ContextCompat.getColor(ctx, R.color.error_color)),
        cursorColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    )
    val textColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    val create = stringResource(R.string.create_new_var)
    val errvar_not_be_empty = stringResource(R.string.errvar_not_be_empty)
    val var_must_start = stringResource(R.string.var_must_start)
    Dialog(onDismissRequest = {
        state.showNewVarDialog = false
        state.newVarName = ""
        state.newVarError = ""
    }) {
        Surface(
            color = Color(ContextCompat.getColor(ctx, R.color.dialog)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .shadow(10.dp, shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(ctx, R.color.shadow)))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = create,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.newVarName,
                    onValueChange = { state.newVarName = it },
                    label = { Text(stringResource(R.string.var_nam_or_several),
                        color = textColor,) },
                    isError = state.newVarError != "",
                    modifier = Modifier.fillMaxWidth(),
                    colors =  textAreaColor

                )
                if (state.newVarError != "") {
                    Text(
                        text = state.newVarError,
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Variable Type",
                    color = textColor,)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    VariableType.entries.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.selectedVarType == type,
                                onClick = { state.selectedVarType = type },
                                colors = RadioButtonDefaults.colors(
                                    unselectedColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
                                    selectedColor = Color(ContextCompat.getColor(ctx, R.color.shadow))
                                )

                            )
                            Text(text = type.toString(),
                                color = textColor,)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (state.newVarName.isNotBlank()) {
                                val varsArray = state.newVarName.split(',').map { it.trim() }.toSet()
                                var containsError = false
                                for (v in varsArray) {
                                    when {
                                        v.isBlank() -> {
                                            state.newVarError = errvar_not_be_empty
                                            containsError = true
                                            break
                                        }

                                        state.vars.any { it.name == v } -> {
                                            val var_already_exist = ctx.getString(R.string.var_already_exist, v)
                                            state.newVarError =
                                                var_already_exist
                                            containsError = true
                                            break
                                        }

                                        state.arrays.any { it.name == v } -> {
                                            state.newVarError = ctx.getString(R.string.var_already_exist, v)
                                            containsError = true
                                            break
                                        }

                                        !v[0].isLetter() && v[0] != '_' -> {
                                            state.newVarError =
                                                var_must_start
                                            containsError = true
                                            break
                                        }

                                        v.any { !it.isLetterOrDigit() && it != '_' } -> {
                                            state.newVarError =
                                                "Variable name must contain only digits, letters or underscores"
                                            containsError = true
                                            break
                                        }
                                    }
                                }

                                if (!containsError) {
                                    varsArray.forEach { v ->
                                        state.vars.add(
                                            Variable(
                                                name = v,
                                                expression = "0",
                                                pos = IntOffset(10 + state.vars.size, state.vars.size*10),
                                                type = state.selectedVarType
                                            )
                                        )
                                    }

                                    state.showNewVarDialog = false
                                    state.newVarName = ""
                                    state.newVarError = ""
                                    state.selectedVarType = VariableType.INT
                                }
                            }
                            else state.newVarError = "Variable name must not be empty"
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text)),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.header))
                        )
                    ) {
                        Text(stringResource(R.string.create), color = textColor,)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            state.showNewVarDialog = false
                            state.newVarName = ""
                            state.newVarError = ""
                            state.selectedVarType = VariableType.INT
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text)),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.dark_header))
                        )
                    ) {
                        Text(stringResource(R.string.cancel), color = textColor,)
                    }
                }
            }
        }
    }
}