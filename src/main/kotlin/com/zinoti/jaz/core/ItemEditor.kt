//package com.zinoti.jaz.core
//
//import com.zinoti.jaz.event.ChangeEvent
//import com.zinoti.jaz.event.Event
//
//
//interface ItemEditor<out V> {
//    val value: V
//
//    fun isEditable  (event: Event<*>): Boolean
//    fun stop        (): Boolean
//    fun cancel      (): Boolean
//    fun shouldSelect(event: Event<*>): Boolean
//
//    operator fun plus (aListener: Listener): ItemEditor<V>
//    operator fun minus(aListener: Listener): ItemEditor<V>
//
//    interface Listener {
//        fun editingStopped  (aChangeEvent: ChangeEvent<*>)
//        fun editingCancelled(aChangeEvent: ChangeEvent<*>)
//    }
//}
