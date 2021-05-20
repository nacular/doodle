package io.nacular.doodle.drawing.impl

import io.nacular.doodle.drawing.Font
import io.nacular.doodle.drawing.Font.Style.Italic
import io.nacular.doodle.drawing.Font.Style.Oblique
import io.nacular.doodle.drawing.FontInfo
import io.nacular.doodle.drawing.FontLoader
import org.jetbrains.skija.FontSlant.OBLIQUE
import org.jetbrains.skija.FontSlant.UPRIGHT
import org.jetbrains.skija.FontStyle
import org.jetbrains.skija.Typeface
import org.jetbrains.skija.Font as SkijaFont

/**
 * Created by Nicholas Eddy on 5/20/21.
 */
internal class FontLoaderImpl: FontLoader {
    private val loadedFonts = mutableMapOf<FontInfo, Font>()

    override suspend fun invoke(info: FontInfo.() -> Unit): Font = FontInfo().apply(info).let { info ->
        loadedFonts.getOrPut(info) {
            val slant = when (info.style) {
                Italic     -> OBLIQUE
                is Oblique -> OBLIQUE
                else       -> UPRIGHT
            }

            val typeface = Typeface.makeFromName(info.family, FontStyle(info.weight, 5, slant))
            val skiaFont = SkijaFont(typeface, info.size.toFloat())
            FontImpl(skiaFont)
        }
    }

    override suspend fun invoke(source: String, info: FontInfo.() -> Unit): Font = FontInfo().apply(info).let { info ->
        loadedFonts.getOrPut(info) {
            // FIXME: Incorporate info?
            val typeface = Typeface.makeFromFile(source)

            val skiaFont = SkijaFont(typeface, info.size.toFloat())
            FontImpl(skiaFont)
        }
    }
}