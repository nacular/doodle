package com.zinoti.jaz.controls.text

import com.nectar.doodle.controls.text.TextInput
import com.nectar.doodle.drawing.Canvas
import com.nectar.doodle.theme.Renderer
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl


class TextField: TextInput() {

    val masked get() = mask != null

    var displayText = ""
        get() = if (mask == null) {
            text
        } else field
        private set

    val maskChanged: PropertyObservers<TextField, Char?> by lazy { PropertyObserversImpl<TextField, Char?>(mutableSetOf()) }

    var mask: Char? = null
        set(new) {
            if (field == new) { return }

            val old = field
            field   = new

            mask?.let {
                displayText = "".padEnd(text.length, it)
            }

            (maskChanged as PropertyObserversImpl<TextField, Char?>).forEach { it(this, old, new) }
        }

//    var mask: Char? by object: ObservableProperty<TextField, Char?>(null, { this }, maskChanged as PropertyObserversImpl<TextField, Char?>) {
//        override fun afterChange(property: KProperty<*>, oldValue: Char?, newValue: Char?) {
//            super.afterChange(property, oldValue, newValue)
//
//            mask?.let {
//                displayText = "".padEnd(text.length, it)
//            }
//        }
//    }

    var renderer: Renderer<TextField>? = null

    override fun render(canvas: Canvas) {
        renderer?.render(canvas, this)
    }

    override var text
        get() = super.text
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

    override fun cut() {
        copy          ()
        deleteSelected()
    }

    override fun copy() {
        displayText.substring(selection.start, selection.end).takeIf { !it.isEmpty() }.also {
//            Service.locator().getClipboard().setContents(TextBundle(it))
        }
    }
}
