package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.Selectable
import io.nacular.doodle.event.KeyEvent
import io.nacular.doodle.event.KeyText
import io.nacular.doodle.event.KeyText.Companion.ArrowDown
import io.nacular.doodle.event.KeyText.Companion.ArrowUp
import io.nacular.doodle.system.SystemInputEvent.Modifier.Ctrl
import io.nacular.doodle.system.SystemInputEvent.Modifier.Meta
import io.nacular.doodle.system.SystemInputEvent.Modifier.Shift

/**
 * Created by Nicholas Eddy on 5/10/19.
 */
interface SelectableListKeyHandler {
    fun keyPressed(event: KeyEvent) {
        (event.source as? Selectable<Int>)?.let { list ->
            when (event.key){
                ArrowUp, ArrowDown -> {
                    when (Shift) {
                        in event -> {
                            list.selectionAnchor?.let { anchor ->
                                list.lastSelection?.let { if (event.key == ArrowUp) list.previous(it) else list.next(it) }?.let { current ->
                                    when {
                                        current < anchor  -> list.setSelection((current .. anchor ).reversed().toSet())
                                        anchor  < current -> list.setSelection((anchor  .. current).           toSet())
                                        else              -> list.setSelection(setOf(current))
                                    }
                                }
                            }
                        }
                        else -> list.lastSelection?.let { if (event.key == ArrowUp) list.previous(it) else list.next(it) }?.let { list.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }

                KeyText("a"), KeyText("A") -> {
                    if (Ctrl in event || Meta in event) {
                        list.selectAll()

                        event.consume()
                    }
                }
                else -> return
            }

            event.consume()
        }
    }
}