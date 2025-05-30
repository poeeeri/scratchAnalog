package com.example.test.ui.theme.menu

import com.example.test.R
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat

// меню выбора переменной для присвоения
@Composable
fun MenuBoxForAssignments(
    context: Context,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,

    ) {
    val textAreaColor = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(ContextCompat.getColor(context, R.color.shadow)),
        unfocusedBorderColor = Color(ContextCompat.getColor(context, R.color.cycle_main_color)),
        errorBorderColor = Color(ContextCompat.getColor(context, R.color.error_color)),
        cursorColor = Color(ContextCompat.getColor(context, R.color.light_green_for_text))
    )
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopStart)
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Var") },
            // создаю типа кликабельную иконку чтобы сворачивать менюшку
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = textAreaColor
        )
        DropdownMenu(
            expanded = expanded,
            modifier = Modifier.background(
                Color(
                    ContextCompat.getColor(
                        context,
                        R.color.dark_header
                    )
                )
            ),
            onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = Color(
                            ContextCompat.getColor(
                                context,
                                R.color.light_green_for_text
                            )
                        )
                    ),
                )
            }

        }
    }
}

