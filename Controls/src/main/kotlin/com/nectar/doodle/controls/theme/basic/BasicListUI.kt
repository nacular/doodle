package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.list.ItemPositioner
import com.nectar.doodle.controls.list.ItemUIGenerator
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.ListRenderer
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color.Companion.red
import com.nectar.doodle.drawing.ColorBrush

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

class LabelItemUIGenerator<T>(private val labelFactory: LabelFactory): ItemUIGenerator<T> {
    override fun invoke(list: List<T, *>, row: T, index: Int, selected: Boolean, hasFocus: Boolean): Gizmo {
        return labelFactory(row.toString())
    }
}

class BasicListUI<T>(labelFactory: LabelFactory): ListRenderer<T> {
    override val positioner: ItemPositioner
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val uiGenerator = LabelItemUIGenerator<T>(labelFactory)

    override fun render(gizmo: List<T, *>, canvas: Canvas) {
        canvas.rect(gizmo.bounds.atOrigin, ColorBrush(red))
    }
}