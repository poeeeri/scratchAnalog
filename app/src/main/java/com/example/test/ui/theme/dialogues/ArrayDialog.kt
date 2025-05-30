package com.example.test.ui.theme.dialogues

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.test.ArrayBlock
import com.example.test.CodeBlockState
import com.example.test.PrintBlock
import com.example.test.R
import com.example.test.utils.preprocessArrayExprForDisplay
import com.example.test.utils.setArrayElement
import java.util.UUID

@SuppressLint("StringFormatInvalid")
@Composable
fun NewArrayDialog(state: CodeBlockState, ctx: Context) {
    val textColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    Dialog(onDismissRequest = {
        state.showNewArrayDialog = false
        state.newArrayName = ""
        state.newArraySize = ""
        state.arrayError = ""
    }) {
        Surface(
            color = Color(ContextCompat.getColor(ctx, R.color.dialog)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .shadow(
                    10.dp, shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(ctx, R.color.shadow))
                )

        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.create_array),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.newArrayName,
                    onValueChange = { state.newArrayName = it },
                    label = {
                        Text(
                            stringResource(R.string.array_name),
                            color = textColor
                        )
                    },
                    isError = state.arrayError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
                        unfocusedBorderColor = Color(
                            ContextCompat.getColor(
                                ctx,
                                R.color.cycle_main_color
                            )
                        ),
                        errorBorderColor = Color(ContextCompat.getColor(ctx, R.color.error_color)),
                        cursorColor = Color(
                            ContextCompat.getColor(
                                ctx,
                                R.color.light_green_for_text
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.newArraySize,
                    onValueChange = { state.newArraySize = it },
                    label = {
                        Text(
                            stringResource(R.string.array_size),
                            color = textColor
                        )
                    },
                    isError = state.arrayError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
                        unfocusedBorderColor = Color(
                            ContextCompat.getColor(
                                ctx,
                                R.color.cycle_main_color
                            )
                        ),
                        errorBorderColor = Color(ContextCompat.getColor(ctx, R.color.error_color)),
                        cursorColor = Color(
                            ContextCompat.getColor(
                                ctx,
                                R.color.light_green_for_text
                            )
                        )
                    )
                )
                if (state.arrayError.isNotEmpty()) {
                    Text(
                        text = state.arrayError,
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (state.newArrayName.isBlank()) {
                                state.arrayError = "Array name must not be empty"
                                return@Button
                            }
                            if (!state.newArrayName[0].isLetter() && state.newArrayName[0] != '_') {
                                state.arrayError =
                                    "Array name must start with a letter or underscore"
                                return@Button
                            }
                            if (state.newArrayName.any { !it.isLetterOrDigit() && it != '_' }) {
                                state.arrayError =
                                    "Array name must contain only letters, digits, or underscores"
                                return@Button
                            }
                            if (state.arrays.any { it.name == state.newArrayName }) {
                                state.arrayError =
                                    "Array with name '${state.newArrayName}' already exists"
                                return@Button
                            }
                            if (state.vars.any { it.name == state.newArrayName }) {
                                state.arrayError =
                                    "Array with name '${state.newArrayName}' already exists"
                                return@Button
                            }

                            val size = state.newArraySize.toIntOrNull()
                            if (size == null) {
                                state.arrayError = "Array size must be a valid number"
                                return@Button
                            }
                            if (size <= 0) {
                                state.arrayError = "Array size must be greater than 0"
                                return@Button
                            }
                            if (size > 100) {
                                state.arrayError = "Array size must not exceed 100 elements"
                                return@Button
                            }

                            val newArray = ArrayBlock(
                                id = UUID.randomUUID().toString(),
                                name = state.newArrayName,
                                size = size,
                                pos = IntOffset(10, 10 + state.arrays.size * 220)
                            )
                            state.arrays.add(newArray)

                            if (state.printBlocks.isEmpty()) {
                                state.printBlocks.add(
                                    PrintBlock(
                                        id = UUID.randomUUID().toString(),
                                        pos = IntOffset(10, 10)
                                    )
                                )
                            }

                            state.showNewArrayDialog = false
                            state.newArrayName = ""
                            state.newArraySize = ""
                            state.arrayError = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.light_green_for_text
                                )
                            ),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.header))
                        )
                    ) {
                        Text(
                            stringResource(R.string.create),
                            color = textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            state.showNewArrayDialog = false
                            state.newArrayName = ""
                            state.newArraySize = ""
                            state.arrayError = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.light_green_for_text
                                )
                            ),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.dark_header))
                        )
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("StringFormatInvalid")
@Composable
fun EditArrayDialog(state: CodeBlockState, ctx: Context) {
    val textColor = Color(ContextCompat.getColor(ctx, R.color.light_green_for_text))
    Dialog(onDismissRequest = {
        state.showNewArrayDialog = false
        state.selectedArrayId = ""
        state.newArrayName = ""
        state.newArraySize = ""
        state.arrayError = ""
    }) {
        Surface(
            color = Color(ContextCompat.getColor(ctx, R.color.dialog)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .shadow(
                    10.dp, shape = RoundedCornerShape(8.dp),
                    spotColor = Color(ContextCompat.getColor(ctx, R.color.shadow))
                )

        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.edit_array),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.newArrayName,
                    onValueChange = { state.newArrayName = it },
                    label = {
                        Text(
                            stringResource(R.string.array_name),
                            color = textColor
                        )
                    },
                    isError = state.arrayError.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
                        errorBorderColor = Color(ContextCompat.getColor(ctx, R.color.error_color)),
                        unfocusedBorderColor = Color(
                            ContextCompat.getColor(
                                ctx,
                                R.color.cycle_main_color
                            )
                        ),
                        cursorColor = Color(
                            ContextCompat.getColor(
                                ctx,
                                R.color.light_green_for_text
                            )
                        )
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.newArraySize,
                    onValueChange = { state.newArraySize = it },
                    label = {
                        Text(
                            stringResource(R.string.array_size),
                            color = textColor
                        )
                    },
                    isError = state.arrayError.isNotEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(ContextCompat.getColor(ctx, R.color.shadow)),
                        unfocusedBorderColor = Color(
                            ContextCompat.getColor(
                                ctx,
                                R.color.cycle_main_color
                            )
                        ),
                        errorBorderColor = Color(ContextCompat.getColor(ctx, R.color.error_color)),
                        cursorColor = Color(
                            ContextCompat.getColor(
                                ctx,
                                R.color.light_green_for_text
                            )
                        )
                    )
                )
                if (state.arrayError.isNotEmpty()) {
                    Text(
                        text = state.arrayError,
                        color = textColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (state.newArrayName.isBlank()) {
                                state.arrayError = "Array name must not be empty"
                                return@Button
                            }
                            if (!state.newArrayName[0].isLetter() && state.newArrayName[0] != '_') {
                                state.arrayError =
                                    "Array name must start with a letter or underscore"
                                return@Button
                            }
                            if (state.newArrayName.any { !it.isLetterOrDigit() && it != '_' }) {
                                state.arrayError =
                                    "Array name must contain only letters, digits, or underscores"
                                return@Button
                            }
                            if (state.arrays.any { it.name == state.newArrayName && it.id != state.selectedArrayId }) {
                                state.arrayError =
                                    "Array with name '${state.newArrayName}' already exists"
                                return@Button
                            }
                            if (state.vars.any { it.name == state.newArrayName }) {
                                state.arrayError =
                                    "Array with name '${state.newArrayName}' already exists"
                                return@Button
                            }

                            val size = state.newArraySize.toIntOrNull()
                            if (size == null) {
                                state.arrayError = "Array size must be a valid number"
                                return@Button
                            }
                            if (size <= 0) {
                                state.arrayError = "Array size must be greater than 0"
                                return@Button
                            }
                            if (size > 100) {
                                state.arrayError = "Array size must not exceed 100 elements"
                                return@Button
                            }

                            val id = state.arrays.indexOfFirst { it.id == state.selectedArrayId }
                            if (id >= 0) {
                                val mas = state.arrays[id];
                                val oldSize = mas.size
                                val newElems = MutableList(size) { i ->
                                    if (i < oldSize && i < mas.elems.size) mas.elems[i]
                                    else "0"
                                }

                                state.arrays[id] = ArrayBlock(
                                    id = mas.id,
                                    name = state.newArrayName,
                                    size = size,
                                    elems = newElems,
                                    pos = mas.pos
                                )
                                state.showEditArrayDialog = false
                                state.selectedArrayId = ""
                                state.newArrayName = ""
                                state.newArraySize = ""
                                state.arrayError = ""
                            } else state.arrayError = "Array not found"
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.light_green_for_text
                                )
                            ),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.header))
                        )
                    ) {
                        Text(
                            stringResource(R.string.update),
                            color = textColor,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            state.showEditArrayDialog = false
                            state.selectedArrayId = ""
                            state.newArrayName = ""
                            state.newArraySize = ""
                            state.arrayError = ""
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(
                                ContextCompat.getColor(
                                    ctx,
                                    R.color.light_green_for_text
                                )
                            ),
                            containerColor = Color(ContextCompat.getColor(ctx, R.color.dark_header))
                        )
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}