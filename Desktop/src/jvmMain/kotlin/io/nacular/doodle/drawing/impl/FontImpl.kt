package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.drawing.Font.Style.Normal
import org.jetbrains.skija.FontStyle.BOLD
import org.jetbrains.skija.FontStyle.BOLD_ITALIC
import org.jetbrains.skija.FontStyle.ITALIC
import org.jetbrains.skija.FontStyle.NORMAL
import org.jetbrains.skija.Font as SkijaFont

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
internal class FontImpl(internal val skiaFont: SkijaFont): Font {
    override val size: Int get() = skiaFont.size.toInt()
    override val style: Style
        get() = when (skiaFont.typefaceOrDefault.fontStyle) {
            NORMAL      -> Normal
            BOLD        -> Normal
            ITALIC      -> Italic
            BOLD_ITALIC -> Italic
            else        -> Normal
        }
    override val weight: Int get() = skiaFont.typefaceOrDefault.fontStyle.weight
    override val family: String get() = skiaFont.typefaceOrDefault.familyName
}