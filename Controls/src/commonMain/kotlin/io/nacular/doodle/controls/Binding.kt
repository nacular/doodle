package io.nacular.doodle.controls

import io.nacular.doodle.utils.observable
import kotlin.properties.ReadWriteProperty

public interface Binding {
    public fun unbind()
}

public fun binding(initial: Binding): ReadWriteProperty<Any, Binding> = observable(initial) { old,_ ->
    old.unbind()
}