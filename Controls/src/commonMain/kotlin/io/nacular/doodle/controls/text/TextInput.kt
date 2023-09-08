package io.nacular.doodle.controls.text

import io.nacular.doodle.accessibility.TextBoxRole
import io.nacular.doodle.core.View
import io.nacular.doodle.utils.HorizontalAlignment
import io.nacular.doodle.utils.HorizontalAlignment.Left
import io.nacular.doodle.utils.PropertyObservers
import io.nacular.doodle.utils.PropertyObserversImpl
import io.nacular.doodle.utils.intersect
import io.nacular.doodle.utils.observable
import io.nacular.doodle.utils.size
import kotlin.math.max
import kotlin.math.min


public class Selection(public val position: Int, public val anchor: Int) {
    public val start: Int get() = min(position, anchor)
    public val end  : Int get() = max(position, anchor)
}

public typealias Validator = (String) -> String

public abstract class TextInput(text: String = "", protected val role: TextBoxRole = TextBoxRole()): View(role) {

    public val horizontalAlignmentChanged: PropertyObservers<TextInput, HorizontalAlignment> by lazy { PropertyObserversImpl(this) }

    public var horizontalAlignment: HorizontalAlignment by observable(Left, horizontalAlignmentChanged as PropertyObserversImpl<TextInput, HorizontalAlignment>)

    public val textChanged: PropertyObservers<TextInput, String> by lazy { PropertyObserversImpl(this) }

    public open var text: String by observable(text, textChanged as PropertyObserversImpl<TextInput, String>) { _,_ ->
        select(min(text.length, selection.position) .. min(text.length, selection.anchor))
    }

    public var cursorVisible: Boolean = true

    public var validator: Validator = { it }
        set(new) {
            field = new
            text  = text /* re-validate text */
        }

    public val selectionChanged: PropertyObservers<TextInput, Selection> by lazy { PropertyObserversImpl(this) }

    public var selection: Selection by observable(Selection(0, 0), selectionChanged as PropertyObserversImpl<TextInput, Selection>)
        private set

    init {
        mirrorWhenRightLeft = false
    }

    public fun selectAll(): Unit = select(0 .. text.length)

    public open fun cut(): String = copy().also {
        deleteSelected()
    }

    public open fun copy(): String = text.substring(selection.start, selection.end)

    public fun paste(text: String) {
        deleteSelected()

        insert(text, selection.start)
    }

    public fun insert(text: String, at: Int) {
        this.text = this.text.substring(0, at) + text + this.text.substring(at)

        when {
            at < selection.start -> select(selection.start + text.length .. selection.end + text.length)
            at < selection.end   -> select(selection.start               .. selection.end + text.length)
        }
    }

    public fun deleteSelected(): Unit = delete(selection.start .. selection.end)

    public fun delete(range: ClosedRange<Int>) {
        if (range.size >= 0 && range.start >= 0 && range.endInclusive <= text.length) {
            val newText = text.substring(0, range.start) + text.substring(range.endInclusive)

            val leftShift = if (range.start < selection.start) {
                min(selection.start, range.endInclusive) - range.start
            } else 0

            val selectionOverlap = (selection.start .. selection.end).intersect(range)

            val selectStart = minOf(newText.length, selection.start - leftShift)

            select(selectStart .. min(newText.length, selection.end - selectionOverlap.size - leftShift))

            text = newText
        }
    }

    public fun select(range: ClosedRange<Int>) {
        val start = range.start
        val end   = range.endInclusive

        if (selection.position != start &&
            start              >= 0 &&
            start              <= text.length || selection.anchor != end &&
            end                >= 0 &&
            end                <= text.length) {
            selection = Selection(start, end)
        }
    }
}
