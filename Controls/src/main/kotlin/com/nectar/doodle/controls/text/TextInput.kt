package com.nectar.doodle.controls.text

import com.nectar.doodle.core.Gizmo
import com.nectar.doodle.event.Event
import com.nectar.doodle.utils.HorizontalAlignment
import com.nectar.doodle.utils.HorizontalAlignment.Left
import com.nectar.doodle.utils.ObservableProperty
import com.nectar.doodle.utils.PropertyObservers
import com.nectar.doodle.utils.PropertyObserversImpl
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KProperty


class SelectionEvent(textInput: TextInput,
        val newPosition: Int,
        val newAnchor  : Int,
        val oldPosition: Int,
        val oldAnchor  : Int): Event<TextInput>(textInput)


typealias SelectionListener = (SelectionEvent) -> Unit

abstract class TextInput: Gizmo() /*, ContentRequestMonitor*/ {

    val selectionStart get() = min(cursorPosition, cursorAnchor)
    val selectionEnd   get() = max(cursorPosition, cursorAnchor)
    val selectionRange get() = abs(cursorAnchor - cursorPosition)

    private val selectionListeners = mutableSetOf<SelectionListener>()

    val horizontalAlignmentChanged: PropertyObservers<TextInput, HorizontalAlignment> by lazy { PropertyObserversImpl<TextInput, HorizontalAlignment>(mutableSetOf()) }

    var horizontalAlignment: HorizontalAlignment by ObservableProperty(Left, { this }, horizontalAlignmentChanged as PropertyObserversImpl<TextInput, HorizontalAlignment>)

    val textChanged: PropertyObservers<TextInput, String> by lazy { PropertyObserversImpl<TextInput, String>(mutableSetOf()) }

    open var text: String by object: ObservableProperty<TextInput, String>("", { this }, textChanged as PropertyObserversImpl<TextInput, String>) {
        override fun afterChange(property: KProperty<*>, oldValue: String, newValue: String) {
            super.afterChange(property, oldValue, validator?.validate(newValue) ?: newValue)

            select(min(text.length, cursorPosition) .. min(text.length, cursorAnchor))
        }
    }

    var cursorVisible = true

    var validator: Validator? = null
        set(new) {
            field = new

            field?.let { text = text /* re-validate text */ }
        }

    var cursorAnchor = 0
        private set
    var cursorPosition = 0
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

    operator fun plusAssign(listener: SelectionListener) {
        selectionListeners += listener
    }

    operator fun minusAssign(listener: SelectionListener) {
        selectionListeners -= listener
    }

//    fun contentsRetrieved(aDataBundle: DataBundle?) {
//        if (aDataBundle != null && aDataBundle!!.isSupportedType(DataType.TEXT)) {
//            val aClipboardText = (aDataBundle!!.getData(DataType.TEXT) as String).replace("\n", " ")
//
//            deleteSelectedText()
//
//            insert(aClipboardText, this.cursorPosition)
//
//            val aSelectionStart = min(selectionStart + aClipboardText.length, text.length)
//
//            select(aSelectionStart, aSelectionStart)
//        }
//    }

    fun selectAll() {
        select(0..text.length)
    }

    open fun cut() {
//        Service.locator().getClipboard().setContents(TextBundle(text.substring(selectionStart, selectionEnd)))

        deleteSelected()
    }

    open fun copy() {
        val text = text.substring(selectionStart, selectionEnd)

        if (!text.isEmpty()) {
//            Service.locator().getClipboard().setContents(TextBundle(text))
        }
    }

    fun paste() {
//        Service.locator().getClipboard().getContents(this)
    }

    fun insert(text: String, at: Int) {
        this.text = text.substring(0, at) + text + text.substring(at)
    }

    fun deleteSelected() {
        if (cursorPosition != cursorAnchor) {
            val oldAnchor         = cursorAnchor
            val oldPosition       = cursorPosition
            val oldSelectionEnd   = selectionEnd
            val oldSelectionStart = selectionStart

            cursorAnchor = cursorPosition
            cursorPosition = oldSelectionStart

            notifySelectionChanged(oldPosition, oldAnchor)

            text = text.substring(0, oldSelectionStart) + text.substring(oldSelectionEnd)
        }
    }

    fun delete(range: ClosedRange<Int>) {
        if (range.start >= 0 && range.endInclusive <= text.length) {
            val newText = text.substring(0, range.start) + text.substring(range.endInclusive)

            select(min(newText.length, cursorPosition) .. min(newText.length, cursorAnchor))

            text = newText
        }
    }

    fun select(range: ClosedRange<Int>) {
        val start = range.start
        val end   = range.endInclusive

        if (cursorPosition != start &&
                start >= 0 &&
                start <= text.length || cursorAnchor != end &&
                end >= 0 &&
                end <= text.length) {
            val oldAnchor   = cursorAnchor
            val oldPosition = cursorPosition

            cursorAnchor   = end
            cursorVisible  = true
            cursorPosition = start

            notifySelectionChanged(oldPosition, oldAnchor)
        }
    }

    private fun notifySelectionChanged(oldPosition: Int, oldAnchor: Int) {
        SelectionEvent(this, cursorPosition, cursorAnchor, oldPosition, oldAnchor).also { event ->
            selectionListeners.forEach { it(event) }
        }
    }

//    private inner class TextProperty: NamedProperty<String> {
//        val name: String
//            get() = TEXT
//        var value: String?
//            get() = text
//            set(aValue) {
//                select(min(aValue!!.length, cursorPosition),
//                        min(aValue.length, cursorAnchor))
//
//                text = aValue ?: ""
//            }
//    }

    interface Validator {
        fun validate(text: String): String
    }

    companion object {
//        val TEXT = TextInput::class.simpleName + ".TEXT"
        private val DEFAULT_CURSOR_INTERVAL = 600
    }
}
