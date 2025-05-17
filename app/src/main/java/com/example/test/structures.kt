package com.example.test

import android.view.ContextMenu
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import java.util.UUID

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CodeBlockState {
    val vars: SnapshotStateList<Variable> = mutableStateListOf()
    val ifBlock: SnapshotStateList<IfBlock> = mutableStateListOf()
    val errors: SnapshotStateList<VarError> = mutableStateListOf()

    var showNewAssignmentDialog by mutableStateOf(false)
    var showNewIfDialog by mutableStateOf(false)
    var showNewVarDialog by mutableStateOf(false)
    var showDeleteAllDialog by mutableStateOf(false)

    var selectedTargetVar by mutableStateOf("")
    var assignmentArithmExpr by mutableStateOf("")
    var assignmentError by mutableStateOf("")

    var leftIfExpression by mutableStateOf("")
    var rightIfExpression by mutableStateOf("")
    var ifBlockError by mutableStateOf("")
    var newIfCommand by mutableStateOf("")
    var newVarName by mutableStateOf("")
    var newVarError by mutableStateOf("")
    var selectedIfBlock by mutableStateOf("")
    var selectedComparisonOperator by mutableStateOf("==")
    var curBlockCommands: SnapshotStateList<String> = mutableStateListOf()

    var contextMenuState by mutableStateOf(ContextMenuState())
}

data class Variable(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val expression: String,
    var pos: IntOffset = IntOffset(0, 0)
)

data class VarError(
    val msg: String,
    val blockId: String = ""
)

data class ContextMenuState(
    val shown: Boolean = false,
    val position: Offset = Offset.Zero,
    val variableName: String? = null,
    val ifBlockId: String? = null
)

data class IfBlock(
    val id: String = UUID.randomUUID().toString(),
    val condition: String = "",
    val leftExpression: String = "",
    val rightExpression: String = "",
    val comparisonOperator: String = "",
    val commands: MutableList<String> = mutableStateListOf(),
    var pos: IntOffset = IntOffset(0, 0)
)