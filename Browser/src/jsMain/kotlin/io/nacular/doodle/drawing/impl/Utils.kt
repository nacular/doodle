package io.nacular.doodle.drawing.impl

import io.nacular.doodle.dom.Event
import io.nacular.doodle.dom.MouseEvent
import io.nacular.doodle.dom.SystemStyler
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun cssStyle(initial: SystemStyler.Style? = null): ReadWriteProperty<NativeTextField, SystemStyler.Style?> = object: ReadWriteProperty<NativeTextField, SystemStyler.Style?> {
    private var value = initial

    override fun getValue(thisRef: NativeTextField, property: KProperty<*>) = value

    override fun setValue(thisRef: NativeTextField, property: KProperty<*>, @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") new: SystemStyler.Style?) {
        if (new?.css != value?.css) {
            value?.delete()

            value = new
        }
    }
}

internal fun isKeyboardClick(event: Event) = event is MouseEvent && event.detail <= 0