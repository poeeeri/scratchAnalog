package com.example.test

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
    val whileBlocks: SnapshotStateList<WhileBlock> = mutableStateListOf()

    var showNewAssignmentDialog by mutableStateOf(false)
    var showNewIfDialog by mutableStateOf(false)
    var showNewVarDialog by mutableStateOf(false)
    var showDeleteAllDialog by mutableStateOf(false)
    var showNewWhileDialog by mutableStateOf(false)

    // меню с кнопками на выбор при создании команды в ифе или вайле
    var showChooseWhileDialog by mutableStateOf(false)
    var showChooseIfDialog by mutableStateOf(false)

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

    var leftWhileExpression by mutableStateOf("")
    var rightWhileExpression by mutableStateOf("")
    var selectedWhileOperator by mutableStateOf("==")
    var whileBlockError by mutableStateOf("")
    var curWhileCommands: SnapshotStateList<CommandBlock> = mutableStateListOf()
    var curBlockCommands: SnapshotStateList<CommandBlock> = mutableStateListOf()
    var newWhileCommand by mutableStateOf("")
    var selectedWhileTargetId by mutableStateOf("")

    var contextMenuState by mutableStateOf(ContextMenuState())
    var targetCommandsList: SnapshotStateList<CommandBlock>? = null
}


data class Variable(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var expression: String,
    var pos: IntOffset = IntOffset(0, 0)
)

data class VarBlockCommand(
    val variable: Variable
) : CommandBlock() {
    override val id: String
        get() = variable.id
    override var pos: IntOffset
        get() = variable.pos
        set(value) { variable.pos = value }
}

data class VarError(
    val msg: String,
    val blockId: String = ""
)


data class WhileBlock(
    val id: String = UUID.randomUUID().toString(),
    val leftExpression: String = "",
    val comparisonOperator: String = "",
    val rightExpression: String = "",
    val commands: SnapshotStateList<CommandBlock> = mutableStateListOf(),
    var pos: IntOffset = IntOffset(0, 0)
)

data class WhileBlockCommand(
    val whileBlock: WhileBlock
) : CommandBlock() {
    override val id: String
        get() = whileBlock.id
    override var pos: IntOffset
        get() = whileBlock.pos
        set(value) { whileBlock.pos = value }
}


data class IfBlock(
    val id: String = UUID.randomUUID().toString(),
    val condition: String = "",
    val leftExpression: String = "",
    val rightExpression: String = "",
    val comparisonOperator: String = "",
    val commands: SnapshotStateList<CommandBlock> = mutableStateListOf(),
    var pos: IntOffset = IntOffset(0, 0)
)

data class IfBlockCommand(
    val ifBlock: IfBlock
) : CommandBlock() {
    override val id: String
        get() = ifBlock.id
    override var pos: IntOffset
        get() = ifBlock.pos
        set(value) { ifBlock.pos = value }
}


sealed class CommandBlock {
    abstract val id: String
    abstract var pos: IntOffset
}

data class ContextMenuState(
    val shown: Boolean = false,
    val position: Offset = Offset.Zero,
    val variableName: String? = null,
    val ifBlockId: String? = null,
    val whileBlockId: String? = null
)