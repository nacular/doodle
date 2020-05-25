package com.nectar.doodle.themes.basic

import com.nectar.doodle.controls.Selectable
import com.nectar.doodle.event.KeyEvent
import com.nectar.doodle.event.KeyEvent.Companion.ArrowDown
import com.nectar.doodle.event.KeyEvent.Companion.ArrowUp
import com.nectar.doodle.event.KeyEvent.Companion.KeyA
import com.nectar.doodle.system.SystemInputEvent.Modifier.Ctrl
import com.nectar.doodle.system.SystemInputEvent.Modifier.Meta
import com.nectar.doodle.system.SystemInputEvent.Modifier.Shift

/**
 * Created by Nicholas Eddy on 5/10/19.
 */
interface SelectableListKeyHandler {
    fun keyPressed(event: KeyEvent) {
        (event.source as Selectable<Int>).let { list ->
            when (event.code) {
                ArrowUp, ArrowDown -> {
                    when (Shift) {
                        in event -> {
                            list.selectionAnchor?.let { anchor ->
                                list.lastSelection?.let { if (event.code == ArrowUp) list.previous(it) else list.next(it) }?.let { current ->
                                    when {
                                        current < anchor  -> list.setSelection((current .. anchor ).reversed().toSet())
                                        anchor  < current -> list.setSelection((anchor  .. current).           toSet())
                                        else              -> list.setSelection(setOf(current))
                                    }
                                }
                            }
                        }
                        else -> list.lastSelection?.let { if (event.code == ArrowUp) list.previous(it) else list.next(it) }?.let { list.setSelection(setOf(it)) }
                    }?.let { Unit } ?: Unit
                }

                KeyA           -> {
                    if (Ctrl in event || Meta in event) {
                        list.selectAll()
                    }
                }
            }
        }
    }
}