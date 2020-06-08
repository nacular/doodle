package io.nacular.doodle.controls.document

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 2/13/20.
 */
abstract class Document protected constructor(): View() {
    abstract fun inline(text: StyledText                )
    abstract fun inline(text: String, font: Font? = null)

    abstract fun inline(view: View)

    abstract fun wrapText(view: View)

    abstract fun breakText(view: View)
}