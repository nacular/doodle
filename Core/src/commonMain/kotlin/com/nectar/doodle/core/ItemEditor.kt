package com.nectar.doodle.core

import com.nectar.doodle.event.ChangeEvent
import com.nectar.doodle.event.Event


interface ItemEditor<out V> {
    val value: V

    fun isEditable  (event: Event<*>): Boolean
    fun stop        (): Boolean
    fun cancel      (): Boolean
    fun shouldSelect(event: Event<*>): Boolean

    operator fun plusAssign (listener: Listener)
    operator fun minusAssign(listener: Listener)

    interface Listener {
        fun editingStopped  (aChangeEvent: ChangeEvent<*>)
        fun editingCancelled(aChangeEvent: ChangeEvent<*>)
    }
}
