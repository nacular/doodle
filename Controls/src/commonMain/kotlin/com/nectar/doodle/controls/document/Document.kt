package com.nectar.doodle.controls.document

import com.nectar.doodle.core.View
import com.nectar.doodle.drawing.Font
import com.nectar.doodle.text.StyledText

/**
 * Created by Nicholas Eddy on 2/13/20.
 */
abstract class Document protected constructor(): View() {
    abstract fun append(text: StyledText                )
    abstract fun append(text: String, font: Font? = null)
}