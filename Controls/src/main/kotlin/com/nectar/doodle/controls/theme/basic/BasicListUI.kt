package com.nectar.doodle.controls.theme.basic

import com.nectar.doodle.controls.list.ItemPositioner
import com.nectar.doodle.controls.list.ItemUIGenerator
import com.nectar.doodle.controls.list.List
import com.nectar.doodle.controls.list.ListRenderer
import com.nectar.doodle.controls.text.LabelFactory
import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.drawing.Color.Companion.gray
import com.nectar.doodle.drawing.Color.Companion.green
import com.nectar.doodle.drawing.Color.Companion.lightgray
import com.nectar.doodle.drawing.Pen
import com.nectar.doodle.event.MouseEvent
import com.nectar.doodle.event.MouseListener
import com.nectar.doodle.geometry.Rectangle
import com.nectar.doodle.layout.Insets
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.isEven

/**
 * Created by Nicholas Eddy on 3/20/18.
 */

class LabelItemUIGenerator<T>(private val labelFactory: LabelFactory): ItemUIGenerator<T> {
    override fun invoke(list: List<T, *>, row: T, index: Int, selected: Boolean, hasFocus: Boolean): Gizmo {
        return labelFactory(row.toString()).apply {
            fitText             = false
            horizontalAlignment = Left

            var background = if (selected) green else lightgray

            background = when {
                index.isEven -> background.lighter()
                else         -> background
            }

            backgroundColor = background

            mouseChanged += object: MouseListener {
                private var pressed   = false
                private var mouseOver = false

                override fun mouseEntered(event: MouseEvent) {
                    mouseOver       = true
                    backgroundColor = backgroundColor?.lighter(0.25f)
                }

                override fun mouseExited(event: MouseEvent) {
                    mouseOver       = false
                    backgroundColor = background
                }

                override fun mousePressed(event: MouseEvent) {
                    pressed   = true
                    mouseOver = true
                }

                override fun mouseReleased(event: MouseEvent) {
                    if (mouseOver && pressed) {
                        setOf(index).also {
                            list.apply {
                                if (selected(index)) {
                                    removeSelection(it)
                                } else {
                                    setSelection(it)
                                }
                            }
                        }
                    }
                    pressed = false
                }
            }
        }
    }
}

private class BasicPositioner<T>(private val height: Double): ItemPositioner<T> {
    override fun invoke(list: List<T, *>, row: T, index: Int, selected: Boolean, hasFocus: Boolean): Rectangle {
        return Rectangle(0.0, index * height, list.width, height)
    }
}

class BasicListUI<T>(labelFactory: LabelFactory): ListRenderer<T> {
    override val positioner: ItemPositioner<T> = BasicPositioner(20.0)

    override val uiGenerator = LabelItemUIGenerator<T>(labelFactory)

    override fun render(gizmo: List<T, *>, canvas: Canvas) {
        canvas.rect(gizmo.bounds.atOrigin, Pen(gray))
    }

    override fun install(gizmo: List<T, *>) {
        gizmo.insets = Insets(2.0)
    }
}