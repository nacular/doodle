package com.nectar.doodle.controls.text

import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl


class TextField: TextInput() {

    val masked get() = mask != null

    var displayText = ""
        get() = if (mask == null) text else field
        private set

    val maskChanged: PropertyObservers<TextField, Char?> by lazy { PropertyObserversImpl<TextField, Char?>(this) }

    var mask: Char? = null
        set(new) {
            if (field == new) return

            val old = field
            field   = new

            mask?.let {
                displayText = "".padEnd(text.length, it)
            }

            (maskChanged as PropertyObserversImpl<TextField, Char?>)(old, new)
        }

    var renderer: Renderer<TextField>? = null

    override fun render(canvas: Canvas) {
        renderer?.render(this, canvas)
    }

    override var text
        get(   ) = super.text
        set(new) {
            super.text = new

            mask?.let {
                if (displayText.length > text.length) {
                    displayText = displayText.substring(0, text.length)
                } else {
                    displayText.padEnd(text.length - displayText.length, it)
                }
            }
        }

    override fun cut() = copy().also { deleteSelected() }

    override fun copy() = displayText.substring(selection.start, selection.end)
}
