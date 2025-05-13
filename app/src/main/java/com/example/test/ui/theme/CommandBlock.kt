package com.example.test.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

//interface CodeElement{
//    val id: String
//}
//
//class DragDropState {
//    var dragging: CodeElement? by mutableStateOf()
//    var dropTarget: DropTarget? by mutableStateOf()
//
//    data class DropTarget(
//        val parents: SnapshotStateList<CodeElement>,
//        val idx: Int
//    )
//
//    fun performDrop() {
//        val el = dragging ?: return
//        val target = dropTarget?: return
//    }
//}