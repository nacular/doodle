package com.nectar.doodle.controls.text

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KProperty


class Selection(val position: Int, val anchor: Int) {
    val start get() = min(position, anchor)
    val end   get() = max(position, anchor)
}

typealias Validator = (String) -> String

abstract class TextInput : Gizmo() /*, ContentRequestMonitor*/ {

    val horizontalAlignmentChanged: PropertyObservers<TextInput, HorizontalAlignment> by lazy { PropertyObserversImpl<TextInput, HorizontalAlignment>(this) }

    var horizontalAlignment: HorizontalAlignment by ObservableProperty(Left, { this }, horizontalAlignmentChanged as PropertyObserversImpl<TextInput, HorizontalAlignment>)

    val textChanged: PropertyObservers<TextInput, String> by lazy { PropertyObserversImpl<TextInput, String>(this) }

    open var text: String by object: ObservableProperty<TextInput, String>("", { this }, textChanged as PropertyObserversImpl<TextInput, String>) {
        override fun afterChange(property: KProperty<*>, oldValue: String, newValue: String) {
            super.afterChange(property, oldValue, validator(newValue))

            select(min(text.length, selection.position) .. min(text.length, selection.anchor))
        }
    }

    var cursorVisible = true

    var validator: Validator = { it }
        set(new) {
            text = text /* re-validate text */
        }

    val selectionChanged: PropertyObservers<TextInput, Selection> by lazy { PropertyObserversImpl<TextInput, Selection>(this) }

    var selection: Selection by ObservableProperty(Selection(0, 0), { this }, selectionChanged as PropertyObserversImpl<TextInput, Selection>)
        private set

    init {
//        setDataTransporter(object : TextTransporter() {
//            protected fun getText(aGizmo: Gizmo): String? {
//                return this@TextInput.text
//            }
//
//            protected fun setText(aGizmo: Gizmo, aText: String) {
//                this@TextInput.text = aText
//            }
//        })
    }

    fun selectAll() = select(0 .. text.length)

    open fun cut() = copy().also {
        deleteSelected()
    }

    open fun copy() = text.substring(selection.start, selection.end)

    fun paste(text: String) {
        deleteSelected()

        insert(text, selection.start)

        val selectionStart = min(selection.start + text.length, text.length)

        select(selectionStart .. selectionStart)
    }

    fun insert(text: String, at: Int) {
        this.text = text.substring(0, at) + text + text.substring(at)
    }

    fun deleteSelected() = delete(selection.start .. selection.end)

    fun delete(range: ClosedRange<Int>) {
        if (range.start >= 0 && range.endInclusive <= text.length) {
            val newText = text.substring(0, range.start) + text.substring(range.endInclusive)

            select(min(newText.length, selection.position) .. min(newText.length, selection.anchor))

            text = newText
        }
    }

    fun select(range: ClosedRange<Int>) {
        val start = range.start
        val end   = range.endInclusive

        if (selection.position != start &&
                start >= 0 &&
                start <= text.length || selection.anchor != end &&
                end   >= 0 &&
                end   <= text.length) {

            cursorVisible = true
            selection     = Selection(start, end)
        }
    }
}
