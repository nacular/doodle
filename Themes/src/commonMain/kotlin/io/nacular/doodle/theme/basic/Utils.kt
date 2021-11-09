package io.nacular.doodle.theme.basic

import io.nacular.doodle.controls.EditOperation
import io.nacular.doodle.controls.spinner.Spinner
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.event.KeyListener.Companion.released
import io.nacular.doodle.event.KeyText.Companion.Enter
import io.nacular.doodle.event.KeyText.Companion.Escape
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.layout.Constraints
import io.nacular.doodle.layout.ParentConstraints
import io.nacular.doodle.utils.Editable
import io.nacular.doodle.utils.Encoder
import io.nacular.doodle.utils.HorizontalAlignment

/**
 * Created by Nicholas Eddy on 6/23/20.
 */
public class ConstraintWrapper(delegate: Constraints, parent: (ParentConstraints) -> ParentConstraints): Constraints by delegate {
    override val parent: ParentConstraints = parent(delegate.parent)
}

public open class ParentConstraintWrapper(delegate: ParentConstraints): ParentConstraints by delegate

public typealias ColorMapper = (Color) -> Color

public open class GenericTextEditOperation<T, V>(
        private val focusManager: FocusManager?,
        private val mapper      : Encoder<T, String>,
        private val view        : V,
                    value       : T,
                    current     : View): EditOperation<T> where V: View, V: Editable {

    protected open val cancelOnFocusLost : Boolean = true
    protected open val selectAllInitially: Boolean = true

    protected val textField: TextField = TextField().apply {
        text                = mapper.to(value).getOrDefault("")
        borderVisible       = false
        font                = current.font
        foregroundColor     = current.foregroundColor
        backgroundColor     = current.backgroundColor ?: Color.Transparent
        horizontalAlignment = HorizontalAlignment.Center

        focusChanged += { _,_,_ ->
            if (!hasFocus && cancelOnFocusLost) {
                view.cancelEditing()
            }
        }

        keyChanged += released { event ->
            when (event.key) {
                Enter  -> { view.completeEditing(); focusManager?.requestFocus(view) }
                Escape -> { view.cancelEditing  (); focusManager?.requestFocus(view) }
            }
        }

        displayChange += { _,_, displayed ->
            if (displayed) {
                focusManager?.requestFocus(this)
                if (selectAllInitially) { selectAll() }
            }
        }
    }

    private val changed = { _: Spinner<T, *> ->
        view.cancelEditing()
    }

    override fun invoke(): View = textField

    override fun complete(): T? = mapper.from(textField.text).getOrNull()

    override fun cancel() {}
}