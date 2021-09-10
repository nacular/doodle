package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.drawing.Font.Style.Normal
import org.jetbrains.skia.FontSlant.ITALIC
import org.jetbrains.skia.FontSlant.OBLIQUE
import org.jetbrains.skia.paragraph.TextStyle

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
internal class FontImpl(internal val textStyle: TextStyle): Font {
    override val size: Int get() = textStyle.fontSize.toInt()
    override val style: Style
        get() = when (textStyle.fontStyle.slant) {
            ITALIC  -> Italic
            OBLIQUE -> Style.Oblique()
            else    -> Normal
        }
    override val weight: Int    get() = textStyle.fontStyle.weight
    override val family: String get() = textStyle.fontFamilies.first()
}