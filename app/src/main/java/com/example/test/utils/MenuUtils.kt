package com.example.test.utils

import com.example.test.CodeBlockState
import com.example.test.ContextMenuState

//обработчик для изменения переменной
fun handleEdit(state: CodeBlockState) {
    state.contextMenuState.variableName?.let { varName ->
        val variable = state.vars.firstOrNull { it.name == varName }
        variable?.let {
            state.selectedTargetVar = varName
            state.assignmentArithmExpr = it.expression
            state.showNewAssignmentDialog = true
        }
    }
    state.contextMenuState = ContextMenuState()
}

//обработчик для удаления переменной
fun handleDelete(state: CodeBlockState) {
    state.contextMenuState.variableName?.let { varName ->
        state.vars.removeAll { it.name == varName }
    }
    state.contextMenuState = ContextMenuState()
}

// Обработка изменения условия
fun handleEditIfBlock(state: CodeBlockState) {
    state.contextMenuState.ifBlockId?.let { blockId ->
        val block = state.ifBlock.firstOrNull { it.id == blockId }
        block?.let {
            state.selectedIfBlock = blockId
            state.leftIfExpression = it.leftExpression
            state.rightIfExpression = it.rightExpression
            state.selectedComparisonOperator = it.comparisonOperator
            state.curBlockCommands.clear()
            state.curBlockCommands.addAll(it.commands)
            state.showNewIfDialog = true
        }
    }
}

// Обработка удаления условия
fun handleDeleteIfBlock(state: CodeBlockState) {
    state.contextMenuState.ifBlockId?.let { blockId ->
        state.ifBlock.removeAll { it.id == blockId }
    }
}