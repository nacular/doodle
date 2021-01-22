package io.nacular.doodle.controls.document

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Font
import io.nacular.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 2/13/20.
 */
public abstract class Document protected constructor(): View() {
    public abstract fun inline(text: StyledText                )
    public abstract fun inline(text: String, font: Font? = null)

    public abstract fun inline(view: View)

    public abstract fun wrapText(view: View)

    public abstract fun breakText(view: View)
}