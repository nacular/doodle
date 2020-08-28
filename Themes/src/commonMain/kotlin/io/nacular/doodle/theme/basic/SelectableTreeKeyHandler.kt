package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.tree.TreeLike
import io.nacular.doodle.core.ContentDirection.LeftRight
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyText
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowLeft
import io.nacular.doodle.event.KeyText.Companion.ArrowRight
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift

/**
 * Created by Nicholas Eddy on 5/10/19.
 */
interface SelectableTreeKeyHandler {
    fun keyPressed(event: KeyEvent) {
        (event.source as? TreeLike)?.let { tree ->
            val (expandKey, collapseKey) = when (tree.contentDirection) {
                LeftRight -> ArrowRight to ArrowLeft
                else      -> ArrowLeft  to ArrowRight
            }

            when (event.key) {
                ArrowUp, ArrowDown -> {
                    when (Shift) {
                        in event -> {
                            tree.selectionAnchor?.let { anchor ->
                                tree.lastSelection?.let { if (event.key == ArrowUp) tree.previous(it) else tree.next(it) }?.let { current ->
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
                        else -> tree.lastSelection?.let { if (event.key == ArrowUp) tree.previous(it) else tree.next(it) }?.let { tree.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }
                collapseKey        -> tree.selection.firstOrNull()?.also { if (tree.expanded(it)) { tree.collapse(it) } else it.parent?.let { tree.setSelection(setOf(it)) } }?.let { Unit } ?: Unit
                expandKey          -> tree.selection.firstOrNull()?.also { tree.expand(it) }?.let { Unit } ?: Unit
                KeyText("a"), KeyText("A") -> {
                    if (Ctrl in event || Meta in event) {
                        tree.selectAll()
                    }
                }
            }
        }
    }
}