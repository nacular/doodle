package com.zinoti.jaz.core

import com.zinoti.jaz.containers.Padding
import com.zinoti.jaz.drawing.Brush
import com.zinoti.jaz.geometry.Size
import com.zinoti.jaz.system.Cursor
import com.zinoti.jaz.utils.ObservableList
import com.zinoti.jaz.utils.PropertyObservers


interface Display: Iterable<Gizmo> {

    override fun iterator(): Iterator<Gizmo> = children.iterator()

    var cursor: Cursor

    val size                : Size
    var layout              : Layout?
    var padding             : Padding
    val children            : ObservableList<Gizmo>
    val sizeChange          : PropertyObservers<Size>
    var minimumSize         : Size
//    var focusTraversalPolicy: FocusTraversalPolicy

    fun fill(brush: Brush)
}
