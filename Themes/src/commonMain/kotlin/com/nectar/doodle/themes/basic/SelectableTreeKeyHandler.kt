package com.nectar.doodle.themes.basic

import com.nectar.doodle.controls.tree.TreeLike
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.ArrowDown
import com.nectar.doodle.event.KeyEvent.Companion.ArrowLeft
import com.nectar.doodle.event.KeyEvent.Companion.ArrowRight
import com.nectar.doodle.event.KeyEvent.Companion.ArrowUp
import com.nectar.doodle.event.KeyEvent.Companion.KeyA
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift

/**
 * Created by Nicholas Eddy on 5/10/19.
 */
interface SelectableTreeKeyHandler {
    fun keyPressed(event: KeyEvent) {
        (event.source as TreeLike).let { tree ->
            when (event.code) {
                ArrowUp, ArrowDown -> {
                    when (Shift) {
                        in event -> {
                            tree.selectionAnchor?.let { anchor ->
                                tree.lastSelection?.let { if (event.code == ArrowUp) tree.previous(it) else tree.next(it) }?.let { current ->
                                    val currentRow = tree.rowFromPath(current)
                                    val anchorRow  = tree.rowFromPath(anchor )

                                    if (currentRow != null && anchorRow != null) {
                                        when {
                                            currentRow < anchorRow  -> tree.setSelection((currentRow..anchorRow).reversed().toSet())
                                            anchorRow  < currentRow -> tree.setSelection((anchorRow..currentRow).toSet())
                                            else                    -> tree.setSelection(setOf(currentRow))
                                        }
                                    }
                                }
                            }
                        }
                        else -> tree.lastSelection?.let { if (event.code == ArrowUp) tree.previous(it) else tree.next(it) }?.let { tree.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }
                ArrowLeft        -> tree.selection.firstOrNull()?.also { if (tree.expanded(it)) { tree.collapse(it) } else it.parent?.let { tree.setSelection(setOf(it)) } }?.let { Unit } ?: Unit
                ArrowRight       -> tree.selection.firstOrNull()?.also { tree.expand(it) }?.let { Unit } ?: Unit
                KeyA             -> {
                    if (Ctrl in event || Meta in event) {
                        tree.selectAll()
                    }
                }
            }
        }
    }
}