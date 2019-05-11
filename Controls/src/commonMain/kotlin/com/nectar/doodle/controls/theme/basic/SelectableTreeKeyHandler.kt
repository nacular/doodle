package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.tree.TreeLike
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.VK_A
import com.nectar.doodle.event.KeyEvent.Companion.VK_DOWN
import com.nectar.doodle.event.KeyEvent.Companion.VK_LEFT
import com.nectar.doodle.event.KeyEvent.Companion.VK_RIGHT
import com.nectar.doodle.event.KeyEvent.Companion.VK_UP
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
                VK_UP, VK_DOWN -> {
                    when (Shift) {
                        in event -> {
                            tree.selectionAnchor?.let { anchor ->
                                tree.lastSelection?.let { if (event.code == VK_UP) tree.previous(it) else tree.next(it) }?.let { current ->
                                    val currentRow = tree.rowFromPath(current)
                                    val anchorRow  = tree.rowFromPath(anchor )

                                    if (currentRow != null && anchorRow != null) {
                                        when {
                                            currentRow < anchorRow -> tree.setSelection((currentRow..anchorRow).reversed().toSet())
                                            anchorRow < currentRow -> tree.setSelection((anchorRow..currentRow).toSet())
                                            else                   -> tree.setSelection(setOf(currentRow))
                                        }
                                    }
                                }
                            }
                        }
                        else -> tree.lastSelection?.let { if (event.code == VK_UP) tree.previous(it) else tree.next(it) }?.let { tree.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }
                VK_LEFT        -> tree.selection.firstOrNull()?.also { if (tree.expanded(it)) { tree.collapse(it) } else it.parent?.let { tree.setSelection(setOf(it)) } }?.let { Unit } ?: Unit
                VK_RIGHT       -> tree.selection.firstOrNull()?.also { tree.expand(it) }?.let { Unit } ?: Unit
                VK_A           -> {
                    if (Ctrl in event || Meta in event) {
                        tree.selectAll()
                    }
                }
            }
        }
    }
}