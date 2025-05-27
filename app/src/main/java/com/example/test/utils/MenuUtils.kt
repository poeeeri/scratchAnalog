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
            state.curBlockCommands.addAll(block.commands)
            state.targetCommandsList = block.commands

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

fun handleEditArrayBlock(state: CodeBlockState) {
    state.contextMenuState.arrayBlockId?.let { blockId ->
        val block = state.arrays.firstOrNull { it.id == blockId }
        block?.let {
            state.selectedArrayId = blockId
            state.newArrayName = it.name
            state.newArraySize = it.size.toString()
            state.showEditArrayDialog = true
        }
    }
    state.contextMenuState = ContextMenuState()
}

fun handleDeleteArrayBlock(state: CodeBlockState) {
    state.contextMenuState.arrayBlockId?.let { blockId ->
        state.arrays.removeAll { it.id == blockId }
    }
    state.contextMenuState = ContextMenuState()
}

fun handleWhileEdit(state: CodeBlockState) {
    state.contextMenuState.whileBlockId?.let { blockId ->
        val block = state.whileBlocks.firstOrNull { it.id == blockId }
        block?.let{
            state.selectedWhileOperator = it.comparisonOperator
            state.leftWhileExpression = it.leftExpression
            state.rightWhileExpression = it.rightExpression
            state.curWhileCommands.clear()
            state.selectedWhileTargetId = blockId
            state.showNewWhileDialog = true
            state.curWhileCommands.addAll(block.commands)
            state.targetCommandsList = block.commands
        }
    }
}

fun handleWhileDelete(state: CodeBlockState) {
    state.contextMenuState.whileBlockId?.let { blockId ->
        state.whileBlocks.removeAll{it.id == blockId }
    }
}


fun handleEditForBlock(state: CodeBlockState) {
    state.contextMenuState.forBlockId?.let { blockId ->
        val block = state.forBlocks.firstOrNull { it.id == blockId }
        block?.let{
            state.selectedForOperator = it.comparisonOperator
            state.newForStartExpr = it.startExpression
            state.newForEndExpr = it.endExpression
            state.newForVar = it.variable
            state.newForStepIter = it.stepIter.toString()
            state.curForCommands.clear()
            state.selectedForTargetId = blockId
            state.showNewForDialog = true
            state.curForCommands.addAll(block.commands)
            state.targetCommandsList = block.commands
        }
    }
}

fun handleDeleteForBlock(state: CodeBlockState) {
    state.contextMenuState.forBlockId?.let { blockId ->
        state.forBlocks.removeAll{ it.id == blockId }
    }
}